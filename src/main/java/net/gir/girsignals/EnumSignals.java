package net.gir.girsignals;

import net.minecraft.util.IStringSerializable;

public class EnumSignals {

	public interface Offable<T extends Enum<T>> extends IStringSerializable {

		@SuppressWarnings({ "unchecked" })
		@Override
		public default String getName() {
			return ((Enum<T>) this).name();
		}

		@SuppressWarnings("unchecked")
		public default T getOffState() {
			return (T) Enum.valueOf((Class<T>) this.getClass(), "OFF");
		}

	}

	public enum HPVR implements Offable<HPVR> {
		OFF, HPVR0, HPVR1, HPVR2, OFF_STATUS_LIGHT, HPVR0_RS;
	}

	public enum ZS32 implements Offable<ZS32> {
		OFF(true), A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z, Z2, Z3, Z4, Z5, Z6, Z7,
		Z8, Z9, Z10, Z11, Z12, Z13, ZS13(true), ZS6(true), ZS8(true);

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
		OFF, HP0, KS1, KS2;
	}

	public enum HL implements Offable<HL> {
		OFF, HP0, HP0_ALTERNATE_RED, HL1, HL2, HL3A, HL3B, HL4, HL5, HL6A, HL6B, HL7, HL8, HL9A, HL9B, HL10, HL11,
		HL12A, HL12B;
	}

}
