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
                    "jar:" + CPLoaderTest.class.getResource("sounds.zip").toExternalForm());
            final FileSystem system = FileSystems.newFileSystem(uri, Collections.emptyMap());
            FileReader.addToFileSystem(system);
        } catch (final IOException | URISyntaxException e) {
            e.printStackTrace();
        }

    }

}
