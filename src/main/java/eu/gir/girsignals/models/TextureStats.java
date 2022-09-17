package eu.gir.girsignals.models;

import java.util.Map;

public class TextureStats {

    private String blockstate;
    private Map<String, String> retexture;
    private Map<String, Map<String, String>> extentions;

    public boolean isautoBlockstate() {
        return blockstate == null;
    }

    public void resetStates(final String newblockstate, final Map<String, String> retexture) {
        this.blockstate = newblockstate;

        this.retexture = retexture;
    }

    public String getBlockstate() {
        return blockstate;
    }

    public Map<String, String> getRetextures() {
        return retexture;
    }

    /**
     * 1. String: Name of the file to get load 2. String: SEProperty 3. String: The
     * key of the retexture map
     */
    public Map<String, Map<String, String>> getExtentions() {
        return extentions;
    }

    public void appendExtention(final String seprop, final String enums, final String retexturekey,
            final String retexureval) {

        retexture.put(retexturekey, retexureval);

        if (blockstate.isEmpty() || blockstate == null) {

            blockstate = "with(" + seprop + "." + enums + ")";

        } else {

            blockstate += " && with(" + seprop + "." + enums + ")";
        }
    }

    /*
     * private static void appendRetexture(final TextureStats states, final String
     * retexturekey, final String retextureval) {
     * 
     * states.getRetextures().put(retexturekey, retextureval); }
     * 
     * private static void appendBlockstate(final TextureStats states, final String
     * seprop, final String enums, final boolean empty) {
     * 
     * if (empty) {
     * 
     * states.resetAndSetNewBlockstate("with(" + seprop + "." + enums + ")");
     * 
     * } else {
     * 
     * states.appendBlockstate("&& with(" + seprop + "." + enums + ")"); } }
     */
}
