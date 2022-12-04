package com.troblecodings.signals.contentpacks;

import java.io.File;

import com.troblecodings.signals.OpenSignalsMain;

public final class ContentPackMain {

    private ContentPackMain() {
    }

    public static final File ASSET_FOLDER = new File("./config/girsignals");

    public static void loadContentPacks() {

        if (!ASSET_FOLDER.exists()) {
            ASSET_FOLDER.mkdirs();
        } else {

            final File[] contentPacks = ASSET_FOLDER
                    .listFiles((dir, name) -> name.endsWith(".zip"));

            if (contentPacks == null || contentPacks.length == 0)
                OpenSignalsMain.log.info("No ContentPacks found!");
        }
    }
}