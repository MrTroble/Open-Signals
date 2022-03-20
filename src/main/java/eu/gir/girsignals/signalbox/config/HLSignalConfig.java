package eu.gir.girsignals.signalbox.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Lists;

import eu.gir.girsignals.EnumSignals.HL;
import eu.gir.girsignals.EnumSignals.HL_DISTANT;
import eu.gir.girsignals.EnumSignals.HL_LIGHTBAR;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.ISignalAutoconfig;
import eu.gir.girsignals.blocks.signals.SignalHL;
import eu.gir.girsignals.tileentitys.SignalTileEnity;

public final class HLSignalConfig implements ISignalAutoconfig {
	
	public static final HLSignalConfig INSTANCE = new HLSignalConfig();
	
	private HLSignalConfig() {
	}
	
	@SuppressWarnings({ "rawtypes" })
	private void speedCheck(final int speed, final Map<SEProperty, Object> values, HL normal, HL restricted) {
		if (speed >= 1 && speed <= 10) {
			values.put(SignalHL.STOPSIGNAL, restricted);
			if (speed <= 5) {
				values.put(SignalHL.LIGHTBAR, HL_LIGHTBAR.OFF);
			} else if (speed <= 9) {
				values.put(SignalHL.LIGHTBAR, HL_LIGHTBAR.YELLOW);
			} else if (speed == 10) {
				values.put(SignalHL.LIGHTBAR, HL_LIGHTBAR.GREEN);
			}
		} else {
			values.put(SignalHL.STOPSIGNAL, normal);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void change(int speed, SignalTileEnity current, SignalTileEnity next) {
		final HashMap<SEProperty, Object> values = new HashMap<>();
		next.getProperty(SignalHL.STOPSIGNAL).ifPresent(hl -> current.getProperty(SignalHL.DISTANTSIGNAL).ifPresent(_u -> {
			final boolean stop = hl.equals(HL.HP0) || hl.equals(HL.HP0_ALTERNATE_RED);
			if (stop) {
				current.setProperty(SignalHL.DISTANTSIGNAL, HL_DISTANT.HL10);
			} else if (hl.equals(HL.HL4)) {
				current.setProperty(SignalHL.DISTANTSIGNAL, HL_DISTANT.HL4);
			} else if (hl.equals(HL.HL2_3) || hl.equals(HL.HL5_6) || hl.equals(HL.HL7) || hl.equals(HL.HL8_9) || hl.equals(HL.HL11_12)) {
				current.setProperty(SignalHL.DISTANTSIGNAL, HL_DISTANT.HL7);
			} else {
				current.setProperty(SignalHL.DISTANTSIGNAL, HL_DISTANT.HL1);
			}
		}));
		final ArrayList<HL> stopCheck = Lists.newArrayList(HL.HP0, HL.HP0_ALTERNATE_RED, HL.HL_ZS1, HL.HL_SHUNTING);
		final ArrayList<HL> unchanged = Lists.newArrayList(HL.HL1, HL.HL4, HL.HL7, HL.HL10);
		final boolean stop = next.getProperty(SignalHL.STOPSIGNAL).filter(o -> stopCheck.contains(o) || (unchanged.contains(o) && next.getProperty(SignalHL.LIGHTBAR).filter(lbar -> !lbar.equals(HL_LIGHTBAR.OFF)).isPresent())).isPresent();
		final ArrayList<HL> nextChangedSpeed = Lists.newArrayList(HL.HL2_3, HL.HL5_6, HL.HL8_9, HL.HL11_12);
		final boolean changed100 = next.getProperty(SignalHL.STOPSIGNAL).filter(nextChangedSpeed::contains).isPresent() && next.getProperty(SignalHL.LIGHTBAR).filter(HL_LIGHTBAR.GREEN::equals).isPresent();
		final boolean normalSpeed = next.getProperty(SignalHL.STOPSIGNAL).filter(unchanged::contains).isPresent() && next.getProperty(SignalHL.LIGHTBAR).filter(HL_LIGHTBAR.OFF::equals).isPresent();
		
		if (stop) {
			speedCheck(speed, values, HL.HL10, HL.HL11_12);
		} else if (changed100) {
			speedCheck(speed, values, HL.HL4, HL.HL5_6);
		} else if (normalSpeed) {
			speedCheck(speed, values, HL.HL1, HL.HL2_3);
		} else {
			speedCheck(speed, values, HL.HL7, HL.HL8_9);
		}
		values.forEach((property, signal) -> current.getProperty(property).ifPresent(_u -> current.setProperty(property, (Comparable) signal)));
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void reset(SignalTileEnity current) {
		final HashMap<SEProperty, Object> values = new HashMap<>();
		values.put(SignalHL.LIGHTBAR, HL_LIGHTBAR.OFF);
		values.put(SignalHL.DISTANTSIGNAL, HL_DISTANT.HL10);
		values.put(SignalHL.STOPSIGNAL, HL.HP0);
		values.put(SignalHL.ZS2, ZS32.OFF);
		this.changeIfPresent(values, current);
	}
	
}
