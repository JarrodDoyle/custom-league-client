package com.hawolt.util.os.utility;

import com.hawolt.util.os.process.ProcessReference;
import com.hawolt.util.os.process.observer.ProcessCallback;
import com.hawolt.util.os.process.observer.ProcessObserver;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public interface SystemUtility {
    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    static ScheduledFuture<?> listen(String name, ProcessCallback callback) {
        return service.scheduleAtFixedRate(new ProcessObserver(name, callback), 0, 1000L, TimeUnit.MILLISECONDS);
    }

    Optional<ProcessReference> getProcessByPID(int pid) throws IOException;

    List<ProcessReference> getProcessList() throws IOException;

    int[] getProcessByName(String name) throws IOException;

    boolean isProcessRunning(String name) throws IOException;

    String translate(String path);
}
