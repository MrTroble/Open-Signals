package com.troblecodings.signals.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.troblecodings.signals.SignalsMain;
import com.troblecodings.signals.init.SignalBlocks;

public final class FileReader {

    private static FileSystem fileSystemCache = null;

    private FileReader() {
    }

    public static Map<String, String> readallFilesfromDierectory(final String directory) {
        final Map<String, String> files = new HashMap<>();
        final Optional<Path> filepath = getRessourceLocation(directory);
        if (filepath.isPresent()) {
            final Path pathlocation = filepath.get();
            try {

                final Stream<Path> inputs = Files.list(pathlocation);

                inputs.forEach(file -> {
                    try {
                        final String content = new String(Files.readAllBytes(file));
                        final String name = file.getFileName().toString();

                        files.put(name, content);
                    } catch (final IOException e) {
                        SignalsMain.log
                                .warn("There was a problem during loading " + file + " !");
                        e.printStackTrace();
                    }
                });
                inputs.close();
                return files;
            } catch (final IOException e) {
                SignalsMain.log.warn(
                        "There was a problem during listing all files from " + pathlocation + " !");
                e.printStackTrace();
            }
        }
        return files;
    }

    public static Optional<Path> getRessourceLocation(final String location) {

        String filelocation = location;

        final URL url = SignalBlocks.class.getResource("/assets/signals");
        try {
            if (url != null) {

                final URI uri = url.toURI();

                if ("file".equals(uri.getScheme())) {

                    if (!location.startsWith("/"))
                        filelocation = "/" + filelocation;

                    final URL resource = SignalBlocks.class.getResource(filelocation);

                    if (resource == null)
                        return Optional.empty();

                    return Optional.of(Paths.get(resource.toURI()));

                } else {

                    if (!"jar".equals(uri.getScheme())) {
                        return Optional.empty();
                    }

                    if (fileSystemCache == null) {
                        fileSystemCache = FileSystems.newFileSystem(uri, Collections.emptyMap());
                    }
                    return Optional.of(fileSystemCache.getPath(filelocation));
                }
            }
        } catch (final IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
