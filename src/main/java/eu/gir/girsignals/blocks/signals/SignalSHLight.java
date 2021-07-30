package eu.gir.girsignals.blocks.signals;

import eu.gir.girsignals.EnumSignals.SH_LIGHT;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.nbt.NBTTagCompound;

public class SignalSHLight extends Signal {

	public SignalSHLight() {
		super(builder(GIRItems.PLACEMENT_TOOL, "SHLight").height(0).offsetX(-12.5f).offsetY(-6.3f).signHeight(0.35f).build());
	}

	public static final SEProperty<SH_LIGHT> SHLIGHT_0 = SEProperty.of("sh_light_bottom", SH_LIGHT.OFF);
	public static final SEProperty<SH_LIGHT> SHLIGHT_2 = SEProperty.of("sh_light_top", SH_LIGHT.OFF);

	@Override
	public int getHeight(NBTTagCompound comp) {
		if (comp.getBoolean(SHLIGHT_2.getName())) {
			return 2;
		}
		return super.getHeight(comp);
	}
	
	@Override
	public void renderOverlay(double x, double y, double z, SignalTileEnity te, FontRenderer font) {
		super.renderOverlay(x, y, z, te, font,
				te.getProperty(SHLIGHT_2).isPresent() ? 2.35f
						: this.prop.customNameRenderHeight);
	}	
		
}
