package eu.gir.girsignals.signalbox.config;

import eu.gir.girsignals.EnumSignals.HL;
import eu.gir.girsignals.EnumSignals.HP;
import eu.gir.girsignals.EnumSignals.KS;
import eu.gir.girsignals.EnumSignals.SH_LIGHT;
import eu.gir.girsignals.blocks.ISignalAutoconfig;
import eu.gir.girsignals.blocks.signals.SignalHL;
import eu.gir.girsignals.blocks.signals.SignalHV;
import eu.gir.girsignals.blocks.signals.SignalKS;
import eu.gir.girsignals.blocks.signals.SignalSHLight;
import eu.gir.girsignals.tileentitys.SignalTileEnity;

public class RSSignalConfig implements ISignalAutoconfig {
	
	@Override
	public void change(int speed, SignalTileEnity current, SignalTileEnity next) {
		current.getProperty(SignalSHLight.SHLIGHT_0).ifPresent(_u -> current.setProperty(SignalSHLight.SHLIGHT_0, SH_LIGHT.SH1));
		current.getProperty(SignalHV.STOPSIGNAL).ifPresent(_u -> current.setProperty(SignalHV.STOPSIGNAL, HP.SHUNTING));
		current.getProperty(SignalKS.STOPSIGNAL).ifPresent(_u -> current.setProperty(SignalKS.STOPSIGNAL, KS.KS_SHUNTING));
		current.getProperty(SignalHL.STOPSIGNAL).ifPresent(_u -> current.setProperty(SignalHL.STOPSIGNAL, HL.HL_SHUNTING));
	}
	
	@Override
	public void reset(SignalTileEnity current) {
		current.getProperty(SignalSHLight.SHLIGHT_0).ifPresent(_u -> current.setProperty(SignalSHLight.SHLIGHT_0, SH_LIGHT.SH0));
	}
	
}
