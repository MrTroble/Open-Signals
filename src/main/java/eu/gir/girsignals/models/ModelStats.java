package eu.gir.girsignals.models;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import eu.gir.girsignals.FileReader;

public class ModelStats {

    private Map<String, String> textures;
    private Map<String, Models> models;

    public Map<String, String> getTextures() {
        return textures;
    }

    public Map<String, Models> getModels() {
        return models;
    }

    public static Map<String, ModelStats> getfromJson(final String directory) {
        final Gson gson = new Gson();
        final Map<String, String> entrySet = FileReader.readallFilesfromDierectory(directory);
        final Map<String, ModelStats> content = new HashMap<>();
        if (entrySet != null) {
            entrySet.forEach((filename, file) -> {
                final ModelStats json = gson.fromJson(file, ModelStats.class);
                content.put(filename, json);
            });
        }
        return content;
    }

    public static String getLampPath(final String lampname, final ModelStats states) {
        for (Map.Entry<String, String> entry : states.getTextures().entrySet()) {
            if (entry.getKey().equalsIgnoreCase(lampname))
                return entry.getValue();
        }
        return null;
    }
}