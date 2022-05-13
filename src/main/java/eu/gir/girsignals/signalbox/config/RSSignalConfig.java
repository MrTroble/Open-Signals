package eu.gir.girsignals.signalbox.config;

import eu.gir.girsignals.EnumSignals.HL;
import eu.gir.girsignals.EnumSignals.HLExit;
import eu.gir.girsignals.EnumSignals.HP;
import eu.gir.girsignals.EnumSignals.KS;
import eu.gir.girsignals.EnumSignals.KSMain;
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
    public void change(final ConfigInfo info) {
        info.current.getProperty(SignalSHLight.SHLIGHT_0)
                .ifPresent(_u -> info.current.setProperty(SignalSHLight.SHLIGHT_0, SHLight.SH1));
        info.current.getProperty(SignalHV.STOPSIGNAL)
                .ifPresent(_u -> info.current.setProperty(SignalHV.STOPSIGNAL, HP.SHUNTING));
        info.current.getProperty(SignalKS.STOPSIGNAL)
                .ifPresent(_u -> info.current.setProperty(SignalKS.STOPSIGNAL, KS.KS_SHUNTING));
        info.current.getProperty(SignalKS.MAINSIGNAL)
                .ifPresent(_u -> info.current.setProperty(SignalKS.MAINSIGNAL, KSMain.KS_SHUNTING));
        info.current.getProperty(SignalHL.STOPSIGNAL)
                .ifPresent(_u -> info.current.setProperty(SignalHL.STOPSIGNAL, HL.HL_SHUNTING));
        info.current.getProperty(SignalHL.EXITSIGNAL)
                .ifPresent(_u -> info.current.setProperty(SignalHL.EXITSIGNAL, HLExit.HL_SHUNTING));
    }

    @Override
    public void reset(final SignalTileEnity current) {
        current.getProperty(SignalSHLight.SHLIGHT_0)
                .ifPresent(_u -> current.setProperty(SignalSHLight.SHLIGHT_0, SHLight.SH0));
    }

}
