package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;
import com.troblecodings.core.HexConverter;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.config.ConfigHandler;
import com.troblecodings.signals.enums.SignalBoxIcons;

import net.minecraft.util.ResourceLocation;

public class UISignalBoxProfile {

    public static final UISignalBoxProfile DEFAULT = getDefaultProfile();
    public static final Map<String, UISignalBoxProfile> NAME_FOR_PROFILE = new HashMap<>();
    public static final List<UISignalBoxProfile> UI_PROFILES = new ArrayList<>();
    private static final Gson GSON = new Gson();

    private int id;
    private String name;
    private String backgroundColor = ConfigHandler.signalboxBackgroundColor;
    private EditorModeSettings editorSettings = new EditorModeSettings();
    private OperationModeSettings operationSettings = new OperationModeSettings();
    private TextureSettings textureSettings = new TextureSettings();

    public void initializeData() {
        this.id = UI_PROFILES.size();
        UI_PROFILES.add(this);
        NAME_FOR_PROFILE.put(name, this);
    }

    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getBackgroundColor() {
        return HexConverter.decodeARGB(backgroundColor);
    }

    public EditorModeSettings getEditorModeSettings() {
        return editorSettings;
    }

    public OperationModeSettings getOperationModeSettings() {
        return operationSettings;
    }

    public TextureSettings getTextureSettings() {
        return textureSettings;
    }

    @Override
    public int hashCode() {
        return Objects.hash(editorSettings, backgroundColor, name, operationSettings,
                textureSettings);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        final UISignalBoxProfile other = (UISignalBoxProfile) obj;
        return Objects.equals(editorSettings, other.editorSettings)
                && backgroundColor == other.backgroundColor && Objects.equals(name, other.name)
                && Objects.equals(operationSettings, other.operationSettings)
                && Objects.equals(textureSettings, other.textureSettings);
    }

    @Override
    public String toString() {
        return "UISignalBoxProfile [" + name + "]";
    }

    public static void loadSignalBoxUIProfiles() {
        DEFAULT.initializeData();
        OpenSignalsMain.contentPacks.getFiles("signalbox/profiles").forEach(entry -> GSON
                .fromJson(entry.getValue(), UISignalBoxProfile.class).initializeData());
    }

    private static UISignalBoxProfile getDefaultProfile() {
        final UISignalBoxProfile defaultProfile = new UISignalBoxProfile();
        defaultProfile.name = "default";
        defaultProfile.operationSettings.borderSettings.showLines = false;
        return defaultProfile;
    }

    public static class EditorModeSettings {

        protected UIBorderSettings borderSettings = new UIBorderSettings();

        private String modePreviewBackgroundColor = "0xFFAFAFAF";
        private String modePreviewHighlightColor = "0x45339933";

        public UIBorderSettings getUIBorderSettings() {
            return borderSettings;
        }

        public int getModePreviewBackgroundColor() {
            return HexConverter.decodeARGB(modePreviewBackgroundColor);
        }

        public int getModePreviewHighlightColor() {
            return HexConverter.decodeARGB(modePreviewHighlightColor);
        }

    }

    public static class OperationModeSettings {

        protected UIBorderSettings borderSettings = new UIBorderSettings();

        private String userSelectionColor = "0x2900FF00";
        private String editColor = "0x5000A2FF";

        private String freeColor = ConfigHandler.signalboxFreeColor;
        private String selectColor = ConfigHandler.signalboxSelectColor;
        private String usedColor = ConfigHandler.signalboxUsedColor;
        private String preparedColor = ConfigHandler.signalboxPreparedColor;
        private String protectedColor = ConfigHandler.signalboxPreparedColor;
        private String shuntingColor = ConfigHandler.signalboxShuntingColor;
        private String outputColor = "0xffff00";

        private String trainNumberColor = ConfigHandler.signalboxTrainNumberColor;
        private String trainnumberBackgroundColor =
                ConfigHandler.signalboxTrainnumberBackgroundColor;

        public UIBorderSettings getUIBorderSettings() {
            return borderSettings;
        }

        public int getUserSelectionColor() {
            return HexConverter.decodeARGB(userSelectionColor);
        }

        public int getOutputColor() {
            return HexConverter.decodeARGB(outputColor);
        }

        public int getEditColor() {
            return HexConverter.decodeARGB(editColor);
        }

        public int getFreeColor() {
            return HexConverter.decodeARGB(freeColor);
        }

        public int getSelectColor() {
            return HexConverter.decodeARGB(selectColor);
        }

        public int getUsedColor() {
            return HexConverter.decodeARGB(usedColor);
        }

        public int getPreparedColor() {
            return HexConverter.decodeARGB(preparedColor);
        }

        public int getTrainNumberColor() {
            return HexConverter.decodeARGB(trainNumberColor);
        }

        public int getShuntingColor() {
            return HexConverter.decodeARGB(shuntingColor);
        }

        public int getProtectedColor() {
            return HexConverter.decodeARGB(protectedColor);
        }

        public int getTrainnumberBackgroundColor() {
            return HexConverter.decodeARGB(trainnumberBackgroundColor);
        }

    }

    public static class TextureSettings {

        private String signsTexturePath =
                SignalBoxIcons.SIGNS.getResourceLocation().getResourcePath();
        private String signalsTexturePath =
                SignalBoxIcons.SIGNALS.getResourceLocation().getResourcePath();

        private transient ResourceLocation signsLoc;
        private transient ResourceLocation signalsLoc;

        private void generateLocs() {
            signsLoc = new ResourceLocation(OpenSignalsMain.MODID, signsTexturePath);
            signalsLoc = new ResourceLocation(OpenSignalsMain.MODID, signalsTexturePath);
        }

        public ResourceLocation getSignsLoc() {
            if (signsLoc == null) {
                generateLocs();
            }
            return signsLoc;
        }

        public ResourceLocation getSignalsLoc() {
            if (signalsLoc == null) {
                generateLocs();
            }
            return signalsLoc;
        }

    }

    public static class UIBorderSettings {

        private boolean showLines = true;
        private String lineColor = "0xFF5B5B5B";
        private float lineWidth = 0.5f;

        public boolean isShowLines() {
            return showLines;
        }

        public int getLineColor() {
            return HexConverter.decodeARGB(lineColor);
        }

        public float getLineWidth() {
            return lineWidth;
        }

    }

}
