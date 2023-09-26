package com.hawolt.ui.generic.dialog;

import com.hawolt.ui.generic.component.LFlatButton;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.dialog.impl.SwiftInputDialog;
import com.hawolt.ui.generic.dialog.impl.SwiftNotificationDialog;
import com.hawolt.ui.generic.dialog.impl.SwiftOptionDialog;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.HighlightType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

/**
 * Created: 27/09/2023 00:20
 * Author: Twitter @hawolt
 **/

public abstract class SwiftDialog extends JDialog implements ActionListener {
    protected final ChildUIComponent container;
    protected int selection = -1;
    private final ChildUIComponent main = new ChildUIComponent(new BorderLayout());
    private final Frame reference;


    public SwiftDialog(DialogOption... options) {
        this(null, options);
    }

    public SwiftDialog(Frame reference, DialogOption... options) {
        this(reference, Arrays.stream(options).map(DialogOption::name).toArray(String[]::new));
    }

    public SwiftDialog(Frame reference, String... options) {
        this.reference = reference;
        this.main.setBorder(
                BorderFactory.createCompoundBorder(
                        new MatteBorder(1, 1, 1, 1, Color.BLACK),
                        new EmptyBorder(5, 5, 5, 5)
                )
        );
        this.setContentPane(main);
        this.container = getContainer();
        this.main.add(container, BorderLayout.CENTER);
        this.container.setBorder(new EmptyBorder(5, 5, 5, 5));
        ChildUIComponent selection = new ChildUIComponent(new GridLayout(0, options.length, 5, 0));
        selection.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Color.DARK_GRAY));
        for (int i = 0; i < options.length; i++) {
            String option = options[i];
            LFlatButton button = new LFlatButton(option, LTextAlign.CENTER, HighlightType.COMPONENT);
            button.setActionCommand(String.valueOf(i));
            button.addActionListener(this);
            selection.add(button);
        }
        this.main.add(selection, BorderLayout.SOUTH);
        this.setUndecorated(true);
        this.setResizable(false);
    }


    public SwiftDialog configureAndShow() {
        this.pack();
        this.setLocationRelativeTo(reference);
        this.setModal(true);
        this.setVisible(true);
        return this;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.selection = Integer.parseInt(e.getActionCommand());
        this.dispose();
    }

    public int getSelection() {
        return selection;
    }

    protected abstract ChildUIComponent getContainer();

    public static int showMessageDialog(Frame frame, String... lines) {
        return new SwiftNotificationDialog(frame, lines).configureAndShow().getSelection();
    }

    public static int showOptionDialog(Frame frame, String message, String... options) {
        return new SwiftOptionDialog(frame, message, options).configureAndShow().getSelection();
    }

    public static int showOptionDialog(Frame frame, String[] messages, String... options) {
        return new SwiftOptionDialog(frame, messages, options).configureAndShow().getSelection();
    }

    public static String showInputDialog(Frame frame, String message) {
        SwiftInputDialog dialog = (SwiftInputDialog) new SwiftInputDialog(frame, message).configureAndShow();
        if (dialog.getSelection() != 0) return null;
        return dialog.getInput();
    }
}
