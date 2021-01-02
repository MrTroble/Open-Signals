package net.gir.girsignals;

import net.minecraft.util.IStringSerializable;

public class EnumsHV {
	
	public interface Offable<T extends Enum<T>> extends IStringSerializable {
	
		@SuppressWarnings({ "unchecked" })
		@Override
		public default String getName() {
			return ((Enum<T>)this).name();
		}
		
		@SuppressWarnings("unchecked")
		public default T getOffState() {
			return (T) Enum.valueOf((Class<T>) this.getClass(), "OFF");
		}
		
	}

	public enum HPVR implements Offable<HPVR> {
		OFF, HPVR0, HPVR1, HPVR2;
	}

	public enum ZS2 implements Offable<ZS2> {
		OFF, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z;
	}

	public enum ZS3 implements Offable<ZS3> {
		OFF, Z2, Z3, Z4, Z5, Z6, Z7, Z8, Z9, Z10, Z11, Z12, Z13;
	}

}
