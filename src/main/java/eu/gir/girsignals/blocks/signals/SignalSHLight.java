package eu.gir.girsignals.blocks.signals;

import java.util.HashMap;

import eu.gir.girsignals.EnumSignals.SH_LIGHT;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import net.minecraft.client.gui.FontRenderer;

public class SignalSHLight extends Signal {

	public SignalSHLight() {
		super(builder(GIRItems.PLACEMENT_TOOL, "SHLight").height(0).offsetX(-12.5f).offsetY(-6.3f).signHeight(0.35f).build());
	}

	public static final SEProperty<SH_LIGHT> SHLIGHT_0 = SEProperty.of("sh_light_bottom", SH_LIGHT.OFF);
	public static final SEProperty<SH_LIGHT> SHLIGHT_2 = SEProperty.of("sh_light_top", SH_LIGHT.OFF);

	@Override
	public int getHeight(final HashMap<SEProperty<?>, Object> map) {
		if (map.containsKey(SHLIGHT_2)) {
			return 2;
		}
		return super.getHeight(map);
	}
	
	@Override
	public void renderOverlay(double x, double y, double z, SignalTileEnity te, FontRenderer font) {
		super.renderOverlay(x, y, z, te, font,
				te.getProperty(SHLIGHT_2).isPresent() ? 2.35f
						: this.prop.customNameRenderHeight);
	}	
		
}
