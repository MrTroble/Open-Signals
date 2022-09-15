package eu.gir.girsignals.models;

import java.util.List;
import java.util.Map;

import eu.gir.girsignals.GirsignalsMain;

public class TextureStats {

    private boolean autoBlockstate;
    private String blockstate;
    private Map<String, String> retexture;
    private List<String> extentions;

    public boolean isautoBlockstate() {
        return autoBlockstate;
    }

    public String getBlockstate() {
        return blockstate;
    }

    public Map<String, String> getRetextures() {
        return retexture;
    }

    public List<String> getExtentions() {
        return extentions;
    }

    public void appendExtention(final String extentionStr, final String retexture) {

        final String ext_val = "extention_val";

        if (!blockstate.contains(ext_val)) {
            GirsignalsMain.log
                    .warn("There was a problem while loading the extention " + extentionStr
                            + "! Please check that its named " + ext_val + " to get loaded!");
            return;
        }
        
        blockstate += blockstate.replace(ext_val, extentionStr);
        this.retexture.put(retexture, ext_val);
    }
}
