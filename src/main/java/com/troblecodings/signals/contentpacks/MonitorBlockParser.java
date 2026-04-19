package com.troblecodings.signals.contentpacks;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.Monitor;
import com.troblecodings.signals.core.MonitorBlockProperties;
import com.troblecodings.signals.init.OSBlocks;

public class MonitorBlockParser {

    private List<MonitorBlockProperties> monitors = new ArrayList<>();

    private static final Gson GSON = new Gson();

    public static void loadMonitorBlocks() {
        OpenSignalsMain.contentPacks.getFiles("signalbox/monitors").forEach(entry -> {
            final MonitorBlockParser monitors =
                    GSON.fromJson(entry.getValue(), MonitorBlockParser.class);
            for (final MonitorBlockProperties props : monitors.monitors) {
                final Monitor monitor = new Monitor(props);
                OSBlocks.loadBlock(monitor, props.getName());
            }
        });
    }

}
