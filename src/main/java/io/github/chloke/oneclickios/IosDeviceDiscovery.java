package io.github.chloke.oneclickios;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class IosDeviceDiscovery {
    private static final Pattern DEVICE_PATTERN = Pattern.compile(
            "\\\"name\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"\\s*,\\s*" +
                    "\\\"id\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"\\s*,\\s*" +
                    "\\\"isSupported\\\"\\s*:\\s*(true|false)\\s*,\\s*" +
                    "\\\"targetPlatform\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"\\s*,\\s*" +
                    "\\\"emulator\\\"\\s*:\\s*(true|false)\\s*,\\s*" +
                    "\\\"sdk\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"",
            Pattern.DOTALL
    );

    private IosDeviceDiscovery() {
    }

    static List<IosDevice> discover(Path flutterExecutable, Path projectRoot) throws ExecutionException {
        GeneralCommandLine commandLine = new GeneralCommandLine(flutterExecutable.toString())
                .withParameters("devices", "--machine")
                .withWorkDirectory(projectRoot.toFile())
                .withCharset(StandardCharsets.UTF_8);
        commandLine.withEnvironment("FLUTTER_SUPPRESS_ANALYTICS", "true");

        ProcessOutput output = new CapturingProcessHandler(commandLine).runProcess(60_000);
        if (output.isTimeout()) {
            throw new ExecutionException("Timed out while asking Flutter for connected devices.");
        }
        if (output.getExitCode() != 0) {
            String details = output.getStderr().isBlank() ? output.getStdout() : output.getStderr();
            throw new ExecutionException("Flutter could not list connected devices.\n" + details.trim());
        }
        return parseDevices(output.getStdout());
    }

    static List<IosDevice> parseDevices(String json) {
        List<IosDevice> devices = new ArrayList<>();
        Matcher matcher = DEVICE_PATTERN.matcher(json);
        while (matcher.find()) {
            boolean supported = Boolean.parseBoolean(matcher.group(3));
            String targetPlatform = unescapeJson(matcher.group(4));
            boolean emulator = Boolean.parseBoolean(matcher.group(5));
            if (supported && "ios".equals(targetPlatform) && !emulator) {
                devices.add(new IosDevice(
                        unescapeJson(matcher.group(1)),
                        unescapeJson(matcher.group(2)),
                        unescapeJson(matcher.group(6))
                ));
            }
        }
        return devices;
    }

    private static String unescapeJson(String value) {
        StringBuilder result = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (current != '\\' || index + 1 >= value.length()) {
                result.append(current);
                continue;
            }

            char escaped = value.charAt(++index);
            switch (escaped) {
                case '\"' -> result.append('\"');
                case '\\' -> result.append('\\');
                case '/' -> result.append('/');
                case 'b' -> result.append('\b');
                case 'f' -> result.append('\f');
                case 'n' -> result.append('\n');
                case 'r' -> result.append('\r');
                case 't' -> result.append('\t');
                case 'u' -> {
                    if (index + 4 < value.length()) {
                        String hex = value.substring(index + 1, index + 5);
                        try {
                            result.append((char) Integer.parseInt(hex, 16));
                            index += 4;
                        } catch (NumberFormatException exception) {
                            result.append("\\u").append(hex);
                            index += 4;
                        }
                    } else {
                        result.append("\\u");
                    }
                }
                default -> result.append(escaped);
            }
        }
        return result.toString();
    }
}
