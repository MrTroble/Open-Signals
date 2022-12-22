package com.troblecodings.signals.test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.troblecodings.signals.utils.FileReader;

public class CPLoaderTest {

    @Test
    public void test() {
        try {
            final URI uri = new URI(
                    "jar:file:///C:/Programming/Minecraft/Open-Signals/bin/main/assets/girsignals/forge-1.12.2-14.23.5.2860-mdk.zip");
            final FileSystem system = FileSystems.newFileSystem(uri, Collections.emptyMap());
            FileReader.addToFileSystem(system);
        } catch (final IOException | URISyntaxException e) {
            e.printStackTrace();
        }

    }

}
