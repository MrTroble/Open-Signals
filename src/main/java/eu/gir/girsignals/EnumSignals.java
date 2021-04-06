package eu.gir.girsignals;

import net.minecraft.util.IStringSerializable;

public class EnumSignals {

	public interface IIntegerable<T> {

		public T getObjFromID(int obj);

		public int count();
	}

	public interface DefaultName<T extends Enum<T>> extends IStringSerializable, Comparable<T> {
		@Override
		public default String getName() {
			return this.toString();
		}
	}

	public interface Offable<T extends Enum<T>> extends DefaultName<T> {

		@SuppressWarnings("unchecked")
		public default T getOffState() {
			return (T) Enum.valueOf((Class<T>) this.getClass(), "OFF");
		}

	}

	public enum HP implements Offable<HP> {
		OFF, HP0, HP1, HP2, STATUS_LIGHT, SHUNTING, MALFUNCTION;
	}

	public enum VR implements Offable<VR> {
		OFF, VR0, VR1, VR2;
	}

	public enum ZS32 implements Offable<ZS32> {
		OFF(true), A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z, Z1, Z2, Z3, Z4, Z5, Z6,
		Z7, Z8, Z9, Z10, Z11, Z12, Z13, Z14, Z15, ZS6(true), ZS8(true), ZS13(true);

		private final boolean test;

		private ZS32() {
			this.test = false;
		}

		private ZS32(boolean test) {
			this.test = test;
		}

		public String getDistant() {
			if (test)
				return name();
			else
				return "v" + name();
		}
	}

	public enum MAST_SIGN implements Offable<MAST_SIGN> {
		OFF, WRW, WYWYW, WBWBW;
	}

	public enum KS implements Offable<KS> {
		OFF, HP0, KS1, KS1_BLINK, KS1_BLINK_LIGHT, KS2, KS2_LIGHT, KS_ZS1, KS_ZS7, KS_SHUNTING, KS_STATUS_LIGHT,
		MALFUNCTION;
	}

	public enum KS_DISTANT implements Offable<KS_DISTANT> {
		OFF, KS1, KS1_REPEAT, KS1_BLINK, KS1_BLINK_LIGHT, KS1_BLINK_REPEAT, KS2, KS2_LIGHT, KS2_REPEAT, KS_STATUS_LIGHT;
	}

	public enum HL implements Offable<HL> {
		OFF, HP0, HP0_ALTERNATE_RED, HL1, HL2_3, HL4, HL5_6, HL7, HL8_9, HL10, HL11_12, HL_ZS1, HL_SHUNTING,
		HL_STATUS_LIGHT, MALFUNCTION;
	}

	public enum HL_DISTANT implements Offable<HL_DISTANT> {
		OFF, HL1, HL4, HL7, HL10, HL_STATUS_LIGHT;
	}

	public enum HL_LIGHTBAR implements Offable<HL_LIGHTBAR> {
		OFF, GREEN, YELLOW;
	}

	public enum SH_LIGHT implements Offable<SH_LIGHT> {
		OFF, SH0, SH1;
	}

	public enum TRAM implements Offable<TRAM> {
		OFF, F0, F1, F2, F3, F4, F5;
	}

	public enum CAR implements Offable<CAR> {
		OFF, RED, YELLOW, GREEN;
	}

	public enum PED implements Offable<PED> {
		OFF, RED, GREEN;
	}

	public enum LF implements DefaultName<LF> {
		Z1, Z2, Z3, Z4, Z5, Z6, Z7, Z8, Z9, Z10, Z11, Z12, Z13, Z14, Z15, A, E;

		public String[] getOverlayRename() {
			return new String[] { "overlay", "girsignals:blocks/zs3/n" + this.getName().toLowerCase() };
		}

	}

	public enum LFBACKGROUND implements DefaultName<LFBACKGROUND> {
		LF1, LF2, LF3_5, LF4, LF6, LF7;
	}

	public enum EL implements DefaultName<EL> {
		EL1V, EL1, EL2, EL3, EL4, EL5, EL6;
	}

	public enum EL_ARROW implements Offable<EL_ARROW> {
		OFF, LEFT, RIGHT, LEFT_RIGHT, UP;
	}

	public enum RA implements Offable<RA> {
		OFF, RA10, RA11A, RA11B, RA12;
	}

	public enum RA_LIGHT implements Offable<RA_LIGHT> {
		OFF, SH1
	}

	public enum BUE implements DefaultName<BUE> {
		BUE4, BUE5;
	}

	public enum BUE_LIGHT implements Offable<BUE_LIGHT> {
		OFF, BUE_1;
	}

	public enum OTHER_SIGAL implements DefaultName<OTHER_SIGAL> {
		HM, OB, CROSS;
	}
	
	public enum NE implements DefaultName<NE> {
		NE1, NE2, NE2_1, NE2_2, NE2_3, NE3_1, NE3_2, NE3_3, NE4, NE4_small, NE6;
	}
}
