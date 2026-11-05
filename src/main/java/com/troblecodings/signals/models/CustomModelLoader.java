package com.troblecodings.signals.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;

import com.mojang.math.Transformation;

import com.troblecodings.core.VectorWrapper;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.animation.SignalAnimation;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.contentpacks.SignalAnimationConfigParser;
import com.troblecodings.signals.core.SignalAngel;
import com.troblecodings.signals.parser.FunctionParsingInfo;
import com.troblecodings.signals.parser.LogicParser;
import com.troblecodings.signals.parser.LogicalParserException;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.SimpleModelState;

@OnlyIn(Dist.CLIENT)
public final class CustomModelLoader implements ResourceManagerReloadListener {

    private static HashMap<String, List<SignalModelLoaderInfo>> registeredModels = new HashMap<>();

    public static final CustomModelLoader INSTANCE = new CustomModelLoader();

    /**
     * Models injected during ModelEvent.ModifyBakingResult, held until the atlases have been
     * uploaded so their quads can be built. See {@link #onResourceManagerReload}.
     */
    private final List<SignalBakedModel> pendingBakes = new ArrayList<>();

    private CustomModelLoader() {
    }

    /**
     * Minimal {@link ModelBaker} over the bakery handed out by ModelEvent.
     *
     * Up to 1.18 the signal models were injected by swapping a custom Map into
     * {@code ForgeModelBakery.unbakedCache}. That class was deleted in Forge 1.19, so the models
     * are now baked here and pushed into the baked model map instead. ModelBakery's own
     * ModelBakerImpl is package private, hence this small implementation.
     */
    private static final class SignalModelBaker implements ModelBaker {

        private final ModelBakery bakery;
        private final Function<Material, TextureAtlasSprite> sprites;

        private SignalModelBaker(final ModelBakery bakery,
                final Function<Material, TextureAtlasSprite> sprites) {
            this.bakery = bakery;
            this.sprites = sprites;
        }

        @Override
        public UnbakedModel getModel(final ResourceLocation location) {
            return bakery.getModel(location);
        }

        @Override
        public Function<Material, TextureAtlasSprite> getModelTextureGetter() {
            return sprites;
        }

        @Override
        public BakedModel bake(final ResourceLocation location, final ModelState state) {
            return bake(location, state, sprites);
        }

        @Override
        public BakedModel bake(final ResourceLocation location, final ModelState state,
                final Function<Material, TextureAtlasSprite> spriteGetter) {
            return getModel(location).bake(this, spriteGetter, state, location);
        }
    }

    /**
     * Bakes a SignalCustomModel for every registered signal and angle and writes them into the
     * baked model map, replacing whatever the missing blockstate files produced.
     */
    public void bakeInto(final Map<ResourceLocation, BakedModel> models, final ModelBakery bakery) {
        // The reload listener runs after the model manager has already baked, so the definitions
        // are (re)read here instead. 1.18 did the same from ModelRegistryEvent.
        loadDefinitions();
        pendingBakes.clear();
        final ModelBaker baker = new SignalModelBaker(bakery, Material::sprite);
        int baked = 0;
        for (final Entry<String, List<SignalModelLoaderInfo>> entry : registeredModels.entrySet()) {
            for (final SignalAngel angel : SignalAngel.values()) {
                final ModelResourceLocation location = new ModelResourceLocation(
                        OpenSignalsMain.MODID, entry.getKey(),
                        "angel=" + angel.getNameWrapper());
                final BakedModel model = new SignalCustomModel(angel, entry.getValue()).bake(baker,
                        Material::sprite, new SimpleModelState(Transformation.identity()),
                        location);
                if (model instanceof SignalBakedModel) {
                    models.put(location, model);
                    pendingBakes.add((SignalBakedModel) model);
                    baked++;
                }
            }
        }
        OpenSignalsMain.getLogger().info("Baked {} models for {} signal types.", baked,
                registeredModels.size());
    }

    public static Map<String, List<SignalModelLoaderInfo>> getRegisteredModels() {
        return registeredModels;
    }

    private static void loadExtention(final TextureStats texturestate,
            final Map<String, ModelExtention> extention, final String modelname,
            final ModelStats states, final Models models, final FunctionParsingInfo info,
            final List<SignalModelLoaderInfo> accumulator) {

        final String blockstate = texturestate.getBlockstate();

        final Map<String, String> retexture = texturestate.getRetextures();

        for (final Map.Entry<String, Map<String, String>> extentions : texturestate.getExtentions()
                .entrySet()) {

            final String extentionName = extentions.getKey();
            final Map<String, String> extentionProperties = extentions.getValue();
            final ModelExtention extentionValues = extention.get(extentionName);
            if (extentionValues == null)
                OpenSignalsMain.exitMinecraftWithMessage(
                        String.format("There doesn't exists an extention named [%s]!",
                                extentionName) + " Valid Extentions: " + extention.keySet());

            for (final Map.Entry<String, String> entry : extentionValues.getExtention()
                    .entrySet()) {

                final String enumValue = entry.getKey();
                final String retextureValue = entry.getValue();

                for (final Map.Entry<String, String> extProperties : extentionProperties
                        .entrySet()) {
                    final String seProperty = extProperties.getKey();
                    final String retextureKey = extProperties.getValue();

                    final boolean load = texturestate.appendExtention(seProperty, enumValue,
                            retextureKey, retextureValue);

                    if (load) {

                        try {
                            accumulator
                                    .add(new SignalModelLoaderInfo(modelname,
                                            LogicParser.predicate(texturestate.getBlockstate(),
                                                    info),
                                            models.getX(texturestate.getOffsetX()),
                                            models.getY(texturestate.getOffsetY()),
                                            models.getZ(texturestate.getOffsetZ()),
                                            states.createRetexture(texturestate.getRetextures())));
                        } catch (final LogicalParserException e) {
                            OpenSignalsMain.exitMinecraftWithMessage(
                                    "There was an problem during loading an extention into "
                                            + modelname + " with the blockstate '"
                                            + texturestate.getBlockstate() + "'! " + e);
                        } finally {
                            texturestate.resetStates(blockstate, retexture);
                        }
                    }
                }
            }

        }
    }

    /**
     * Runs after ModelManager has finished, which is the first point at which the texture atlases
     * have been uploaded, so the models injected during baking can now build their quads. Doing it
     * here rather than on first render also keeps
     * {@link SignalCustomModel#getModelFromLocation} populated for the animation handler.
     */
    @Override
    public void onResourceManagerReload(final ResourceManager manager) {
        pendingBakes.forEach(SignalBakedModel::resolve);
        pendingBakes.clear();
    }

    private void loadDefinitions() {
        registeredModels.clear();

        final Map<String, ModelExtention> extentions = new HashMap<>();

        final Map<String, Object> modelmap = ModelStats.getfromJson("modeldefinitions");

        modelmap.forEach((filename, content) -> {

            if (filename.endsWith(".extention.json")) {

                final ModelExtention ext = (ModelExtention) content;
                extentions.put(filename.replace(".extention", ""), ext);
            }
        });

        for (final Entry<String, Object> modelstatemap : modelmap.entrySet()) {

            final String filename = modelstatemap.getKey();

            if (!filename.endsWith(".extention.json")) {
                final List<SignalModelLoaderInfo> accumulator = new ArrayList<>();

                final ModelStats content = (ModelStats) modelstatemap.getValue();
                final String lowercaseName = filename.replace(".json", "").toLowerCase();
                final Signal signaltype = Signal.SIGNALS.get(lowercaseName);

                if (signaltype == null) {
                    throw new IllegalArgumentException("There doesn't exists a signalsystem named "
                            + lowercaseName + "! Allowed Signals: " + Signal.SIGNALS.keySet());
                }

                final FunctionParsingInfo parsinginfo = new FunctionParsingInfo(signaltype);

                for (final Map.Entry<String, Models> modelsmap : content.getModels().entrySet()) {

                    final String modelname = modelsmap.getKey();
                    final Models modelstats = modelsmap.getValue();

                    for (int i = 0; i < modelstats.getTexture().size(); i++) {

                        final TextureStats texturestate = modelstats.getTexture().get(i);

                        final String blockstate = texturestate.getBlockstate();

                        Predicate<ModelInfoWrapper> state = null;

                        boolean extentionloaded = false;

                        if (!texturestate.isautoBlockstate()) {

                            final Map<String, Map<String, String>> texturemap = texturestate
                                    .getExtentions();

                            if (texturemap != null && !texturemap.isEmpty()) {

                                loadExtention(texturestate, extentions, modelname, content,
                                        modelstats, parsinginfo, accumulator);

                                extentionloaded = true;
                            }

                            if (!extentionloaded) {

                                try {
                                    state = LogicParser.predicate(blockstate, parsinginfo);
                                } catch (final LogicalParserException e) {
                                    OpenSignalsMain.getLogger()
                                            .error("There was an problem during loading "
                                                    + modelname + " with the blockstate '"
                                                    + texturestate.getBlockstate() + " '!");
                                    e.printStackTrace();
                                    return;
                                }
                            }
                        }

                        if ((state != null || texturestate.isautoBlockstate())
                                && !extentionloaded) {
                            accumulator.add(new SignalModelLoaderInfo(modelname,
                                    texturestate.isautoBlockstate() ? (_u -> true) : state,
                                    modelstats.getX(texturestate.getOffsetX()),
                                    modelstats.getY(texturestate.getOffsetY()),
                                    modelstats.getZ(texturestate.getOffsetZ()),
                                    content.createRetexture(texturestate.getRetextures())));
                        } else if (state == null && !texturestate.isautoBlockstate()
                                && !extentionloaded) {
                            OpenSignalsMain.getLogger().error("The predicate of " + modelname
                                    + " in " + filename + " is null! This shouldn't be the case!");
                            return;
                        }
                    }
                }
                SignalAnimationConfigParser.loadAllAnimations();
                final Map<Entry<String, VectorWrapper>, List<SignalAnimation>> animations = //
                        SignalAnimationConfigParser.ALL_ANIMATIONS.get(signaltype);
                if (animations != null) {
                    animations.keySet()
                            .forEach(entry -> accumulator.add(
                                    new SignalModelLoaderInfo(entry.getKey(), predicate -> false, 0,
                                            0, 0, new HashMap<>()).setOnAnimation()));
                }
                registeredModels.put(lowercaseName, accumulator);
            }
        }
    }
}