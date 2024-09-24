package com.troblecodings.signals.animation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.troblecodings.core.VectorWrapper;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.contentpacks.SignalAnimationConfigParser;
import com.troblecodings.signals.core.RenderAnimationInfo;
import com.troblecodings.signals.core.SignalAngel;
import com.troblecodings.signals.models.SignalCustomModel;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;

public class SignalAnimationHandler {

    private final SignalTileEntity tile;

    public SignalAnimationHandler(final SignalTileEntity tile) {
        this.tile = tile;
    }

    private final Map<BakedModel, Entry<ModelTranslation, List<SignalAnimationState>>>//
    animationPerModel = new HashMap<>();

    public void render(final RenderAnimationInfo info) {
        final BlockState state = tile.getBlockState();
        final SignalAngel angle = state.getValue(Signal.ANGEL);
        final ModelBlockRenderer renderer = info.dispatcher.getModelRenderer();
        final VertexConsumer vertex = info.source
                .getBuffer(ItemBlockRenderTypes.getRenderType(state, false));
        final IModelData data = tile.getModelData();

        animationPerModel.forEach((model, entry) -> {
            final ModelTranslation translation = entry.getKey();
            if (!translation.shouldRenderModel())
                return;

            info.stack.pushPose();
            info.stack.translate(0.5f, 0.5f, 0.5f);
            info.stack.mulPose(angle.getQuaternion());
            translation.translate(info);
            renderer.renderModel(info.stack.last(), vertex, state, model, 0, 0, 0, info.lightColor,
                    info.overlayTexture, data);
            info.stack.popPose();

            if (translation.isAnimationAssigned())
                updateAnimation(translation);
        });
    }

    private void updateAnimation(final ModelTranslation translation) {
        final SignalAnimationState animation = translation.getAssigendAnimation();
        if (animation.isFinished()) {
            translation.setUpNewTranslation(animation.getFinalModelTranslation());
            translation.removeAnimation();
            animation.reset();
            return;
        }
        animation.updateAnimation();
        translation.setUpNewTranslation(animation.getModelTranslation());
    }

    public void updateStates(final Map<SEProperty, String> newProperties,
            final Map<SEProperty, String> oldProperties) {
        final Map<SEProperty, String> changedProperties = new HashMap<>();
        newProperties.entrySet().stream().filter(entry -> {
            final String oldState = oldProperties.get(entry.getKey());
            return oldState != null && !entry.getValue().equals(oldState);
        }).forEach(entry -> changedProperties.put(entry.getKey(), entry.getValue()));

        if (changedProperties.isEmpty()) {
            updateToFinalizedAnimations(newProperties);
        } else {
            updateAnimations(changedProperties);
        }
    }

    private void updateAnimations(final Map<SEProperty, String> changedProperties) {
        animationPerModel.values().forEach(entry -> {
            for (final SignalAnimationState animation : entry.getValue()) {
                if (animation.test(changedProperties)) {
                    final ModelTranslation translation = entry.getKey();
                    if (translation.isAnimationAssigned()) {
                        final SignalAnimationState other = translation.getAssigendAnimation();
                        other.reset();
                    }
                    animation.setUpAnimationValues(translation);
                    translation.setUpNewTranslation(animation.getModelTranslation());
                    translation.assignAnimation(animation);
                }
            }
        });
    }

    private void updateToFinalizedAnimations(final Map<SEProperty, String> newProperties) {
        animationPerModel.values().forEach((entry) -> {
            for (final SignalAnimationState animation : entry.getValue()) {
                if (animation.test(newProperties)) {
                    final ModelTranslation translation = entry.getKey();
                    translation.setUpNewTranslation(animation.getFinalModelTranslation());
                    translation.setRenderModel(true);
                }
            }
        });
    }

    public void updateAnimationListFromBlock() {
        animationPerModel.clear();
        final Map<String, List<SignalAnimationState>> map = SignalAnimationConfigParser.ALL_ANIMATIONS
                .get(tile.getSignal());
        map.forEach((modelName, animations) -> {
            final BakedModel model = SignalCustomModel
                    .getModelFromLocation(new ResourceLocation(OpenSignalsMain.MODID, modelName));
            final ModelTranslation translation = new ModelTranslation(VectorWrapper.ZERO,
                    new Quaternion(0, 0, 0, 0), VectorWrapper.ZERO);
            animationPerModel.put(model, Maps.immutableEntry(translation, animations));
        });
    }

}
