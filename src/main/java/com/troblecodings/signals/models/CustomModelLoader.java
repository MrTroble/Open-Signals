package com.troblecodings.signals.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.Signal.SignalAngel;
import com.troblecodings.signals.parser.FunctionParsingInfo;
import com.troblecodings.signals.parser.LogicParser;
import com.troblecodings.signals.parser.LogicalParserException;

import net.minecraft.client.renderer.block.model.BuiltInModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CustomModelLoader implements ICustomModelLoader {

    private static HashMap<String, Consumer<SignalCustomModel>> registeredModels = new HashMap<>();

    @SuppressWarnings("unchecked")
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
                final String lowercaseName = filename.replace(".json", "").toLowerCase();
                final Signal signaltype = Signal.SIGNALS.get(lowercaseName);

                if (signaltype == null) {
                    OpenSignalsMain.getLogger().error("There doesn't exists a signalsystem named "
                            + filename.replace(".json", "") + "!");
                    FMLCommonHandler.instance().exitJava(-1, false);
                    return;
                }

                final FunctionParsingInfo parsinginfo = new FunctionParsingInfo(signaltype);

                registeredModels.put(signaltype.getRegistryName().toString()
                        .replace(OpenSignalsMain.MODID + ":", ""), cm -> {

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
                                                OpenSignalsMain.log.error(
                                                        "There was an problem during loading "
                                                                + modelname
                                                                + " with the blockstate '"
                                                                + texturestate.getBlockstate()
                                                                + " '!");
                                                e.printStackTrace();
                                                FMLCommonHandler.instance().exitJava(-1, false);
                                                return;
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
                                        OpenSignalsMain.log.warn(
                                                "The predicate of " + modelname + " in " + filename
                                                        + " is null! This shouldn't be the case!");
                                        FMLCommonHandler.instance().exitJava(-1, false);
                                        return;
                                    }
                                }
                            }
                        });
            }
        }
    }

    @Override
    public boolean accepts(final ResourceLocation modelLocation) {
        if (!modelLocation.getResourceDomain().equals(OpenSignalsMain.MODID))
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
