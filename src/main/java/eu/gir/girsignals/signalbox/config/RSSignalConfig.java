package eu.gir.girsignals.signalbox.config;

import java.util.HashMap;

import eu.gir.girsignals.EnumSignals.HL;
import eu.gir.girsignals.EnumSignals.HLExit;
import eu.gir.girsignals.EnumSignals.HP;
import eu.gir.girsignals.EnumSignals.KS;
import eu.gir.girsignals.EnumSignals.KSMain;
import eu.gir.girsignals.EnumSignals.SHLight;
import eu.gir.girsignals.EnumSignals.SHMech;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.boards.SignalRA;
import eu.gir.girsignals.blocks.signals.SignalHL;
import eu.gir.girsignals.blocks.signals.SignalHV;
import eu.gir.girsignals.blocks.signals.SignalKS;
import eu.gir.girsignals.blocks.signals.SignalSHLight;
import eu.gir.girsignals.blocks.signals.SignalSHMech;
import eu.gir.girsignals.blocks.signals.SignalSemaphore;
import eu.gir.girsignals.enums.PathType;
import eu.gir.girsignals.tileentitys.SignalTileEnity;

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
