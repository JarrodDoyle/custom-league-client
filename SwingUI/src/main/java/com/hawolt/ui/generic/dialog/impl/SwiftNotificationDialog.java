package com.hawolt.ui.generic.dialog.impl;

import com.hawolt.ui.generic.component.SwiftLabel;
import com.hawolt.ui.generic.dialog.DialogOption;
import com.hawolt.ui.generic.dialog.SwiftDialog;
import com.hawolt.ui.generic.utility.ChildUIComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created: 27/09/2023 00:25
 * Author: Twitter @hawolt
 **/

public class SwiftNotificationDialog extends SwiftDialog {
    public SwiftNotificationDialog(String... lines) {
        this(null, lines);
    }

    public SwiftNotificationDialog(Frame frame, String... lines) {
        super(frame, DialogOption.OK);
        StringBuilder builder = new StringBuilder();
        builder.append("<html><div style='text-align: center;'>");
        for (int i = 0; i < lines.length; i++) {
            if (i != 0) builder.append("<br>");
            String message = lines[i];
            builder.append(message);
        }
        builder.append("</div></html>");
        container.add(new SwiftLabel(builder.toString()), BorderLayout.CENTER);
    }

    @Override
    protected ChildUIComponent getContainer() {
        return new ChildUIComponent(new BorderLayout());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.selection = DialogOption.valueOf(e.getActionCommand()).ordinal();
        this.dispose();
    }

    @Override
    public int getSelection() {
        return selection;
    }
}
