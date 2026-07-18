package io.github.chloke.oneclickios;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

@Service(Service.Level.PROJECT)
@State(
        name = "OneClickReleaseInstallState",
        storages = @Storage(StoragePathMacros.WORKSPACE_FILE)
)
final class ReleaseInstallState implements PersistentStateComponent<ReleaseInstallState.StoredState> {
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final StoredState storedState = new StoredState();

    public static final class StoredState {
        public String preferredDeviceId;
    }

    @Override
    public StoredState getState() {
        return storedState;
    }

    @Override
    public void loadState(@NotNull StoredState state) {
        XmlSerializerUtil.copyBean(state, storedState);
    }

    boolean begin() {
        return running.compareAndSet(false, true);
    }

    void finish() {
        running.set(false);
    }

    boolean isRunning() {
        return running.get();
    }

    String preferredDeviceId() {
        return storedState.preferredDeviceId;
    }

    void preferDevice(String deviceId) {
        storedState.preferredDeviceId = deviceId;
    }
}
