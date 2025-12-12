package com.troblecodings.signals.animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Quaternion;

import com.google.common.collect.Maps;
import com.troblecodings.core.QuaternionWrapper;
import com.troblecodings.core.VectorWrapper;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.contentpacks.SignalAnimationConfigParser;
import com.troblecodings.signals.core.RenderAnimationInfo;
import com.troblecodings.signals.core.SignalAngel;
import com.troblecodings.signals.models.ModelInfoWrapper;
import com.troblecodings.signals.models.SignalCustomModel;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.LightUtil;

public class SignalAnimationHandler {

    private final SignalTileEntity tile;
    private BlockRendererDispatcher blockRenderer;
    private int animationsRunning = 0;
    private long lastWorldTick = -1;

    public static final float BASIC_ANIMATION_SPEED = 0.001f;

    public SignalAnimationHandler(final SignalTileEntity tile) {
        this.tile = tile;
    }

    private final Map<Entry<IBakedModel, BufferBuilder>, Entry<ModelTranslation, List<SignalAnimation>>> //
        animationPerModel = new HashMap<>();

    public void render(final RenderAnimationInfo info) {
        final World world = tile.getWorld();
        final BlockPos pos = tile.getPos();
        final IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof Signal))
            return;
        final long currentTick = Minecraft.getSystemTime();
        if (lastWorldTick < 0)
            this.lastWorldTick = currentTick;
        final SignalAngel angle = state.getValue(Signal.ANGEL);
        if (blockRenderer == null) {
            blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        }

        final float tick = (float) (currentTick - this.lastWorldTick);
        this.lastWorldTick = currentTick;

        animationPerModel.forEach((first, entry) -> {
            final ModelTranslation translation = entry.getKey();
            if (!translation.shouldRenderModel())
                return;

            GlStateManager.pushMatrix();
            GlStateManager.translate(info.x + 0.5f, info.y + 0.5f, info.z + 0.5f);
            GlStateManager.rotate(QuaternionWrapper.fromXYZ(0, (float) angle.getRadians(), 0));
            translation.translate();
            GlStateManager.rotate(-90, 0, 1, 0);
            Minecraft.getMinecraft().getTextureManager()
                    .bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.disableLighting();
            blockRenderer.getBlockModelRenderer().renderModel(world, first.getKey(), state, pos,
                    first.getValue(), false);
            blockRenderer.getBlockModelRenderer().renderModelBrightness(first.getKey(), state,
                    0.65f, false);

            GlStateManager.popMatrix();

            if (translation.isAnimationAssigned()) {
                updateAnimation(translation, tick);
            } else {
                first.getValue().reset();
            }
        });
    }

    public boolean areAnimationsRunning() {
        return animationsRunning > 0;
    }

    private void updateAnimation(final ModelTranslation translation, final float ticks) {
        final SignalAnimation animation = translation.getAssigendAnimation();
        animation.updateAnimation(ticks);
        if (animation.isFinished()) {
            translation.setUpNewTranslation(animation.getFinalModelTranslation());
            translation.removeAnimation();
            animation.reset();
            animationsRunning--;
            return;
        }
        translation.setUpNewTranslation(animation.getModelTranslation());
    }

    public void updateStates(final Map<SEProperty, String> properties, final boolean firstLoad) {
        final ModelInfoWrapper wrapper = new ModelInfoWrapper(tile.getBlockType(), properties);
        if (firstLoad) {
            updateToFinalizedAnimations(wrapper);
        } else {
            updateAnimations(wrapper);
        }
    }

    private void updateAnimations(final ModelInfoWrapper wrapper) {
        animationPerModel.values().forEach(entry -> {
            entry.getKey().setRenderModel(false);
            for (final SignalAnimation animation : entry.getValue()) {
                if (animation.test(wrapper)) {
                    final ModelTranslation translation = entry.getKey();
                    translation.setRenderModel(true);
                    if (translation.isAnimationAssigned()) {
                        final SignalAnimation other = translation.getAssigendAnimation();
                        other.reset();
                        animationsRunning--;
                    }
                    animation.setUpAnimationValues(translation);
                    translation.setUpNewTranslation(animation.getModelTranslation());
                    translation.assignAnimation(animation);
                    animationsRunning++;
                }
            }
        });
    }

    private void updateToFinalizedAnimations(final ModelInfoWrapper wrapper) {
        animationPerModel.values().forEach((entry) -> {
            for (final SignalAnimation animation : entry.getValue()) {
                if (animation.test(wrapper)) {
                    final ModelTranslation translation = entry.getKey();
                    translation.setUpNewTranslation(animation.getFinalModelTranslation());
                    translation.setRenderModel(true);
                }
            }
        });
    }

    public void updateAnimationListFromBlock() {
        animationPerModel.clear();
        final Map<Entry<String, VectorWrapper>, List<SignalAnimation>> map = //
                SignalAnimationConfigParser.ALL_ANIMATIONS.getOrDefault(tile.getSignal(),
                        new HashMap<>());
        map.forEach((entry, animations) -> {
            final IBakedModel model = SignalCustomModel.getModelFromLocation(
                    new ResourceLocation(OpenSignalsMain.MODID, entry.getKey()));
            final ModelTranslation translation = new ModelTranslation(VectorWrapper.ZERO,
                    new Quaternion(0, 0, 0, 0));
            translation.setModelTranslation(entry.getValue().copy());
            final BufferBuilder buffer = getBufferFromModel(model, entry.getValue().copy());
            animationPerModel.put(Maps.immutableEntry(model, buffer),
                    Maps.immutableEntry(translation, animations.stream()
                            .map(animation -> animation.copy()).collect(Collectors.toList())));
        });
    }

    private BufferBuilder getBufferFromModel(final IBakedModel model, final VectorWrapper vec) {
        final BufferBuilder buffer = new BufferBuilder(500);
        final IBlockState ebs = tile.getWorld().getBlockState(tile.getPos());
        assert ebs != null;
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        buffer.setTranslation(vec.getX(), vec.getY(), vec.getZ());
        final List<BakedQuad> lst = new ArrayList<>();
        lst.addAll(model.getQuads(ebs, null, 0));
        for (final EnumFacing face : EnumFacing.VALUES) {
            lst.addAll(model.getQuads(ebs, face, 0));
        }

        final BlockColors blockColors = Minecraft.getMinecraft().getBlockColors();
        for (final BakedQuad quad : lst) {
            final int k = quad.hasTintIndex()
                    ? (blockColors.colorMultiplier(tile.getBlockType().getDefaultState(), null,
                            null, quad.getTintIndex()) + 0xFFFFFFFF)
                    : 0xFFFFFFFF;
            LightUtil.renderQuadColor(buffer, quad, k);
        }
        buffer.finishDrawing();
        return buffer;
    }

}