package eu.gir.girsignals.blocks.signals;

import java.util.HashMap;

import eu.gir.girsignals.EnumSignals.SH_LIGHT;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import net.minecraft.client.gui.FontRenderer;

public class SignalSHLight extends Signal {
	
	public SignalSHLight() {
		super(builder(GIRItems.PLACEMENT_TOOL, "SHLight").height(0).offsetX(-7.5f).offsetY(-6.3f).signHeight(0.35f).build());
	}
	
	public static final SEProperty<SH_LIGHT> SHLIGHT_0 = SEProperty.of("sh_light_bottom", SH_LIGHT.OFF, ChangeableStage.APISTAGE_NONE_CONFIG);
	public static final SEProperty<Boolean> SH_HIGH = SEProperty.of("sh_high", false, ChangeableStage.GUISTAGE);
	
	@Override
	public int getHeight(final HashMap<SEProperty<?>, Object> map) {
		if ((Boolean) map.getOrDefault(SH_HIGH, false)) {
			return 2;
		}
		return super.getHeight(map);
	}
	
	@Override
	public void renderOverlay(double x, double y, double z, SignalTileEnity te, FontRenderer font) {
		super.renderOverlay(x, y, z, te, font, te.getProperty(SH_HIGH).filter(b -> (Boolean) b).isPresent() ? 2.35f : this.prop.customNameRenderHeight);
	}
	
}
