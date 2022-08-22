package eu.gir.girsignals.models;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import eu.gir.girsignals.GIRFileReader;

public class ModelStats {

    private Map<String, String> texture;
    private Map<String, Models> models;

    public Map<String, String> getTextures() {
        return texture;
    }

    public Map<String, Models> getModels() {
        return models;
    }

    public static Map<String, ModelStats> getfromJson(final String directory) {
        final Gson gson = new Gson();
        final Map<String, String> entrySet = GIRFileReader.readallFilesfromDierectory(directory);
        final Map<String, ModelStats> content = new HashMap<>();
        if (entrySet != null) {
            entrySet.forEach((filename, file) -> {
                final ModelStats json = gson.fromJson(file, ModelStats.class);
                content.put(filename, json);
            });
        }
        return content;
    }

    public static String createRetexture(final String lampname, final String lamppath,
            final Map<String, String> map) {
        if (getLampPath(lamppath, map) != null)
            return lampname + " " + getLampPath(lamppath, map);
        else
            return null;
    }

    private static String getLampPath(final String lampname, final Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(lampname))
                return entry.getValue();
        }
        return null;
    }
}