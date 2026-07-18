package io.github.chloke.oneclickios;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FlutterExecutableLocatorTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void readsFlutterSdkFromAndroidLocalProperties() throws IOException {
        Path androidDirectory = Files.createDirectories(temporaryDirectory.resolve("android"));
        Files.writeString(androidDirectory.resolve("local.properties"), "flutter.sdk=/opt/flutter\n");

        Path result = FlutterExecutableLocator.readFlutterPathFromLocalProperties(temporaryDirectory);

        assertEquals(Path.of("/opt/flutter/bin/flutter"), result);
    }

    @Test
    void returnsNullWhenLocalPropertiesDoesNotExist() {
        assertNull(FlutterExecutableLocator.readFlutterPathFromLocalProperties(temporaryDirectory));
    }
}
