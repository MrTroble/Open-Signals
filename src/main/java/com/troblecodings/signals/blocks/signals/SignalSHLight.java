package com.troblecodings.signals.blocks.signals;

import java.util.Map;

import com.troblecodings.signals.ChangeableStage;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.EnumSignals.SHLight;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.init.GIRItems;
import com.troblecodings.signals.signalbox.config.RSSignalConfig;
import com.troblecodings.signals.tileentitys.SignalTileEnity;

import net.minecraft.client.gui.FontRenderer;

public class SignalSHLight extends Signal {

    public SignalSHLight() {
        super(builder(GIRItems.PLACEMENT_TOOL, "SHLight").height(0).offsetX(-7.5f).offsetY(-6.3f)
                .signHeight(0.373f).config(RSSignalConfig.RS_CONFIG).build());
    }

    public static final SEProperty<SHLight> SHLIGHT_0 = SEProperty.of("sh_light_bottom",
            SHLight.OFF, ChangeableStage.APISTAGE_NONE_CONFIG);
    public static final SEProperty<Boolean> SH_HIGH = SEProperty.of("sh_high", false,
            ChangeableStage.GUISTAGE);

    @Override
    public int getHeight(final Map<SEProperty<?>, Object> map) {
        if ((Boolean) map.getOrDefault(SH_HIGH, false)) {
            return 2;
        }
        return super.getHeight(map);
    }

    @Override
    public void renderOverlay(final double x, final double y, final double z,
            final SignalTileEnity te, final FontRenderer font) {
        super.renderOverlay(x, y, z, te, font,
                te.getProperty(SH_HIGH).filter(b -> (Boolean) b).isPresent() ? 2.373f
                        : this.prop.customNameRenderHeight);
    }

}