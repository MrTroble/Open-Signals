package eu.gir.girsignals.models;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import eu.gir.girsignals.EnumSignals.DefaultName;
import eu.gir.girsignals.EnumSignals.HL;
import eu.gir.girsignals.EnumSignals.HL_LIGHTBAR;
import eu.gir.girsignals.EnumSignals.HPVR;
import eu.gir.girsignals.EnumSignals.KS;
import eu.gir.girsignals.EnumSignals.LF1;
import eu.gir.girsignals.EnumSignals.MAST_SIGN;
import eu.gir.girsignals.EnumSignals.Offable;
import eu.gir.girsignals.EnumSignals.SH_LIGHT;
import eu.gir.girsignals.EnumSignals.TRAM;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.blocks.SignalBlock;
import eu.gir.girsignals.blocks.SignalBlock.SignalAngel;
import eu.gir.girsignals.blocks.SignalHL;
import eu.gir.girsignals.blocks.SignalHV;
import eu.gir.girsignals.blocks.SignalKS;
import eu.gir.girsignals.blocks.SignalLF;
import eu.gir.girsignals.blocks.SignalSHLight;
import eu.gir.girsignals.blocks.SignalTram;
import net.minecraft.client.renderer.block.model.BuiltInModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResourceManager;
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
	
	@SuppressWarnings("rawtypes")
	private static <T extends DefaultName> Predicate<IExtendedBlockState> withN(IUnlistedProperty<T> property, Predicate<T> t) {
		return ebs -> t.test(ebs.getValue(property));
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
			cm.register("hv_ne2", has(SignalHV.NE2).and(has(SignalHV.STOPSIGNAL).negate()), 0);
			cm.register("hv_mast1", ebs -> true, 1);
			
			for (MAST_SIGN sign : MAST_SIGN.values())
				if(!sign.equals(MAST_SIGN.OFF))
					cm.register("hv_sign", with(SignalHV.MASTSIGN, ms -> ms.equals(sign)), 1, "2", "girsignals:blocks/" + sign.getName());
			
			cm.register("hv_mast2", ebs -> true, 2);
			cm.register("hv_mast3", ebs -> true, 3);
			cm.register("hv_mast4", ebs -> true, 4);
			
			cm.register("hv_number", has(SignalBlock.CUSTOMNAME), 2);

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
				cm.register("hv_zs3v", with(SignalHV.ZS3V, pZs3 -> pZs3.equals(zs3)), 3f, "7", "girsignals:blocks/zs3/" + zs3.getDistant());
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
			cm.register("ks_ne2", has(SignalKS.NE2).and(has(SignalKS.DISTANTSIGNAL)), 0);
			cm.register("ks_mast1", ebs -> true, 1);
			cm.register("ks_sign_distant", has(SignalKS.MASTSIGNDISTANT), 1);
			cm.register("ks_mast2", ebs -> true, 2);

			cm.register("ks_number", has(SignalBlock.CUSTOMNAME), 4);
			
			for (MAST_SIGN sign : MAST_SIGN.values())
				if(!sign.equals(MAST_SIGN.OFF))
					cm.register("ks_sign", with(SignalKS.MASTSIGN, ms -> ms.equals(sign)), 2, "13", "girsignals:blocks/" + sign.getName());
			
			cm.register("ks_mast3", ebs -> true, 3);
			cm.register("ks_mast4", ebs -> true, 4);
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
			// KS 1 Blink
			cm.register("ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS1_BLINK)), 5, "lamp_greennorth", "girsignals:blocks/lamp_green_blink");
			// KS 1 Blink Light
			cm.register("ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS1_BLINK_LIGHT)), 5, "lamp_greennorth", "girsignals:blocks/lamp_green_blink", "lamp_white_identifiernorth", "girsignals:blocks/lamp_white_small");
			// KS 2
			cm.register("ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS2)), 5, "lamp_yellownorth", "girsignals:blocks/lamp_yellow");
			// KS 2 Light
			cm.register("ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS2_LIGHT)), 5, "lamp_yellownorth", "girsignals:blocks/lamp_yellow", "lamp_white_identifiernorth", "girsignals:blocks/lamp_white_small");
			// KS Zs1
			cm.register("ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS_ZS1)), 5, "lamp_rednorth", "girsignals:blocks/lamp_red", "lamp_white_sh_zsnorth", "girsignals:blocks/lamp_white_small_blink");
			// KS Zs7
			cm.register("ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS_ZS7)), 5, "lamp_rednorth", "girsignals:blocks/lamp_red", "lamp_zs7north", "girsignals:blocks/lamp_yellow_small");
			// KS RS
			cm.register("ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS_RS)), 5, "lamp_rednorth", "girsignals:blocks/lamp_red", "lamp_white_sh_zsnorth", "girsignals:blocks/lamp_white_small", "lamp_white_shnorth", "girsignals:blocks/lamp_white_small");
			// KS Status light
			cm.register("ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS_STATUS_LIGHT)), 5, "lamp_white_identifiernorth", "girsignals:blocks/lamp_white_small");
			
			//KS off Distant
			cm.register("ks_signal_dist", with(SignalKS.DISTANTSIGNAL, ks -> ks.equals(KS.OFF)), 5);
			//KS 1 Distant 
			cm.register("ks_signal_dist", with(SignalKS.DISTANTSIGNAL, ks -> ks.equals(KS.KS1)), 5, "lamp_greennorth", "girsignals:blocks/lamp_green");
			// KS 1 Light Distant
			cm.register("ks_signal_dist", with(SignalKS.DISTANTSIGNAL, ks -> ks.equals(KS.KS1_LIGHT)), 5, "lamp_greennorth", "girsignals:blocks/lamp_green", "lamp_white_identifiernorth", "girsignals:blocks/lamp_white_small");
			// KS 1 Repeat Distant
			cm.register("ks_signal_dist", with(SignalKS.DISTANTSIGNAL, ks -> ks.equals(KS.KS1_REPEAT)), 5, "lamp_greennorth", "girsignals:blocks/lamp_green", "lamp_white_sh_zsnorth", "girsignals:blocks/lamp_white_small");
			// KS 1 Blink Distant
			cm.register("ks_signal_dist", with(SignalKS.DISTANTSIGNAL, ks -> ks.equals(KS.KS1_BLINK)), 5, "lamp_greennorth", "girsignals:blocks/lamp_green_blink");
			// KS 1 Blink Light Distant
			cm.register("ks_signal_dist", with(SignalKS.DISTANTSIGNAL, ks -> ks.equals(KS.KS1_BLINK_LIGHT)), 5, "lamp_greennorth", "girsignals:blocks/lamp_green_blink", "lamp_white_identifiernorth", "girsignals:blocks/lamp_white_small");
			// KS 1 Blink Repeat Distant
			cm.register("ks_signal_dist", with(SignalKS.DISTANTSIGNAL, ks -> ks.equals(KS.KS1_BLINK_REPEAT)), 5, "lamp_greennorth", "girsignals:blocks/lamp_green_blink", "lamp_white_sh_zsnorth", "girsignals:blocks/lamp_white_small");
			// KS 2 Distant
			cm.register("ks_signal_dist", with(SignalKS.DISTANTSIGNAL, ks -> ks.equals(KS.KS2)), 5, "lamp_yellownorth", "girsignals:blocks/lamp_yellow");
			// KS 2 Light Distant
			cm.register("ks_signal_dist", with(SignalKS.DISTANTSIGNAL, ks -> ks.equals(KS.KS2_LIGHT)), 5, "lamp_yellownorth", "girsignals:blocks/lamp_yellow", "lamp_white_identifiernorth", "girsignals:blocks/lamp_white_small");
			// KS 2 Repeat Distant
			cm.register("ks_signal_dist", with(SignalKS.DISTANTSIGNAL, ks -> ks.equals(KS.KS2_REPEAT)), 5, "lamp_yellownorth", "girsignals:blocks/lamp_yellow", "lamp_white_sh_zsnorth", "girsignals:blocks/lamp_white_small");
			// KS Status light Distant
			cm.register("ks_signal_dist", with(SignalKS.DISTANTSIGNAL, ks -> ks.equals(KS.KS_STATUS_LIGHT)), 5, "lamp_white_identifiernorth", "girsignals:blocks/lamp_white_small");
		});
		registeredModels.put("hlsignal", cm -> {
			cm.register("hl_base", ebs -> true, 0);
			cm.register("hl_ne2", has(SignalHL.NE2).and(has(SignalHL.DISTANTSIGNAL)), 0);
			cm.register("hl_ne2_4", has(SignalHL.NE2_4).and(has(SignalHL.DISTANTSIGNAL)), 1);
			cm.register("hl_mast1", ebs -> true, 1);
			cm.register("hl_ne2_2", has(SignalHL.NE2).and(has(SignalHL.NE2_2)).and(has(SignalHL.DISTANTSIGNAL)), 1);
			cm.register("hl_sign_distant", has(SignalHL.MASTSIGNDISTANT), 1);
			cm.register("hl_mast2", ebs -> true, 2);
			
			cm.register("hl_number", has(SignalBlock.CUSTOMNAME), 0);
			
			for (MAST_SIGN sign : MAST_SIGN.values())
				if(!sign.equals(MAST_SIGN.OFF))
					cm.register("hl_sign_main", with(SignalHL.MASTSIGN, ms -> ms.equals(sign)), 2, "9", "girsignals:blocks/" + sign.getName());
			
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
			
			// HL off Distant
			cm.register("hl_shield_dist", with(SignalHL.DISTANTSIGNAL, hl -> hl.equals(HL.OFF)), 5);
			// HL 1
			cm.register("hl_shield_dist", with(SignalHL.DISTANTSIGNAL, hl -> hl.equals(HL.HL1)), 5, "lamp_greennorth", "girsignals:blocks/lamp_green");
			// HL 4
			cm.register("hl_shield_dist", with(SignalHL.DISTANTSIGNAL, hl -> hl.equals(HL.HL4)), 5, "lamp_greennorth", "girsignals:blocks/lamp_green_blink");
			// HL 7
			cm.register("hl_shield_dist", with(SignalHL.DISTANTSIGNAL, hl -> hl.equals(HL.HL7)), 5, "lamp_yellownorth", "girsignals:blocks/lamp_yellow_blink");
			// HL 10
			cm.register("hl_shield_dist", with(SignalHL.DISTANTSIGNAL, hl -> hl.equals(HL.HL10)), 5, "lamp_yellownorth", "girsignals:blocks/lamp_yellow");
			// HL Status light
			cm.register("hl_shield_dist", with(SignalHL.DISTANTSIGNAL, hl -> hl.equals(HL.HL_STATUS_LIGHT)), 5,"lamp_white_sh_2north", "girsignals:blocks/lamp_white_small");
		});
		registeredModels.put("shlight", cm -> {
			// SH ground off
			cm.register("sh_light", with(SignalSHLight.SHLIGHT_0, sh -> sh.equals(SH_LIGHT.OFF)).and(has(SignalSHLight.SHLIGHT_2).negate()), 0);
			// SH ground sh0
			cm.register("sh_light", with(SignalSHLight.SHLIGHT_0, sh -> sh.equals(SH_LIGHT.SH0)).and(has(SignalSHLight.SHLIGHT_2).negate()), 0, "lamp_rednorth", "girsignals:blocks/lamp_red_small");
			// SH ground sh1
			cm.register("sh_light", with(SignalSHLight.SHLIGHT_0, sh -> sh.equals(SH_LIGHT.SH1)).and(has(SignalSHLight.SHLIGHT_2).negate()), 0, "lamp_whitenorth", "girsignals:blocks/lamp_white_small");
			// Mast
			cm.register("sh_mast", has(SignalSHLight.SHLIGHT_2).and(has(SignalSHLight.SHLIGHT_0).negate()), 0);
			cm.register("sh_mast", has(SignalSHLight.SHLIGHT_2).and(has(SignalSHLight.SHLIGHT_0).negate()), 1);
			// SH above off
			cm.register("sh_light", with(SignalSHLight.SHLIGHT_2, sh -> sh.equals(SH_LIGHT.OFF)).and(has(SignalSHLight.SHLIGHT_0).negate()), 2);
			// SH above sh0
			cm.register("sh_light", with(SignalSHLight.SHLIGHT_2, sh -> sh.equals(SH_LIGHT.SH0)).and(has(SignalSHLight.SHLIGHT_0).negate()), 2, "lamp_rednorth", "girsignals:blocks/lamp_red_small");
			// SH above sh1
			cm.register("sh_light", with(SignalSHLight.SHLIGHT_2, sh -> sh.equals(SH_LIGHT.SH1)).and(has(SignalSHLight.SHLIGHT_0).negate()), 2, "lamp_whitenorth", "girsignals:blocks/lamp_white_small");
		});
		registeredModels.put("tramsignal", cm -> {
			// TRAM off
			cm.register("trafficlight_tram", with(SignalTram.TRAMSIGNAL, tr -> tr.equals(TRAM.OFF)).and(has(SignalTram.CARSIGNAL).negate()), 0);
			// TRAM f0
			cm.register("trafficlight_tram", with(SignalTram.TRAMSIGNAL, tr -> tr.equals(TRAM.F0)).and(has(SignalTram.CARSIGNAL).negate()), 0, "rednorth", "girsignals:blocks/f_0");
			// TRAM f4
			cm.register("trafficlight_tram", with(SignalTram.TRAMSIGNAL, tr -> tr.equals(TRAM.F4)).and(has(SignalTram.CARSIGNAL).negate()), 0, "yellownorth", "girsignals:blocks/f_4");
			// TRAM f5
			cm.register("trafficlight_tram", with(SignalTram.TRAMSIGNAL, tr -> tr.equals(TRAM.F5)).and(has(SignalTram.CARSIGNAL).negate()), 0, "yellownorth", "girsignals:blocks/f_5");
			// TRAM f1
			cm.register("trafficlight_tram", with(SignalTram.TRAMSIGNAL, tr -> tr.equals(TRAM.F1)).and(has(SignalTram.CARSIGNAL).negate()), 0, "greennorth", "girsignals:blocks/f_1");
			// TRAM f2
			cm.register("trafficlight_tram", with(SignalTram.TRAMSIGNAL, tr -> tr.equals(TRAM.F2)).and(has(SignalTram.CARSIGNAL).negate()), 0, "greennorth", "girsignals:blocks/f_2");
			// TRAM f3
			cm.register("trafficlight_tram", with(SignalTram.TRAMSIGNAL, tr -> tr.equals(TRAM.F3)).and(has(SignalTram.CARSIGNAL).negate()), 0, "greennorth", "girsignals:blocks/f_3");
			// CAR off
			cm.register("trafficlight_car", with(SignalTram.CARSIGNAL, tr -> tr.equals(TRAM.OFF)).and(has(SignalTram.TRAMSIGNAL).negate()), 0);
			// Car red
			cm.register("trafficlight_car", with(SignalTram.CARSIGNAL, tr -> tr.equals(TRAM.RED)).and(has(SignalTram.TRAMSIGNAL).negate()), 0, "rednorth", "girsignals:blocks/lamp_red");
			// Car yellow
			cm.register("trafficlight_car", with(SignalTram.CARSIGNAL, tr -> tr.equals(TRAM.YELLOW)).and(has(SignalTram.TRAMSIGNAL).negate()), 0, "yellownorth", "girsignals:blocks/lamp_yellow");
			// Car green
			cm.register("trafficlight_car", with(SignalTram.CARSIGNAL, tr -> tr.equals(TRAM.GREEN)).and(has(SignalTram.TRAMSIGNAL).negate()), 0, "greennorth", "girsignals:blocks/lamp_green");
		});
		registeredModels.put("lfsignal", cm -> {
			cm.register("mast_lamps", hasAndIs(SignalLF.LAMPS), 0);
			cm.register("mast", hasAndIsNot(SignalLF.LAMPS), 0);
			for(LF1 lf1 : LF1.values()) {
				cm.register("lf1", hasAndIs(SignalLF.PRESIGNAL).and(withN(SignalLF.INDICATOR, lf1::equals)), 1, "4", "girsignals:blocks/zs3/n" + lf1.getName().toLowerCase());
				cm.register("lf1_2", hasAndIsNot(SignalLF.PRESIGNAL).and(withN(SignalLF.INDICATOR, lf1::equals)), 1, "overlay", "girsignals:blocks/zs3/n" + lf1.getName().toLowerCase());
			}
		});
	}

	@Override
	public boolean accepts(ResourceLocation modelLocation) {
		if (!modelLocation.getResourceDomain().equals(GirsignalsMain.MODID))
			return false;
		return registeredModels.containsKey(modelLocation.getResourcePath()) || modelLocation.getResourcePath().equals("ghostblock");
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws Exception {
		if(modelLocation.getResourcePath().equals("ghostblock"))
			return (state, format, bak) -> new BuiltInModel(ItemCameraTransforms.DEFAULT, ItemOverrideList.NONE);
		ModelResourceLocation mrl = (ModelResourceLocation) modelLocation;
		String[] strs = mrl.getVariant().split("=");
		if(strs.length < 2)
			return new SignalCustomModel(registeredModels.get(modelLocation.getResourcePath()),
					SignalAngel.ANGEL0);
		return new SignalCustomModel(registeredModels.get(modelLocation.getResourcePath()),
				SignalAngel.valueOf(strs[1].toUpperCase()));
	}

}
