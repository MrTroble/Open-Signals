package com.troblecodings.signals.contentpacks;

import java.io.File;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Collections;

import com.troblecodings.contentpacklib.FileReader;
import com.troblecodings.signals.OpenSignalsMain;

public final class ContentPackMain {

    private ContentPackMain() {
    }

    public static final File ASSET_FOLDER = new File("./config/opensignals");

    public static void addContentPacks() {
        if (!ASSET_FOLDER.exists()) {
            ASSET_FOLDER.mkdirs();
        } else {
            final File[] contentPacks = ASSET_FOLDER
                    .listFiles((dir, name) -> name.endsWith(".zip"));

            if (contentPacks == null || contentPacks.length == 0) {
                OpenSignalsMain.getLogger().info("No ContentPacks found!");
                return;
            }
            for (final File contentPack : contentPacks) {
                try {
                    final URI uri = new URI("jar:" + contentPack.getPath());
                    final FileSystem fileSystem = FileSystems.newFileSystem(uri,
                            Collections.emptyMap());
                    FileReader.addToFileSystem(fileSystem);
                } catch (final Exception e) {
                    OpenSignalsMain.getLogger().error(String.format(
                            "Something went wrong during adding [%s] at [%s] to OS! Skipping this pack!",
                            contentPack.getName(), contentPack.getPath()));
                    e.printStackTrace();
                }
            }
        }
    }
}