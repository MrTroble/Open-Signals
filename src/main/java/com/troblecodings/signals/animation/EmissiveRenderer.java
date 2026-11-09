package com.troblecodings.signals.animation;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.troblecodings.signals.OpenSignalsMain;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Draws "_e" textures at full brightness on top of models, independent of
 * OptiFine. It loads "<texture>_e.png" itself.
 *
 * - render(model, x, y, z): for models that are drawn manually (animations)
 * - renderBlock(...): for the normal block model of a block, called from a
 * TESR (the chunk renderer of vanilla/Forge cannot do per-quad brightness)
 *
 * Against flickering (z-fighting) the emissive quads are moved a tiny bit along
 * their surface normal. The distance grows with the distance to the camera
 * (the depth buffer gets less precise), but is always limited, so nothing
 * behind thin plates shines through.
 */
@SideOnly(Side.CLIENT)
public final class EmissiveRenderer {

    private static final String SUFFIX = "_e";

    /** Smallest offset along the normal (in blocks) and number of steps (x2 each). */
    private static final float MIN_OFFSET = 0.0003f;
    private static final int LEVELS = 7;

    /**
     * Wanted offset = squared distance to the camera * this value. 0 = no offset at
     * all. Higher = stronger (flickers less at a distance, but may shine through).
     */
    public static float offsetPerSquaredDistance = 5.0e-6f;

    /**
     * Optional additional polygon offset (off by default). Slope-scaled offsets
     * easily make thin plates shine through, use only if needed.
     */
    public static float depthOffsetFactor = 0.0f;
    public static float depthOffsetUnits = 0.0f;

    private static final class Emissive {
        private final ResourceLocation file;
        private final int[] data;
        private final int tint;
        private final EnumFacing face;
        private final TextureAtlasSprite sprite;
        private final boolean diffuse;
        private final VertexFormat format;
        private final float nx, ny, nz;
        private final BakedQuad[] byLevel = new BakedQuad[LEVELS];

        private Emissive(final ResourceLocation file, final BakedQuad quad,
                final TextureAtlasSprite sprite, final int[] remapped, final float[] normal) {
            this.file = file;
            this.data = remapped;
            this.tint = quad.getTintIndex();
            this.face = quad.getFace();
            this.sprite = sprite;
            this.diffuse = quad.shouldApplyDiffuseLighting();
            this.format = quad.getFormat();
            this.nx = normal[0];
            this.ny = normal[1];
            this.nz = normal[2];
        }

        private BakedQuad quadFor(final int level) {
            BakedQuad quad = byLevel[level];
            if (quad == null) {
                final int[] copy = data.clone();
                final float eps = MIN_OFFSET * (1 << level);
                final int size = format.getIntegerSize();
                for (int i = 0; i + 2 < copy.length; i += size) {
                    copy[i] = Float.floatToRawIntBits(Float.intBitsToFloat(copy[i]) + nx * eps);
                    copy[i + 1] = Float
                            .floatToRawIntBits(Float.intBitsToFloat(copy[i + 1]) + ny * eps);
                    copy[i + 2] = Float
                            .floatToRawIntBits(Float.intBitsToFloat(copy[i + 2]) + nz * eps);
                }
                quad = new BakedQuad(copy, tint, face, sprite, diffuse, format);
                byLevel[level] = quad;
            }
            return quad;
        }
    }

    private static final Emissive NONE = new Emissive(null, new BakedQuad(new int[0], 0, null,
            null, false, DefaultVertexFormats.ITEM), null, new int[0], new float[3]);

    // animated models: model -> its emissive quads
    private static final Map<IBakedModel, List<Emissive>> MODEL_CACHE = new IdentityHashMap<>();
    // every quad is analysed only once
    private static final Map<BakedQuad, Emissive> QUAD_CACHE = new IdentityHashMap<>();
    private static final Map<ResourceLocation, Boolean> EXISTS = new HashMap<>();

    /**
     * AUTO: renderBlock draws only if OptiFine does not already make the block
     * model glow. ALWAYS / NEVER force the behaviour (e.g. for a config option).
     * Does not affect render(model): animated models are always drawn here.
     */
    public enum BlockMode {
        AUTO, ALWAYS, NEVER
    }

    public static BlockMode blockMode = BlockMode.AUTO;

    private static boolean optiFineChecked = false;
    private static boolean optiFinePresent = false;
    private static Field optiFineEmissiveField = null;
    private static Method optiFineEmissiveMethod = null;

    private EmissiveRenderer() {
    }

    /** Call on every resource reload (models are rebaked). */
    public static void clearCache() {
        MODEL_CACHE.clear();
        QUAD_CACHE.clear();
        EXISTS.clear();
    }

    /**
     * For manually drawn models. Call inside the pushed matrix of the model, after
     * the normal model was drawn. x, y, z: the (camera relative) position the TESR
     * got, only used to get the distance to the camera.
     */
    public static void render(final IBakedModel model, final double x, final double y,
            final double z) {
        final List<Emissive> list = MODEL_CACHE.computeIfAbsent(model,
                m -> analyseAll(allQuads(m, null)));
        draw(list, x * x + y * y + z * z);
    }

    /**
     * For the normal block model. Call from the TESR render method with the
     * (camera relative) x, y, z that the TESR gets.
     */
    public static void renderBlock(final World world, final BlockPos pos, final double x,
            final double y, final double z) {
        if (!shouldRenderBlock())
            return;
        final IBlockState state = world.getBlockState(pos);
        final IBlockState actual = state.getActualState(world, pos);
        final IBlockState extended = actual.getBlock().getExtendedState(actual, world, pos);
        final IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher()
                .getBlockModelShapes().getModelForState(actual);
        final List<Emissive> list = analyseAll(allQuads(model, extended));
        if (list.isEmpty())
            return;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        draw(list, x * x + y * y + z * z);
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
    }

    private static int level(final double squaredDistance) {
        final double wanted = squaredDistance * offsetPerSquaredDistance;
        int level = 0;
        float offset = MIN_OFFSET;
        while (level < LEVELS - 1 && offset < wanted) {
            offset *= 2;
            level++;
        }
        return level;
    }

    private static void draw(final List<Emissive> emissives, final double squaredDistance) {
        if (emissives.isEmpty())
            return;

        final int level = level(squaredDistance);
        final Map<ResourceLocation, List<BakedQuad>> perTexture = new HashMap<>();
        for (final Emissive emissive : emissives) {
            perTexture.computeIfAbsent(emissive.file, f -> new ArrayList<>())
                    .add(emissive.quadFor(level));
        }

        final Minecraft mc = Minecraft.getMinecraft();
        final RenderItem renderItem = mc.getRenderItem();

        // remember the lightmap value of the caller, so that everything drawn after
        // us (other models, other TESRs) keeps its normal world brightness
        final float lastLightX = OpenGlHelper.lastBrightnessX;
        final float lastLightY = OpenGlHelper.lastBrightnessY;

        // back faces (inner sides of thin plates) must not be drawn
        final boolean wasCulling = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        GlStateManager.enableCull();

        GlStateManager.disableLighting();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        final boolean offset = depthOffsetFactor != 0.0f || depthOffsetUnits != 0.0f;
        if (offset) {
            GlStateManager.enablePolygonOffset();
            GlStateManager.doPolygonOffset(depthOffsetFactor, depthOffsetUnits);
        }
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);

        for (final Map.Entry<ResourceLocation, List<BakedQuad>> entry : perTexture.entrySet()) {
            mc.getTextureManager().bindTexture(entry.getKey());
            final Tessellator tessellator = Tessellator.getInstance();
            final BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
            renderItem.renderQuads(buffer, entry.getValue(), 0xFFFFFFFF, ItemStack.EMPTY);
            tessellator.draw();
        }

        if (offset) {
            GlStateManager.doPolygonOffset(0.0f, 0.0f);
            GlStateManager.disablePolygonOffset();
        }
        GlStateManager.disableBlend();
        if (!wasCulling)
            GlStateManager.disableCull();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastLightX,
                lastLightY);
    }

    private static List<BakedQuad> allQuads(final IBakedModel model, final IBlockState state) {
        final List<BakedQuad> all = new ArrayList<>(model.getQuads(state, null, 0));
        for (final EnumFacing face : EnumFacing.VALUES) {
            all.addAll(model.getQuads(state, face, 0));
        }
        return all;
    }

    private static List<Emissive> analyseAll(final List<BakedQuad> quads) {
        final List<Emissive> result = new ArrayList<>();
        for (final BakedQuad quad : quads) {
            final Emissive emissive = QUAD_CACHE.computeIfAbsent(quad, EmissiveRenderer::analyse);
            if (emissive != NONE)
                result.add(emissive);
        }
        return result;
    }

    private static Emissive analyse(final BakedQuad quad) {
        final TextureAtlasSprite sprite = quad.getSprite();
        if (sprite == null)
            return NONE;
        final ResourceLocation base = new ResourceLocation(sprite.getIconName());
        if (base.getResourcePath().endsWith(SUFFIX))
            return NONE;
        final ResourceLocation file = new ResourceLocation(base.getResourceDomain(),
                "textures/" + base.getResourcePath() + SUFFIX + ".png");
        if (!EXISTS.computeIfAbsent(file, EmissiveRenderer::resourceExists))
            return NONE;
        return new Emissive(file, quad, sprite, remapUV(quad, sprite),
                normal(quad.getVertexData(), quad.getFormat().getIntegerSize()));
    }

    private static boolean resourceExists(final ResourceLocation file) {
        try {
            Minecraft.getMinecraft().getResourceManager().getResource(file).close();
            return true;
        } catch (final IOException e) {
            return false;
        }
    }

    /** Converts atlas UVs of the quad into 0..1 UVs of the standalone _e file. */
    private static int[] remapUV(final BakedQuad quad, final TextureAtlasSprite sprite) {
        final int[] data = quad.getVertexData().clone();
        final int size = quad.getFormat().getIntegerSize();
        final float du = sprite.getMaxU() - sprite.getMinU();
        final float dv = sprite.getMaxV() - sprite.getMinV();
        // BLOCK and ITEM format: position (0-2), color (3), uv (4-5)
        for (int i = 0; i + 5 < data.length; i += size) {
            final float u = (Float.intBitsToFloat(data[i + 4]) - sprite.getMinU()) / du;
            final float v = (Float.intBitsToFloat(data[i + 5]) - sprite.getMinV()) / dv;
            data[i + 4] = Float.floatToRawIntBits(u);
            data[i + 5] = Float.floatToRawIntBits(v);
        }
        return data;
    }

    /**
     * Normal of the quad (pointing outwards, vertices are counter-clockwise). Works
     * for rotated models too, as it is calculated from the vertex positions.
     */
    private static float[] normal(final int[] data, final int size) {
        final float[] result = new float[3];
        if (data.length < size * 3 + 3)
            return result;
        final float ax = pos(data, size, 2, 0) - pos(data, size, 0, 0);
        final float ay = pos(data, size, 2, 1) - pos(data, size, 0, 1);
        final float az = pos(data, size, 2, 2) - pos(data, size, 0, 2);
        final float bx = pos(data, size, 3, 0) - pos(data, size, 1, 0);
        final float by = pos(data, size, 3, 1) - pos(data, size, 1, 1);
        final float bz = pos(data, size, 3, 2) - pos(data, size, 1, 2);
        final float nx = ay * bz - az * by;
        final float ny = az * bx - ax * bz;
        final float nz = ax * by - ay * bx;
        final float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (length < 1.0e-6f)
            return result;
        result[0] = nx / length;
        result[1] = ny / length;
        result[2] = nz / length;
        return result;
    }

    private static float pos(final int[] data, final int size, final int vertex,
            final int axis) {
        return Float.intBitsToFloat(data[vertex * size + axis]);
    }

    private static boolean shouldRenderBlock() {
        switch (blockMode) {
            case ALWAYS:
                return true;
            case NEVER:
                return false;
            default:
                return !optiFineMakesBlocksGlow();
        }
    }

    /** true if OptiFine is installed and its "Emissive Textures" option is on. */
    private static boolean optiFineMakesBlocksGlow() {
        if (!optiFineChecked) {
            optiFineChecked = true;
            optiFinePresent = FMLClientHandler.instance().hasOptifine();
            if (optiFinePresent) {
                initOptiFineAccess();
                OpenSignalsMain.getLogger()
                        .info("[Emissive] OptiFine detected, emissive option readable: "
                                + (optiFineEmissiveField != null || optiFineEmissiveMethod != null));
            }
        }
        if (!optiFinePresent)
            return false;
        try {
            if (optiFineEmissiveField != null)
                return optiFineEmissiveField.getBoolean(Minecraft.getMinecraft().gameSettings);
            if (optiFineEmissiveMethod != null)
                return (Boolean) optiFineEmissiveMethod.invoke(null);
        } catch (final Exception e) {
            // fall through
        }
        // Could not read the option -> draw ourselves (a double glow is harmless)
        return false;
    }

    private static void initOptiFineAccess() {
        try {
            final Field field = GameSettings.class.getDeclaredField("ofEmissiveTextures");
            field.setAccessible(true);
            if (field.getType() == boolean.class) {
                optiFineEmissiveField = field;
                return;
            }
        } catch (final Exception e) {
            // try the next way
        }
        for (final String className : new String[] { "net.optifine.Config", "Config" }) {
            try {
                optiFineEmissiveMethod = Class.forName(className)
                        .getMethod("isEmissiveTextures");
                return;
            } catch (final Exception e) {
                // try the next class
            }
        }
    }
}