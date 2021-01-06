package net.gir.girsignals;

import net.minecraft.util.IStringSerializable;

public class EnumsHL {
	public enum HL implements IStringSerializable {
		None, Off;

		@Override
		public String getName() {
			return this.name();
		}	
	}
	
	public enum ZS2 implements IStringSerializable {
		None, Off, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z;

		@Override
		public String getName() {
			return this.name();
		}
	}
}
