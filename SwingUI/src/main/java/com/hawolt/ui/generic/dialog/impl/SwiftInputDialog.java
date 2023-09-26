package com.hawolt.ui.generic.dialog.impl;

import com.hawolt.ui.generic.component.LHintTextField;
import com.hawolt.ui.generic.component.SwiftLabel;
import com.hawolt.ui.generic.dialog.SwiftDialog;
import com.hawolt.ui.generic.utility.ChildUIComponent;

import java.awt.*;

/**
 * Created: 27/09/2023 00:25
 * Author: Twitter @hawolt
 **/

public class SwiftInputDialog extends SwiftDialog {
    private final LHintTextField input;

    public SwiftInputDialog(String message) {
        this(null, message);
    }

    public SwiftInputDialog(Frame frame, String message) {
        this(frame, message, "OK", "CANCEL");
    }

    public SwiftInputDialog(Frame frame, String message, String... options) {
        super(frame, options);
        String builder = "<html><div style='text-align: center;'>" + message + "</div></html>";
        container.add(new SwiftLabel(builder), BorderLayout.NORTH);
        this.input = new LHintTextField("");
        container.add(input, BorderLayout.CENTER);
    }

    @Override
    protected ChildUIComponent getContainer() {
        return new ChildUIComponent(new BorderLayout());
    }

    public String getInput() {
        return input.getText();
    }
}
