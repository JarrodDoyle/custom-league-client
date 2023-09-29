package com.hawolt.util.os.process.impl;

import com.hawolt.util.os.process.ProcessReference;

/**
 * Created: 29/09/2023 15:28
 * Author: Twitter @hawolt
 **/

public class WindowsProcess extends ProcessReference {
    public WindowsProcess(String line) {
        super(line);
    }

    @Override
    public void configure(String line) {
        int lastIndex = line.lastIndexOf(" ");
        String pid = line.substring(lastIndex + 1);
        String remainder = line.substring(0, lastIndex);
        this.name = remainder.trim();
        this.pid = Integer.parseInt(pid);
    }
}
