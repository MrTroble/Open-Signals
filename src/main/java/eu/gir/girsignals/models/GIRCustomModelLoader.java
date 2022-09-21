package eu.gir.girsignals.models;

import static eu.gir.girsignals.models.parser.PredicateHolder.has;
import static eu.gir.girsignals.models.parser.PredicateHolder.hasAndIs;
import static eu.gir.girsignals.models.parser.PredicateHolder.hasAndIsNot;
import static eu.gir.girsignals.models.parser.PredicateHolder.hasNot;
import static eu.gir.girsignals.models.parser.PredicateHolder.with;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import eu.gir.girsignals.EnumSignals.Arrow;
import eu.gir.girsignals.EnumSignals.BUE;
import eu.gir.girsignals.EnumSignals.BUEAdd;
import eu.gir.girsignals.EnumSignals.CAR;
import eu.gir.girsignals.EnumSignals.EL;
import eu.gir.girsignals.EnumSignals.ELArrow;
import eu.gir.girsignals.EnumSignals.HL;
import eu.gir.girsignals.EnumSignals.HLDistant;
import eu.gir.girsignals.EnumSignals.HLExit;
import eu.gir.girsignals.EnumSignals.HLType;
import eu.gir.girsignals.EnumSignals.LF;
import eu.gir.girsignals.EnumSignals.LFBachground;
import eu.gir.girsignals.EnumSignals.MastSignal;
import eu.gir.girsignals.EnumSignals.NE;
import eu.gir.girsignals.EnumSignals.NEAddition;
import eu.gir.girsignals.EnumSignals.OtherSignal;
import eu.gir.girsignals.EnumSignals.PED;
import eu.gir.girsignals.EnumSignals.RA;
import eu.gir.girsignals.EnumSignals.RALight;
import eu.gir.girsignals.EnumSignals.STNumber;
import eu.gir.girsignals.EnumSignals.Tram;
import eu.gir.girsignals.EnumSignals.TramAdd;
import eu.gir.girsignals.EnumSignals.TramSwitch;
import eu.gir.girsignals.EnumSignals.TramType;
import eu.gir.girsignals.EnumSignals.WNCross;
import eu.gir.girsignals.EnumSignals.WNNormal;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.blocks.Signal.SignalAngel;
import eu.gir.girsignals.blocks.boards.SignalBUE;
import eu.gir.girsignals.blocks.boards.SignalBUELight;
import eu.gir.girsignals.blocks.boards.SignalEL;
import eu.gir.girsignals.blocks.boards.SignalLF;
import eu.gir.girsignals.blocks.boards.SignalNE;
import eu.gir.girsignals.blocks.boards.SignalOther;
import eu.gir.girsignals.blocks.boards.SignalRA;
import eu.gir.girsignals.blocks.boards.SignalWN;
import eu.gir.girsignals.blocks.boards.StationNumberPlate;
import eu.gir.girsignals.blocks.signals.SignalHL;
import eu.gir.girsignals.blocks.signals.SignalTram;
import eu.gir.girsignals.models.parser.FunctionParsingInfo;
import eu.gir.girsignals.models.parser.LogicParser;
import net.minecraft.client.renderer.block.model.BuiltInModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GIRCustomModelLoader implements ICustomModelLoader {

    private static HashMap<String, Consumer<SignalCustomModel>> registeredModels = new HashMap<>();

    private static final Map<String, Signal> TRANSLATION_TABLE = new HashMap<>();

    private static final List<Signal> SIGNALS = new ArrayList<>(Signal.SIGNALLIST);

    static {

        SIGNALS.forEach(signal -> {

            TRANSLATION_TABLE.put(signal.getSignalTypeName(), signal);
        });
    }

    @Override
    public void onResourceManagerReload(final IResourceManager resourceManager) {

        registeredModels.clear();

        final Map<String, ModelExtention> extentions = new HashMap<>();

        final Map<String, Object> modelmap = ModelStats
                .getfromJson("/assets/girsignals/modeldefinitions");

        modelmap.forEach((filename, content) -> {

            if (filename.endsWith(".extention.json")) {

                final ModelExtention ext = (ModelExtention) content;
                extentions.put(filename.replace(".extention", ""), ext);
            }
        });

        for (final Entry<String, Object> modelstatemap : modelmap.entrySet()) {

            final String filename = modelstatemap.getKey();

            if (!filename.endsWith(".extention.json")) {

                final ModelStats content = (ModelStats) modelstatemap.getValue();

                Signal signaltype = null;

                for (final Map.Entry<String, Signal> entry : TRANSLATION_TABLE.entrySet()) {

                    final String signalname = entry.getKey();
                    final Signal signal = entry.getValue();

                    if (filename.replace(".json", "").equalsIgnoreCase(signalname)) {

                        signaltype = signal;
                    }
                }

                if (signaltype == null) {

                    GirsignalsMain.log.error("There doesn't exists a signalsystem named "
                            + filename.replace(".json", "") + "!");
                    return;
                }

                final FunctionParsingInfo parsinginfo = new FunctionParsingInfo(signaltype);

                registeredModels.put(
                        signaltype.getRegistryName().toString().replace("girsignals:", ""), cm -> {

                            for (final Map.Entry<String, Models> entry2 : content.getModels()
                                    .entrySet()) {

                                final String modelname = entry2.getKey();
                                final Models modelstats = entry2.getValue();

                                for (int i = 0; i < modelstats.getTexture().size(); i++) {

                                    final TextureStats texturestate = modelstats.getTexture()
                                            .get(i);

                                    final String blockstate = texturestate.getBlockstate();

                                    final Map<String, String> retexture = texturestate
                                            .getRetextures();

                                    Predicate<IExtendedBlockState> state = null;

                                    if (!texturestate.isautoBlockstate()) {

                                        boolean extentionloaded = false;

                                        for (final Map.Entry<String, ModelExtention> entry : extentions
                                                .entrySet()) {

                                            if (texturestate.getExtentions() != null) {

                                                for (final Map.Entry<String, Map<String, String>> entry1 : texturestate
                                                        .getExtentions().entrySet()) {

                                                    final String nametoextend = entry1.getKey();
                                                    final Map<String, String> ex = entry1
                                                            .getValue();

                                                    if (nametoextend
                                                            .equalsIgnoreCase(entry.getKey())) {

                                                        for (final Map.Entry<String, String> entry3 : entry
                                                                .getValue().getExtention()
                                                                .entrySet()) {

                                                            final String enums = entry3.getKey();
                                                            final String retextureval = entry3
                                                                    .getValue();

                                                            for (final Map.Entry<String, String> entry4 : ex
                                                                    .entrySet()) {

                                                                final String seprop = entry4
                                                                        .getKey();
                                                                final String retexturekey = entry4
                                                                        .getValue();

                                                                final boolean load = texturestate
                                                                        .appendExtention(seprop,
                                                                                enums, retexturekey,
                                                                                retextureval,
                                                                                modelname);

                                                                if (load) {

                                                                    state = LogicParser.predicate(
                                                                            texturestate
                                                                                    .getBlockstate(),
                                                                            parsinginfo);

                                                                    cm.register(modelname, state,
                                                                            modelstats.getX(
                                                                                    texturestate
                                                                                            .getOffsetX()),
                                                                            modelstats.getY(
                                                                                    texturestate
                                                                                            .getOffsetY()),
                                                                            modelstats.getZ(
                                                                                    texturestate
                                                                                            .getOffsetZ()),
                                                                            ModelStats
                                                                                    .createRetexture(
                                                                                            texturestate
                                                                                                    .getRetextures(),
                                                                                            content.getTextures()));
                                                                }

                                                                texturestate.resetStates(blockstate,
                                                                        retexture);

                                                                extentionloaded = true;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (!extentionloaded) {

                                            state = LogicParser.predicate(blockstate, parsinginfo);
                                        }
                                    }

                                    if (texturestate.isautoBlockstate()) {

                                        cm.register(modelname, new ImplAutoBlockstatePredicate(),
                                                modelstats.getX(texturestate.getOffsetX()),
                                                modelstats.getY(texturestate.getOffsetY()),
                                                modelstats.getZ(texturestate.getOffsetZ()),
                                                ModelStats.createRetexture(
                                                        texturestate.getRetextures(),
                                                        content.getTextures()));

                                    } else if (state != null && !texturestate.isautoBlockstate()) {

                                        cm.register(modelname, state,
                                                modelstats.getX(texturestate.getOffsetX()),
                                                modelstats.getY(texturestate.getOffsetY()),
                                                modelstats.getZ(texturestate.getOffsetZ()),
                                                ModelStats.createRetexture(
                                                        texturestate.getRetextures(),
                                                        content.getTextures()));

                                    } else if (state == null && !texturestate.isautoBlockstate()) {
                                        GirsignalsMain.log.warn(
                                                "The predicate of " + modelname + " in " + filename
                                                        + " is null! This shouldn´t be the case!");
                                    }
                                }
                            }
                        });
            }
        }

        registeredModels.put("hlsignal", cm -> {

            for (final MastSignal sign : MastSignal.values())
                if (!sign.equals(MastSignal.OFF))
                    cm.register("hl/hl_sign_main", with(SignalHL.MASTSIGN, ms -> ms.equals(sign)),
                            2, "9", "girsignals:blocks/mast_sign/" + sign.getName());

            cm.register("hl/hl_mast3", ebs -> true, 3);
            for (final ZS32 zs3 : ZS32.values()) {
                cm.register("hl/hl_zs2", with(SignalHL.ZS2, pZs3 -> pZs3.equals(zs3)), 3, "overlay",
                        "girsignals:blocks/zs3/" + zs3.name());
                cm.register("hl/hl_zs2v", with(SignalHL.ZS2V, pZs3 -> pZs3.equals(zs3)), 3,
                        "overlay", "girsignals:blocks/zs3/" + zs3.name());
            }
            for (final ZS32 zs3 : ZS32.values()) {
                if (ZS32.OFF == zs3)
                    continue;
                cm.register("zs/zs3",
                        with(SignalHL.ZS3_PLATE, pZs3 -> pZs3.equals(zs3))
                                .and(has(SignalHL.ZS2).negate()),
                        3.6875f, "overlay", "girsignals:blocks/zs3/" + zs3.name());
                cm.register("zs/zs3v",
                        with(SignalHL.ZS3V_PLATE, pZs3 -> pZs3.equals(zs3))
                                .and(has(SignalHL.ZS2).negate()),
                        3.6875f, "overlay", "girsignals:blocks/zs3/" + zs3.name());
            }
            // HL off
            cm.register("hl/hl_shield1",
                    with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.OFF))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.MAIN))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5);
            // HL red
            cm.register("hl/hl_shield1",
                    with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HP0))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.MAIN))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_rednorth", "girsignals:blocks/lamps/lamp_red");
            // HL alternate red
            cm.register("hl/hl_shield1",
                    with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HP0_ALTERNATE_RED))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.MAIN))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_red2north", "girsignals:blocks/lamps/lamp_red");
            // HL 1
            cm.register("hl/hl_shield1",
                    with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL1))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.MAIN))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_greennorth", "girsignals:blocks/lamps/lamp_green");
            // HL 2/3
            cm.register("hl/hl_shield1",
                    with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL2_3))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.MAIN))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_greennorth", "girsignals:blocks/lamps/lamp_green", "lamp_yellow2north",
                    "girsignals:blocks/lamps/lamp_yellow");
            // HL 4
            cm.register("hl/hl_shield1",
                    with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL4))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.MAIN))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_greennorth", "girsignals:blocks/lamps/lamp_green_blink");
            // HL 5/6
            cm.register("hl/hl_shield1",
                    with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL5_6))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.MAIN))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_greennorth", "girsignals:blocks/lamps/lamp_green_blink",
                    "lamp_yellow2north", "girsignals:blocks/lamps/lamp_yellow");
            // HL 7
            cm.register("hl/hl_shield1",
                    with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL7))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.MAIN))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_yellownorth", "girsignals:blocks/lamps/lamp_yellow_blink");
            // HL 8/9
            cm.register("hl/hl_shield1",
                    with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL8_9))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.MAIN))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_yellownorth", "girsignals:blocks/lamps/lamp_yellow_blink",
                    "lamp_yellow2north", "girsignals:blocks/lamps/lamp_yellow");
            // HL 10
            cm.register("hl/hl_shield1",
                    with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL10))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.MAIN))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_yellownorth", "girsignals:blocks/lamps/lamp_yellow");
            // HL 11/12
            cm.register("hl/hl_shield1",
                    with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL11_12))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.MAIN))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_yellownorth", "girsignals:blocks/lamps/lamp_yellow",
                    "lamp_yellow2north", "girsignals:blocks/lamps/lamp_yellow");
            // HL Zs1
            cm.register("hl/hl_shield1",
                    with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL_ZS1))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.MAIN))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_rednorth", "girsignals:blocks/lamps/lamp_red", "lamp_white_sh_2north",
                    "girsignals:blocks/lamps/lamp_white_blink");
            // HL RS
            cm.register("hl/hl_shield1",
                    with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL_SHUNTING))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.MAIN))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_rednorth", "girsignals:blocks/lamps/lamp_red", "lamp_white_sh_1north",
                    "girsignals:blocks/lamps/lamp_white", "lamp_white_sh_2north",
                    "girsignals:blocks/lamps/lamp_white");
            // HL Status light
            cm.register("hl/hl_shield1",
                    with(SignalHL.STOPSIGNAL, hl -> hl.equals(HL.HL_STATUS_LIGHT))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.MAIN))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_white_sh_2north", "girsignals:blocks/lamps/lamp_white");

            // HL Exit Off
            cm.register("hl/hl_main",
                    with(SignalHL.EXITSIGNAL, hl -> hl.equals(HLExit.OFF))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.EXIT)))
                            .or(hasNot(SignalHL.HLTYPE)),
                    5);

            // HL Exit red
            cm.register("hl/hl_main",
                    with(SignalHL.EXITSIGNAL, hl -> hl.equals(HLExit.HP0))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.EXIT))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_rednorth", "girsignals:blocks/lamps/lamp_red");

            // HL Exit ALternate red
            cm.register("hl/hl_main",
                    with(SignalHL.EXITSIGNAL, hl -> hl.equals(HLExit.HP0_ALTERNATE_RED))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.EXIT))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_red2north", "girsignals:blocks/lamps/lamp_red");

            // HL Exit HL 1
            cm.register("hl/hl_main",
                    with(SignalHL.EXITSIGNAL, hl -> hl.equals(HLExit.HL1))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.EXIT))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_greennorth", "girsignals:blocks/lamps/lamp_green");

            // HL Exit HL 2_3
            cm.register("hl/hl_main",
                    with(SignalHL.EXITSIGNAL, hl -> hl.equals(HLExit.HL2_3))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.EXIT))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_greennorth", "girsignals:blocks/lamps/lamp_green", "lamp_yellow2north",
                    "girsignals:blocks/lamps/lamp_yellow");

            // HL Exit Zs1
            cm.register("hl/hl_main",
                    with(SignalHL.EXITSIGNAL, hl -> hl.equals(HLExit.HL_ZS1))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.EXIT))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_rednorth", "girsignals:blocks/lamps/lamp_red", "lamp_white_sh_2north",
                    "girsignals:blocks/lamps/lamp_white_blink");

            // HL Exit HL RS
            cm.register("hl/hl_sh1", hasAndIs(SignalHL.SHUNTINGLIGHT).and(has(SignalHL.EXITSIGNAL))
                    .and(with(SignalHL.EXITSIGNAL, hl -> hl.equals(HLExit.HL_SHUNTING)).negate()
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.EXIT))
                                    .or(hasNot(SignalHL.HLTYPE)))),
                    5);

            cm.register("hl/hl_main",
                    with(SignalHL.EXITSIGNAL, hl -> hl.equals(HLExit.HL_SHUNTING))
                            .and(hasAndIsNot(SignalHL.SHUNTINGLIGHT))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.EXIT))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_rednorth", "girsignals:blocks/lamps/lamp_red");

            cm.register("hl/hl_sh1",
                    hasAndIs(SignalHL.SHUNTINGLIGHT).and(has(SignalHL.EXITSIGNAL))
                            .and(with(SignalHL.EXITSIGNAL, hl -> hl.equals(HLExit.HL_SHUNTING))),
                    5, "lamp_white_sh_1north", "girsignals:blocks/lamps/lamp_white");

            cm.register("hl/hl_main",
                    with(SignalHL.EXITSIGNAL, hl -> hl.equals(HLExit.HL_SHUNTING))
                            .and(hasAndIs(SignalHL.SHUNTINGLIGHT))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.EXIT))
                                    .or(hasNot(SignalHL.HLTYPE))
                                    .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.EXIT))
                                            .or(hasNot(SignalHL.HLTYPE)))),
                    5, "lamp_rednorth", "girsignals:blocks/lamps/lamp_red", "lamp_white_sh_2north",
                    "girsignals:blocks/lamps/lamp_white");

            // HL Exit Status Light
            cm.register("hl/hl_main",
                    with(SignalHL.EXITSIGNAL, hl -> hl.equals(HLExit.HL_STATUS_LIGHT))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.EXIT))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_white_sh_2north", "girsignals:blocks/lamps/lamp_white");

            // HL off Distant
            cm.register("hl/hl_shield_dist",
                    with(SignalHL.DISTANTSIGNAL, hl -> hl.equals(HLDistant.OFF))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.DISTANT))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5);
            // HL 1 Distant
            cm.register("hl/hl_shield_dist",
                    with(SignalHL.DISTANTSIGNAL, hl -> hl.equals(HLDistant.HL1))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.DISTANT))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_greennorth", "girsignals:blocks/lamps/lamp_green");
            // HL 4 Distant
            cm.register("hl/hl_shield_dist",
                    with(SignalHL.DISTANTSIGNAL, hl -> hl.equals(HLDistant.HL4))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.DISTANT))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_greennorth", "girsignals:blocks/lamps/lamp_green_blink");
            // HL 7 Distant
            cm.register("hl/hl_shield_dist",
                    with(SignalHL.DISTANTSIGNAL, hl -> hl.equals(HLDistant.HL7))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.DISTANT))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_yellownorth", "girsignals:blocks/lamps/lamp_yellow_blink");
            // HL 10 Distant
            cm.register("hl/hl_shield_dist",
                    with(SignalHL.DISTANTSIGNAL, hl -> hl.equals(HLDistant.HL10))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.DISTANT))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_yellownorth", "girsignals:blocks/lamps/lamp_yellow");
            // HL Status light Distant
            cm.register("hl/hl_shield_dist",
                    with(SignalHL.DISTANTSIGNAL, hl -> hl.equals(HLDistant.HL_STATUS_LIGHT))
                            .and(with(SignalHL.HLTYPE, hlt -> hlt.equals(HLType.DISTANT))
                                    .or(hasNot(SignalHL.HLTYPE))),
                    5, "lamp_white_sh_2north", "girsignals:blocks/lamps/lamp_white");
        });
        registeredModels.put("tramsignal", cm -> {
            // TRAM off
            cm.register("trafficlight/trafficlight_tram",
                    with(SignalTram.TRAMSIGNAL, Tram.OFF::equals)
                            .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.TRAM::equals)),
                    0);
            // TRAM f0
            cm.register("trafficlight/trafficlight_tram",
                    with(SignalTram.TRAMSIGNAL, Tram.F0::equals)
                            .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.TRAM::equals)),
                    0, "rednorth", "girsignals:blocks/tram/f_0");
            // TRAM f4
            cm.register("trafficlight/trafficlight_tram",
                    with(SignalTram.TRAMSIGNAL, Tram.F4::equals)
                            .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.TRAM::equals)),
                    0, "yellownorth", "girsignals:blocks/tram/f_4");
            // TRAM f5
            cm.register("trafficlight/trafficlight_tram",
                    with(SignalTram.TRAMSIGNAL, Tram.F5::equals)
                            .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.TRAM::equals)),
                    0, "greennorth", "girsignals:blocks/tram/f_5");
            // TRAM f1
            cm.register("trafficlight/trafficlight_tram",
                    with(SignalTram.TRAMSIGNAL, Tram.F1::equals)
                            .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.TRAM::equals)),
                    0, "greennorth", "girsignals:blocks/tram/f_1");
            // TRAM f2
            cm.register("trafficlight/trafficlight_tram",
                    with(SignalTram.TRAMSIGNAL, Tram.F2::equals)
                            .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.TRAM::equals)),
                    0, "greennorth", "girsignals:blocks/tram/f_2");
            // TRAM f3
            cm.register("trafficlight/trafficlight_tram",
                    with(SignalTram.TRAMSIGNAL, Tram.F3::equals)
                            .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.TRAM::equals)),
                    0, "greennorth", "girsignals:blocks/tram/f_3");
            // TRAM addition
            cm.register("trafficlight/trafficlight_tram_single",
                    with(SignalTram.TRAMSIGNAL_ADD, TramAdd.A::equals)
                            .and(hasAndIsNot(SignalTram.TRAMSIGNAL_A))
                            .and(with(SignalTram.TRAMSIGNAL_TYPE,
                                    tram -> tram.equals(TramType.TRAM))),
                    1);
            cm.register("trafficlight/trafficlight_tram_single",
                    with(SignalTram.TRAMSIGNAL_ADD, TramAdd.A::equals)
                            .and(hasAndIs(SignalTram.TRAMSIGNAL_A))
                            .and(with(SignalTram.TRAMSIGNAL_TYPE,
                                    tram -> tram.equals(TramType.TRAM))),
                    1, "rednorth", "girsignals:blocks/tram/a");
            cm.register("trafficlight/trafficlight_tram_single",
                    with(SignalTram.TRAMSIGNAL_ADD, TramAdd.T::equals)
                            .and(hasAndIsNot(SignalTram.TRAMSIGNAL_T))
                            .and(with(SignalTram.TRAMSIGNAL_TYPE,
                                    tram -> tram.equals(TramType.TRAM))),
                    1);
            cm.register("trafficlight/trafficlight_tram_single",
                    with(SignalTram.TRAMSIGNAL_ADD, TramAdd.T::equals)
                            .and(hasAndIs(SignalTram.TRAMSIGNAL_T))
                            .and(with(SignalTram.TRAMSIGNAL_TYPE,
                                    tram -> tram.equals(TramType.TRAM))),
                    1, "rednorth", "girsignals:blocks/tram/t");
            cm.register("trafficlight/trafficlight_tram_single",
                    with(SignalTram.TRAMSIGNAL_ADD, TramAdd.AT::equals)
                            .and(hasAndIsNot(SignalTram.TRAMSIGNAL_T))
                            .and(with(SignalTram.TRAMSIGNAL_TYPE,
                                    tram -> tram.equals(TramType.TRAM))),
                    1);
            cm.register("trafficlight/trafficlight_tram_single",
                    with(SignalTram.TRAMSIGNAL_ADD, TramAdd.AT::equals)
                            .and(hasAndIs(SignalTram.TRAMSIGNAL_T))
                            .and(with(SignalTram.TRAMSIGNAL_TYPE,
                                    tram -> tram.equals(TramType.TRAM))),
                    1, "rednorth", "girsignals:blocks/tram/t");
            cm.register("trafficlight/trafficlight_tram_single",
                    with(SignalTram.TRAMSIGNAL_ADD, TramAdd.AT::equals)
                            .and(hasAndIsNot(SignalTram.TRAMSIGNAL_A))
                            .and(with(SignalTram.TRAMSIGNAL_TYPE,
                                    tram -> tram.equals(TramType.TRAM))),
                    1.375f);
            cm.register("trafficlight/trafficlight_tram_single",
                    with(SignalTram.TRAMSIGNAL_ADD, TramAdd.AT::equals)
                            .and(hasAndIs(SignalTram.TRAMSIGNAL_A))
                            .and(with(SignalTram.TRAMSIGNAL_TYPE,
                                    tram -> tram.equals(TramType.TRAM))),
                    1.375f, "rednorth", "girsignals:blocks/tram/a");
            // TRAM Switch
            cm.register("trafficlight/trafficlight_tram",
                    with(SignalTram.TRAM_SWITCH, TramSwitch.OFF::equals)
                            .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.SWITCH::equals)),
                    0);
            cm.register("trafficlight/trafficlight_tram",
                    with(SignalTram.TRAM_SWITCH, TramSwitch.W1::equals)
                            .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.SWITCH::equals)),
                    0, "rednorth", "girsignals:blocks/tram/w1");
            cm.register("trafficlight/trafficlight_tram",
                    with(SignalTram.TRAM_SWITCH, TramSwitch.W11::equals)
                            .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.SWITCH::equals)),
                    0, "rednorth", "girsignals:blocks/tram/w11");
            cm.register("trafficlight/trafficlight_tram",
                    with(SignalTram.TRAM_SWITCH, TramSwitch.W2::equals)
                            .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.SWITCH::equals)),
                    0, "yellownorth", "girsignals:blocks/tram/w2");
            cm.register("trafficlight/trafficlight_tram",
                    with(SignalTram.TRAM_SWITCH, TramSwitch.W12::equals)
                            .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.SWITCH::equals)),
                    0, "yellownorth", "girsignals:blocks/tram/w12");
            cm.register("trafficlight/trafficlight_tram",
                    with(SignalTram.TRAM_SWITCH, TramSwitch.W3::equals)
                            .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.SWITCH::equals)),
                    0, "greennorth", "girsignals:blocks/tram/w3");
            cm.register("trafficlight/trafficlight_tram",
                    with(SignalTram.TRAM_SWITCH, TramSwitch.W13::equals)
                            .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.SWITCH::equals)),
                    0, "greennorth", "girsignals:blocks/tram/w13");
            cm.register("trafficlight/trafficlight_tram",
                    with(SignalTram.TRAM_SWITCH, TramSwitch.W14::equals)
                            .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.SWITCH::equals)),
                    0, "greennorth", "girsignals:blocks/tram/w14");
            // CAR off
            cm.register("trafficlight/trafficlight_car", with(SignalTram.CARSIGNAL, CAR.OFF::equals)
                    .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.CAR::equals)), 0);
            // Car red
            cm.register("trafficlight/trafficlight_car",
                    with(SignalTram.CARSIGNAL, CAR.RED::equals)
                            .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.CAR::equals)),
                    0, "rednorth", "girsignals:blocks/lamps/lamp_red");
            // Car yellow
            cm.register("trafficlight/trafficlight_car",
                    with(SignalTram.CARSIGNAL, CAR.YELLOW::equals)
                            .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.CAR::equals)),
                    0, "yellownorth", "girsignals:blocks/lamps/lamp_yellow");
            // Car green
            cm.register("trafficlight/trafficlight_car",
                    with(SignalTram.CARSIGNAL, CAR.GREEN::equals)
                            .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.CAR::equals)),
                    0, "greennorth", "girsignals:blocks/lamps/lamp_green");
            // Car red-yellow
            cm.register("trafficlight/trafficlight_car",
                    with(SignalTram.CARSIGNAL, CAR.RED_YELLOW::equals)
                            .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.CAR::equals)),
                    0, "rednorth", "girsignals:blocks/lamps/lamp_red", "yellownorth",
                    "girsignals:blocks/lamps/lamp_yellow");
            // Pedestrian Signal
            cm.register("trafficlight/trafficlight_ped", with(SignalTram.PEDSIGNAL, PED.OFF::equals)
                    .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.PEDESTRIAN::equals)), 0);
            cm.register("trafficlight/trafficlight_ped",
                    with(SignalTram.PEDSIGNAL, PED.RED::equals)
                            .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.PEDESTRIAN::equals)),
                    0, "rednorth", "girsignals:blocks/lamps/lamp_red");
            cm.register("trafficlight/trafficlight_ped",
                    with(SignalTram.PEDSIGNAL, PED.GREEN::equals)
                            .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.PEDESTRIAN::equals)),
                    0, "greennorth", "girsignals:blocks/lamps/lamp_green");
            cm.register("trafficlight/trafficlight_ped",
                    with(SignalTram.PEDSIGNAL, PED.YELLOW::equals)
                            .and(with(SignalTram.TRAMSIGNAL_TYPE, TramType.PEDESTRIAN::equals)),
                    0, "rednorth", "girsignals:blocks/lamps/lamp_yellow_blink", "greennorth",
                    "girsignals:blocks/lamps/lamp_yellow_blink_i");
        });
        registeredModels.put("lfsignal", cm -> {
            cm.register("mast_lamps",
                    with(SignalLF.LFTYPE, lamps -> lamps.equals(LFBachground.LF1)), 0);
            cm.register("mast", ebs -> true, 0);
            for (final LF lf1 : LF.values()) {
                final String[] rename = lf1.getOverlayRename();
                cm.register("lf/lf1", with(SignalLF.LFTYPE, LFBachground.LF1::equals)
                        .and(with(SignalLF.INDICATOR, lf1::equals)), 1, rename);
                cm.register("lf/lf1_2", with(SignalLF.LFTYPE, LFBachground.LF2::equals)
                        .and(with(SignalLF.INDICATOR, lf1::equals)), 1, rename);
                cm.register("lf/lf3_5", with(SignalLF.LFTYPE, LFBachground.LF3_5::equals)
                        .and(with(SignalLF.INDICATOR, lf1::equals)), 1, rename);
                cm.register("lf/lf4", with(SignalLF.LFTYPE, LFBachground.LF4::equals)
                        .and(with(SignalLF.INDICATOR, lf1::equals)), 1, rename);
                cm.register("lf/lf6", with(SignalLF.LFTYPE, LFBachground.LF6::equals)
                        .and(with(SignalLF.INDICATOR, lf1::equals)), 1, rename);
                cm.register("lf/lf7", with(SignalLF.LFTYPE, LFBachground.LF7::equals)
                        .and(with(SignalLF.INDICATOR, lf1::equals)), 1, rename);
            }
        });
        registeredModels.put("elsignal", cm -> {
            cm.register("mast", ebs -> true, 0);
            cm.register("el/el", with(SignalEL.ELTYPE, EL.EL1V::equals), 1, "2",
                    "girsignals:blocks/el/el1v");
            cm.register("el/el", with(SignalEL.ELTYPE, EL.EL1::equals), 1, "2",
                    "girsignals:blocks/el/el1");
            cm.register("el/el", with(SignalEL.ELTYPE, EL.EL2::equals), 1, "2",
                    "girsignals:blocks/el/el2");
            cm.register("el/el", with(SignalEL.ELTYPE, EL.EL3::equals), 1, "2",
                    "girsignals:blocks/el/el3");
            cm.register("el/el", with(SignalEL.ELTYPE, EL.EL4::equals), 1, "2",
                    "girsignals:blocks/el/el4");
            cm.register("el/el", with(SignalEL.ELTYPE, EL.EL5::equals), 1, "2",
                    "girsignals:blocks/el/el5");
            cm.register("el/el", with(SignalEL.ELTYPE, EL.EL6::equals), 1, "2",
                    "girsignals:blocks/el/el6");
            cm.register("el/el_arrow_lr",
                    with(SignalEL.ELARROW, ela -> ela.equals(ELArrow.LEFT_RIGHT)), 2);
            cm.register("el/el_arrow_l", with(SignalEL.ELARROW, ela -> ela.equals(ELArrow.LEFT)),
                    2);
            cm.register("el/el_arrow_r", with(SignalEL.ELARROW, ela -> ela.equals(ELArrow.RIGHT)),
                    2);
            cm.register("el/el_arrow_up", with(SignalEL.ELARROW, ela -> ela.equals(ELArrow.UP)), 2);
        });
        registeredModels.put("shsignal", cm -> {
            cm.register("sh/sh2_mast", ebs -> true, 0);
            cm.register("sh/sh2", ebs -> true, 1);
        });
        registeredModels.put("rasignal", cm -> {
            cm.register("mast", with(SignalRA.RATYPE, mast -> mast.equals(RA.RA12)).negate()
                    .and(with(SignalRA.RATYPE, mast -> mast.equals(RA.RA6_9)).negate()), 0);
            cm.register("mast",
                    with(SignalRA.RATYPE, mast -> mast.equals(RA.RA12)).negate()
                            .and(with(SignalRA.RATYPE, mast -> mast.equals(RA.RA10)).negate())
                            .and(with(SignalRA.RATYPE, mast -> mast.equals(RA.RA6_9)).negate()),
                    1);
            cm.register("mast",
                    with(SignalRA.RATYPE, mast -> mast.equals(RA.RA12)).negate()
                            .and(with(SignalRA.RATYPE, mast -> mast.equals(RA.RA10)).negate())
                            .and(with(SignalRA.RATYPE, mast -> mast.equals(RA.RA6_9)).negate()),
                    2);
            cm.register("ra/ra10", with(SignalRA.RATYPE, ra -> ra.equals(RA.RA10)), 1);
            cm.register("ra/ra11", with(SignalRA.RATYPE, ra -> ra.equals(RA.RA11A)), 3, "2",
                    "girsignals:blocks/ra/ra11a");
            cm.register("ra/ra11", with(SignalRA.RATYPE, ra -> ra.equals(RA.RA11B)), 3, "2",
                    "girsignals:blocks/ra/ra11b");
            cm.register("ra/ra12", with(SignalRA.RATYPE, ra -> ra.equals(RA.RA12)), 0);
            cm.register("ra/ra11_sh1",
                    hasAndIsNot(SignalRA.RALIGHT)
                            .and(with(SignalRA.RATYPE, ra -> ra.equals(RA.RA11A))
                                    .or(with(SignalRA.RATYPE, ra -> ra.equals(RA.RA11B)))),
                    3);
            cm.register("ra/ra11_sh1",
                    hasAndIs(SignalRA.RALIGHT).and(with(SignalRA.RATYPE, ra -> ra.equals(RA.RA11A))
                            .or(with(SignalRA.RATYPE, ra -> ra.equals(RA.RA11B)))),
                    3, "3", "girsignals:blocks/lamps/lamp_white");
            cm.register("hv/hv_base", with(SignalRA.RATYPE, ra -> ra.equals(RA.RA6_9)), 0);
            cm.register("hv/hv_mast1", with(SignalRA.RATYPE, ra -> ra.equals(RA.RA6_9)), 1);
            cm.register("hv/hv_mast2", with(SignalRA.RATYPE, ra -> ra.equals(RA.RA6_9)), 2);
            cm.register("hv/hv_mast3", with(SignalRA.RATYPE, ra -> ra.equals(RA.RA6_9)), 3);
            cm.register("ra/basket", with(SignalRA.RATYPE, ra -> ra.equals(RA.RA6_9)), 3);
            cm.register("ra/ra6_9", with(SignalRA.RATYPE, ra -> ra.equals(RA.RA6_9))
                    .and(with(SignalRA.RALIGHTSIGNAL, light -> light.equals(RALight.OFF))), 4);
            cm.register("ra/ra6_9",
                    with(SignalRA.RATYPE, ra -> ra.equals(RA.RA6_9))
                            .and(with(SignalRA.RALIGHTSIGNAL, light -> light.equals(RALight.RA6))),
                    4, "lamp_2", "girsignals:blocks/lamps/lamp_white", "lamp_4",
                    "girsignals:blocks/lamps/lamp_white", "lamp_5",
                    "girsignals:blocks/lamps/lamp_white");
            cm.register("ra/ra6_9",
                    with(SignalRA.RATYPE, ra -> ra.equals(RA.RA6_9))
                            .and(with(SignalRA.RALIGHTSIGNAL, light -> light.equals(RALight.RA7))),
                    4, "lamp_2", "girsignals:blocks/lamps/lamp_white", "lamp_6",
                    "girsignals:blocks/lamps/lamp_white");
            cm.register("ra/ra6_9",
                    with(SignalRA.RATYPE, ra -> ra.equals(RA.RA6_9))
                            .and(with(SignalRA.RALIGHTSIGNAL, light -> light.equals(RALight.RA8))),
                    4, "lamp_1", "girsignals:blocks/lamps/lamp_white", "lamp_2",
                    "girsignals:blocks/lamps/lamp_white", "lamp_3",
                    "girsignals:blocks/lamps/lamp_white");
            cm.register("ra/ra6_9",
                    with(SignalRA.RATYPE, ra -> ra.equals(RA.RA6_9))
                            .and(with(SignalRA.RALIGHTSIGNAL, light -> light.equals(RALight.RA9))),
                    4, "lamp_1", "girsignals:blocks/lamps/lamp_white", "lamp_2",
                    "girsignals:blocks/lamps/lamp_white", "lamp_5",
                    "girsignals:blocks/lamps/lamp_white");
        });
        registeredModels.put("buesignal", cm -> {
            cm.register("mast", ebs -> true, 0);
            cm.register("bue/bue2", with(SignalBUE.BUETYPE, BUE.BUE2_1::equals), 1, "2",
                    "girsignals:blocks/bue/bue2_1");
            cm.register("bue/bue2", with(SignalBUE.BUETYPE, BUE.BUE2_2::equals), 1, "2",
                    "girsignals:blocks/bue/bue2_2");
            cm.register("bue/bue2", with(SignalBUE.BUETYPE, BUE.BUE2_3::equals), 1, "2",
                    "girsignals:blocks/bue/bue2_3");
            cm.register("bue/bue2", with(SignalBUE.BUETYPE, BUE.BUE2_4::equals), 1, "2",
                    "girsignals:blocks/bue/bue2_4");
            cm.register("bue/bue3", with(SignalBUE.BUETYPE, BUE.BUE3::equals), 1);
            cm.register("bue/bue4", with(SignalBUE.BUETYPE, BUE.BUE4::equals), 1);
            cm.register("bue/bue4", with(SignalBUE.BUEADD, buea -> buea.equals(BUEAdd.BUE4))
                    .and(with(SignalBUE.BUETYPE, bue -> bue.equals(BUE.BUE4))), 2);
            cm.register("bue/bue5", with(SignalBUE.BUETYPE, BUE.BUE5::equals), 1);
            cm.register("bue/bueadd",
                    with(SignalBUE.BUEADD, buea -> buea.equals(BUEAdd.ADD))
                            .and(with(SignalBUE.BUETYPE, bue -> bue.equals(BUE.BUE4))
                                    .or(with(SignalBUE.BUETYPE, bue -> bue.equals(BUE.BUE5)))),
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
            cm.register("mast",
                    with(SignalOther.OTHERTYPE, other -> other.equals(OtherSignal.CROSS)).negate(),
                    0);
            cm.register("other_signals/hm_sign",
                    with(SignalOther.OTHERTYPE, OtherSignal.HM::equals), 1);
            cm.register("other_signals/ob_sign",
                    with(SignalOther.OTHERTYPE, OtherSignal.OB::equals), 1);
            cm.register("other_signals/cross_sign",
                    with(SignalOther.OTHERTYPE, OtherSignal.CROSS::equals), 0);
        });
        registeredModels.put("nesignal", cm -> {
            cm.register("mast", with(SignalNE.NETYPE, ne -> ne.equals(NE.NE6))
                    .or(with(SignalNE.NETYPE, ne -> ne.equals(NE.NE4_small))).negate(), 0);
            cm.register("ne/ne1", with(SignalNE.NETYPE, NE.NE1::equals), 1, "2",
                    "girsignals:blocks/ne/ne1");
            cm.register("ne/ne2", with(SignalNE.NETYPE, NE.NE2::equals), 0, "2",
                    "girsignals:blocks/ne/ne2");
            cm.register("ne/ne2", with(SignalNE.NETYPE, NE.NE2_1::equals), 0, "2",
                    "girsignals:blocks/ne/ne2_1");
            cm.register("ne/ne3_4", with(SignalNE.NETYPE, NE.NE3_1::equals), 1, "2",
                    "girsignals:blocks/ne/ne3_1");
            cm.register("ne/ne3_4", with(SignalNE.NETYPE, NE.NE3_2::equals), 1, "2",
                    "girsignals:blocks/ne/ne3_2");
            cm.register("ne/ne3_4", with(SignalNE.NETYPE, NE.NE3_3::equals), 1, "2",
                    "girsignals:blocks/ne/ne3_3");
            cm.register("ne/ne3_4", with(SignalNE.NETYPE, NE.NE3_4::equals), 1, "2",
                    "girsignals:blocks/ne/ne3_4");
            cm.register("ne/ne3_4", with(SignalNE.NETYPE, NE.NE3_5::equals), 1, "2",
                    "girsignals:blocks/ne/ne3_5");
            cm.register("ne/ne3_4", with(SignalNE.NETYPE, NE.NE4::equals), 1, "2",
                    "girsignals:blocks/ne/ne4");
            cm.register("ne/ne4_small", with(SignalNE.NETYPE, NE.NE4_small::equals), 0);
            cm.register("lf/lf3_5", with(SignalNE.NETYPE, NE.NE5::equals), 1, "overlay",
                    "girsignals:blocks/zs3/h");
            cm.register("ne/ne6", with(SignalNE.NETYPE, NE.NE6::equals), 1, "2",
                    "girsignals:blocks/ne/ne6");
            cm.register("ne/ne6_mast", with(SignalNE.NETYPE, NE.NE6::equals), 0);
            cm.register("ne/ne2_2",
                    with(SignalNE.NEADDITION, nea -> nea.equals(NEAddition.PRE1)).and(
                            with(SignalNE.NETYPE, ne -> ne.equals(NE.NE2) || ne.equals(NE.NE2_1))),
                    1);
            cm.register("ne/ne2_3",
                    with(SignalNE.NEADDITION, nea -> nea.equals(NEAddition.PRE2)).and(
                            with(SignalNE.NETYPE, ne -> ne.equals(NE.NE2) || ne.equals(NE.NE2_1))),
                    1);
            @SuppressWarnings("unchecked")
            final Map.Entry<NE, Float>[] entrys = new Map.Entry[] {
                    Maps.immutableEntry(NE.NE5, 1.875f), Maps.immutableEntry(NE.NE3_1, 3.0f),
                    Maps.immutableEntry(NE.NE3_2, 3.0f), Maps.immutableEntry(NE.NE3_3, 3.0f),
                    Maps.immutableEntry(NE.NE3_4, 3.0f), Maps.immutableEntry(NE.NE3_5, 3.0f),
                    Maps.immutableEntry(NE.NE4, 3.0f), Maps.immutableEntry(NE.NE2_1, 1.125f),
                    Maps.immutableEntry(NE.NE2, 1.125f), Maps.immutableEntry(NE.NE1, 1.9375f),
                    Maps.immutableEntry(NE.NE4_small, 0.875f), Maps.immutableEntry(NE.NE6, 2.0f)
            };
            for (final Map.Entry<NE, Float> entry : entrys) {
                for (final Arrow arrow : Arrow.values()) {
                    if (arrow == Arrow.OFF)
                        continue;
                    cm.register("arrow",
                            with(SignalNE.ARROWPROP, arrow::equals)
                                    .and(with(SignalNE.NETYPE, entry.getKey()::equals)),
                            entry.getValue(), "1",
                            "girsignals:blocks/arrows/" + arrow.name().toLowerCase());
                }
            }
        });
        registeredModels.put("stationnumberplate", cm -> {
            for (final STNumber num : STNumber.values()) {
                final String[] rename = num.getOverlayRename();
                cm.register("other_signals/station_number",
                        (with(StationNumberPlate.STATIONNUMBER, num::equals)), 0, rename);
            }
        });
        registeredModels.put("wnsignal", cm -> {
            cm.register("wn/wn1_2", hasAndIsNot(SignalWN.WNTYPE)
                    .and(with(SignalWN.WNNORMAL, wn -> wn.equals(WNNormal.OFF))), 0);
            cm.register("wn/wn1_2",
                    hasAndIsNot(SignalWN.WNTYPE)
                            .and(with(SignalWN.WNNORMAL, wn -> wn.equals(WNNormal.WN1))),
                    0, "lamp_1", "girsignals:blocks/lamps/lamp_white", "lamp_3",
                    "girsignals:blocks/lamps/lamp_white");
            cm.register("wn/wn1_2",
                    hasAndIsNot(SignalWN.WNTYPE)
                            .and(with(SignalWN.WNNORMAL, wn -> wn.equals(WNNormal.WN2))),
                    0, "lamp_2", "girsignals:blocks/lamps/lamp_white", "lamp_3",
                    "girsignals:blocks/lamps/lamp_white");
            cm.register("wn/wn1_2",
                    hasAndIsNot(SignalWN.WNTYPE)
                            .and(with(SignalWN.WNNORMAL, wn -> wn.equals(WNNormal.BLINK))),
                    0, "lamp_3", "girsignals:blocks/lamps/lamp_white_blink");
            cm.register("wn/wn3_6", hasAndIs(SignalWN.WNTYPE)
                    .and(with(SignalWN.WNCROSS, wn -> wn.equals(WNCross.OFF))), 0);
            cm.register("wn/wn3_6",
                    hasAndIs(SignalWN.WNTYPE)
                            .and(with(SignalWN.WNCROSS, wn -> wn.equals(WNCross.WN3))),
                    0, "lamp_1", "girsignals:blocks/lamps/lamp_white", "lamp_3",
                    "girsignals:blocks/lamps/lamp_white", "lamp_5",
                    "girsignals:blocks/lamps/lamp_white");
            cm.register("wn/wn3_6",
                    hasAndIs(SignalWN.WNTYPE)
                            .and(with(SignalWN.WNCROSS, wn -> wn.equals(WNCross.WN4))),
                    0, "lamp_1", "girsignals:blocks/lamps/lamp_white", "lamp_2",
                    "girsignals:blocks/lamps/lamp_white", "lamp_4",
                    "girsignals:blocks/lamps/lamp_white");
            cm.register("wn/wn3_6",
                    hasAndIs(SignalWN.WNTYPE)
                            .and(with(SignalWN.WNCROSS, wn -> wn.equals(WNCross.WN5))),
                    0, "lamp_1", "girsignals:blocks/lamps/lamp_white", "lamp_3",
                    "girsignals:blocks/lamps/lamp_white", "lamp_4",
                    "girsignals:blocks/lamps/lamp_white");
            cm.register("wn/wn3_6",
                    hasAndIs(SignalWN.WNTYPE)
                            .and(with(SignalWN.WNCROSS, wn -> wn.equals(WNCross.WN6))),
                    0, "lamp_1", "girsignals:blocks/lamps/lamp_white", "lamp_2",
                    "girsignals:blocks/lamps/lamp_white", "lamp_5",
                    "girsignals:blocks/lamps/lamp_white");
            cm.register("wn/wn3_6",
                    hasAndIs(SignalWN.WNTYPE)
                            .and(with(SignalWN.WNCROSS, wn -> wn.equals(WNCross.BLINK))),
                    0, "lamp_1", "girsignals:blocks/lamps/lamp_white_blink");
        });
        registeredModels.put("stationname", cm -> {
            cm.register("other_signals/station_name", t -> true, 0);
        });
    }

    @Override
    public boolean accepts(final ResourceLocation modelLocation) {
        if (!modelLocation.getResourceDomain().equals(GirsignalsMain.MODID))
            return false;
        return registeredModels.containsKey(modelLocation.getResourcePath())
                || modelLocation.getResourcePath().equals("ghostblock");
    }

    @Override
    public IModel loadModel(final ResourceLocation modelLocation) throws Exception {
        if (modelLocation.getResourcePath().equals("ghostblock"))
            return (state, format, bak) -> new BuiltInModel(ItemCameraTransforms.DEFAULT,
                    ItemOverrideList.NONE);
        final ModelResourceLocation mrl = (ModelResourceLocation) modelLocation;
        final String[] strs = mrl.getVariant().split("=");
        if (strs.length < 2)
            return new SignalCustomModel(registeredModels.get(modelLocation.getResourcePath()),
                    SignalAngel.ANGEL0);
        return new SignalCustomModel(registeredModels.get(modelLocation.getResourcePath()),
                SignalAngel.valueOf(strs[1].toUpperCase()));
    }
}
