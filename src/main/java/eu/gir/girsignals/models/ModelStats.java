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

        final Map<String, String> retexturemap = new HashMap<>();

        if (retexture != null) {
            for (final Map.Entry<String, String> entry : retexture.entrySet()) {

                final String key = entry.getKey();
                final String val = entry.getValue();

                retexturemap.put(key, getLampPath(val, lamp));
            }
        }
        return retexturemap;
    }

    private static String getLampPath(final String lampname, final Map<String, String> map) {
        if (lampname.startsWith("lamp_")) {
            if (map != null) {
                for (final Map.Entry<String, String> entry : map.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(lampname))
                        return entry.getValue();
                }
            }
        }
        return lampname;
    }
}