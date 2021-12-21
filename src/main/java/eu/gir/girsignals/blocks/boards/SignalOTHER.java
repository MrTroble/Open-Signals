package eu.gir.girsignals.blocks.boards;

import java.util.HashMap;

import eu.gir.girsignals.EnumSignals.OTHER_SIGNAL;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import net.minecraft.client.gui.FontRenderer;

public class SignalOTHER extends Signal {

	public SignalOTHER() {
		super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "othersignal").signScale(3.5f).offsetX(-5).offsetY(1.8f).noLink()
				.build());
	}

	public static final SEProperty<OTHER_SIGNAL> OTHERTYPE = SEProperty.of("othertype", OTHER_SIGNAL.HM,
			ChangeableStage.GUISTAGE, false);

	@Override
	public boolean canHaveCustomname(final HashMap<SEProperty<?>, Object> map) {
		return map.entrySet().stream()
				.anyMatch(entry -> entry.getKey().equals(OTHERTYPE) && entry.getValue().equals(OTHER_SIGNAL.HM));
	}

	@Override
	public void renderOverlay(double x, double y, double z, SignalTileEnity te, FontRenderer font) {
		super.renderOverlay(x, y, z, te, font,
				te.getProperty(OTHERTYPE).filter(OTHER_SIGNAL.HM::equals).isPresent() ? 2.1f
						: this.prop.customNameRenderHeight);
	}
}
