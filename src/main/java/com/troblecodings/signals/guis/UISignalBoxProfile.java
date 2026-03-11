package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.config.ConfigHandler;

import net.minecraft.resources.ResourceLocation;

public class UISignalBoxProfile {

    public static final UISignalBoxProfile DEFAULT = getDefaultProfile();
    public static final List<UISignalBoxProfile> UI_PROFILES = new ArrayList<>();
    private static final Gson GSON = new Gson();

    private String name;
    private int backgroundColor = ConfigHandler.CLIENT.signalboxBackgroundColor.get();
    private EditorModeSettings editorSettings = new EditorModeSettings();
    private OperationModeSettings operationSettings = new OperationModeSettings();
    private TextureSettings textureSettings = new TextureSettings();

    public String getName() {
        return name;
    }

    public int getBackgroundColor() {
        return backgroundColor;
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
        UI_PROFILES.add(DEFAULT);
        OpenSignalsMain.contentPacks.getFiles("signalbox").forEach(entry -> UI_PROFILES
                .add(GSON.fromJson(entry.getValue(), UISignalBoxProfile.class)));
    }

    private static UISignalBoxProfile getDefaultProfile() {
        final UISignalBoxProfile defaultProfile = new UISignalBoxProfile();
        defaultProfile.name = "default";
        defaultProfile.operationSettings.borderSettings.showLines = false;
        return defaultProfile;
    }

    public static class EditorModeSettings {

        protected UIBorderSettings borderSettings = new UIBorderSettings();

        public UIBorderSettings getUIBorderSettings() {
            return borderSettings;
        }

    }

    public static class OperationModeSettings extends EditorModeSettings {

        private int userSelectionColor = 0x2900FF00;
        private int editColor = 0x5000A2FF;

        private int freeColor = ConfigHandler.CLIENT.signalboxFreeColor.get();
        private int selectColor = ConfigHandler.CLIENT.signalboxSelectColor.get();
        private int usedColor = ConfigHandler.CLIENT.signalboxUsedColor.get();
        private int preparedColor = ConfigHandler.CLIENT.signalboxPreparedColor.get();
        private int protectedColor = ConfigHandler.CLIENT.signalboxPreparedColor.get();
        private int shuntingColor = ConfigHandler.CLIENT.signalboxShuntingColor.get();
        private int outputColor = 0xffff00;

        private int trainNumberColor = ConfigHandler.CLIENT.signalboxTrainNumberColor.get();
        private int trainnumberBackgroundColor =
                ConfigHandler.CLIENT.signalboxTrainnumberBackgroundColor.get();;

        public int getUserSelectionColor() {
            return userSelectionColor;
        }

        public int getOutputColor() {
            return outputColor;
        }

        public int getEditColor() {
            return editColor;
        }

        public int getFreeColor() {
            return freeColor;
        }

        public int getSelectColor() {
            return selectColor;
        }

        public int getUsedColor() {
            return usedColor;
        }

        public int getPreparedColor() {
            return preparedColor;
        }

        public int getTrainNumberColor() {
            return trainNumberColor;
        }

        public int getShuntingColor() {
            return shuntingColor;
        }

        public int getProtectedColor() {
            return protectedColor;
        }

        public int getTrainnumberBackgroundColor() {
            return trainnumberBackgroundColor;
        }

    }

    public static class TextureSettings {
        // TODO Integrate in system

        private String iconsTexturePath = "gui/textures/symbols.png";
        private String signalsTexturePath = "gui/textures/signals.png";

        private transient final ResourceLocation iconsLoc =
                new ResourceLocation(OpenSignalsMain.MODID, iconsTexturePath);
        private transient final ResourceLocation signalsLoc =
                new ResourceLocation(OpenSignalsMain.MODID, signalsTexturePath);

        public ResourceLocation getIconsLoc() {
            return iconsLoc;
        }

        public ResourceLocation getSignalsLoc() {
            return signalsLoc;
        }

    }

    public static class UIBorderSettings {

        private boolean showLines = true;
        private int lineColor = 0xFF5B5B5B;
        private float lineWidth = 0.5f;

        public boolean isShowLines() {
            return showLines;
        }

        public int getLineColor() {
            return lineColor;
        }

        public float getLineWidth() {
            return lineWidth;
        }

    }

}
