package net.gir.girsignals.models;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.text.html.ParagraphView;

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
			cm.register("hv_mast_without_zs3v", has(SignalHV.ZS32V).and(has(SignalHV.DISTANTSIGNAL)).negate(), 3);
			cm.register("hv_mast_without_vr", has(SignalHV.DISTANTSIGNAL).negate(), 4);
			cm.register("hv_zs1", hasAndIs(SignalHV.ZS1).and(has(SignalHV.STOPSIGNAL)), 4.4f, "lamp1north", "girsignals:blocks/lamp_white_small");
			cm.register("hv_zs7", hasAndIs(SignalHV.ZS7).and(has(SignalHV.STOPSIGNAL)), 4.6f, "lamp1north", "girsignals:blocks/lamp_white_small");
			// HP 0
			cm.register("hv_hp", with(SignalHV.STOPSIGNAL, hpvr -> hpvr.equals(HPVR.HPVR0)), 5.4f, "lamp_red_primarynorth", "girsignals:blocks/lamp_red", "lamp_red_secondarynorth", "girsignals:blocks/lamp_red");
			// HP 1
			cm.register("hv_hp", with(SignalHV.STOPSIGNAL, hpvr -> hpvr.equals(HPVR.HPVR1)), 5.4f, "lamp_greennorth", "girsignals:blocks/lamp_green");
			// HP 2
			cm.register("hv_hp", with(SignalHV.STOPSIGNAL, hpvr -> hpvr.equals(HPVR.HPVR2)), 5.4f, "lamp_greennorth", "girsignals:blocks/lamp_green", "lamp_yellownorth", "girsignals:blocks/lamp_yellow");
			// HP off
			cm.register("hv_hp", with(SignalHV.STOPSIGNAL, hpvr -> hpvr.equals(HPVR.OFF)), 5.4f);
			// HP Status light
			cm.register("hv_hp", with(SignalHV.STOPSIGNAL, hpvr -> hpvr.equals(HPVR.OFF_STATUS_LIGHT)), 5.4f, "lamp_white_identifiernorth", "girsignals:blocks/lamp_white_small");
			// HP RS
			cm.register("hv_hp", with(SignalHV.STOPSIGNAL, hpvr -> hpvr.equals(HPVR.HPVR0_RS)), 5.4f, "lamp_red_primarynorth", "girsignals:blocks/lamp_red", "lamp_white_sh_1north, \"girsignals:blocks/lamp_white_small");
			
			for(ZS32 zs3 : ZS32.values()) {
				cm.register("hv_zs3", with(SignalHV.ZS32, pZs3 -> pZs3.equals(zs3)).and(has(SignalHV.STOPSIGNAL)), 6.9f, "7", "girsignals:blocks/zs3/" + zs3.name());
				cm.register("hv_zs3v", with(SignalHV.ZS32V, pZs3 -> pZs3.equals(zs3)).and(has(SignalHV.DISTANTSIGNAL)), 3f, "7", "girsignals:blocks/zs3/" + zs3.getDistant());
			}
			
			// VR0
			cm.register("hv_vr", with(SignalHV.DISTANTSIGNAL, hpvr -> hpvr.equals(HPVR.HPVR0)), 4, "lamp_yellow_1north", "girsignals:blocks/lamp_yellow", "lamp_yellow_2north", "girsignals:blocks/lamp_yellow");
			// VR1
			cm.register("hv_vr", with(SignalHV.DISTANTSIGNAL, hpvr -> hpvr.equals(HPVR.HPVR1)), 4, "lamp_green_1north", "girsignals:blocks/lamp_green", "lamp_green_2north", "girsignals:blocks/lamp_green");
			// VR2
			cm.register("hv_vr", with(SignalHV.DISTANTSIGNAL, hpvr -> hpvr.equals(HPVR.HPVR2)), 4, "lamp_green_1north", "girsignals:blocks/lamp_green", "lamp_yellow_2north", "girsignals:blocks/lamp_yellow");
			// VR off
			cm.register("hv_vr", with(SignalHV.DISTANTSIGNAL, hpvr -> hpvr.equals(HPVR.OFF)), 4);
			// VR Status light
			cm.register("hv_vr_statuslight", hasAndIs(SignalHV.DISTANTS_STATUS_LIGHT).and(has(SignalHV.DISTANTSIGNAL)), 4, "lamp_white_identifiernorth", "girsignals:blocks/lamp_white_small");

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
