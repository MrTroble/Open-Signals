package com.troblecodings.signals.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.parser.FunctionParsingInfo;
import com.troblecodings.signals.parser.LogicParser;
import com.troblecodings.signals.parser.LogicalParserException;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;

@OnlyIn(Dist.CLIENT)
public class CustomModelLoader implements ResourceManagerReloadListener {

    private static HashMap<String, Consumer<SignalCustomModel>> registeredModels = new HashMap<>();

    public static final CustomModelLoader INSTANCE = new CustomModelLoader();

    private CustomModelLoader() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onResourceManagerReload(final ResourceManager resourceManager) {

        registeredModels.clear();

        final Map<String, ModelExtention> extentions = new HashMap<>();

        final Map<String, Object> modelmap = ModelStats
                .getfromJson("/assets/opensignals/modeldefinitions");

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
                    OpenSignalsMain.getLogger().error(
                            "There doesn't exists a signalsystem named " + lowercaseName + "!");
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

                                    Predicate<IModelData> state = null;

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
                                        OpenSignalsMain.log.error(
                                                "The predicate of " + modelname + " in " + filename
                                                        + " is null! This shouldn't be the case!");
                                        return;
                                    }
                                }
                            }
                        });
            }
        }
    }

    public void register(Map<ResourceLocation, BakedModel> registry) {
        
    }
}
