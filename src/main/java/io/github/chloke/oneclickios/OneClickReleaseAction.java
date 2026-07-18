package io.github.chloke.oneclickios;

import com.intellij.execution.ExecutionException;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class OneClickReleaseAction extends AnAction implements DumbAware {
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        boolean flutterProject = project != null
                && project.getBasePath() != null
                && Files.isRegularFile(Path.of(project.getBasePath(), "pubspec.yaml"));
        boolean running = project != null && project.getService(ReleaseInstallState.class).isRunning();
        event.getPresentation().setEnabled(flutterProject && !running);
        event.getPresentation().setVisible(project != null);
        event.getPresentation().setDescription(running
                ? "A release installation is already running"
                : "Build, install, and launch this Flutter app in release mode on a physical iOS device");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null || project.getBasePath() == null) {
            return;
        }

        Path projectRoot = Path.of(project.getBasePath());
        if (!Files.isRegularFile(projectRoot.resolve("pubspec.yaml"))) {
            Messages.showErrorDialog(project, "The current project does not contain pubspec.yaml.", "Not a Flutter Project");
            return;
        }

        Path flutterExecutable = FlutterExecutableLocator.locateOrPrompt(project);
        if (flutterExecutable == null) {
            return;
        }

        ReleaseInstallState state = project.getService(ReleaseInstallState.class);
        if (!state.begin()) {
            ReleaseInstallRunner.notify(project, "Release installation already running",
                    "Wait for the current Flutter process to finish or stop it in the Run window.",
                    NotificationType.WARNING);
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Finding physical iOS devices", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                try {
                    List<IosDevice> devices = IosDeviceDiscovery.discover(flutterExecutable, projectRoot);
                    ApplicationManager.getApplication().invokeLater(() -> chooseDeviceAndRun(
                            project, flutterExecutable, projectRoot, devices, state));
                } catch (ExecutionException exception) {
                    state.finish();
                    ApplicationManager.getApplication().invokeLater(() -> ReleaseInstallRunner.notify(
                            project,
                            "Could not find iOS devices",
                            exception.getMessage(),
                            NotificationType.ERROR));
                }
            }
        });
    }

    private static void chooseDeviceAndRun(Project project, Path flutterExecutable, Path projectRoot,
                                           List<IosDevice> devices, ReleaseInstallState state) {
        if (project.isDisposed()) {
            state.finish();
            return;
        }
        if (devices.isEmpty()) {
            state.finish();
            Messages.showErrorDialog(
                    project,
                    "No supported physical iOS device was found. Connect and trust an iPhone or iPad, then try again.",
                    "No Physical iOS Device"
            );
            return;
        }

        if (devices.size() == 1) {
            ReleaseInstallRunner.run(project, flutterExecutable, projectRoot, devices.getFirst(), state);
            return;
        }

        String preferredDeviceId = state.preferredDeviceId();
        if (preferredDeviceId != null) {
            IosDevice preferredDevice = devices.stream()
                    .filter(device -> preferredDeviceId.equals(device.id()))
                    .findFirst()
                    .orElse(null);
            if (preferredDevice != null) {
                ReleaseInstallRunner.run(project, flutterExecutable, projectRoot, preferredDevice, state);
                return;
            }
        }

        DeviceSelectionDialog dialog = new DeviceSelectionDialog(project, devices);
        if (!dialog.showAndGet()) {
            state.finish();
            return;
        }

        IosDevice selectedDevice = dialog.selectedDevice();
        if (selectedDevice == null) {
            state.finish();
            return;
        }
        if (dialog.shouldRememberSelection()) {
            state.preferDevice(selectedDevice.id());
        }
        ReleaseInstallRunner.run(project, flutterExecutable, projectRoot, selectedDevice, state);
    }
}
