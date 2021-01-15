package net.gir.girsignals.models;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.gir.girsignals.EnumSignals.HL;
import net.gir.girsignals.EnumSignals.HL_LIGHTBAR;
import net.gir.girsignals.EnumSignals.HPVR;
import net.gir.girsignals.EnumSignals.KS;
import net.gir.girsignals.EnumSignals.Offable;
import net.gir.girsignals.EnumSignals.ZS32;
import net.gir.girsignals.GirsignalsMain;
import net.gir.girsignals.blocks.SignalHL;
import net.gir.girsignals.blocks.SignalHV;
import net.gir.girsignals.blocks.SignalKS;
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
	
	@SuppressWarnings({ "rawtypes", "unused" })
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
			return bool != null && bool.booleanValue();
		};
	}
	
	private static Predicate<IExtendedBlockState> hasAndIsNot(IUnlistedProperty<Boolean> property) {
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
			cm.register("hv_ne2", has(SignalHV.NE2).and(has(SignalHV.STOPSIGNAL).negate()), 0); //Ne2 ist nicht zwangsläufig dran, Vorsignalwiederholer
			cm.register("hv_mast1", ebs -> true, 1);
			cm.register("hv_sign", has(SignalHV.STOPSIGNAL), 1);
			cm.register("hv_mast2", ebs -> true, 2);
			//cm.register("hv_mast_number", hasAndIs(SignalHV.), 2);
			cm.register("hv_mast3", ebs -> true, 3);
			cm.register("hv_mast4", ebs -> true, 4);
			// Zs1 on
			cm.register("hv_zs1", hasAndIs(SignalHV.ZS1).and(has(SignalHV.STOPSIGNAL)), 4.4f, "lamp1north", "girsignals:blocks/lamp_white_small");
			// Zs1 off
			cm.register("hv_zs1", hasAndIsNot(SignalHV.ZS1).and(has(SignalHV.STOPSIGNAL)), 4.4f);
			// Zs7 on
			cm.register("hv_zs7", hasAndIs(SignalHV.ZS7).and(has(SignalHV.STOPSIGNAL)), 4.6f, "lamp1north", "girsignals:blocks/lamp_yellow_small");
			// Zs7 off
			cm.register("hv_zs7", hasAndIsNot(SignalHV.ZS7).and(has(SignalHV.STOPSIGNAL)), 4.6f);
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
			cm.register("hv_hp", with(SignalHV.STOPSIGNAL, hpvr -> hpvr.equals(HPVR.HPVR0_RS)), 5.4f, "lamp_red_primarynorth", "girsignals:blocks/lamp_red", "lamp_white_sh_1north", "girsignals:blocks/lamp_white_small");
			// Zs2, Zs2v, Zs3, Zs3v
			for(ZS32 zs3 : ZS32.values()) {
				cm.register("hv_zs3", with(SignalHV.ZS3, pZs3 -> pZs3.equals(zs3)).and(has(SignalHV.STOPSIGNAL)), 6.9f, "7", "girsignals:blocks/zs3/" + zs3.name());
				cm.register("hv_zs3v", with(SignalHV.ZS3V, pZs3 -> pZs3.equals(zs3)).and(has(SignalHV.DISTANTSIGNAL)), 3f, "7", "girsignals:blocks/zs3/" + zs3.getDistant());
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
			cm.register("hv_vr_statuslight", hasAndIs(SignalHV.VR_LIGHT).and(has(SignalHV.DISTANTSIGNAL)), 4, "lamp_white_identifiernorth", "girsignals:blocks/lamp_white_small");
			// VR Status light off
			cm.register("hv_vr_statuslight", hasAndIsNot(SignalHV.VR_LIGHT).and(has(SignalHV.DISTANTSIGNAL)), 4);
		});
		registeredModels.put("kssignal", cm -> {
			cm.register("ks_base", ebs -> true, 0);
			cm.register("ks_ne2", has(SignalKS.NE2), 0); //!!!
			cm.register("ks_mast1", ebs -> true, 1);
			cm.register("ks_sign_distant", has(SignalKS.MASTSIGNDISTANT), 1); //!!!
			cm.register("ks_mast2", ebs -> true, 2);
			cm.register("ks_sign", has(SignalKS.MASTSIGN), 2); //!!!
			cm.register("ks_mast3", ebs -> true, 3);
			cm.register("ks_mast4", ebs -> true, 4);
			//cm.register("ks_number", ebs -> true, 4);
			// Zs2, Zs2v, Zs3, Zs3v
			for(ZS32 zs3 : ZS32.values()) {
				cm.register("ks_zs3", with(SignalKS.ZS3, pZs3 -> pZs3.equals(zs3)), 6, "15", "girsignals:blocks/zs3/" + zs3.name());
				cm.register("ks_zs3v", with(SignalKS.ZS3V, pZs3 -> pZs3.equals(zs3)), 4, "15", "girsignals:blocks/zs3/" + zs3.getDistant());
				cm.register("ks_zs2", with(SignalKS.ZS2, pZs3 -> pZs3.equals(zs3)), 3, "15", "girsignals:blocks/zs3/" + zs3.getDistant());
			}
			// KS off
			cm.register("ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.OFF)), 5);
			// HP 0
			cm.register("ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.HP0)), 5, "lamp_rednorth", "girsignals:blocks/lamp_red");
			// KS 1
			cm.register("ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS1)), 5, "lamp_greennorth", "girsignals:blocks/lamp_green");
			// KS 1 Light
			cm.register("ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS1_LIGHT)), 5, "lamp_greennorth", "girsignals:blocks/lamp_green", "lamp_white_identifiernorth", "girsignals:blocks/lamp_white_small");
			// KS 1 Repeat
			cm.register("ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS1_REPEAT)), 5, "lamp_greennorth", "girsignals:blocks/lamp_green", "lamp_white_sh_zsnorth", "girsignals:blocks/lamp_white_small");
			// KS 1 Blink
			cm.register("ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS1_BLINK)), 5, "lamp_greennorth", "girsignals:blocks/lamp_green_blink");
			// KS 1 Blink Light
			cm.register("ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS1_BLINK_LIGHT)), 5, "lamp_greennorth", "girsignals:blocks/lamp_green_blink", "lamp_white_identifiernorth", "girsignals:blocks/lamp_white_small");
			// KS 1 Blink Repeat
			cm.register("ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS1_BLINK_REPEAT)), 5, "lamp_greennorth", "girsignals:blocks/lamp_green_blink", "lamp_white_sh_zsnorth", "girsignals:blocks/lamp_white_small");
			// KS 2
			cm.register("ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS2)), 5, "lamp_yellownorth", "girsignals:blocks/lamp_yellow");
			// KS 2 Light
			cm.register("ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS2_LIGHT)), 5, "lamp_yellownorth", "girsignals:blocks/lamp_yellow", "lamp_white_identifiernorth", "girsignals:blocks/lamp_white_small");
			// KS 2 Repeat
			cm.register("ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS2_REPEAT)), 5, "lamp_yellownorth", "girsignals:blocks/lamp_yellow", "lamp_white_sh_zsnorth", "girsignals:blocks/lamp_white_small");
			// KS Zs1
			cm.register("ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS_ZS1)), 5, "lamp_rednorth", "girsignals:blocks/lamp_red", "lamp_white_sh_zsnorth", "girsignals:blocks/lamp_white_small_blink");
			// KS Zs7
			cm.register("ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS_ZS7)), 5, "lamp_rednorth", "girsignals:blocks/lamp_red", "lamp_zs7north", "girsignals:blocks/lamp_yellow_small");
			// KS RS
			cm.register("ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS_RS)), 5, "lamp_rednorth", "girsignals:blocks/lamp_red", "lamp_white_sh_zsnorth", "girsignals:blocks/lamp_white_small", "lamp_white_shnorth", "girsignals:blocks/lamp_white_small");
			// KS Status light
			cm.register("ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS_STATUS_LIGHT)), 5, "lamp_white_identifiernorth", "girsignals:blocks/lamp_white_small");
		});
		registeredModels.put("hlsignal", cm -> {
			cm.register("hl_base", ebs -> true, 0);
			cm.register("hl_ne2", has(SignalHL.NE2), 0);//!!!
			//cm.register("hl_number", ebs -> true, 0);
			cm.register("hl_mast1", ebs -> true, 1);
			cm.register("hl_ne2_2", has(SignalHL.NE2).and(has(SignalHL.NE2_2)), 1);
			cm.register("hl_sign_distant", has(SignalHL.MASTSIGNDISTANT), 1);
			cm.register("hl_mast2", ebs -> true, 2);
			cm.register("hl_sign_main", has(SignalHL.MASTSIGN), 2);
			cm.register("hl_mast3", ebs -> true, 3);
			for(ZS32 zs3 : ZS32.values()) {
				cm.register("hl_zs2", with(SignalHL.ZS2, pZs3 -> pZs3.equals(zs3)), 3, "signalnorth", "girsignals:blocks/zs3/" + zs3.name());
			}
			cm.register("hl_mast4", ebs -> true, 4);
			// HL Lightbar off
			cm.register("hl_shield2", with(SignalHL.LIGHTBAR, hllb -> hllb.equals(HL_LIGHTBAR.OFF)), 4);
			// HL Lightbar green
			cm.register("hl_shield2", with(SignalHL.LIGHTBAR, hllb -> hllb.equals(HL_LIGHTBAR.GREEN)), 4, "lamp_greennorth", "girsignals:blocks/lamp_green_small");
			// HL Lightbar yellow
			cm.register("hl_shield2", with(SignalHL.LIGHTBAR, hllb -> hllb.equals(HL_LIGHTBAR.YELLOW)), 4, "lamp_yellownorth", "girsignals:blocks/lamp_yellow_small");
			// HL off
			cm.register("hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.OFF)), 5);
			// HL red
			cm.register("hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HP0)), 5, "lamp_rednorth", "girsignals:blocks/lamp_red");
			// HL alternate red
			cm.register("hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HP0_ALTERNATE_RED)), 5, "lamp_red2north", "girsignals:blocks/lamp_red");
			// HL 1
			cm.register("hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL1)), 5, "lamp_greennorth", "girsignals:blocks/lamp_green");
			// HL 2/3
			cm.register("hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL2_3)), 5, "lamp_greennorth", "girsignals:blocks/lamp_green", "lamp_yellow2north", "girsignals:blocks/lamp_yellow");
			// HL 4
			cm.register("hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL4)), 5, "lamp_greennorth", "girsignals:blocks/lamp_green_blink");
			// HL 5/6
			cm.register("hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL5_6)), 5, "lamp_greennorth", "girsignals:blocks/lamp_green_blink", "lamp_yellow2north", "girsignals:blocks/lamp_yellow");
			// HL 7
			cm.register("hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL7)), 5, "lamp_yellownorth", "girsignals:blocks/lamp_yellow_blink");
			// HL 8/9
			cm.register("hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL8_9)), 5, "lamp_yellownorth", "girsignals:blocks/lamp_yellow_blink", "lamp_yellow2north", "girsignals:blocks/lamp_yellow");
			// HL 10
			cm.register("hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL10)), 5, "lamp_yellownorth", "girsignals:blocks/lamp_yellow");
			// HL 11/12
			cm.register("hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL11_12)), 5, "lamp_yellownorth", "girsignals:blocks/lamp_yellow", "lamp_yellow2north", "girsignals:blocks/lamp_yellow");
			// HL Zs1
			cm.register("hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL_ZS1)), 5, "lamp_rednorth", "girsignals:blocks/lamp_red", "lamp_white_sh_2north", "girsignals:blocks/lamp_white_small_blink");
			// HL RS
			cm.register("hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL_RS)), 5, "lamp_rednorth", "girsignals:blocks/lamp_red", "lamp_white_sh_1north", "girsignals:blocks/lamp_white_small", "lamp_white_sh_2north", "girsignals:blocks/lamp_white_small");
			// HL Status light
			cm.register("hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL_STATUS_LIGHT)), 5,"lamp_white_sh_2north", "girsignals:blocks/lamp_white_small");
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
