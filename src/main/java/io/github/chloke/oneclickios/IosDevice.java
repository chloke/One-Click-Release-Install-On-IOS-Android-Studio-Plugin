package io.github.chloke.oneclickios;

record IosDevice(String name, String id, String sdk) {
    String displayName() {
        return sdk == null || sdk.isBlank() ? name : name + " — " + sdk;
    }
}
