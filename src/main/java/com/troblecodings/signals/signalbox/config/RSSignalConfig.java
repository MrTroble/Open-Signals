package com.troblecodings.signals.signalbox.config;

import java.util.HashMap;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.EnumSignals.HL;
import com.troblecodings.signals.EnumSignals.HLBlockExit;
import com.troblecodings.signals.EnumSignals.HLExit;
import com.troblecodings.signals.EnumSignals.HP;
import com.troblecodings.signals.EnumSignals.KS;
import com.troblecodings.signals.EnumSignals.KSMain;
import com.troblecodings.signals.EnumSignals.SHLight;
import com.troblecodings.signals.EnumSignals.SHMech;
import com.troblecodings.signals.blocks.boards.SignalRA;
import com.troblecodings.signals.blocks.signals.SignalHL;
import com.troblecodings.signals.blocks.signals.SignalHV;
import com.troblecodings.signals.blocks.signals.SignalKS;
import com.troblecodings.signals.blocks.signals.SignalSHLight;
import com.troblecodings.signals.blocks.signals.SignalSHMech;
import com.troblecodings.signals.blocks.signals.SignalSemaphore;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.tileentitys.SignalTileEnity;

public final class RSSignalConfig implements ISignalAutoconfig {

    public static final RSSignalConfig RS_CONFIG = new RSSignalConfig();

    private RSSignalConfig() {
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void change(final ConfigInfo info) {

        final HashMap<SEProperty, Object> values = new HashMap<>();

        values.put(SignalSHLight.SHLIGHT_0, SHLight.SH1);
        values.put(SignalHV.STOPSIGNAL, HP.SHUNTING);
        values.put(SignalKS.STOPSIGNAL, KS.KS_SHUNTING);
        values.put(SignalKS.MAINSIGNAL, KSMain.KS_SHUNTING);
        values.put(SignalHL.STOPSIGNAL, HL.HL_SHUNTING);
        values.put(SignalHL.EXITSIGNAL, HLExit.HL_SHUNTING);
        values.put(SignalSemaphore.RA12, true);
        values.put(SignalSHMech.SH_MECH, SHMech.SH1);
        values.put(SignalHL.BLOCKEXITSIGNAL, HLBlockExit.HL_SHUNTING);

        if (info.type.equals(PathType.SHUNTING)) {
            values.put(SignalRA.RALIGHT, true);
        }
        this.changeIfPresent(values, info.current);
    }

    @Override
    public void reset(final SignalTileEnity current) {
        current.getProperty(SignalSHLight.SHLIGHT_0)
                .ifPresent(_u -> current.setProperty(SignalSHLight.SHLIGHT_0, SHLight.SH0));
        current.getProperty(SignalRA.RALIGHT)
                .ifPresent(_u -> current.setProperty(SignalRA.RALIGHT, false));
        current.getProperty(SignalSHMech.SH_MECH)
                .ifPresent(_u -> current.setProperty(SignalSHMech.SH_MECH, SHMech.SH0));
    }
}
