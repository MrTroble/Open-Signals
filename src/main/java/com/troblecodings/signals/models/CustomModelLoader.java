package com.troblecodings.signals.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.troblecodings.signals.SignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.Signal.SignalAngel;
import com.troblecodings.signals.models.parser.FunctionParsingInfo;
import com.troblecodings.signals.models.parser.LogicParser;
import com.troblecodings.signals.models.parser.LogicalParserException;

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
public class CustomModelLoader implements ICustomModelLoader {

    private static HashMap<String, Consumer<SignalCustomModel>> registeredModels = new HashMap<>();

    private static final Map<String, Signal> TRANSLATION_TABLE = new HashMap<>();

    private static final List<Signal> SIGNALS = new ArrayList<>(Signal.SIGNALLIST);

    static {

        SIGNALS.forEach(signal -> {

            TRANSLATION_TABLE.put(signal.getSignalTypeName(), signal);
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onResourceManagerReload(final IResourceManager resourceManager) {

        registeredModels.clear();

        final Map<String, ModelExtention> extentions = new HashMap<>();

        final Map<String, Object> modelmap = ModelStats
                .getfromJson("/assets/signals/modeldefinitions");

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

                    SignalsMain.log.error("There doesn't exists a signalsystem named "
                            + filename.replace(".json", "") + "!");
                    return;
                }

                final FunctionParsingInfo parsinginfo = new FunctionParsingInfo(signaltype);

                registeredModels.put(signaltype.getRegistryName().toString()
                        .replace(SignalsMain.MODID + ":", ""), cm -> {

                            for (final Map.Entry<String, Models> modelsmap : content.getModels()
                                    .entrySet()) {

                                final String modelname = modelsmap.getKey();
                                final Models modelstats = modelsmap.getValue();

                                for (int i = 0; i < modelstats.getTexture().size(); i++) {

                                    final TextureStats texturestate = modelstats.getTexture()
                                            .get(i);

                                    final String blockstate = texturestate.getBlockstate();

                                    Predicate<IExtendedBlockState> state = null;

                                    boolean extentionloaded = false;

                                    if (!texturestate.isautoBlockstate()) {

                                        final Map<String, Map<String, String>> texturemap = texturestate
                                                .getExtentions();

                                        if (texturemap != null && !texturemap.isEmpty()) {

                                            cm.loadExtention(texturestate, extentions, modelname,
                                                    content, modelstats, parsinginfo);

                                            extentionloaded = true;
                                        }

                                        if (!extentionloaded) {

                                            try {
                                                state = LogicParser.predicate(blockstate,
                                                        parsinginfo);
                                            } catch (final LogicalParserException e) {
                                                SignalsMain.log.error(
                                                        "There was an problem during loading "
                                                                + modelname
                                                                + " with the blockstate '"
                                                                + texturestate.getBlockstate()
                                                                + " '!");
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                    if (texturestate.isautoBlockstate()) {

                                        cm.register(modelname, new ImplAutoBlockstatePredicate(),
                                                modelstats.getX(texturestate.getOffsetX()),
                                                modelstats.getY(texturestate.getOffsetY()),
                                                modelstats.getZ(texturestate.getOffsetZ()),
                                                content.createRetexture(
                                                        texturestate.getRetextures()));

                                    } else if (state != null && !texturestate.isautoBlockstate()
                                            && !extentionloaded) {

                                        cm.register(modelname, state,
                                                modelstats.getX(texturestate.getOffsetX()),
                                                modelstats.getY(texturestate.getOffsetY()),
                                                modelstats.getZ(texturestate.getOffsetZ()),
                                                content.createRetexture(
                                                        texturestate.getRetextures()));

                                    } else if (state == null && !texturestate.isautoBlockstate()
                                            && !extentionloaded) {
                                        SignalsMain.log.warn(
                                                "The predicate of " + modelname + " in " + filename
                                                        + " is null! This shouldn´t be the case!");
                                    }
                                }
                            }
                        });
            }
        }
    }

    @Override
    public boolean accepts(final ResourceLocation modelLocation) {
        if (!modelLocation.getResourceDomain().equals(SignalsMain.MODID))
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
