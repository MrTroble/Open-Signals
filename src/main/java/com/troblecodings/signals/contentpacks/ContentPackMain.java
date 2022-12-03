package com.troblecodings.signals.contentpacks;

import java.io.File;

import eu.gir.girsignals.GIRSignalsMain;

public class ContentPackMain {

    public static final File assetsFolder = new File("./config/girsignals");

    public static void loadContentPacks() {

        if (!assetsFolder.exists()) {
            assetsFolder.mkdirs();
        } else {

        final File[] contentPacks = assetsFolder.listFiles((dir, name) -> name.endsWith(".zip"));

        if (contentPacks == null || contentPacks.length == 0)
            GIRSignalsMain.log.info("No ContentPacks found!");
        }
    }
}