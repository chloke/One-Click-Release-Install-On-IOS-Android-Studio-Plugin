package io.github.chloke.oneclickios;

import com.intellij.openapi.components.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Service(Service.Level.PROJECT)
final class ReleaseInstallState {
    private final AtomicBoolean running = new AtomicBoolean(false);

    boolean begin() {
        return running.compareAndSet(false, true);
    }

    void finish() {
        running.set(false);
    }

    boolean isRunning() {
        return running.get();
    }
}
