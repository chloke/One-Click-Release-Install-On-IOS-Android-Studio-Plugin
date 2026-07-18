package io.github.chloke.oneclickios;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.util.List;

final class DeviceSelectionDialog extends DialogWrapper {
    private final JBList<IosDevice> deviceList;
    private final JBCheckBox rememberSelection;

    DeviceSelectionDialog(Project project, List<IosDevice> devices) {
        super(project);
        deviceList = new JBList<>(devices);
        deviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        deviceList.setCellRenderer(SimpleListCellRenderer.create("", IosDevice::displayName));
        deviceList.setVisibleRowCount(Math.min(devices.size(), 8));

        rememberSelection = new JBCheckBox("Always run on selected device if available");
        rememberSelection.setToolTipText(
                "Save this device for the current project and skip this chooser while it is connected"
        );

        setTitle("Install Flutter Release on iOS");
        setOKButtonText("Run");
        init();
        deviceList.setSelectedIndex(0);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JBScrollPane scrollPane = new JBScrollPane(deviceList);
        scrollPane.setPreferredSize(JBUI.size(480, Math.max(96, deviceList.getVisibleRowCount() * 32)));

        JPanel panel = new JPanel(new BorderLayout(0, JBUI.scale(12)));
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(rememberSelection, BorderLayout.SOUTH);
        return panel;
    }

    IosDevice selectedDevice() {
        return deviceList.getSelectedValue();
    }

    boolean shouldRememberSelection() {
        return rememberSelection.isSelected();
    }
}
