package eu.gir.girsignals.signalbox.config;

import eu.gir.girsignals.EnumSignals.HL;
import eu.gir.girsignals.EnumSignals.HP;
import eu.gir.girsignals.EnumSignals.KS;
import eu.gir.girsignals.EnumSignals.SHLight;
import eu.gir.girsignals.blocks.signals.SignalHL;
import eu.gir.girsignals.blocks.signals.SignalHV;
import eu.gir.girsignals.blocks.signals.SignalKS;
import eu.gir.girsignals.blocks.signals.SignalSHLight;
import eu.gir.girsignals.tileentitys.SignalTileEnity;

public final class RSSignalConfig implements ISignalAutoconfig {

    public static final RSSignalConfig RS_CONFIG = new RSSignalConfig();

    private RSSignalConfig() {
    }

    @Override
    public void change(final int speed, final SignalTileEnity current, final SignalTileEnity next) {
        current.getProperty(SignalSHLight.SHLIGHT_0)
                .ifPresent(_u -> current.setProperty(SignalSHLight.SHLIGHT_0, SHLight.SH1));
        current.getProperty(SignalHV.STOPSIGNAL)
                .ifPresent(_u -> current.setProperty(SignalHV.STOPSIGNAL, HP.SHUNTING));
        current.getProperty(SignalKS.STOPSIGNAL)
                .ifPresent(_u -> current.setProperty(SignalKS.STOPSIGNAL, KS.KS_SHUNTING));
        current.getProperty(SignalHL.STOPSIGNAL)
                .ifPresent(_u -> current.setProperty(SignalHL.STOPSIGNAL, HL.HL_SHUNTING));
    }

    @Override
    public void reset(final SignalTileEnity current) {
        current.getProperty(SignalSHLight.SHLIGHT_0)
                .ifPresent(_u -> current.setProperty(SignalSHLight.SHLIGHT_0, SHLight.SH0));
    }

}
