package eu.gir.girsignals;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import eu.gir.girsignals.init.GIRBlocks;

public final class GIRFileReader {

    private GIRFileReader() {
    }

    public static Map<String, String> readallFilesfromDierectory(final String directory) {
        final Map<String, String> files = new HashMap<>();
        final Optional<Path> pathloc = getRessourceLocation(directory);
        if (pathloc.isPresent()) {
            final Path pathlocation = pathloc.get();
            try {

                final Stream<Path> inputs = Files.list(pathlocation);

                inputs.forEach(file -> {
                    try {
                        final List<String> text = Files.readAllLines(file);
                        final String content = toString(text);
                        final String name = file.getFileName().toString();

                        files.put(name, content);
                    } catch (final IOException e) {
                        GirsignalsMain.log
                                .warn("There was a problem during loading " + file + " !");
                        e.printStackTrace();
                    }
                });
                inputs.close();
                return files;
            } catch (final IOException e) {
                GirsignalsMain.log.warn(
                        "There was a problem during listing all files from " + pathlocation + " !");
                e.printStackTrace();
            }
        }
        return files;
    }

    private static String toString(final List<String> text) {
        final StringBuilder stringbuilder = new StringBuilder();
        text.forEach(string -> {
            stringbuilder.append(string);
            stringbuilder.append("\n");
        });
        return stringbuilder.toString();
    }

    public static Optional<Path> getRessourceLocation(final String location) {

        String loc = location;

        final URL url = GIRBlocks.class.getResource("/assets/girsignals");
        try {
            if (url != null) {

                final URI uri = url.toURI();

                if ("file".equals(uri.getScheme())) {

                    if (!location.startsWith("/"))
                        loc = "/" + loc;

                    final URL resource = GIRBlocks.class.getResource(loc);

                    if (resource == null)
                        return Optional.empty();

                    return Optional.of(Paths.get(resource.toURI()));

                } else {

                    if (!"jar".equals(uri.getScheme())) {
                        return Optional.empty();
                    }

                    try (final FileSystem filesystem = FileSystems.newFileSystem(uri,
                            Collections.emptyMap())) {
                        return Optional.of(filesystem.getPath(loc));
                    }
                }
            }
        } catch (final IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
