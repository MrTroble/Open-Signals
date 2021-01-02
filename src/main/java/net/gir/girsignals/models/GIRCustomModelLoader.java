package net.gir.girsignals.models;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.gir.girsignals.EnumsHV.HPVR;
import net.gir.girsignals.EnumsHV.Offable;
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
		
		public ModelPred(IUnlistedProperty<T> property, Predicate<T> t) {
			this.property = property;
			this.t = t;
		}
		
		@SuppressWarnings("unlikely-arg-type")
		@Override
		public boolean test(IExtendedBlockState bs) {
			T test = bs.getValue(this.property);
			return test != null && (test.getOffState().equals(test) || t.test(test));
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	private static <T extends Offable> Predicate<IExtendedBlockState> with(IUnlistedProperty<T> property, Predicate<T> t) {
		return new ModelPred<T>(property, t);
	}
	
	private static Predicate<IExtendedBlockState> hasAndIs(IUnlistedProperty<Boolean> property) {
		return ebs -> {
			Boolean bool = ebs.getValue(property);
			return bool != null && !bool.booleanValue();
		};
	}
	
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		registeredModels.clear();
		registeredModels.put("hvsignal", cm -> {
			cm.register("hv_base", ebs -> true, 0);
			cm.register("hv_mast_sign", ebs -> true, 1);
			cm.register("hv_mast_number", ebs -> true, 2);
			cm.register("hv_zs3v", has(SignalHV.ZS3V), 3);
			cm.register("hv_vr", has(SignalHV.VORSIGNAL), 4);
			cm.register("hv_zs1", has(SignalHV.ZS1), 4.4f);
			cm.register("hv_zs7", has(SignalHV.ZS7), 4.6f);
			cm.register("hv_hp", has(SignalHV.HAUPTSIGNAL), 5.4f);
			cm.register("hv_zs3", has(SignalHV.ZS3), 6.9f);
			
			// HP 2
			cm.register("lamp_black", with(SignalHV.HAUPTSIGNAL, hpvr -> !hpvr.equals(HPVR.HPVR2)), (3.5f/32.0f), 5 - (1/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			// HP 0
			Predicate<IExtendedBlockState> hp0 = with(SignalHV.HAUPTSIGNAL, hpvr -> !hpvr.equals(HPVR.HPVR0));
			cm.register("lamp_black", hp0, (3.5f/32.0f), 5 + (23/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black", hp0, -(6.5f/32.0f), 5 + (23/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			// HP 1/2 (green)
			cm.register("lamp_black", with(SignalHV.HAUPTSIGNAL, hpvr -> hpvr.equals(HPVR.HPVR0)), (3.5f/32.0f), 6 + (1/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			
			// VR0
			cm.register("lamp_black", with(SignalHV.VORSIGNAL, hpvr -> !(hpvr.equals(HPVR.HPVR1) || hpvr.equals(HPVR.HPVR2))), (10.5f/32.0f), 3 + (12.5f/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black", with(SignalHV.VORSIGNAL, hpvr -> !hpvr.equals(HPVR.HPVR0)), -(5.5f/32.0f), 3 + (30.5f/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			
			// VR1
			cm.register("lamp_black", with(SignalHV.VORSIGNAL, hpvr -> !hpvr.equals(HPVR.HPVR1)), (2.5f/32.0f), 3 + (12.5f/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black", with(SignalHV.VORSIGNAL, hpvr -> !(hpvr.equals(HPVR.HPVR1) || hpvr.equals(HPVR.HPVR2))), -(13.5f/32.0f), 3 + (30.5f/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			
			// RS
			Predicate<IExtendedBlockState> rsPred = hasAndIs(SignalHV.RANGIERSIGNAL);
			cm.register("lamp_black_small", rsPred, -(6.5f/32.0f), 5 + (15/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black_small", rsPred, (3.5f/32.0f), 5 + (15/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			
			// Status light TODO implement
			cm.register("lamp_black_small", ebs -> false, (3.5f/32.0f), 5 + (7/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			
			// ZS 1
			Predicate<IExtendedBlockState> zs1Pred = hasAndIs(SignalHV.ZS1);
			cm.register("lamp_black_small", zs1Pred, -(1.5f/32.0f), 4 + (21/32.0f), -((8/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black_small", zs1Pred, -(4.5f/32.0f), 4 + (15.3f/32.0f), -((8/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black_small", zs1Pred, (1.5f/32.0f), 4 + (15.3f/32.0f), -((8/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			
			// VR Short distance lamp TODO implement
			cm.register("lamp_black_small", ebs -> false, (8.5f/32.0f), 3 + (30.5f/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			
			// ZS 7
			Predicate<IExtendedBlockState> zs7Pred = hasAndIs(SignalHV.ZS7);
			cm.register("lamp_black_small", zs7Pred, -(1.5f/32.0f), 4 + (15.5f/32.0f), -((8/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black_small", zs7Pred, -(4.5f/32.0f), 4 + (21.5f/32.0f), -((8/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black_small", zs7Pred, (1.5f/32.0f), 4 + (21.5f/32.0f), -((8/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
		});
	}

	@Override
	public boolean accepts(ResourceLocation modelLocation) {
		if(!modelLocation.getResourceDomain().equals(GirsignalsMain.MODID)) return false;
		return registeredModels.containsKey(modelLocation.getResourcePath());
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws Exception {
		ModelResourceLocation mrl = (ModelResourceLocation) modelLocation;
		return new SignalCustomModel(registeredModels.get(modelLocation.getResourcePath()), EnumFacing.byName(mrl.getVariant().split("=")[1]));
	}

}
