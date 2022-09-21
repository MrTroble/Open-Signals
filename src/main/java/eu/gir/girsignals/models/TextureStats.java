package eu.gir.girsignals.models;

import java.util.HashMap;
import java.util.Map;

import eu.gir.girsignals.GirsignalsMain;

public class TextureStats {

    private float x = 0;
    private float y = 0;
    private float z = 0;

    private boolean loadOFFstate = true;
    private String blockstate;
    private Map<String, String> retexture;
    private Map<String, Map<String, String>> extentions;

    public float getOffsetX() {
        return x;
    }

    public float getOffsetY() {
        return y;
    }

    public float getOffsetZ() {
        return z;
    }

    public boolean loadOFFstate() {
        return loadOFFstate;
    }

    public boolean isautoBlockstate() {
        return blockstate == null;
    }

    public String getBlockstate() {
        return blockstate;
    }

    public void resetStates(final String state, final Map<String, String> retexture) {

        blockstate = state;

        this.retexture = retexture;
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

    /**
     * @return true if this model with these parameters should be loaded
     */

    public boolean appendExtention(final String seprop, final String enums,
            final String retexturekey, final String retexureval, final String modelname) {

        if (!loadOFFstate && enums.equalsIgnoreCase("OFF"))
            return false;

        if (retexture == null) {

            retexture = new HashMap<>();
        }

        retexture.put(retexturekey, retexureval);

        if (!blockstate.contains("with(prop.prop)")) {

            GirsignalsMain.log.error("Unable to load an extention into " + modelname
                    + "! Did you included 'with(prop.prop)' to your blockstate? The state was "
                    + blockstate + ".");
            return false;
        }

        blockstate = blockstate.replace("prop.prop", seprop + "." + enums);
        return true;

    }
}
