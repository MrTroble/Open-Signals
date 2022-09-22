package eu.gir.girsignals.models;

import static eu.gir.girsignals.models.parser.PredicateHolder.hasAndIs;
import static eu.gir.girsignals.models.parser.PredicateHolder.hasAndIsNot;
import static eu.gir.girsignals.models.parser.PredicateHolder.with;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;

import eu.gir.girsignals.EnumSignals.CAR;
import eu.gir.girsignals.EnumSignals.PED;
import eu.gir.girsignals.EnumSignals.RA;
import eu.gir.girsignals.EnumSignals.RALight;
import eu.gir.girsignals.EnumSignals.Tram;
import eu.gir.girsignals.EnumSignals.TramAdd;
import eu.gir.girsignals.EnumSignals.TramSwitch;
import eu.gir.girsignals.EnumSignals.TramType;
import eu.gir.girsignals.EnumSignals.WNCross;
import eu.gir.girsignals.EnumSignals.WNNormal;
import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.blocks.Signal.SignalAngel;
import eu.gir.girsignals.blocks.boards.SignalRA;
import eu.gir.girsignals.blocks.boards.SignalWN;
import eu.gir.girsignals.blocks.signals.SignalTram;
import eu.gir.girsignals.models.parser.FunctionParsingInfo;
import eu.gir.girsignals.models.parser.LogicParser;
import eu.gir.girsignals.models.parser.LogicalParserException;
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

                            for (final Map.Entry<String, Models> modelsmap : content.getModels()
                                    .entrySet()) {

                                final String modelname = modelsmap.getKey();
                                final Models modelstats = modelsmap.getValue();

                                for (int i = 0; i < modelstats.getTexture().size(); i++) {

                                    final TextureStats texturestate = modelstats.getTexture()
                                            .get(i);

                                    final String blockstate = texturestate.getBlockstate();

                                    final Map<String, String> retexture = texturestate
                                            .getRetextures();

                                    Predicate<IExtendedBlockState> state = null;

                                    if (!texturestate.isautoBlockstate()) {

                                        boolean extentionloaded = false;

                                        for (final Map.Entry<String, ModelExtention> extention : extentions
                                                .entrySet()) {

                                            if (texturestate.getExtentions() != null) {

                                                for (final Map.Entry<String, Map<String, String>> filext : texturestate
                                                        .getExtentions().entrySet()) {

                                                    final String nametoextend = filext.getKey();
                                                    final Map<String, String> ex = filext
                                                            .getValue();

                                                    if (nametoextend
                                                            .equalsIgnoreCase(extention.getKey())) {

                                                        for (final Map.Entry<String, String> extentionprops : extention
                                                                .getValue().getExtention()
                                                                .entrySet()) {

                                                            final String enums = extentionprops
                                                                    .getKey();
                                                            final String retextureval = extentionprops
                                                                    .getValue();

                                                            for (final Map.Entry<String, String> extentionvals : ex
                                                                    .entrySet()) {

                                                                final String seprop = extentionvals
                                                                        .getKey();
                                                                final String retexturekey = extentionvals
                                                                        .getValue();

                                                                final boolean load = texturestate
                                                                        .appendExtention(seprop,
                                                                                enums, retexturekey,
                                                                                retextureval,
                                                                                modelname);

                                                                final String blstate = texturestate
                                                                        .getBlockstate();

                                                                texturestate.resetStates(blockstate,
                                                                        retexture);

                                                                if (load) {

                                                                    try {
                                                                        state = LogicParser
                                                                                .predicate(blstate,
                                                                                        parsinginfo);

                                                                    } catch (final LogicalParserException e) {

                                                                        GirsignalsMain.log.error(
                                                                                "There was an problem during loading "
                                                                                        + modelname
                                                                                        + " from "
                                                                                        + filename
                                                                                        + "!");
                                                                        e.printStackTrace();
                                                                    }

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
                                                                    extentionloaded = true;

                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (!extentionloaded) {

                                            try {
                                                state = LogicParser.predicate(blockstate,
                                                        parsinginfo);
                                            } catch (final LogicalParserException e) {
                                                GirsignalsMain.log.error(
                                                        "There was an problem during loading "
                                                                + modelname + " from " + filename
                                                                + "!");
                                                e.printStackTrace();
                                            }
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
