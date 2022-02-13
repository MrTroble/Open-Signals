package eu.gir.girsignals.signalbox;

import eu.gir.girsignals.EnumSignals.HP;
import eu.gir.girsignals.EnumSignals.HP_BLOCK;
import eu.gir.girsignals.EnumSignals.HP_HOME;
import eu.gir.girsignals.EnumSignals.HP_TYPE;
import eu.gir.girsignals.EnumSignals.VR;
import eu.gir.girsignals.blocks.ISignalAutoconifig;
import eu.gir.girsignals.blocks.signals.SignalHV;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public final class HVSignalConfig implements ISignalAutoconifig {
	
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

	
	@Override
	public void change(int speed, SignalTileEnity current, SignalTileEnity next) {
		if (speed < 15) {
			// TODO
		} else {
			current.setProperty(SignalHV.HPBLOCK, HP_BLOCK.HP1);
			current.setProperty(SignalHV.HPHOME, HP_HOME.HP1);
			current.setProperty(SignalHV.STOPSIGNAL, HP.HP1);
		}
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
		final World world = current.getWorld();
		final IBlockState state = world.getBlockState(current.getPos());
		world.markAndNotifyBlock(current.getPos(), null, state, state, 3);
	}
	
	@Override
	public void reset(SignalTileEnity current, SignalTileEnity prev) {
		
	}
	
}
