package eu.gir.girsignals.models;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import eu.gir.girsignals.GIRFileReader;

public class ModelStats {

    private Map<String, String> textures;
    private Map<String, Models> models;

    public Map<String, String> getTextures() {
        return textures;
    }

    public Map<String, Models> getModels() {
        return models;
    }

    public static Map<String, Object> getfromJson(final String directory) {

        final Gson gson = new Gson();

        final Map<String, String> entrySet = GIRFileReader.readallFilesfromDierectory(directory);

        final Map<String, Object> content = new HashMap<>();

        if (entrySet != null) {
            entrySet.forEach((filename, file) -> {

                if (!filename.endsWith("extention.json")) {

                    final ModelStats json = gson.fromJson(file, ModelStats.class);
                    content.put(filename, json);

                } else {

                    final ModelExtention json = gson.fromJson(file, ModelExtention.class);
                    content.put(filename, json);
                }
            });
        }
        return content;
    }

    public static Map<String, String> createRetexture(final Map<String, String> retexture,
            final Map<String, String> lamp) {

        if (lamp.isEmpty()) {
            return retexture;
        }

        final Map<String, String> retexturemap = new HashMap<>();

        if (retexture != null) {
            for (final Map.Entry<String, String> entry : retexture.entrySet()) {

                final String key = entry.getKey();
                final String val = entry.getValue();
                final String lam = lamp.get(val);

                if (lam == null) {
                    retexturemap.put(key, val);
                } else {
                    retexturemap.put(key, lam);
                }
            }
        }
        return retexturemap;
    }
}