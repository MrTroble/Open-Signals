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
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.gson.Gson;

import eu.gir.girsignals.init.GIRBlocks;

public class ModelStateBuilder {

    private String name;
    private String model;
    private String texture;
    private String has;
    private String hasnot;
    private String with;
    private String hasandis;
    private String hasandisnot;
    private String lamp;
    private String retexture;
    private Integer x;
    private Integer y;
    private Integer z;

    final Gson gson = new Gson();

    public ModelStateBuilder(final String name, final String model, final String texture,
            final String has, final String hasnot, final String with, final String hasandis,
            final String hasandisnot, final String lamp, final String retexture, final int x,
            final int y, final int z) {
        this.name = name;
        this.model = model;
        this.texture = texture;
        this.has = has;
        this.hasnot = hasnot;
        this.with = with;
        this.hasandis = hasandis;
        this.hasandisnot = hasandisnot;
        this.lamp = lamp;
        this.retexture = retexture;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Map<String, String> readfromFile(final String location) {
        final Optional<Path> pathloc = getRessourceLocation(location);
        if (pathloc.isPresent()) {
            final Path pathlocation = pathloc.get();
            try {
                final Stream<Path> inputs = Files.list(pathlocation);
                final Map<String, String> files = new HashMap<String, String>();
                inputs.forEach(file -> {
                    try {
                        final List<String> text = Files.readAllLines(file);
                        final String output = text.toString();
                        final String name = file.getFileName().toString();
                        files.put(name, output);
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

    public String getHasandis() {
        return hasandis;
    }

    public void setHasandis(String hasandis) {
        this.hasandis = hasandis;
    }

    public String getHasandisnot() {
        return hasandisnot;
    }

    public void setHasandisnot(String hasandisnot) {
        this.hasandisnot = hasandisnot;
    }

    public String getHas() {
        return has;
    }

    public void setHas(String has) {
        this.has = has;
    }

    public String getHasnot() {
        return hasnot;
    }

    public void setHasnot(String hasnot) {
        this.hasnot = hasnot;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getLamp() {
        return lamp;
    }

    public void setLamp(String lamp) {
        this.lamp = lamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRetexture() {
        return retexture;
    }

    public void setRetexture(String retexture) {
        this.retexture = retexture;
    }

    public String getTexture() {
        return texture;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }

    public String getWith() {
        return with;
    }

    public void setWith(String with) {
        this.with = with;
    }
}
