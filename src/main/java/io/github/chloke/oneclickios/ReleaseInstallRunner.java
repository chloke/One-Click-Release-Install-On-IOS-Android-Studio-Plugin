package io.github.chloke.oneclickios;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.RunContentExecutor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.KillableProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

final class ReleaseInstallRunner {
    private static final String NOTIFICATION_GROUP = "One Click Release Install on iOS";

    private ReleaseInstallRunner() {
    }

    static void run(Project project, Path flutterExecutable, Path projectRoot, IosDevice device,
                    ReleaseInstallState state) {
        GeneralCommandLine commandLine = new GeneralCommandLine(flutterExecutable.toString())
                .withParameters("run", "--release", "--no-resident", "-d", device.id())
                .withWorkDirectory(projectRoot.toFile())
                .withCharset(StandardCharsets.UTF_8);
        commandLine.withEnvironment("FLUTTER_SUPPRESS_ANALYTICS", "true");

        try {
            KillableProcessHandler handler = new KillableProcessHandler(commandLine);
            ConsoleView console = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
            console.print("$ " + commandLine.getCommandLineString() + "\n\n", ConsoleViewContentType.SYSTEM_OUTPUT);
            console.attachToProcess(handler);

            handler.addProcessListener(new ProcessListener() {
                @Override
                public void processTerminated(@NotNull ProcessEvent event) {
                    state.finish();
                    if (event.getExitCode() == 0) {
                        ReleaseInstallRunner.notify(project,
                                "Release installed on " + device.name(),
                                "The app was launched and Flutter detached automatically.",
                                NotificationType.INFORMATION);
                    } else {
                        ReleaseInstallRunner.notify(project,
                                "Release installation failed",
                                "Flutter exited with code " + event.getExitCode() + ". Open the Run window for details.",
                                NotificationType.ERROR);
                    }
                }
            });

            new RunContentExecutor(project, handler)
                    .withTitle("iOS Release Install — " + device.name())
                    .withConsole(console)
                    .withActivateToolWindow(true)
                    .withFocusToolWindow(false)
                    .run();
        } catch (ExecutionException exception) {
            state.finish();
            notify(project, "Could not start Flutter", exception.getMessage(), NotificationType.ERROR);
        }
    }

    static void notify(Project project, String title, String content, NotificationType type) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup(NOTIFICATION_GROUP)
                .createNotification(title, content == null ? "" : content, type)
                .notify(project);
    }
}
