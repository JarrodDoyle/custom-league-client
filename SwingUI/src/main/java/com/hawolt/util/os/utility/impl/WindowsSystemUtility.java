package com.hawolt.util.os.utility.impl;

import com.hawolt.util.os.process.ProcessReference;
import com.hawolt.util.os.process.impl.WindowsProcess;
import com.hawolt.util.os.utility.BasicSystemUtility;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created: 29/09/2023 15:20
 * Author: Twitter @hawolt
 **/

public class WindowsSystemUtility extends BasicSystemUtility {
    @Override
    public String translate(String path) {
        return path;
    }

    @Override
    public List<ProcessReference> getProcessList() throws IOException {
        ProcessBuilder builder = new ProcessBuilder("WMIC", "path", "win32_process", "get", "Caption,Processid");
        builder.redirectErrorStream(true);
        Process process = builder.start();
        List<ProcessReference> references = new ArrayList<>();
        try (InputStream stream = process.getInputStream()) {
            String[] list = readStream(stream).split("\n");
            for (int i = 1; i < list.length; i++) {
                String line = list[i];
                line = line.trim().replaceAll(" +", " ");
                if (line.isEmpty()) continue;
                references.add(new WindowsProcess(line));
            }
        }
        return references;
    }
}
