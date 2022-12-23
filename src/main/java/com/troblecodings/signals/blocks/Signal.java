package com.troblecodings.signals.blocks;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.contentpacks.ContentPackException;
import com.troblecodings.signals.contentpacks.SoundPropertyParser;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.init.OSSounds;
import com.troblecodings.signals.items.Placementtool;
import com.troblecodings.signals.parser.FunctionParsingInfo;
import com.troblecodings.signals.parser.LogicParser;
import com.troblecodings.signals.parser.LogicalParserException;
import com.troblecodings.signals.parser.ValuePack;
import com.troblecodings.signals.properties.FloatProperty;
import com.troblecodings.signals.properties.HeightProperty;
import com.troblecodings.signals.properties.SoundProperty;
import com.troblecodings.signals.tileentitys.SignalTileEnity;
import com.troblecodings.signals.utils.JsonEnum;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.models.blockstates.VariantProperties.Rotation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;

public class Signal extends Block implements EntityBlock {

    public static enum SignalAngel {

        ANGEL0, ANGEL22P5, ANGEL45, ANGEL67P5, ANGEL90, ANGEL112P5, ANGEL135, ANGEL157P5, ANGEL180,
        ANGEL202P5, ANGEL225, ANGEL247P5, ANGEL270, ANGEL292P5, ANGEL315, ANGEL337P5;

        public String getName() {
            return this.name().toLowerCase();
        }

        public float getDegree() {
            return this.ordinal() * 22.5f;
        }

        public double getRadians() {
            return (this.ordinal() / 16.0) * Math.PI * 2.0;
        }
    }

    public static class SignalProperties {

        public final Placementtool placementtool;
        public final float customNameRenderHeight;
        public final int defaultHeight;
        public final List<HeightProperty> signalHeights;
        public final List<FloatProperty> customRenderHeights;
        public final float signWidth;
        public final float offsetX;
        public final float offsetY;
        public final float signScale;
        public final boolean canLink;
        public final List<Integer> colors;
        public final List<SoundProperty> sounds;
        public final List<ValuePack> redstoneOutputs;

        public SignalProperties(final Placementtool placementtool,
                final float customNameRenderHeight, final int height,
                final List<HeightProperty> signalHeights, final float signWidth,
                final float offsetX, final float offsetY, final float signScale,
                final boolean canLink, final List<Integer> colors,
                final List<FloatProperty> renderheights, final List<SoundProperty> sounds,
                final List<ValuePack> redstoneOutputs) {
            this.placementtool = placementtool;
            this.customNameRenderHeight = customNameRenderHeight;
            this.defaultHeight = height;
            this.signWidth = signWidth;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.signScale = signScale;
            this.canLink = canLink;
            this.colors = colors;
            this.signalHeights = signalHeights;
            this.customRenderHeights = renderheights;
            this.sounds = sounds;
            this.redstoneOutputs = redstoneOutputs;
        }

    }

    public static class SignalPropertiesBuilder {

        private transient Placementtool placementtool = null;
        private final String placementToolName = null;
        private final int defaultHeight = 1;
        private Map<String, Integer> signalHeights;
        private final float customNameRenderHeight = -1;
        private Map<String, Float> renderHeights;
        private final float signWidth = 22;
        private final float offsetX = 0;
        private final float offsetY = 0;
        private final float signScale = 1;
        private final boolean canLink = true;
        private List<Integer> colors;
        private Map<String, SoundPropertyParser> sounds;
        private Map<String, String> redstoneOutputs;

        public SignalPropertiesBuilder() {
        }

        public SignalPropertiesBuilder(final Placementtool placementtool,
                final String signalTypeName) {
            this.placementtool = placementtool;
        }

        @SuppressWarnings("rawtypes")
        public SignalProperties build(final FunctionParsingInfo info) {
            if (placementToolName != null) {
                OSItems.placementtools.forEach(item -> {
                    if (item.getRegistryName().toString().replace(OpenSignalsMain.MODID + ":", "")
                            .equalsIgnoreCase(placementToolName)) {
                        placementtool = item;
                        return;
                    }

                });
                if (placementtool == null)
                    throw new ContentPackException(
                            "There doesn't exists a placementtool with the name '"
                                    + placementToolName + "'!");
            }

            final List<HeightProperty> signalheights = new ArrayList<>();
            if (signalHeights != null) {
                signalHeights.forEach((property, height) -> {
                    if (info != null) {
                        try {
                            signalheights.add(new HeightProperty(
                                    LogicParser.predicate(property, info), height));
                        } catch (final LogicalParserException e) {
                            OpenSignalsMain.getLogger().error(
                                    "Something went wrong during the registry of a predicate in "
                                            + info.signalName + "!\nWith statement:" + property);
                            e.printStackTrace();
                        }
                    }

                });
            }

            final List<FloatProperty> renderheights = new ArrayList<>();
            if (renderHeights != null) {
                renderHeights.forEach((property, height) -> {
                    if (info != null) {
                        try {
                            renderheights.add(new FloatProperty(
                                    LogicParser.predicate(property, info), height));
                        } catch (final LogicalParserException e) {
                            OpenSignalsMain.getLogger().error(
                                    "Something went wrong during the registry of a predicate in "
                                            + info.signalName + "!\nWith statement:" + property);
                            e.printStackTrace();
                        }
                    }
                });
            }

            final List<SoundProperty> soundProperties = new ArrayList<>();
            if (sounds != null) {
                for (final Map.Entry<String, SoundPropertyParser> soundProperty : sounds
                        .entrySet()) {
                    final SoundPropertyParser soundProp = soundProperty.getValue();
                    final SoundEvent sound = OSSounds.SOUNDS.get(soundProp.getName().toLowerCase());
                    if (sound == null) {
                        OpenSignalsMain.getLogger().error("The sound with the name "
                                + soundProp.getName() + " doesn't exists!");
                        continue;
                    }
                    try {
                        soundProperties.add(new SoundProperty(sound,
                                LogicParser.predicate(soundProperty.getKey(), info),
                                soundProp.getLength()));
                    } catch (final LogicalParserException e) {
                        OpenSignalsMain.getLogger()
                                .error("Something went wrong during the registry of a predicate in "
                                        + info.signalName + "!\nWith statement:"
                                        + soundProperty.getKey());
                        e.printStackTrace();
                    }
                }
            }

            final List<ValuePack> rsOutputs = new ArrayList<>();
            if (redstoneOutputs != null) {
                for (final Map.Entry<String, String> outputs : redstoneOutputs.entrySet()) {
                    final SEProperty property = (SEProperty) info.getProperty(outputs.getValue());
                    rsOutputs.add(
                            new ValuePack(property, LogicParser.predicate(outputs.getKey(), info)));
                }
            }

            this.colors = this.colors == null ? new ArrayList<>() : this.colors;

            return new SignalProperties(placementtool, customNameRenderHeight, defaultHeight,
                    ImmutableList.copyOf(signalheights), signWidth, offsetX, offsetY, signScale,
                    canLink, colors, ImmutableList.copyOf(renderheights),
                    ImmutableList.copyOf(soundProperties), ImmutableList.copyOf(rsOutputs));
        }
    }

    public static final SignalPropertiesBuilder builder(final Placementtool placementtool,
            final String signalTypeName) {
        return new SignalPropertiesBuilder(placementtool, signalTypeName);
    }

    public static final ArrayList<Signal> SIGNALLIST = new ArrayList<Signal>();

    public static final Map<String, Signal> SIGNALS = new HashMap<>();

    public static final EnumProperty<SignalAngel> ANGEL = EnumProperty.create("angel",
            SignalAngel.class);
    public static final SEProperty CUSTOMNAME = new SEProperty("customname",
            JsonEnum.PROPERTIES.get("boolean"), "false", ChangeableStage.AUTOMATICSTAGE, t -> true);

    private final int id;
    protected final SignalProperties prop;

    @SuppressWarnings("rawtypes")
    public static Consumer<List<SEProperty>> nextConsumer = _u -> {
    };

    public Signal(final SignalProperties prop) {
        super(Properties.of(Material.STONE));
        this.prop = prop;
        setDefaultState(getDefaultState().withProperty(ANGEL, SignalAngel.ANGEL0));
        id = SIGNALLIST.size();
        SIGNALLIST.add(this);
        prop.placementtool.addSignal(this);
    }

    @Override
    public AABB getBoundingBox(final BlockState state, final LevelAccessor source,
            final BlockPos pos) {
        final SignalTileEnity te = (SignalTileEnity) source.getBlockEntity(pos);
        if (te == null)
            return FULL_BLOCK_AABB;
        return FULL_BLOCK_AABB.expand(0, getHeight(te.getProperties()), 0);
    }

    @Override
    public AABB getCollisionBoundingBox(final BlockState blockState, final LevelAccessor worldIn,
            final BlockPos pos) {
        return getBoundingBox(blockState, worldIn, pos);
    }

    public static ItemStack pickBlock(final Player player, final Item item) {
        // Compatibility issues with other mods ...
        if (!Minecraft.getInstance().gameSettings.keyBindPickBlock.isKeyDown())
            return new ItemStack(item);
        for (int k = 0; k < InventoryPlayer.getHotbarSize(); ++k) {
            if (player.inventory.getStackInSlot(k).getItem().equals(item)) {
                player.inventory.currentItem = k;
                return ItemStack.EMPTY;
            }
        }
        return new ItemStack(item);
    }

    @Override
    public ItemStack getPickBlock(final BlockState state, final RayTraceResult target,
            final Level world, final BlockPos pos, final Player player) {
        return pickBlock(player, prop.placementtool);
    }

    @Override
    public BlockState getStateForPlacement(final Level world, final BlockPos pos,
            final Direction facing, final float hitX, final float hitY, final float hitZ,
            final int meta, final EntityLivingBase placer, final EnumHand hand) {
        final int index = 15
                - (MathHelper.floor(placer.getRotationYawHead() * 16.0F / 360.0F - 0.5D) & 15);
        return getDefaultState().withProperty(ANGEL, SignalAngel.values()[index]);
    }

    @Override
    public BlockState getStateFromMeta(final int meta) {
        return getDefaultState().withProperty(ANGEL, SignalAngel.values()[meta]);
    }

    @Override
    public int getMetaFromState(final BlockState state) {
        return state.getValue(ANGEL).ordinal();
    }

    @Override
    public BlockState withRotation(final BlockState state, final Rotation rot) {
        return state.withRotation(rot);
    }

    @Override
    public BlockState withMirror(final BlockState state, final Mirror mirrorIn) {
        return state.withMirror(mirrorIn);
    }

    @Override
    public boolean canRenderInLayer(final BlockState state, final BlockRenderLayer layer) {
        return layer.equals(BlockRenderLayer.CUTOUT_MIPPED);
    }

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    @Override
    public BlockState getExtendedState(final BlockState state, final LevelAccessor world,
            final BlockPos pos) {
        final AtomicReference<IModelData> blockState = new AtomicReference<>(
                (IModelData) super.getExtendedState(state, world, pos));
        final SignalTileEnity entity = (SignalTileEnity) world.getBlockEntity(pos);
        if (entity != null)
            entity.getProperties().forEach((property, value) -> blockState.getAndUpdate(
                    oldState -> oldState.withProperty((SEProperty) (property), value)));
        return blockState.get();
    }

    @Override
    public boolean isTranslucent(final BlockState state) {
        return true;
    }

    @Override
    public boolean isOpaqueCube(final BlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(final BlockState state) {
        return false;
    }

    private SEProperty[] propcache = null;

    private void buildCacheIfNull() {
        if (propcache == null) {
            final Collection<SEProperty> props = ((ExtendedBlockState) this.getBlockState())
                    .getUnlistedProperties();
            propcache = props.toArray(new SEProperty[props.size()]);
        }
    }

    public int getIDFromProperty(final SEProperty propertyIn) {
        buildCacheIfNull();
        for (int i = 0; i < propcache.length; i++)
            if (propcache[i].equals(propertyIn))
                return i;
        return -1;
    }

    public SEProperty getPropertyFromID(final int id) {
        buildCacheIfNull();
        return propcache[id];
    }

    @SuppressWarnings("rawtypes")
    private ArrayList<SEProperty> signalProperties;

    @SuppressWarnings("rawtypes")
    @Override
    protected BlockStateContainer createBlockState() {
        this.signalProperties = new ArrayList<>();
        this.signalProperties.clear();
        if (!this.getClass().equals(Signal.class)) {
            for (final Field f : this.getClass().getDeclaredFields()) {
                final int mods = f.getModifiers();
                if (Modifier.isFinal(mods) && Modifier.isStatic(mods) && Modifier.isPublic(mods)) {
                    try {
                        this.signalProperties.add((SEProperty) f.get(null));
                    } catch (final IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        nextConsumer.accept(signalProperties);
        nextConsumer = _u -> {
        };

        this.signalProperties.add(CUSTOMNAME);
        return new ExtendedBlockState(this, new IProperty<?>[] {
                ANGEL
        }, this.signalProperties.toArray(new SEProperty[signalProperties.size()]));
    }

    @SuppressWarnings("rawtypes")
    public ImmutableList<SEProperty> getProperties() {
        return ImmutableList.copyOf(this.signalProperties);
    }

    public String getSignalTypeName() {
        return this.getRegistryName().getResourcePath();
    }

    public int getID() {
        return id;
    }

    @Override
    public void destroy(final LevelAccessor accessor, final BlockPos pos, final BlockState state) {
        super.destroy(accessor, pos, state);

        if (!level.isClientSide)
            GhostBlock.destroyUpperBlock(worldIn, pos);
    }

    @SuppressWarnings("unchecked")
    public int getHeight(final Map<SEProperty, Object> map) {
        for (final HeightProperty property : this.prop.signalHeights) {
            if (property.predicate.test(map))
                return property.height;
        }
        return this.prop.defaultHeight;
    }

    public boolean canHaveCustomname(final Map<SEProperty, Object> map) {
        return this.prop.customNameRenderHeight != -1 || !this.prop.customRenderHeights.isEmpty();
    }

    @Override
    public String toString() {
        return this.getUnlocalizedName();
    }

    public final boolean canBeLinked() {
        return this.prop.canLink;
    }

    @OnlyIn(Dist.CLIENT)
    public int colorMultiplier(final BlockState state, final LevelAccessor worldIn,
            final BlockPos pos, final int tintIndex) {
        return this.prop.colors.get(tintIndex);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean hasCostumColor() {
        return !this.prop.colors.isEmpty();
    }

    @OnlyIn(Dist.CLIENT)
    public void renderOverlay(final double x, final double y, final double z,
            final SignalTileEnity te, final FontRenderer font) {
        this.renderOverlay(x, y, z, te, font, this.prop.customNameRenderHeight);
    }

    @SuppressWarnings("unchecked")
    @OnlyIn(Dist.CLIENT)
    public void renderOverlay(final double x, final double y, final double z,
            final SignalTileEnity te, final FontRenderer font, final float renderHeight) {
        float customRenderHeight = renderHeight;
        final Map<SEProperty, Object> map = te.getProperties();
        for (final FloatProperty property : this.prop.customRenderHeights) {
            if (property.predicate.test(map)) {
                customRenderHeight = property.height;
            }
        }
        if (customRenderHeight == -1)
            return;
        final Level world = te.getLevel();
        final BlockPos pos = te.getPos();
        final BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof Signal)) {
            return;
        }
        final ITextComponent name = te.getDisplayName();
        final SignalAngel face = state.getValue(Signal.ANGEL);
        final float angel = face.getDegree();

        final String[] display = name.getFormattedText().split("\\[n\\]");

        final float scale = this.prop.signScale;

        GlStateManager.enableAlpha();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5f, y + customRenderHeight, z + 0.5f);
        GlStateManager.scale(0.015f * scale, -0.015f * scale, 0.015f * scale);
        GlStateManager.rotate(angel, 0, 1, 0);

        renderSingleOverlay(display, font, te);

        GlStateManager.popMatrix();
    }

    @OnlyIn(Dist.CLIENT)
    public void renderSingleOverlay(final String[] display, final FontRenderer font,
            final SignalTileEnity te) {
        final float width = this.prop.signWidth;
        final float offsetX = this.prop.offsetX;
        final float offsetZ = this.prop.offsetY;
        final float scale = this.prop.signScale;
        GlStateManager.pushMatrix();
        GlStateManager.translate(width / 2 + offsetX, 0, -4.2f + offsetZ);
        GlStateManager.scale(-1f, 1f, 1f);
        for (int i = 0; i < display.length; i++) {
            font.drawSplitString(display[i], 0, (int) (i * scale * 2.8f), (int) width, 0);
        }
        GlStateManager.popMatrix();
    }

    public Placementtool getPlacementtool() {
        return this.prop.placementtool;
    }

    @SuppressWarnings("rawtypes")
    private SEProperty powerProperty = null;

    @Override
    public InteractionResult use(final BlockState blockstate, final Level level,
            final BlockPos blockPos, final Player placer, final InteractionHand hand,
            final BlockHitResult blockHit) {
        final BlockEntity tile = level.getBlockEntity(blockPos);
        if (!(tile instanceof SignalTileEnity)) {
            return InteractionResult.FAIL;
        }
        final SignalTileEnity signalTile = (SignalTileEnity) tile;
        if (loadRedstoneOutput(level, blockstate, blockPos, signalTile) && level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        final boolean customname = canHaveCustomname(signalTile.getProperties());
        if (!placer.getItemInHand(hand).getItem().equals(OSItems.LINKING_TOOL)
                && (canBeLinked() || customname)) {
            OpenSignalsMain.handler.invokeGui(Signal.class, placer, level, blockPos);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    private boolean loadRedstoneOutput(final Level worldIn, final BlockState state,
            final BlockPos pos, final SignalTileEnity tile) {
        if (!this.prop.redstoneOutputs.isEmpty()) {
            final Map<SEProperty, Object> properties = tile.getProperties();
            this.powerProperty = null;
            for (final ValuePack pack : this.prop.redstoneOutputs) {
                if (pack.predicate.test(properties)) {
                    this.powerProperty = pack.property;
                    tile.getProperty(pack.property).ifPresent(power -> {
                        if (power.equals("false")) {
                            tile.setProperty(pack.property, "true");
                        } else if (power.equals("true")) {
                            tile.setProperty(pack.property, "false");
                        }

                    });
                    break;
                }
            }
            if (this.powerProperty == null) {
                return false;
            }
            worldIn.setBlock(pos, state, 3);
            worldIn.notifyNeighborsOfStateChange(pos, this, false);
            worldIn.markAndNotifyBlock(pos, null, state, state, 3, 0);
            return true;
        }
        return false;
    }

    @Override
    public boolean canProvidePower(final BlockState state) {
        return !this.prop.redstoneOutputs.isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public int getWeakPower(final BlockState blockState, final LevelAccessor blockAccess,
            final BlockPos pos, final Direction side) {
        if (this.prop.redstoneOutputs.isEmpty() || this.powerProperty == null)
            return 0;

        final SignalTileEnity tile = (SignalTileEnity) blockAccess.getBlockEntity(pos);
        if (tile.getProperty(powerProperty).filter(power -> !(Boolean) power).isPresent()) {
            return 0;
        }
        final Map<SEProperty, Object> properties = tile.getProperties();
        for (final ValuePack pack : this.prop.redstoneOutputs) {
            if (pack.predicate.test(properties)) {
                return 15;
            }
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    public void getUpdate(final Level world, final BlockPos pos) {
        if (this.prop.sounds.isEmpty())
            return;

        final SignalTileEnity tile = (SignalTileEnity) world.getBlockEntity(pos);
        final Map<SEProperty, Object> properties = tile.getProperties();
        final SoundProperty sound = getSound(properties);
        if (sound.duration < 1)
            return;

        if (sound.duration == 1) {
            world.playSound(null, pos, sound.sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else {
            if (world.isUpdateScheduled(pos, this)) {
                return;
            } else {
                if (sound.predicate.test(properties)) {
                    world.scheduleTick(pos, this, 1);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public SoundProperty getSound(final Map<SEProperty, Object> map) {
        for (final SoundProperty property : this.prop.sounds) {
            if (property.predicate.test(map)) {
                return property;
            }
        }
        return new SoundProperty();
    }

    @Override
    public void updateTick(final Level world, final BlockPos pos, final BlockState state,
            final Random rand) {
        if (this.prop.sounds.isEmpty() || !world.isClientSide) {
            return;
        }
        final SignalTileEnity tile = (SignalTileEnity) world.getBlockEntity(pos);
        final SoundProperty sound = getSound(tile.getProperties());
        if (sound.duration <= 1) {
            return;
        }
        world.playSound(null, pos, sound.sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        world.scheduleTick(pos, this, sound.duration);
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos p_153215_, final BlockState p_153216_) {
        return new SignalTileEnity();
    }
}