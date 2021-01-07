package net.gir.girsignals.models;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.gir.girsignals.EnumSignals.HPVR;
import net.gir.girsignals.EnumSignals.Offable;
import net.gir.girsignals.EnumSignals.ZS32;
import net.gir.girsignals.GirsignalsMain;
import net.gir.girsignals.blocks.SignalHV;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GIRCustomModelLoader implements ICustomModelLoader {
	
	private static HashMap<String, Consumer<SignalCustomModel>> registeredModels = new HashMap<>(); 
	
	private static <T> Predicate<IExtendedBlockState> has(IUnlistedProperty<T> property) {
		return ebs -> ebs.getValue(property) != null;
	}
		
	@SuppressWarnings("rawtypes")
	private static class ModelPred<T extends Offable> implements Predicate<IExtendedBlockState> {
		
		private final IUnlistedProperty<T> property;
		private final Predicate<T> t;
		private final Predicate<T> offPred;
		
		public ModelPred(IUnlistedProperty<T> property, Predicate<T> t, boolean negate) {
			this.property = property;
			if(negate) {
				this.t = t.negate();
				this.offPred = test -> test.getOffState() == test;
			} else {
				this.t = t;
				this.offPred = test -> false;
			}
		}
		
		@Override
		public boolean test(IExtendedBlockState bs) {
			T test = bs.getValue(this.property);
			return test != null && t.or(offPred).test(test);
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	private static <T extends Offable> Predicate<IExtendedBlockState> withNot(IUnlistedProperty<T> property, Predicate<T> t) {
		return new ModelPred<T>(property, t, true);
	}
	
	@SuppressWarnings("rawtypes")
	private static <T extends Offable> Predicate<IExtendedBlockState> with(IUnlistedProperty<T> property, Predicate<T> t) {
		return new ModelPred<T>(property, t, false);
	}
	
	private static Predicate<IExtendedBlockState> hasAndIs(IUnlistedProperty<Boolean> property) {
		return ebs -> {
			Boolean bool = ebs.getValue(property);
			return bool != null && !bool.booleanValue();
		};
	}
	
	private static Predicate<IExtendedBlockState> hasAndIsNot(IUnlistedProperty<Boolean> property) {
		return ebs -> {
			Boolean bool = ebs.getValue(property);
			return bool == null || !bool.booleanValue();
		};
	}
	
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		registeredModels.clear();
		registeredModels.put("hvsignal", cm -> {
			cm.register("hv_base", ebs -> true, 0);
			cm.register("hv_ne2", has(SignalHV.STOPSIGNAL).negate().and(has(SignalHV.DISTANTSIGNAL)), 0);
			cm.register("hv_mast_without_sign", has(SignalHV.STOPSIGNAL), 1);
			cm.register("hv_mast_sign", has(SignalHV.STOPSIGNAL).negate(), 1);
			cm.register("hv_mast_number", hasAndIs(SignalHV.MAST_NUMBER), 2);
			cm.register("hv_mast_without_number", hasAndIs(SignalHV.MAST_NUMBER).negate(), 2);
			cm.register("hv_mast_without_zs3v", has(SignalHV.ZS32V).negate(), 3);
			cm.register("hv_mast_without_vr", has(SignalHV.DISTANTSIGNAL).negate(), 4);
			cm.register("hv_vr", has(SignalHV.DISTANTSIGNAL).and(has(SignalHV.VR_LIGHT).negate()), 4);
			cm.register("hv_vr_kennlicht", has(SignalHV.DISTANTSIGNAL).and(has(SignalHV.VR_LIGHT)), 4);
			cm.register("hv_zs1", has(SignalHV.ZS1), 4.4f);
			cm.register("hv_zs7", has(SignalHV.ZS7), 4.6f);
			cm.register("hv_hp", has(SignalHV.STOPSIGNAL), 5.4f);
			
			cm.register("hv_zs3", with(SignalHV.ZS32, pZs3 -> pZs3.equals(ZS32.OFF)), 6.9f, "7", "girsignals:blocks/zs32_off");
			cm.register("hv_zs3v", with(SignalHV.ZS32V, pZs3 -> pZs3.equals(ZS32.OFF)), 3f, "7", "girsignals:blocks/zs32_off");

			ZS32[] values = ZS32.values();
			for (int i = 1; i < values.length; i++) {
				ZS32 zs3 = values[i];
				cm.register("hv_zs3", with(SignalHV.ZS32, pZs3 -> pZs3.equals(zs3)), 6.9f, "7", "girsignals:blocks/zs32_" + zs3.name());
				cm.register("hv_zs3v", with(SignalHV.ZS32V, pZs3 -> pZs3.equals(zs3)), 3f, "7", "girsignals:blocks/zs32v_" + zs3.name());
			}
			
			// HP 2
			cm.register("lamp_black", withNot(SignalHV.STOPSIGNAL, hpvr -> hpvr.equals(HPVR.HPVR2)), (3.5f/32.0f), 5 - (1/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			// HP 0
			cm.register("lamp_black", withNot(SignalHV.STOPSIGNAL, hpvr -> hpvr.equals(HPVR.HPVR0)), (3.5f/32.0f), 5 + (23/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black", withNot(SignalHV.STOPSIGNAL, hpvr -> hpvr.equals(HPVR.HPVR0)), -(6.5f/32.0f), 5 + (23/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			// HP 1/2 (green)
			cm.register("lamp_black", withNot(SignalHV.STOPSIGNAL, hpvr -> hpvr.equals(HPVR.HPVR1) || hpvr.equals(HPVR.HPVR2)), (3.5f/32.0f), 6 + (1/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			
			// VR0
			cm.register("lamp_black", withNot(SignalHV.DISTANTSIGNAL, hpvr -> hpvr.equals(HPVR.HPVR0) || hpvr.equals(HPVR.HPVR2)), (10.5f/32.0f), 3 + (12.5f/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black", withNot(SignalHV.DISTANTSIGNAL, hpvr -> hpvr.equals(HPVR.HPVR0)), -(5.5f/32.0f), 3 + (30.5f/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			
			// VR1
			cm.register("lamp_black", withNot(SignalHV.DISTANTSIGNAL, hpvr -> hpvr.equals(HPVR.HPVR1)), (2.5f/32.0f), 3 + (12.5f/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black", withNot(SignalHV.DISTANTSIGNAL, hpvr -> hpvr.equals(HPVR.HPVR1) || hpvr.equals(HPVR.HPVR2)), -(13.5f/32.0f), 3 + (30.5f/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			
			// RS
			cm.register("lamp_black_small", hasAndIsNot(SignalHV.SHUNTINGSIGNAL).and(has(SignalHV.STOPSIGNAL)), -(6.5f/32.0f), 5 + (15/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black_small", hasAndIsNot(SignalHV.SHUNTINGSIGNAL).and(has(SignalHV.STOPSIGNAL)), (3.5f/32.0f), 5 + (15/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			
			// Status light
			cm.register("lamp_black_small", hasAndIsNot(SignalHV.STATUS_LIGHT).and(has(SignalHV.STOPSIGNAL)), (3.5f/32.0f), 5 + (7/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			
			// ZS 1
			cm.register("lamp_black_small", hasAndIs(SignalHV.ZS1), -(1.5f/32.0f), 4 + (21/32.0f), -((8/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black_small", hasAndIs(SignalHV.ZS1), -(4.5f/32.0f), 4 + (15.3f/32.0f), -((8/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black_small", hasAndIs(SignalHV.ZS1), (1.5f/32.0f), 4 + (15.3f/32.0f), -((8/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			
			// VR Short distance lamp
			cm.register("lamp_black_small", hasAndIs(SignalHV.VR_LIGHT), (8.5f/32.0f), 3 + (30.5f/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			
			// ZS 7
			cm.register("lamp_black_small", hasAndIs(SignalHV.ZS7), -(1.5f/32.0f), 4 + (15.5f/32.0f), -((8/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black_small", hasAndIs(SignalHV.ZS7), -(4.5f/32.0f), 4 + (21.5f/32.0f), -((8/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black_small", hasAndIs(SignalHV.ZS7), (1.5f/32.0f), 4 + (21.5f/32.0f), -((8/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
		});
		registeredModels.put("kssignal", cm -> {
			cm.register("ks_base", ebs -> true, 0);
			cm.register("ks_ne2", ebs -> true, 0);
			cm.register("ks_mast1", ebs -> true, 1);
			cm.register("ks_sign_distant", ebs -> true, 1);
			cm.register("ks_mast2", ebs -> true, 2);
			cm.register("ks_sign", ebs -> true, 2);
			cm.register("ks_mast3", ebs -> true, 3);
			cm.register("ks_zs2", ebs -> true, 3);
			cm.register("ks_mast4", ebs -> true, 4);
			cm.register("ks_zs3v", ebs -> true, 4);
			cm.register("ks_number", ebs -> true, 4);
			cm.register("ks_signal", ebs -> true, 5);
			// cm.register("ks_signal", ebs -> {
			// System.out.println(ebs.toString());
			// return ebs.getValue(SignalKS.KOMBISIGNAL) != null;
			// }, 5);
			cm.register("ks_zs3", ebs -> true, 6);
		});
		registeredModels.put("hlsignal", cm -> {
			cm.register("hl_base", ebs -> true, 0);
			cm.register("hl_ne2", ebs -> true, 0);
			cm.register("hl_number", ebs -> true, 0);
			cm.register("hl_mast1", ebs -> true, 1);
			cm.register("hl_ne2_2", ebs -> true, 1);
			cm.register("hl_sign_distant", ebs -> true, 1);
			cm.register("hl_mast2", ebs -> true, 2);
			cm.register("hl_sign_main", ebs -> true, 2);
			cm.register("hl_mast3", ebs -> true, 3);
			cm.register("hl_zs2", ebs -> true, 3);
			cm.register("hl_mast4", ebs -> true, 4);
			cm.register("hl_shield2", ebs -> true, 4);
			cm.register("hl_shield1", ebs -> true, 5);
		});
	}

	@Override
	public boolean accepts(ResourceLocation modelLocation) {
		if (!modelLocation.getResourceDomain().equals(GirsignalsMain.MODID))
			return false;
		return registeredModels.containsKey(modelLocation.getResourcePath());
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws Exception {
		ModelResourceLocation mrl = (ModelResourceLocation) modelLocation;
		return new SignalCustomModel(registeredModels.get(modelLocation.getResourcePath()),
				EnumFacing.byName(mrl.getVariant().split("=")[1]));
	}

}
