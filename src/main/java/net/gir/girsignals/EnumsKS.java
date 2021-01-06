package net.gir.girsignals;

import net.minecraft.util.IStringSerializable;

public class EnumsKS {
	
	public enum KS implements IStringSerializable {
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

	public enum ZS3 implements IStringSerializable {
		None, Off, Z2, Z3, Z4, Z5, Z6, Z7, Z8, Z9, Z10, Z11, Z12, Z13;

		@Override
		public String getName() {
			return this.name();
		}
	}

}
