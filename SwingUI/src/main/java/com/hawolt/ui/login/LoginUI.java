package com.hawolt.ui.login;

import com.hawolt.LeagueClientUI;
import com.hawolt.objects.LocalSettings;
import com.hawolt.service.LocalSettingsService;
import com.hawolt.ui.impl.JHintTextField;
import com.hawolt.util.panel.MainUIComponent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created: 06/08/2023 13:10
 * Author: Twitter @hawolt
 **/

public class LoginUI extends MainUIComponent implements ActionListener {
    private final JHintTextField username;
    private final JPasswordField password;
    private final ILoginCallback callback;
    private final JCheckBox rememberMe;
    private final JButton login;

    public static LoginUI show(LeagueClientUI leagueClientUI) {
        return new LoginUI(leagueClientUI);
    }

    private LoginUI(LeagueClientUI clientUI) {
        super(clientUI);
        this.setLayout(new GridLayout(0, 1, 0, 5));
        this.setBorder(new EmptyBorder(5, 5, 5, 5));

        this.username = new JHintTextField("");
        this.password = new JPasswordField();
        this.login = new JButton("Login");
        this.rememberMe = new JCheckBox("Remember Me");
        JLabel usernameLabel = new JLabel("Username");
        JLabel passwordLabel = new JLabel("Password");

        this.add(usernameLabel);
        this.add(username);
        this.add(passwordLabel);
        this.add(password);
        this.add(login);
        this.add(rememberMe);
        this.setPreferredSize(new Dimension(300, 200));
        this.login.addActionListener(this);
        this.container.add(this);

        // Using .setLabelFor() to bind labels to corresponding input fields
        usernameLabel.setLabelFor(username);
        passwordLabel.setLabelFor(password);

        // Enter hook so users can log in with enter
        KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_ENTER) return;
                actionPerformed(null);
            }
        };

        username.addKeyListener(enterKeyAdapter);
        password.addKeyListener(enterKeyAdapter);

        this.callback = clientUI;
        this.init();
    }

    public void toggle(boolean state) {
        rememberMe.setEnabled(state);
        username.setEnabled(state);
        password.setEnabled(state);
        login.setEnabled(state);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String pass = new String(password.getPassword());
        String user = username.getText();
        toggle(false);

        if (rememberMe.isSelected()) {
            LocalSettings settings = new LocalSettings(user, pass, rememberMe.isSelected());
            LocalSettingsService.get().writeFile(settings);
        }

        LeagueClientUI.service.execute(() -> callback.onLogin(user, pass));
    }
}
