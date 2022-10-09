package eu.gir.girsignals.models;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import eu.gir.girsignals.GIRFileReader;

public class ModelStats {

    private Map<String, String> textures;
    private Map<String, Models> models;

    public ImmutableMap<String, String> getTextures() {
        return ImmutableMap.copyOf(textures);
    }

    public ImmutableMap<String, Models> getModels() {
        return ImmutableMap.copyOf(models);
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

    public static Map<String, String> createRetexture(final Map<String, String> textureNames,
            final Map<String, String> textureTranslationTable) {

        if (textureTranslationTable.isEmpty()) {
            return textureNames;
        }

        final Map<String, String> retexturemap = new HashMap<>();

        if (textureNames != null) {
            for (final Map.Entry<String, String> entry : textureNames.entrySet()) {

                final String key = entry.getKey();
                final String textureVariable = entry.getValue();
                final String texture = textureTranslationTable.get(textureVariable);

                if (texture == null) {
                    retexturemap.put(key, textureVariable);
                } else {
                    retexturemap.put(key, texture);
                }
            }
        }
        return retexturemap;
    }
}