package eu.gir.girsignals.blocks.boards;

import eu.gir.girsignals.EnumSignals.OTHER_SIGAL;
import eu.gir.girsignals.EnumSignals.ST_NUMBER;

import java.util.HashMap;

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

	public static final SEProperty<OTHER_SIGAL> OTHERTYPE = SEProperty.of("othertype", OTHER_SIGAL.HM,
			ChangeableStage.GUISTAGE);
	public static final SEProperty<ST_NUMBER> STATIONNUMBER = SEProperty.of("stationnumber", ST_NUMBER.Z1,
			ChangeableStage.GUISTAGE);

	@Override
	public boolean canHaveCustomname(final HashMap<SEProperty<?>, Object> map) {
		return map.entrySet().stream()
				.anyMatch(entry -> entry.getKey().equals(OTHERTYPE) && entry.getValue().equals(OTHER_SIGAL.HM));
	}

	@Override
	public void renderOverlay(double x, double y, double z, SignalTileEnity te, FontRenderer font) {
		super.renderOverlay(x, y, z, te, font,
				te.getProperty(OTHERTYPE).filter(OTHER_SIGAL.HM::equals).isPresent() ? 2.1f
						: this.prop.customNameRenderHeight);
	}
}
