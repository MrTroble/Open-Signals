package com.troblecodings.signals.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.troblecodings.signals.OpenSignalsMain;

public final class JsonEnumHolder {

    private JsonEnumHolder() {
    }

    private static final Gson GSON = new Gson();
    public static final Map<String, JsonEnum> PROPERTIES = JsonEnumHolder.getProperties();
    public static final JsonEnum ZS32 = PROPERTIES.get("zs32");

    @SuppressWarnings("unchecked")
    public static Map<String, JsonEnum> getProperties() {
        final HashMap<String, JsonEnum> returnmap = new HashMap<>();
        OpenSignalsMain.contentPacks.getFiles("enumdefinition").forEach((entry) -> {
            final String file = entry.getValue();
            final Map<String, List<String>> map = GSON.fromJson(file,
                    (Class<Map<String, List<String>>>) (Class<?>) Map.class);
            if (map == null)
                throw new IllegalStateException("Could not parse " + file);
            map.forEach((name, list) -> {
                if (list.size() > 256) {
                    OpenSignalsMain.getLogger()
                            .info("Congratulations, you are probably one of the first people on "
                                    + "earth to try to register more than 256 EnumValues. We "
                                    + "don't want to ruin your work, but 256 is the maximum "
                                    + "number of EnumValues!");
                    OpenSignalsMain.exitMinecraftWithMessage(
                            "You have added to many EnumValues! Max. is 256!");
                }
                if (returnmap.containsKey(name.toLowerCase())) {
                    OpenSignalsMain.exitMinecraftWithMessage(
                            "The JsonEnum [" + name.toLowerCase() + "] alredy exists!");
                }
                returnmap.put(name.toLowerCase(), new JsonEnum(name, list));
            });
        });
        returnmap.put(JsonEnum.BOOLEAN.getName(), JsonEnum.BOOLEAN);
        return returnmap;
    }

}
