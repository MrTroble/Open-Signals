package com.troblecodings.signals.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.io.file.SimplePathVisitor;

import com.google.common.io.ByteStreams;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.init.OSBlocks;

public final class FileReader {

    private static FileSystem fileSystemCache = null;

    private FileReader() {
    }

    /**
     * 
     * @param directory : The path to the directory you want to read out from the
     *                  resource folder of this mod
     * @return a map containing all files, as key the filename and as value the
     *         content of this file
     */

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
                        OpenSignalsMain.log
                                .warn("There was a problem during loading " + file + " !");
                        e.printStackTrace();
                    }
                });
                inputs.close();
                return files;
            } catch (final IOException e) {
                OpenSignalsMain.log.warn(
                        "There was a problem during listing all files from " + pathlocation + " !");
                e.printStackTrace();
            }
        }
        if (files.isEmpty()) {
            OpenSignalsMain.getLogger().warn("No files found at " + directory + "!");
        }
        return files;
    }

    public static Optional<Path> getRessourceLocation(final String location) {
        String filelocation = location;
        final URL url = OSBlocks.class.getResource("/assets/girsignals");
        try {
            if (url != null) {
                final URI uri = url.toURI();
                if ("file".equals(uri.getScheme())) {
                    if (!location.startsWith("/"))
                        filelocation = "/" + filelocation;

                    if (fileSystemCache == null) {
                        fileSystemCache = FileSystems.getDefault();
                    }

                    final URL resource = OSBlocks.class.getResource(filelocation);
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

    public static void addToFileSystem(final FileSystem system) {
        if (fileSystemCache == null)
            getRessourceLocation("");

        final URL url = OSBlocks.class.getResource("/assets/girsignals");
        try {
            final URI uri = url.toURI();
            final String scheme = uri.getScheme();
            Path path = null;
            if (scheme.equals("file")) {
                path = fileSystemCache.provider().getPath(uri);
            } else if (scheme.equals("jar")) {
                path = fileSystemCache.getPath("/");
            }
            if (path == null) {
                OpenSignalsMain.getLogger()
                        .error("[Error]: Could not get path to add to file system!");
                return;
            }
            final Path finalPath = path;
            Files.walkFileTree(system.getPath("/"), new SimplePathVisitor() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                        throws IOException {
                    Path nextPath = finalPath.resolve(file.getFileName().toString());
                    ByteStreams.copy(Files.newInputStream(file), Files.newOutputStream(nextPath));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (final URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }
}