package com.hawolt.ui.generic.translucent;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Created: 24/09/2023 06:49
 * Author: Twitter @hawolt
 **/

public class TranslucentButtonGroup implements ActionListener {
    private final List<TranslucentRadioButton> list = new ArrayList<>();

    public void add(TranslucentRadioButton button) {
        this.list.add(button);
        this.list.get(list.size() - 1).addActionListener(this);
    }

    public void add(TranslucentRadioButton... buttons) {
        for (TranslucentRadioButton button : buttons) add(button);
        this.setActiveIndex(0);
    }

    public void setActiveIndex(int index) {
        if (index >= list.size()) return;
        list.get(0).setActive(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (TranslucentRadioButton button : list) button.setActive(false);
        TranslucentRadioButton button = (TranslucentRadioButton) e.getSource();
        button.setActive(true);
    }
}
