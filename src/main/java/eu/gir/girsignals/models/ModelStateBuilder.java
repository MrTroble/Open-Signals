package eu.gir.girsignals.models;

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

import com.google.gson.Gson;

import eu.gir.girsignals.init.GIRBlocks;

public class ModelStateBuilder {

    public Map<String, ModelStats> getfromJson(final String directory) {
        final Gson gson = new Gson();
        final Map<String, String> entrySet = readallFilesfromDierectory(directory);
        final Map<String, ModelStats> content = new HashMap<>();
        if (entrySet != null) {
            entrySet.forEach((filename, file) -> {
                final ModelStats json = gson.fromJson(file, ModelStats.class);
                content.put(filename, json);
            });
        }
        return content;
    }

    public static Map<String, String> readallFilesfromDierectory(final String directory) {
        final Optional<Path> pathloc = getRessourceLocation(directory);
        if (pathloc.isPresent()) {
            final Path pathlocation = pathloc.get();
            try {
                final Stream<Path> inputs = Files.list(pathlocation);
                final Map<String, String> files = new HashMap<>();
                inputs.forEach(file -> {
                    try {
                        final List<String> text = Files.readAllLines(file);
                        final String content = toString(text);
                        final String name = file.getFileName().toString();
                        files.put(name, content);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                inputs.close();
                return files;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static String toString(final List<String> text) {
        final StringBuilder stringbuilder = new StringBuilder();
        text.forEach(string -> {
            stringbuilder.append(string);
            stringbuilder.append("\n");
        });
        return stringbuilder.toString();
    }

    private static Optional<Path> getRessourceLocation(final String location) {
        final URL url = GIRBlocks.class.getResource("/assets/girsignals");
        try {
            if (url != null) {
                final URI uri = url.toURI();
                Path path;
                if ("file".equals(uri.getScheme())) {
                    path = Paths.get(GIRBlocks.class.getResource(location).toURI());
                    return Optional.of(path);
                } else {
                    if (!"jar".equals(uri.getScheme())) {
                        return Optional.empty();
                    }

                    final FileSystem filesystem = FileSystems.newFileSystem(uri,
                            Collections.emptyMap());
                    path = filesystem.getPath(location);
                    return Optional.of(path);
                }
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}