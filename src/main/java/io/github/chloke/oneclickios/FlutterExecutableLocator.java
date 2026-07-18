package io.github.chloke.oneclickios;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

final class FlutterExecutableLocator {
    private static final String SAVED_FLUTTER_PATH = "one.click.release.ios.flutter.executable";

    private FlutterExecutableLocator() {
    }

    static Path locateOrPrompt(Project project) {
        Path detected = locate(project);
        if (detected != null) {
            remember(detected);
            return detected;
        }

        String entered = Messages.showInputDialog(
                project,
                "Flutter could not be located automatically. Enter the full path to the Flutter executable:",
                "Locate Flutter",
                Messages.getQuestionIcon(),
                "",
                null
        );
        if (entered == null || entered.isBlank()) {
            return null;
        }

        Path candidate = Path.of(entered.trim()).toAbsolutePath().normalize();
        if (!isFlutterExecutable(candidate)) {
            Messages.showErrorDialog(project, "No Flutter executable exists at:\n" + candidate, "Invalid Flutter Path");
            return null;
        }

        remember(candidate);
        return candidate;
    }

    static Path locate(Project project) {
        List<Path> candidates = new ArrayList<>();

        String saved = PropertiesComponent.getInstance().getValue(SAVED_FLUTTER_PATH);
        addPath(candidates, saved);

        String basePath = project.getBasePath();
        if (basePath != null) {
            Path fromProperties = readFlutterPathFromLocalProperties(Path.of(basePath));
            if (fromProperties != null) {
                candidates.add(fromProperties);
            }
        }

        String flutterRoot = System.getenv("FLUTTER_ROOT");
        if (flutterRoot != null && !flutterRoot.isBlank()) {
            candidates.add(Path.of(flutterRoot, "bin", "flutter"));
        }

        String path = System.getenv("PATH");
        if (path != null) {
            for (String directory : path.split(java.io.File.pathSeparator)) {
                if (!directory.isBlank()) {
                    candidates.add(Path.of(directory, "flutter"));
                }
            }
        }

        String userHome = System.getProperty("user.home");
        if (userHome != null && !userHome.isBlank()) {
            candidates.add(Path.of(userHome, "development", "flutter", "bin", "flutter"));
            candidates.add(Path.of(userHome, "flutter", "bin", "flutter"));
        }

        return candidates.stream()
                .map(candidate -> candidate.toAbsolutePath().normalize())
                .filter(FlutterExecutableLocator::isFlutterExecutable)
                .findFirst()
                .orElse(null);
    }

    static Path readFlutterPathFromLocalProperties(Path projectRoot) {
        Path localProperties = projectRoot.resolve("android").resolve("local.properties");
        if (!Files.isRegularFile(localProperties)) {
            return null;
        }

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(localProperties)) {
            properties.load(input);
        } catch (IOException ignored) {
            return null;
        }

        String sdk = properties.getProperty("flutter.sdk");
        if (sdk == null || sdk.isBlank()) {
            return null;
        }
        return Path.of(sdk.trim(), "bin", "flutter");
    }

    private static boolean isFlutterExecutable(Path path) {
        return Files.isRegularFile(path) && Files.isExecutable(path);
    }

    private static void addPath(List<Path> candidates, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            candidates.add(Path.of(value));
        } catch (RuntimeException ignored) {
            // Ignore stale or malformed saved paths and continue auto-detection.
        }
    }

    private static void remember(Path executable) {
        PropertiesComponent.getInstance().setValue(SAVED_FLUTTER_PATH, executable.toString());
    }
}
