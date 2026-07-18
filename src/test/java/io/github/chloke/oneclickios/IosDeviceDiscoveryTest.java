package io.github.chloke.oneclickios;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IosDeviceDiscoveryTest {
    @Test
    void returnsOnlySupportedPhysicalIosDevices() {
        String json = """
                [
                  {
                    "name": "Example iPhone",
                    "id": "iphone-id",
                    "isSupported": true,
                    "targetPlatform": "ios",
                    "emulator": false,
                    "sdk": "iOS 26.5.2",
                    "capabilities": {}
                  },
                  {
                    "name": "iPhone Simulator",
                    "id": "simulator-id",
                    "isSupported": true,
                    "targetPlatform": "ios",
                    "emulator": true,
                    "sdk": "iOS 26.5",
                    "capabilities": {}
                  },
                  {
                    "name": "Chrome",
                    "id": "chrome",
                    "isSupported": true,
                    "targetPlatform": "web-javascript",
                    "emulator": false,
                    "sdk": "Chrome",
                    "capabilities": {}
                  }
                ]
                """;

        List<IosDevice> devices = IosDeviceDiscovery.parseDevices(json);

        assertEquals(1, devices.size());
        assertEquals("Example iPhone", devices.getFirst().name());
        assertEquals("iphone-id", devices.getFirst().id());
    }

    @Test
    void decodesEscapedDeviceNames() {
        String json = """
                [{
                  "name": "Developer\\u2019s iPhone",
                  "id": "device\\/id",
                  "isSupported": true,
                  "targetPlatform": "ios",
                  "emulator": false,
                  "sdk": "iOS 27.0",
                  "capabilities": {}
                }]
                """;

        IosDevice device = IosDeviceDiscovery.parseDevices(json).getFirst();

        assertEquals("Developer’s iPhone", device.name());
        assertEquals("device/id", device.id());
    }
}
