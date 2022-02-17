package eu.gir.girsignals.signalbox;

import java.util.HashMap;

import eu.gir.girsignals.EnumSignals.HP;
import eu.gir.girsignals.EnumSignals.HP_BLOCK;
import eu.gir.girsignals.EnumSignals.HP_HOME;
import eu.gir.girsignals.EnumSignals.HP_TYPE;
import eu.gir.girsignals.EnumSignals.VR;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.ISignalAutoconfig;
import eu.gir.girsignals.blocks.signals.SignalHV;
import eu.gir.girsignals.tileentitys.SignalTileEnity;

public final class HVSignalConfig implements ISignalAutoconfig {
	
	public static final HVSignalConfig INSTANCE = new HVSignalConfig();
	
	private HVSignalConfig() {
	}
	
	private VR next(HP hp) {
		switch (hp) {
		case HP0:
			return VR.VR0;
		case HP1:
			return VR.VR1;
		case HP2:
			return VR.VR2;
		case OFF:
		default:
			return VR.OFF;
		}
	}
	
	private VR next(HP_HOME hp) {
		switch (hp) {
		case HP0:
		case HP0_ALTERNATE_RED:
			return VR.VR0;
		case HP1:
			return VR.VR1;
		case HP2:
			return VR.VR2;
		case OFF:
		default:
			return VR.OFF;
		}
	}
	
	private VR next(HP_BLOCK hp) {
		switch (hp) {
		case HP0:
			return VR.VR0;
		case HP1:
			return VR.VR1;
		case OFF:
		default:
			return VR.OFF;
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void change(int speed, SignalTileEnity current, SignalTileEnity next) {
		final HashMap<SEProperty, Object> values = new HashMap<>();
		if (speed < 7 && speed > 0 && speed != 4) {
			current.getProperty(SignalHV.ZS3).ifPresent(_u -> {
				final ZS32 zs32 = ZS32.values()[ZS32.Z.ordinal() + speed];
				current.setProperty(SignalHV.ZS3, zs32);
			});
			values.put(SignalHV.HPBLOCK, HP_BLOCK.HP1);
			values.put(SignalHV.HPHOME, HP_HOME.HP2);
			values.put(SignalHV.STOPSIGNAL, HP.HP2);
		} else if (speed == 4) {
			values.put(SignalHV.HPBLOCK, HP_BLOCK.HP1);
			values.put(SignalHV.HPHOME, HP_HOME.HP2);
			values.put(SignalHV.STOPSIGNAL, HP.HP2);
		} else if (speed >= 7 && speed <= 16 ) {
			current.getProperty(SignalHV.ZS3).ifPresent(_u -> {
				final ZS32 zs32 = ZS32.values()[ZS32.Z.ordinal() + speed];
				current.setProperty(SignalHV.ZS3, zs32);
			});
			values.put(SignalHV.HPBLOCK, HP_BLOCK.HP1);
			values.put(SignalHV.HPHOME, HP_HOME.HP1);
			values.put(SignalHV.STOPSIGNAL, HP.HP1);
		} else {
			values.put(SignalHV.HPBLOCK, HP_BLOCK.HP1);
			values.put(SignalHV.HPHOME, HP_HOME.HP1);
			values.put(SignalHV.STOPSIGNAL, HP.HP1);
		}
		values.forEach((sep, value) -> current.getProperty(sep).ifPresent(_u -> current.setProperty(sep, (Comparable)value)));
		current.getProperty(SignalHV.DISTANTSIGNAL).ifPresent(_u -> next.getProperty(SignalHV.HPTYPE).ifPresent(type -> {
			VR vr = VR.VR0;
			switch ((HP_TYPE) type) {
			case HPBLOCK:
				vr = next((HP_BLOCK) next.getProperty(SignalHV.HPBLOCK).get());
				break;
			case HPHOME:
				vr = next((HP_HOME) next.getProperty(SignalHV.HPHOME).get());
				break;
			case STOPSIGNAL:
				vr = next((HP) next.getProperty(SignalHV.STOPSIGNAL).get());
				break;
			case OFF:
			default:
				break;
			}
			current.setProperty(SignalHV.DISTANTSIGNAL, vr);
		}));
		current.getProperty(SignalHV.ZS3V).ifPresent(_u -> {
			current.setProperty(SignalHV.ZS3V, ZS32.OFF);
			next.getProperty(SignalHV.ZS3).ifPresent(prevzs3 -> current.setProperty(SignalHV.ZS3V, (ZS32) prevzs3));
		});
	}
	
	@Override
	public void reset(SignalTileEnity current, SignalTileEnity prev) {
		
	}
	
}
