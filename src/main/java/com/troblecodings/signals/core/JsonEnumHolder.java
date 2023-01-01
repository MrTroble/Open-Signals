package com.troblecodings.signals.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.contentpacks.ContentPackException;

public class JsonEnumHolder {

	public static final Map<String, JsonEnum> PROPERTIES = JsonEnumHolder.getProperties();

	@SuppressWarnings("unchecked")
	public static Map<String, JsonEnum> getProperties() {
	    final HashMap<String, JsonEnum> returnmap = new HashMap<>();
	    final Map<String, String> files = FileReader
	            .readallFilesfromDierectory("/assets/girsignals/enumdefinition");
	    files.forEach((_u, file) -> {
	        final Map<String, List<String>> map = JsonEnum.GSON.fromJson(file,
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
	                throw new ContentPackException(
	                        "You have added to many EnumValues! Max. is 256!");
	            }
	            returnmap.put(name.toLowerCase(), new JsonEnum(name, list));
	        });
	    });
	    PROPERTIES.put(JsonEnum.BOOLEAN.getName(), JsonEnum.BOOLEAN);
	    return returnmap;
	}

}
