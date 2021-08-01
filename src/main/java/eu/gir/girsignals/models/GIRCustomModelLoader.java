package eu.gir.girsignals.models;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import eu.gir.girsignals.EnumSignals.BUE;
import eu.gir.girsignals.EnumSignals.BUE_ADD;
import eu.gir.girsignals.EnumSignals.CAR;
import eu.gir.girsignals.EnumSignals.DefaultName;
import eu.gir.girsignals.EnumSignals.EL;
import eu.gir.girsignals.EnumSignals.EL_ARROW;
import eu.gir.girsignals.EnumSignals.HL;
import eu.gir.girsignals.EnumSignals.HL_DISTANT;
import eu.gir.girsignals.EnumSignals.HL_LIGHTBAR;
import eu.gir.girsignals.EnumSignals.HP;
import eu.gir.girsignals.EnumSignals.KS;
import eu.gir.girsignals.EnumSignals.KS_DISTANT;
import eu.gir.girsignals.EnumSignals.LF;
import eu.gir.girsignals.EnumSignals.LFBACKGROUND;
import eu.gir.girsignals.EnumSignals.MAST_SIGN;
import eu.gir.girsignals.EnumSignals.NE;
import eu.gir.girsignals.EnumSignals.NE_ADDITION;
import eu.gir.girsignals.EnumSignals.OTHER_SIGNAL;
import eu.gir.girsignals.EnumSignals.Offable;
import eu.gir.girsignals.EnumSignals.PED;
import eu.gir.girsignals.EnumSignals.RA;
import eu.gir.girsignals.EnumSignals.RA_LIGHT;
import eu.gir.girsignals.EnumSignals.SH_LIGHT;
import eu.gir.girsignals.EnumSignals.ST_NUMBER;
import eu.gir.girsignals.EnumSignals.TRAM;
import eu.gir.girsignals.EnumSignals.TRAMTYPE;
import eu.gir.girsignals.EnumSignals.VR;
import eu.gir.girsignals.EnumSignals.WN;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.blocks.Signal.SignalAngel;
import eu.gir.girsignals.blocks.boards.SignalBUE;
import eu.gir.girsignals.blocks.boards.SignalBUELight;
import eu.gir.girsignals.blocks.boards.SignalEL;
import eu.gir.girsignals.blocks.boards.SignalLF;
import eu.gir.girsignals.blocks.boards.SignalNE;
import eu.gir.girsignals.blocks.boards.SignalOTHER;
import eu.gir.girsignals.blocks.boards.SignalRA;
import eu.gir.girsignals.blocks.boards.SignalWN;
import eu.gir.girsignals.blocks.boards.StationNumberPlate;
import eu.gir.girsignals.blocks.signals.SignalHL;
import eu.gir.girsignals.blocks.signals.SignalHV;
import eu.gir.girsignals.blocks.signals.SignalKS;
import eu.gir.girsignals.blocks.signals.SignalSHLight;
import eu.gir.girsignals.blocks.signals.SignalTram;
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
			if (negate) {
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
	private static <T extends Offable> Predicate<IExtendedBlockState> withNot(IUnlistedProperty<T> property,
			Predicate<T> t) {
		return new ModelPred<T>(property, t, true);
	}

	@SuppressWarnings("rawtypes")
	private static <T extends Offable> Predicate<IExtendedBlockState> with(IUnlistedProperty<T> property,
			Predicate<T> t) {
		return new ModelPred<T>(property, t, false);
	}

	@SuppressWarnings("rawtypes")
	private static <T extends DefaultName> Predicate<IExtendedBlockState> withN(IUnlistedProperty<T> property,
			Predicate<T> t) {
		return ebs -> {
			T val = ebs.getValue(property);
			return val != null && t.test(val);
		};
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
			cm.register("hv/hv_base", ebs -> true, 0);
			cm.register("hv/hv_ne2", has(SignalHV.NE2).and(has(SignalHV.STOPSIGNAL).negate()), 0);
			cm.register("hv/hv_mast1", ebs -> true, 1);

			for (MAST_SIGN sign : MAST_SIGN.values())
				if (!sign.equals(MAST_SIGN.OFF))
					cm.register("hv/hv_sign", with(SignalHV.MASTSIGN, ms -> ms.equals(sign)), 1, "2",
							"girsignals:blocks/mast_sign/" + sign.getName());

			cm.register("hv/hv_mast2", ebs -> true, 2);
			cm.register("hv/hv_mast3", ebs -> true, 3);
			cm.register("hv/hv_mast4", ebs -> true, 4);

			cm.register("hv/hv_number", has(Signal.CUSTOMNAME), 2);

			// Zs1 on
			cm.register("hv/hv_zs1", hasAndIs(SignalHV.ZS1).and(has(SignalHV.STOPSIGNAL)), 4.4f, "lamp1north",
					"girsignals:blocks/lamps/lamp_white_small");
			// Zs1 off
			cm.register("hv/hv_zs1", hasAndIsNot(SignalHV.ZS1).and(has(SignalHV.STOPSIGNAL)), 4.4f);
			// Zs7 on
			cm.register("hv/hv_zs7", hasAndIs(SignalHV.ZS7).and(has(SignalHV.STOPSIGNAL)), 4.6f, "lamp1north",
					"girsignals:blocks/lamps/lamp_yellow_small");
			// Zs7 off
			cm.register("hv/hv_zs7", hasAndIsNot(SignalHV.ZS7).and(has(SignalHV.STOPSIGNAL)), 4.6f);
			// HP 0
			cm.register("hv/hv_hp", with(SignalHV.STOPSIGNAL, hpvr -> hpvr.equals(HP.HP0)), 5.4f,
					"lamp_red_primarynorth", "girsignals:blocks/lamps/lamp_red", "lamp_red_secondarynorth",
					"girsignals:blocks/lamps/lamp_red");
			// HP 1
			cm.register("hv/hv_hp", with(SignalHV.STOPSIGNAL, hpvr -> hpvr.equals(HP.HP1)), 5.4f, "lamp_greennorth",
					"girsignals:blocks/lamps/lamp_green");
			// HP 2
			cm.register("hv/hv_hp", with(SignalHV.STOPSIGNAL, hpvr -> hpvr.equals(HP.HP2)), 5.4f, "lamp_greennorth",
					"girsignals:blocks/lamps/lamp_green", "lamp_yellownorth", "girsignals:blocks/lamps/lamp_yellow");
			// HP off
			cm.register("hv/hv_hp", with(SignalHV.STOPSIGNAL, hpvr -> hpvr.equals(HP.OFF)), 5.4f);
			// HP Status light
			cm.register("hv/hv_hp", with(SignalHV.STOPSIGNAL, hpvr -> hpvr.equals(HP.STATUS_LIGHT)), 5.4f,
					"lamp_white_identifiernorth", "girsignals:blocks/lamps/lamp_white_small");
			// HP RS
			cm.register("hv/hv_hp", with(SignalHV.STOPSIGNAL, hpvr -> hpvr.equals(HP.SHUNTING)), 5.4f,
					"lamp_red_primarynorth", "girsignals:blocks/lamps/lamp_red", "lamp_white_sh_1north",
					"girsignals:blocks/lamps/lamp_white_small");
			// HP Malfunction
			cm.register("hv/hv_hp", with(SignalHV.STOPSIGNAL, hpvr -> hpvr.equals(HP.MALFUNCTION)), 5.4f,
					"lamp_red_primarynorth", "girsignals:blocks/lamps/lamp_red", "lamp_greennorth",
					"girsignals:blocks/lamps/lamp_green");
			// Zs2, Zs2v, Zs3, Zs3v
			for (ZS32 zs3 : ZS32.values()) {
				cm.register("hv/hv_zs3", with(SignalHV.ZS3, pZs3 -> pZs3.equals(zs3)).and(has(SignalHV.STOPSIGNAL)),
						6.9f, "overlay", "girsignals:blocks/zs3/" + zs3.name());
				cm.register("hv/hv_zs3v", with(SignalHV.ZS3V, pZs3 -> pZs3.equals(zs3)), 3f, "overlay",
						"girsignals:blocks/zs3/" + zs3.name());
			}
			// VR0
			cm.register("hv/hv_vr", with(SignalHV.DISTANTSIGNAL, hpvr -> hpvr.equals(VR.VR0)), 4, "lamp_yellow_1north",
					"girsignals:blocks/lamps/lamp_yellow", "lamp_yellow_2north", "girsignals:blocks/lamps/lamp_yellow");
			// VR1
			cm.register("hv/hv_vr", with(SignalHV.DISTANTSIGNAL, hpvr -> hpvr.equals(VR.VR1)), 4, "lamp_green_1north",
					"girsignals:blocks/lamps/lamp_green", "lamp_green_2north", "girsignals:blocks/lamps/lamp_green");
			// VR2
			cm.register("hv/hv_vr", with(SignalHV.DISTANTSIGNAL, hpvr -> hpvr.equals(VR.VR2)), 4, "lamp_green_1north",
					"girsignals:blocks/lamps/lamp_green", "lamp_yellow_2north", "girsignals:blocks/lamps/lamp_yellow");
			// VR off
			cm.register("hv/hv_vr", with(SignalHV.DISTANTSIGNAL, hpvr -> hpvr.equals(VR.OFF)), 4);
			// VR Status light
			cm.register("hv/hv_vr_statuslight", hasAndIs(SignalHV.VR_LIGHT).and(has(SignalHV.DISTANTSIGNAL)), 4,
					"lamp_white_identifiernorth", "girsignals:blocks/lamps/lamp_white_small");
			// VR Status light off
			cm.register("hv/hv_vr_statuslight", hasAndIsNot(SignalHV.VR_LIGHT).and(has(SignalHV.DISTANTSIGNAL)), 4);
		});
		registeredModels.put("kssignal", cm -> {
			cm.register("ks/ks_base", ebs -> true, 0);
			cm.register("ks/ks_ne2", has(SignalKS.NE2).and(has(SignalKS.DISTANTSIGNAL)), 0);
			cm.register("ks/ks_mast1", ebs -> true, 1);
			cm.register("ks/ks_sign_distant", has(SignalKS.MASTSIGNDISTANT), 1);
			cm.register("ks/ks_mast2", ebs -> true, 2);

			cm.register("ks/ks_number", has(Signal.CUSTOMNAME), 4);

			for (MAST_SIGN sign : MAST_SIGN.values())
				if (!sign.equals(MAST_SIGN.OFF))
					cm.register("ks/ks_sign", with(SignalKS.MASTSIGN, ms -> ms.equals(sign)), 2, "13",
							"girsignals:blocks/mast_sign/" + sign.getName());

			cm.register("ks/ks_mast3", ebs -> true, 3);
			cm.register("ks/ks_mast4", ebs -> true, 4);
			// Zs2, Zs2v, Zs3, Zs3v
			for (ZS32 zs3 : ZS32.values()) {
				cm.register("ks/ks_zs3", with(SignalKS.ZS3, pZs3 -> pZs3.equals(zs3)), 6, "overlay",
						"girsignals:blocks/zs3/" + zs3.name());
				cm.register("ks/ks_zs3v", with(SignalKS.ZS3V, pZs3 -> pZs3.equals(zs3)), 4, "overlay",
						"girsignals:blocks/zs3/" + zs3.name());
				cm.register("ks/ks_zs2", with(SignalKS.ZS2, pZs3 -> pZs3.equals(zs3)), 3, "overlay",
						"girsignals:blocks/zs3/" + zs3.name());
			}
			// KS off
			cm.register("ks/ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.OFF)), 5);
			// HP 0
			cm.register("ks/ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.HP0)), 5, "lamp_rednorth",
					"girsignals:blocks/lamps/lamp_red");
			// KS 1
			cm.register("ks/ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS1)), 5, "lamp_greennorth",
					"girsignals:blocks/lamps/lamp_green");
			// KS 1 Blink
			cm.register("ks/ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS1_BLINK)), 5, "lamp_greennorth",
					"girsignals:blocks/lamps/lamp_green_blink");
			// KS 1 Blink Light
			cm.register("ks/ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS1_BLINK_LIGHT)), 5,
					"lamp_greennorth", "girsignals:blocks/lamps/lamp_green_blink", "lamp_white_identifiernorth",
					"girsignals:blocks/lamps/lamp_white_small");
			// KS 2
			cm.register("ks/ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS2)), 5, "lamp_yellownorth",
					"girsignals:blocks/lamps/lamp_yellow");
			// KS 2 Light
			cm.register("ks/ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS2_LIGHT)), 5, "lamp_yellownorth",
					"girsignals:blocks/lamps/lamp_yellow", "lamp_white_identifiernorth",
					"girsignals:blocks/lamps/lamp_white_small");
			// KS Zs1
			cm.register("ks/ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS_ZS1)), 5, "lamp_rednorth",
					"girsignals:blocks/lamps/lamp_red", "lamp_white_sh_zsnorth",
					"girsignals:blocks/lamps/lamp_white_small_blink");
			// KS Zs7
			cm.register("ks/ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS_ZS7)), 5, "lamp_rednorth",
					"girsignals:blocks/lamps/lamp_red", "lamp_white_zs7north",
					"girsignals:blocks/lamps/lamp_yellow_small");
			// KS RS
			cm.register("ks/ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS_SHUNTING)), 5, "lamp_rednorth",
					"girsignals:blocks/lamps/lamp_red", "lamp_white_sh_zsnorth",
					"girsignals:blocks/lamps/lamp_white_small", "lamp_white_shnorth",
					"girsignals:blocks/lamps/lamp_white_small");
			// KS Status light
			cm.register("ks/ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.KS_STATUS_LIGHT)), 5,
					"lamp_white_identifiernorth", "girsignals:blocks/lamps/lamp_white_small");
			// KS Malfunction
			cm.register("ks/ks_signal", with(SignalKS.STOPSIGNAL, ks -> ks.equals(KS.MALFUNCTION)), 5, "lamp_rednorth",
					"girsignals:blocks/lamps/lamp_red", "lamp_greennorth", "girsignals:blocks/lamps/lamp_green",
					"lamp_yellownorth", "girsignals:blocks/lamps/lamp_yellow");

			// KS off Distant
			cm.register("ks/ks_signal_dist", with(SignalKS.DISTANTSIGNAL, ks -> ks.equals(KS_DISTANT.OFF)), 5);
			// KS 1 Distant
			cm.register("ks/ks_signal_dist", with(SignalKS.DISTANTSIGNAL, ks -> ks.equals(KS_DISTANT.KS1)), 5,
					"lamp_greennorth", "girsignals:blocks/lamps/lamp_green");
			// KS 1 Repeat Distant
			cm.register("ks/ks_signal_dist", with(SignalKS.DISTANTSIGNAL, ks -> ks.equals(KS_DISTANT.KS1_REPEAT)), 5,
					"lamp_greennorth", "girsignals:blocks/lamps/lamp_green", "lamp_white_sh_zsnorth",
					"girsignals:blocks/lamps/lamp_white_small");
			// KS 1 Blink Distant
			cm.register("ks/ks_signal_dist", with(SignalKS.DISTANTSIGNAL, ks -> ks.equals(KS_DISTANT.KS1_BLINK)), 5,
					"lamp_greennorth", "girsignals:blocks/lamps/lamp_green_blink");
			// KS 1 Blink Light Distant
			cm.register("ks/ks_signal_dist", with(SignalKS.DISTANTSIGNAL, ks -> ks.equals(KS_DISTANT.KS1_BLINK_LIGHT)),
					5, "lamp_greennorth", "girsignals:blocks/lamps/lamp_green_blink", "lamp_white_identifiernorth",
					"girsignals:blocks/lamps/lamp_white_small");
			// KS 1 Blink Repeat Distant
			cm.register("ks/ks_signal_dist", with(SignalKS.DISTANTSIGNAL, ks -> ks.equals(KS_DISTANT.KS1_BLINK_REPEAT)),
					5, "lamp_greennorth", "girsignals:blocks/lamps/lamp_green_blink", "lamp_white_sh_zsnorth",
					"girsignals:blocks/lamps/lamp_white_small");
			// KS 2 Distant
			cm.register("ks/ks_signal_dist", with(SignalKS.DISTANTSIGNAL, ks -> ks.equals(KS_DISTANT.KS2)), 5,
					"lamp_yellownorth", "girsignals:blocks/lamps/lamp_yellow");
			// KS 2 Light Distant
			cm.register("ks/ks_signal_dist", with(SignalKS.DISTANTSIGNAL, ks -> ks.equals(KS_DISTANT.KS2_LIGHT)), 5,
					"lamp_yellownorth", "girsignals:blocks/lamps/lamp_yellow", "lamp_white_identifiernorth",
					"girsignals:blocks/lamps/lamp_white_small");
			// KS 2 Repeat Distant
			cm.register("ks/ks_signal_dist", with(SignalKS.DISTANTSIGNAL, ks -> ks.equals(KS_DISTANT.KS2_REPEAT)), 5,
					"lamp_yellownorth", "girsignals:blocks/lamps/lamp_yellow", "lamp_white_sh_zsnorth",
					"girsignals:blocks/lamps/lamp_white_small");
			// KS Status light Distant
			cm.register("ks/ks_signal_dist", with(SignalKS.DISTANTSIGNAL, ks -> ks.equals(KS_DISTANT.KS_STATUS_LIGHT)),
					5, "lamp_white_identifiernorth", "girsignals:blocks/lamps/lamp_white_small");
		});
		registeredModels.put("hlsignal", cm -> {
			cm.register("hl/hl_base", ebs -> true, 0);
			cm.register("hl/hl_ne2", has(SignalHL.NE2).and(has(SignalHL.DISTANTSIGNAL)), 0);
			cm.register("hl/hl_ne2_4", has(SignalHL.NE2_4).and(has(SignalHL.DISTANTSIGNAL)), 1);
			cm.register("hl/hl_mast1", ebs -> true, 1);
			cm.register("hl/hl_ne2_2", has(SignalHL.NE2).and(has(SignalHL.NE2_2)).and(has(SignalHL.DISTANTSIGNAL)), 1);
			cm.register("hl/hl_sign_distant", has(SignalHL.MASTSIGNDISTANT), 1);
			cm.register("hl/hl_mast2", ebs -> true, 2);

			cm.register("hl/hl_number", has(Signal.CUSTOMNAME), 0);

			for (MAST_SIGN sign : MAST_SIGN.values())
				if (!sign.equals(MAST_SIGN.OFF))
					cm.register("hl/hl_sign_main", with(SignalHL.MASTSIGN, ms -> ms.equals(sign)), 2, "9",
							"girsignals:blocks/mast_sign/" + sign.getName());

			cm.register("hl/hl_mast3", ebs -> true, 3);
			for (ZS32 zs3 : ZS32.values()) {
				cm.register("hl/hl_zs2", with(SignalHL.ZS2, pZs3 -> pZs3.equals(zs3)), 3, "overlay",
						"girsignals:blocks/zs3/" + zs3.name());
			}
			cm.register("hl/hl_mast4", ebs -> true, 4);
			// HL Lightbar off
			cm.register("hl/hl_shield2", with(SignalHL.LIGHTBAR, hllb -> hllb.equals(HL_LIGHTBAR.OFF)), 4);
			// HL Lightbar green
			cm.register("hl/hl_shield2", with(SignalHL.LIGHTBAR, hllb -> hllb.equals(HL_LIGHTBAR.GREEN)), 4,
					"lamp_greennorth", "girsignals:blocks/lamps/lamp_green_small");
			// HL Lightbar yellow
			cm.register("hl/hl_shield2", with(SignalHL.LIGHTBAR, hllb -> hllb.equals(HL_LIGHTBAR.YELLOW)), 4,
					"lamp_yellownorth", "girsignals:blocks/lamps/lamp_yellow_small");
			// HL off
			cm.register("hl/hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.OFF)), 5);
			// HL red
			cm.register("hl/hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HP0)), 5, "lamp_rednorth",
					"girsignals:blocks/lamps/lamp_red");
			// HL alternate red
			cm.register("hl/hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HP0_ALTERNATE_RED)), 5,
					"lamp_red2north", "girsignals:blocks/lamps/lamp_red");
			// HL 1
			cm.register("hl/hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL1)), 5, "lamp_greennorth",
					"girsignals:blocks/lamps/lamp_green");
			// HL 2/3
			cm.register("hl/hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL2_3)), 5, "lamp_greennorth",
					"girsignals:blocks/lamps/lamp_green", "lamp_yellow2north", "girsignals:blocks/lamps/lamp_yellow");
			// HL 4
			cm.register("hl/hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL4)), 5, "lamp_greennorth",
					"girsignals:blocks/lamps/lamp_green_blink");
			// HL 5/6
			cm.register("hl/hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL5_6)), 5, "lamp_greennorth",
					"girsignals:blocks/lamps/lamp_green_blink", "lamp_yellow2north",
					"girsignals:blocks/lamps/lamp_yellow");
			// HL 7
			cm.register("hl/hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL7)), 5, "lamp_yellownorth",
					"girsignals:blocks/lamps/lamp_yellow_blink");
			// HL 8/9
			cm.register("hl/hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL8_9)), 5, "lamp_yellownorth",
					"girsignals:blocks/lamps/lamp_yellow_blink", "lamp_yellow2north",
					"girsignals:blocks/lamps/lamp_yellow");
			// HL 10
			cm.register("hl/hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL10)), 5, "lamp_yellownorth",
					"girsignals:blocks/lamps/lamp_yellow");
			// HL 11/12
			cm.register("hl/hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL11_12)), 5, "lamp_yellownorth",
					"girsignals:blocks/lamps/lamp_yellow", "lamp_yellow2north", "girsignals:blocks/lamps/lamp_yellow");
			// HL Zs1
			cm.register("hl/hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL_ZS1)), 5, "lamp_rednorth",
					"girsignals:blocks/lamps/lamp_red", "lamp_white_sh_2north",
					"girsignals:blocks/lamps/lamp_white_small_blink");
			// HL RS
			cm.register("hl/hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL_SHUNTING)), 5, "lamp_rednorth",
					"girsignals:blocks/lamps/lamp_red", "lamp_white_sh_1north",
					"girsignals:blocks/lamps/lamp_white_small", "lamp_white_sh_2north",
					"girsignals:blocks/lamps/lamp_white_small");
			// HL Status light
			cm.register("hl/hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL_STATUS_LIGHT)), 5,
					"lamp_white_sh_2north", "girsignals:blocks/lamps/lamp_white_small");
			// HL Malfunction
			cm.register("hl/hl_shield1", with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.MALFUNCTION)), 5,
					"lamp_greennorth", "girsignals:blocks/lamps/lamp_green_blink", "lamp_yellow2north",
					"girsignals:blocks/lamps/lamp_yellow", "lamp_rednorth", "girsignals:blocks/lamps/lamp_red",
					"lamp_red2north", "girsignals:blocks/lamps/lamp_red");

			// HL off Distant
			cm.register("hl/hl_shield_dist", with(SignalHL.DISTANTSIGNAL, hl -> hl.equals(HL_DISTANT.OFF)), 5);
			// HL 1 Distant
			cm.register("hl/hl_shield_dist", with(SignalHL.DISTANTSIGNAL, hl -> hl.equals(HL_DISTANT.HL1)), 5,
					"lamp_greennorth", "girsignals:blocks/lamps/lamp_green");
			// HL 4 Distant
			cm.register("hl/hl_shield_dist", with(SignalHL.DISTANTSIGNAL, hl -> hl.equals(HL_DISTANT.HL4)), 5,
					"lamp_greennorth", "girsignals:blocks/lamps/lamp_green_blink");
			// HL 7 Distant
			cm.register("hl/hl_shield_dist", with(SignalHL.DISTANTSIGNAL, hl -> hl.equals(HL_DISTANT.HL7)), 5,
					"lamp_yellownorth", "girsignals:blocks/lamps/lamp_yellow_blink");
			// HL 10 Distant
			cm.register("hl/hl_shield_dist", with(SignalHL.DISTANTSIGNAL, hl -> hl.equals(HL_DISTANT.HL10)), 5,
					"lamp_yellownorth", "girsignals:blocks/lamps/lamp_yellow");
			// HL Status light Distant
			cm.register("hl/hl_shield_dist", with(SignalHL.DISTANTSIGNAL, hl -> hl.equals(HL_DISTANT.HL_STATUS_LIGHT)),
					5, "lamp_white_sh_1north", "girsignals:blocks/lamps/lamp_white_small");
		});
		registeredModels.put("shlight", cm -> {
			// SH ground off
			cm.register("sh/sh_light", with(SignalSHLight.SHLIGHT_0, sh -> sh.equals(SH_LIGHT.OFF))
					.and(hasAndIsNot(SignalSHLight.SH_HIGH)), 0);
			// SH ground sh0
			cm.register("sh/sh_light",
					with(SignalSHLight.SHLIGHT_0, sh -> sh.equals(SH_LIGHT.SH0))
							.and(hasAndIsNot(SignalSHLight.SH_HIGH)),
					0, "lamp_rednorth", "girsignals:blocks/lamps/lamp_red_small");
			// SH ground sh1
			cm.register("sh/sh_light",
					with(SignalSHLight.SHLIGHT_0, sh -> sh.equals(SH_LIGHT.SH1))
							.and(hasAndIsNot(SignalSHLight.SH_HIGH)),
					0, "lamp_whitenorth", "girsignals:blocks/lamps/lamp_white_small");
			// Mast
			cm.register("sh/sh_mast", hasAndIs(SignalSHLight.SH_HIGH), 0);
			cm.register("sh/sh_mast", hasAndIs(SignalSHLight.SH_HIGH), 1);
			// SH above off
			cm.register("sh/sh_light",
					with(SignalSHLight.SHLIGHT_0, sh -> sh.equals(SH_LIGHT.OFF)).and(hasAndIs(SignalSHLight.SH_HIGH)),
					2);
			// SH above sh0
			cm.register("sh/sh_light",
					with(SignalSHLight.SHLIGHT_0, sh -> sh.equals(SH_LIGHT.SH0)).and(hasAndIs(SignalSHLight.SH_HIGH)),
					2, "lamp_rednorth", "girsignals:blocks/lamps/lamp_red_small");
			// SH above sh1
			cm.register("sh/sh_light",
					with(SignalSHLight.SHLIGHT_0, sh -> sh.equals(SH_LIGHT.SH1)).and(hasAndIs(SignalSHLight.SH_HIGH)),
					2, "lamp_whitenorth", "girsignals:blocks/lamps/lamp_white_small");
		});
		registeredModels.put("tramsignal", cm -> {
			// TRAM off
			cm.register("trafficlight/trafficlight_tram", with(SignalTram.TRAMSIGNAL, TRAM.OFF::equals)
					.and(withN(SignalTram.TRAMSIGNAL_TYPE, TRAMTYPE.TRAM::equals)), 0);
			// TRAM f0
			cm.register("trafficlight/trafficlight_tram",
					with(SignalTram.TRAMSIGNAL, TRAM.F0::equals)
							.and(withN(SignalTram.TRAMSIGNAL_TYPE, TRAMTYPE.TRAM::equals)),
					0, "rednorth", "girsignals:blocks/tram/f_0");
			// TRAM f4
			cm.register("trafficlight/trafficlight_tram",
					with(SignalTram.TRAMSIGNAL, TRAM.F4::equals)
							.and(withN(SignalTram.TRAMSIGNAL_TYPE, TRAMTYPE.TRAM::equals)),
					0, "yellownorth", "girsignals:blocks/tram/f_4");
			// TRAM f5
			cm.register("trafficlight/trafficlight_tram",
					with(SignalTram.TRAMSIGNAL, TRAM.F5::equals)
							.and(withN(SignalTram.TRAMSIGNAL_TYPE, TRAMTYPE.TRAM::equals)),
					0, "yellownorth", "girsignals:blocks/tram/f_5");
			// TRAM f1
			cm.register("trafficlight/trafficlight_tram",
					with(SignalTram.TRAMSIGNAL, TRAM.F1::equals)
							.and(withN(SignalTram.TRAMSIGNAL_TYPE, TRAMTYPE.TRAM::equals)),
					0, "greennorth", "girsignals:blocks/tram/f_1");
			// TRAM f2
			cm.register("trafficlight/trafficlight_tram",
					with(SignalTram.TRAMSIGNAL, TRAM.F2::equals)
							.and(withN(SignalTram.TRAMSIGNAL_TYPE, TRAMTYPE.TRAM::equals)),
					0, "greennorth", "girsignals:blocks/tram/f_2");
			// TRAM f3
			cm.register("trafficlight/trafficlight_tram",
					with(SignalTram.TRAMSIGNAL, TRAM.F3::equals)
							.and(withN(SignalTram.TRAMSIGNAL_TYPE, TRAMTYPE.TRAM::equals)),
					0, "greennorth", "girsignals:blocks/tram/f_3");
			// CAR off
			cm.register("trafficlight/trafficlight_car", with(SignalTram.CARSIGNAL, CAR.OFF::equals)
					.and(withN(SignalTram.TRAMSIGNAL_TYPE, TRAMTYPE.CAR::equals)), 0);
			// Car red
			cm.register("trafficlight/trafficlight_car",
					with(SignalTram.CARSIGNAL, CAR.RED::equals)
							.and(withN(SignalTram.TRAMSIGNAL_TYPE, TRAMTYPE.CAR::equals)),
					0, "rednorth", "girsignals:blocks/lamps/lamp_red");
			// Car yellow
			cm.register("trafficlight/trafficlight_car",
					with(SignalTram.CARSIGNAL, CAR.YELLOW::equals)
							.and(withN(SignalTram.TRAMSIGNAL_TYPE, TRAMTYPE.CAR::equals)),
					0, "yellownorth", "girsignals:blocks/lamps/lamp_yellow");
			// Car green
			cm.register("trafficlight/trafficlight_car",
					with(SignalTram.CARSIGNAL, CAR.GREEN::equals)
							.and(withN(SignalTram.TRAMSIGNAL_TYPE, TRAMTYPE.CAR::equals)),
					0, "greennorth", "girsignals:blocks/lamps/lamp_green");
			// Pedestrian Signal
			cm.register("trafficlight/trafficlight_ped", with(SignalTram.PEDSIGNAL, PED.OFF::equals)
					.and(withN(SignalTram.TRAMSIGNAL_TYPE, TRAMTYPE.PEDESTRIAN::equals)), 0);
			cm.register("trafficlight/trafficlight_ped",
					with(SignalTram.PEDSIGNAL, PED.RED::equals)
							.and(withN(SignalTram.TRAMSIGNAL_TYPE, TRAMTYPE.PEDESTRIAN::equals)),
					0, "rednorth", "girsignals:blocks/lamps/lamp_red");
			cm.register("trafficlight/trafficlight_ped",
					with(SignalTram.PEDSIGNAL, PED.GREEN::equals)
							.and(withN(SignalTram.TRAMSIGNAL_TYPE, TRAMTYPE.PEDESTRIAN::equals)),
					0, "greennorth", "girsignals:blocks/lamps/lamp_green");
		});
		registeredModels.put("lfsignal", cm -> {
			cm.register("mast_lamps", withN(SignalLF.LFTYPE, lamps -> lamps.equals(LFBACKGROUND.LF1)), 0);
			cm.register("mast", ebs -> true, 0);
			for (LF lf1 : LF.values()) {
				String[] rename = lf1.getOverlayRename();
				cm.register("lf/lf1",
						withN(SignalLF.LFTYPE, LFBACKGROUND.LF1::equals).and(withN(SignalLF.INDICATOR, lf1::equals)), 1,
						rename);
				cm.register("lf/lf1_2",
						withN(SignalLF.LFTYPE, LFBACKGROUND.LF2::equals).and(withN(SignalLF.INDICATOR, lf1::equals)), 1,
						rename);
				cm.register("lf/lf3_5",
						withN(SignalLF.LFTYPE, LFBACKGROUND.LF3_5::equals).and(withN(SignalLF.INDICATOR, lf1::equals)),
						1, rename);
				cm.register("lf/lf4",
						withN(SignalLF.LFTYPE, LFBACKGROUND.LF4::equals).and(withN(SignalLF.INDICATOR, lf1::equals)), 1,
						rename);
				cm.register("lf/lf6",
						withN(SignalLF.LFTYPE, LFBACKGROUND.LF6::equals).and(withN(SignalLF.INDICATOR, lf1::equals)), 1,
						rename);
				cm.register("lf/lf7",
						withN(SignalLF.LFTYPE, LFBACKGROUND.LF7::equals).and(withN(SignalLF.INDICATOR, lf1::equals)), 1,
						rename);
			}
		});
		registeredModels.put("elsignal", cm -> {
			cm.register("mast", ebs -> true, 0);
			cm.register("el/el", withN(SignalEL.ELTYPE, EL.EL1V::equals), 1, "2", "girsignals:blocks/el/el1v");
			cm.register("el/el", withN(SignalEL.ELTYPE, EL.EL1::equals), 1, "2", "girsignals:blocks/el/el1");
			cm.register("el/el", withN(SignalEL.ELTYPE, EL.EL2::equals), 1, "2", "girsignals:blocks/el/el2");
			cm.register("el/el", withN(SignalEL.ELTYPE, EL.EL3::equals), 1, "2", "girsignals:blocks/el/el3");
			cm.register("el/el", withN(SignalEL.ELTYPE, EL.EL4::equals), 1, "2", "girsignals:blocks/el/el4");
			cm.register("el/el", withN(SignalEL.ELTYPE, EL.EL5::equals), 1, "2", "girsignals:blocks/el/el5");
			cm.register("el/el", withN(SignalEL.ELTYPE, EL.EL6::equals), 1, "2", "girsignals:blocks/el/el6");
			cm.register("el/el_arrow_lr", with(SignalEL.ELARROW, ela -> ela.equals(EL_ARROW.LEFT_RIGHT)), 2);
			cm.register("el/el_arrow_l", with(SignalEL.ELARROW, ela -> ela.equals(EL_ARROW.LEFT)), 2);
			cm.register("el/el_arrow_r", with(SignalEL.ELARROW, ela -> ela.equals(EL_ARROW.RIGHT)), 2);
			cm.register("el/el_arrow_up", with(SignalEL.ELARROW, ela -> ela.equals(EL_ARROW.UP)), 2);
		});
		registeredModels.put("shsignal", cm -> {
			cm.register("sh/sh2_mast", ebs -> true, 0);
			cm.register("sh/sh2", ebs -> true, 1);
		});
		registeredModels.put("rasignal", cm -> {
			cm.register("mast", withN(SignalRA.RATYPE, mast -> mast.equals(RA.RA12)).negate()
					.and(withN(SignalRA.RATYPE, mast -> mast.equals(RA.RA6_9)).negate()), 0);
			cm.register("mast",
					withN(SignalRA.RATYPE, mast -> mast.equals(RA.RA12)).negate()
							.and(withN(SignalRA.RATYPE, mast -> mast.equals(RA.RA10)).negate())
							.and(withN(SignalRA.RATYPE, mast -> mast.equals(RA.RA6_9)).negate()),
					1);
			cm.register("mast",
					withN(SignalRA.RATYPE, mast -> mast.equals(RA.RA12)).negate()
							.and(withN(SignalRA.RATYPE, mast -> mast.equals(RA.RA10)).negate())
							.and(withN(SignalRA.RATYPE, mast -> mast.equals(RA.RA6_9)).negate()),
					2);
			cm.register("ra/ra10", withN(SignalRA.RATYPE, ra -> ra.equals(RA.RA10)), 1);
			cm.register("ra/ra11", withN(SignalRA.RATYPE, ra -> ra.equals(RA.RA11A)), 3, "2",
					"girsignals:blocks/ra/ra11a");
			cm.register("ra/ra11", withN(SignalRA.RATYPE, ra -> ra.equals(RA.RA11B)), 3, "2",
					"girsignals:blocks/ra/ra11b");
			cm.register("ra/ra12", withN(SignalRA.RATYPE, ra -> ra.equals(RA.RA12)), 0);
			cm.register("ra/ra11_sh1",
					hasAndIsNot(SignalRA.RALIGHT).and(withN(SignalRA.RATYPE, ra -> ra.equals(RA.RA11A))), 3);
			cm.register("ra/ra11_sh1",
					hasAndIs(SignalRA.RALIGHT).and(withN(SignalRA.RATYPE, ra -> ra.equals(RA.RA11A))), 3, "3",
					"girsignals:blocks/lamps/lamp_white_small");
			cm.register("hv/hv_base", withN(SignalRA.RATYPE, ra -> ra.equals(RA.RA6_9)), 0);
			cm.register("hv/hv_mast1", withN(SignalRA.RATYPE, ra -> ra.equals(RA.RA6_9)), 1);
			cm.register("hv/hv_mast2", withN(SignalRA.RATYPE, ra -> ra.equals(RA.RA6_9)), 2);
			cm.register("hv/hv_mast3", withN(SignalRA.RATYPE, ra -> ra.equals(RA.RA6_9)), 3);
			cm.register("ra/basket", withN(SignalRA.RATYPE, ra -> ra.equals(RA.RA6_9)), 3);
			cm.register("ra/ra6_9", withN(SignalRA.RATYPE, ra -> ra.equals(RA.RA6_9))
					.and(withN(SignalRA.RALIGHTSIGNAL, light -> light.equals(RA_LIGHT.OFF))), 4);
			cm.register("ra/ra6_9",
					withN(SignalRA.RATYPE, ra -> ra.equals(RA.RA6_9))
							.and(withN(SignalRA.RALIGHTSIGNAL, light -> light.equals(RA_LIGHT.RA6))),
					4, "1", "girsignals:blocks/lamps/lamp_white_small");
			cm.register("ra/ra6_9",
					withN(SignalRA.RATYPE, ra -> ra.equals(RA.RA6_9))
							.and(withN(SignalRA.RALIGHTSIGNAL, light -> light.equals(RA_LIGHT.RA7))),
					4, "1", "girsignals:blocks/lamps/lamp_white_small");
			cm.register("ra/ra6_9",
					withN(SignalRA.RATYPE, ra -> ra.equals(RA.RA6_9))
							.and(withN(SignalRA.RALIGHTSIGNAL, light -> light.equals(RA_LIGHT.RA8))),
					4, "1", "girsignals:blocks/lamps/lamp_white_small");
			cm.register("ra/ra6_9",
					withN(SignalRA.RATYPE, ra -> ra.equals(RA.RA6_9))
							.and(withN(SignalRA.RALIGHTSIGNAL, light -> light.equals(RA_LIGHT.RA9))),
					4, "1", "girsignals:blocks/lamps/lamp_white_small");
		});
		registeredModels.put("buesignal", cm -> {
			cm.register("mast", ebs -> true, 0);
			cm.register("bue/bue2", withN(SignalBUE.BUETYPE, BUE.BUE2_1::equals), 1, "2",
					"girsignals:blocks/bue/bue2_1");
			cm.register("bue/bue2", withN(SignalBUE.BUETYPE, BUE.BUE2_2::equals), 1, "2",
					"girsignals:blocks/bue/bue2_2");
			cm.register("bue/bue2", withN(SignalBUE.BUETYPE, BUE.BUE2_3::equals), 1, "2",
					"girsignals:blocks/bue/bue2_3");
			cm.register("bue/bue2", withN(SignalBUE.BUETYPE, BUE.BUE2_4::equals), 1, "2",
					"girsignals:blocks/bue/bue2_4");
			cm.register("bue/bue3", withN(SignalBUE.BUETYPE, BUE.BUE3::equals), 1);
			cm.register("bue/bue4", withN(SignalBUE.BUETYPE, BUE.BUE4::equals), 1);
			cm.register("bue/bue4", with(SignalBUE.BUEADD, buea -> buea.equals(BUE_ADD.BUE4))
					.and(withN(SignalBUE.BUETYPE, bue -> bue.equals(BUE.BUE4))), 2);
			cm.register("bue/bue5", withN(SignalBUE.BUETYPE, BUE.BUE5::equals), 1);
			cm.register("bue/bueadd",
					with(SignalBUE.BUEADD, buea -> buea.equals(BUE_ADD.ADD))
							.and(withN(SignalBUE.BUETYPE, bue -> bue.equals(BUE.BUE4))
									.or(withN(SignalBUE.BUETYPE, bue -> bue.equals(BUE.BUE5)))),
					2);
		});
		registeredModels.put("buelight", cm -> {
			cm.register("bue/bue_base", ebs -> true, 0);
			cm.register("bue/bue_mast_1", ebs -> true, 1);
			cm.register("bue/bue_mast_2", ebs -> true, 2);
			cm.register("bue/bue_signal_head", hasAndIsNot(SignalBUELight.BUELIGHT), 3);
			cm.register("bue/bue_signal_head", hasAndIs(SignalBUELight.BUELIGHT), 3, "7",
					"girsignals:blocks/lamps/lamp_white_blink");
			cm.register("bue/bue_ne_2_2", hasAndIs(SignalBUELight.NE2_2), 1);
			cm.register("bue/bue_ne_2_4", hasAndIs(SignalBUELight.NE2_4), 1);
		});
		registeredModels.put("othersignal", cm -> {
			cm.register("mast", withN(SignalOTHER.OTHERTYPE, other -> other.equals(OTHER_SIGNAL.CROSS)).negate(), 0);
			cm.register("other_signals/hm_sign", withN(SignalOTHER.OTHERTYPE, OTHER_SIGNAL.HM::equals), 1);
			cm.register("other_signals/ob_sign", withN(SignalOTHER.OTHERTYPE, OTHER_SIGNAL.OB::equals), 1);
			cm.register("other_signals/cross_sign", withN(SignalOTHER.OTHERTYPE, OTHER_SIGNAL.CROSS::equals), 0);
		});
		registeredModels.put("nesignal", cm -> {
			cm.register("mast", withN(SignalNE.NETYPE, ne -> ne.equals(NE.NE6))
					.or(withN(SignalNE.NETYPE, ne -> ne.equals(NE.NE4_small))).negate(), 0);
			cm.register("ne/ne1", withN(SignalNE.NETYPE, NE.NE1::equals), 1, "2", "girsignals:blocks/ne/ne1");
			cm.register("ne/ne2", withN(SignalNE.NETYPE, NE.NE2::equals), 0, "2", "girsignals:blocks/ne/ne2");
			cm.register("ne/ne2", withN(SignalNE.NETYPE, NE.NE2_1::equals), 0, "2", "girsignals:blocks/ne/ne2_1");
			cm.register("ne/ne3_4", withN(SignalNE.NETYPE, NE.NE3_1::equals), 1, "2", "girsignals:blocks/ne/ne3_1");
			cm.register("ne/ne3_4", withN(SignalNE.NETYPE, NE.NE3_2::equals), 1, "2", "girsignals:blocks/ne/ne3_2");
			cm.register("ne/ne3_4", withN(SignalNE.NETYPE, NE.NE3_3::equals), 1, "2", "girsignals:blocks/ne/ne3_3");
			cm.register("ne/ne3_4", withN(SignalNE.NETYPE, NE.NE3_4::equals), 1, "2", "girsignals:blocks/ne/ne3_4");
			cm.register("ne/ne3_4", withN(SignalNE.NETYPE, NE.NE3_5::equals), 1, "2", "girsignals:blocks/ne/ne3_5");
			cm.register("ne/ne3_4", withN(SignalNE.NETYPE, NE.NE4::equals), 1, "2", "girsignals:blocks/ne/ne4");
			cm.register("ne/ne4_small", withN(SignalNE.NETYPE, NE.NE4_small::equals), 0);
			cm.register("lf/lf3_5", withN(SignalNE.NETYPE, NE.NE5::equals), 1, "overlay", "girsignals:blocks/zs3/h");
			cm.register("ne/ne6", withN(SignalNE.NETYPE, NE.NE6::equals), 1, "2", "girsignals:blocks/ne/ne6");
			cm.register("ne/ne6_mast", withN(SignalNE.NETYPE, NE.NE6::equals), 0);
			cm.register("ne/ne2_2", with(SignalNE.NEADDITION, nea -> nea.equals(NE_ADDITION.PRE1))
					.and(withN(SignalNE.NETYPE, ne -> ne.equals(NE.NE2) || ne.equals(NE.NE2_1))), 1);
			cm.register("ne/ne2_3", with(SignalNE.NEADDITION, nea -> nea.equals(NE_ADDITION.PRE2))
					.and(withN(SignalNE.NETYPE, ne -> ne.equals(NE.NE2) || ne.equals(NE.NE2_1))), 1);
		});
		registeredModels.put("stationnumberplate", cm -> {
			for (ST_NUMBER num : ST_NUMBER.values()) {
				String[] rename = num.getOverlayRename();
				cm.register("other_signals/station_number", (withN(StationNumberPlate.STATIONNUMBER, num::equals)), 0,
						rename);
			}
		});
		registeredModels.put("wnsignal", cm -> {
			cm.register("wn/wn1_2", withN(SignalWN.WNTYPE, wn -> wn.equals(WN.OFF)), 0);
			cm.register("wn/wn1_2", withN(SignalWN.WNTYPE, wn -> wn.equals(WN.WN1)), 0, "lamp_rednorth", "girsignals:blocks/lamps/lamp_white_small");
			cm.register("wn/wn1_2", withN(SignalWN.WNTYPE, wn -> wn.equals(WN.WN2)), 0, "lamp_rednorth", "girsignals:blocks/lamps/lamp_white_small");
		});
	}

	@Override
	public boolean accepts(ResourceLocation modelLocation) {
		if (!modelLocation.getResourceDomain().equals(GirsignalsMain.MODID))
			return false;
		return registeredModels.containsKey(modelLocation.getResourcePath())
				|| modelLocation.getResourcePath().equals("ghostblock");
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws Exception {
		if (modelLocation.getResourcePath().equals("ghostblock"))
			return (state, format, bak) -> new BuiltInModel(ItemCameraTransforms.DEFAULT, ItemOverrideList.NONE);
		ModelResourceLocation mrl = (ModelResourceLocation) modelLocation;
		String[] strs = mrl.getVariant().split("=");
		if (strs.length < 2)
			return new SignalCustomModel(registeredModels.get(modelLocation.getResourcePath()), SignalAngel.ANGEL0);
		return new SignalCustomModel(registeredModels.get(modelLocation.getResourcePath()),
				SignalAngel.valueOf(strs[1].toUpperCase()));
	}

}