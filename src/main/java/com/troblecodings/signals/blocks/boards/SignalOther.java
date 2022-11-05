package com.troblecodings.signals.blocks.boards;

import java.util.Map;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.EnumSignals.OtherSignal;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.init.SignaIItems;
import com.troblecodings.signals.tileentitys.SignalTileEnity;

import net.minecraft.client.gui.FontRenderer;

public class SignalOther extends Signal {

    public SignalOther() {
        super(builder(SignaIItems.SIGN_PLACEMENT_TOOL, "othersignal").signScale(3.5f).offsetX(-5)
                .offsetY(1.8f).noLink().build());
    }

    public static final SEProperty<OtherSignal> OTHERTYPE = SEProperty.of("othertype",
            OtherSignal.HM, ChangeableStage.GUISTAGE, false);

    @Override
    public boolean canHaveCustomname(final Map<SEProperty<?>, Object> map) {
        return true;
    }

    @Override
    public void renderOverlay(final double x, final double y, final double z,
            final SignalTileEnity te, final FontRenderer font) {
        super.renderOverlay(x, y, z, te, font,
                te.getProperty(OTHERTYPE).filter(OtherSignal.HM::equals).isPresent() ? 2.1f
                        : this.prop.customNameRenderHeight);
    }
}
