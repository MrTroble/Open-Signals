package eu.gir.girsignals.signalbox.config;

import java.util.HashMap;
import java.util.Optional;

import eu.gir.girsignals.EnumSignals.KS;
import eu.gir.girsignals.EnumSignals.KS_DISTANT;
import eu.gir.girsignals.EnumSignals.KS_MAIN;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.ISignalAutoconfig;
import eu.gir.girsignals.blocks.signals.SignalKS;
import eu.gir.girsignals.tileentitys.SignalTileEnity;

public final class KSSignalConfig implements ISignalAutoconfig {
	
	public static final KSSignalConfig INSTANCE = new KSSignalConfig();
	
	private KSSignalConfig() {
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void change(int speed, SignalTileEnity current, SignalTileEnity next) {
		current.getProperty(SignalKS.ZS3V).ifPresent(_u -> next.getProperty(SignalKS.ZS3).ifPresent(value -> current.setProperty(SignalKS.ZS3V, (ZS32) value)));
		if (speed <= 16 && speed > 0) {
			final ZS32 zs32 = ZS32.values()[ZS32.Z.ordinal() + speed];
			current.getProperty(SignalKS.ZS3).ifPresent(_u -> current.setProperty(SignalKS.ZS3, zs32));
		}
		final boolean changes = next.getProperty(SignalKS.ZS3).filter(e -> ((ZS32) e).ordinal() > ZS32.Z.ordinal() && (((ZS32) e).ordinal() - ZS32.Z.ordinal()) < speed).isPresent();
		final HashMap<SEProperty, Object> values = new HashMap<>();
		values.put(SignalKS.MAINSIGNAL, KS_MAIN.KS1);
		final Optional opt = next.getProperty(SignalKS.STOPSIGNAL);
		final boolean stop = next.getProperty(SignalKS.MAINSIGNAL).filter(KS_MAIN.HP0::equals).isPresent() || opt.filter(KS.HP0::equals).isPresent();
		if (stop) {
			values.put(SignalKS.STOPSIGNAL, KS.KS2);
			values.put(SignalKS.DISTANTSIGNAL, KS_DISTANT.KS2);
		} else if (changes) {
			values.put(SignalKS.STOPSIGNAL, KS.KS1_BLINK);
			values.put(SignalKS.DISTANTSIGNAL, KS_DISTANT.KS1_BLINK);
		} else {
			values.put(SignalKS.STOPSIGNAL, KS.KS1);
			values.put(SignalKS.DISTANTSIGNAL, KS_DISTANT.KS1);
		}
		this.changeIfPresent(values, current);
	}
	
	@SuppressWarnings({ "rawtypes" })
	@Override
	public void reset(SignalTileEnity current) {
		final HashMap<SEProperty, Object> values = new HashMap<>();
		values.put(SignalKS.DISTANTSIGNAL, KS_DISTANT.KS2);
		values.put(SignalKS.STOPSIGNAL, KS.HP0);
		values.put(SignalKS.MAINSIGNAL, KS_MAIN.HP0);
		values.put(SignalKS.ZS2, ZS32.OFF);
		values.put(SignalKS.ZS3, ZS32.OFF);
		values.put(SignalKS.ZS2V, ZS32.OFF);
		values.put(SignalKS.ZS3V, ZS32.OFF);
		this.changeIfPresent(values, current);
	}
	
}
