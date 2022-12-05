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
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.troblecodings.signals.OpenSignalsConfig;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.contentpacks.ContentPackException;
import com.troblecodings.signals.contentpacks.SoundPropertyParser;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.init.OSSounds;
import com.troblecodings.signals.items.Placementtool;
import com.troblecodings.signals.models.parser.FunctionParsingInfo;
import com.troblecodings.signals.models.parser.LogicParser;
import com.troblecodings.signals.models.parser.LogicalParserException;
import com.troblecodings.signals.models.parser.ValuePack;
import com.troblecodings.signals.signalbox.config.ISignalAutoconfig;
import com.troblecodings.signals.tileentitys.SignalTileEnity;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Signal extends Block implements ITileEntityProvider, IConfigUpdatable {

    public static enum SignalAngel implements IStringSerializable {

        ANGEL0, ANGEL22P5, ANGEL45, ANGEL67P5, ANGEL90, ANGEL112P5, ANGEL135, ANGEL157P5, ANGEL180,
        ANGEL202P5, ANGEL225, ANGEL247P5, ANGEL270, ANGEL292P5, ANGEL315, ANGEL337P5;

        @Override
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
        public final ISignalAutoconfig config;
        public final List<SoundProperty> sounds;
        public final List<ValuePack> redstoneOutputs;

        public SignalProperties(final Placementtool placementtool,
                final float customNameRenderHeight, final int height,
                final List<HeightProperty> signalHeights, final float signWidth,
                final float offsetX, final float offsetY, final float signScale,
                final boolean canLink, final ISignalAutoconfig config, final List<Integer> colors,
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
            this.config = config;
            this.signalHeights = signalHeights;
            this.customRenderHeights = renderheights;
            this.sounds = sounds;
            this.redstoneOutputs = redstoneOutputs;
        }

    }

    public static class SignalPropertiesBuilder {

        private transient Placementtool placementtool = null;
        private String placementToolName = null;
        private int defaultHeight = 1;
        private Map<String, Integer> signalHeights;
        private float customNameRenderHeight = -1;
        private Map<String, Float> renderHeights;
        private float signWidth = 22;
        private float offsetX = 0;
        private float offsetY = 0;
        private float signScale = 1;
        private boolean canLink = true;
        private transient ISignalAutoconfig config = null;
        private List<Integer> colors;
        private Map<String, SoundPropertyParser> sounds;
        private Map<String, String> redstoneOutputs;

        public SignalPropertiesBuilder() {
        }

        public SignalPropertiesBuilder(final Placementtool placementtool,
                final String signalTypeName) {
            this.placementtool = placementtool;
        }

        public SignalProperties build() {
            return this.build(null);
        }

        @SuppressWarnings("rawtypes")
        public SignalProperties build(final @Nullable FunctionParsingInfo info) {
            if (placementToolName != null) {
                OSItems.registeredItems.forEach(item -> {
                    if (item instanceof Placementtool) {
                        if (item.getRegistryName().toString()
                                .replace(OpenSignalsMain.MODID + ":", "")
                                .equalsIgnoreCase(placementToolName)) {
                            placementtool = (Placementtool) item;
                            return;
                        }
                    }
                });
                if (placementtool == null)
                    OpenSignalsMain.getLogger()
                            .error("There doesn't exists a placementtool with the name '"
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
                            FMLCommonHandler.instance().exitJava(-1, false);
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
                            FMLCommonHandler.instance().exitJava(-1, false);
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
                        FMLCommonHandler.instance().exitJava(-1, false);
                    }
                }
            }

            final List<ValuePack> rsOutputs = new ArrayList<>();
            if (redstoneOutputs != null) {
                for (final Map.Entry<String, String> outputs : redstoneOutputs.entrySet()) {
                    final SEProperty property = (SEProperty) info.getProperty(outputs.getValue());
                    if (!property.getParent().getClass().equals(PropertyBool.class)) {
                        throw new ContentPackException("The proprty " + outputs.getValue()
                                + " needs to be an bool property to us it with an RS output but it wasn't!");
                    }
                    rsOutputs.add(
                            new ValuePack(property, LogicParser.predicate(outputs.getKey(), info)));
                }
            }

            this.colors = this.colors == null ? new ArrayList<>() : this.colors;

            return new SignalProperties(placementtool, customNameRenderHeight, defaultHeight,
                    ImmutableList.copyOf(signalheights), signWidth, offsetX, offsetY, signScale,
                    canLink, config, colors, ImmutableList.copyOf(renderheights),
                    ImmutableList.copyOf(soundProperties), ImmutableList.copyOf(rsOutputs));
        }

        public SignalPropertiesBuilder placementtoolname(final String placementToolName) {
            this.placementToolName = placementToolName;
            return this;
        }

        public SignalPropertiesBuilder signWidth(final float signWidth) {
            this.signWidth = signWidth;
            return this;
        }

        public SignalPropertiesBuilder offsetX(final float offsetX) {
            this.offsetX = offsetX;
            return this;
        }

        public SignalPropertiesBuilder offsetY(final float offsetY) {
            this.offsetY = offsetY;
            return this;
        }

        public SignalPropertiesBuilder signScale(final float signScale) {
            this.signScale = signScale;
            return this;
        }

        public SignalPropertiesBuilder height(final int height) {
            this.defaultHeight = height;
            return this;
        }

        public SignalPropertiesBuilder signHeight(final float customNameRenderHeight) {
            this.customNameRenderHeight = customNameRenderHeight;
            return this;
        }

        public SignalPropertiesBuilder noLink() {
            this.canLink = false;
            return this;
        }

        public SignalPropertiesBuilder config(final ISignalAutoconfig config) {
            this.config = config;
            return this;
        }
    }

    public static final SignalPropertiesBuilder builder(final Placementtool placementtool,
            final String signalTypeName) {
        return new SignalPropertiesBuilder(placementtool, signalTypeName);
    }

    public static final ArrayList<Signal> SIGNALLIST = new ArrayList<Signal>();

    public static final Map<String, Signal> SIGNALS = new HashMap<>();

    public static final PropertyEnum<SignalAngel> ANGEL = PropertyEnum.create("angel",
            SignalAngel.class);
    public static final SEProperty<Boolean> CUSTOMNAME = SEProperty.of("customname", false,
            ChangeableStage.AUTOMATICSTAGE);

    private final int id;
    protected final SignalProperties prop;

    @SuppressWarnings("rawtypes")
    public static Consumer<List<IUnlistedProperty>> nextConsumer = _u -> {
    };

    public Signal(final SignalProperties prop) {
        super(Material.ROCK);
        this.prop = prop;
        setDefaultState(getDefaultState().withProperty(ANGEL, SignalAngel.ANGEL0));
        id = SIGNALLIST.size();
        SIGNALLIST.add(this);
        prop.placementtool.addSignal(this);
    }

    @Override
    public void dropBlockAsItemWithChance(final World worldIn, final BlockPos pos,
            final IBlockState state, final float chance, final int fortune) {
    }

    @Override
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source,
            final BlockPos pos) {
        final SignalTileEnity te = (SignalTileEnity) source.getTileEntity(pos);
        if (te == null)
            return FULL_BLOCK_AABB;
        return FULL_BLOCK_AABB.expand(0, getHeight(te.getProperties()), 0);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(final IBlockState blockState,
            final IBlockAccess worldIn, final BlockPos pos) {
        return getBoundingBox(blockState, worldIn, pos);
    }

    public static ItemStack pickBlock(final EntityPlayer player, final Item item) {
        // Compatibility issues with other mods ...
        if (!Minecraft.getMinecraft().gameSettings.keyBindPickBlock.isKeyDown())
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
    public ItemStack getPickBlock(final IBlockState state, final RayTraceResult target,
            final World world, final BlockPos pos, final EntityPlayer player) {
        return pickBlock(player, prop.placementtool);
    }

    @Override
    public IBlockState getStateForPlacement(final World world, final BlockPos pos,
            final EnumFacing facing, final float hitX, final float hitY, final float hitZ,
            final int meta, final EntityLivingBase placer, final EnumHand hand) {
        final int index = 15
                - (MathHelper.floor(placer.getRotationYawHead() * 16.0F / 360.0F - 0.5D) & 15);
        return getDefaultState().withProperty(ANGEL, SignalAngel.values()[index]);
    }

    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return getDefaultState().withProperty(ANGEL, SignalAngel.values()[meta]);
    }

    @Override
    public int getMetaFromState(final IBlockState state) {
        return state.getValue(ANGEL).ordinal();
    }

    @Override
    public IBlockState withRotation(final IBlockState state, final Rotation rot) {
        return state.withRotation(rot);
    }

    @Override
    public IBlockState withMirror(final IBlockState state, final Mirror mirrorIn) {
        return state.withMirror(mirrorIn);
    }

    @Override
    public boolean canRenderInLayer(final IBlockState state, final BlockRenderLayer layer) {
        return layer.equals(BlockRenderLayer.CUTOUT_MIPPED);
    }

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    @Override
    public IBlockState getExtendedState(final IBlockState state, final IBlockAccess world,
            final BlockPos pos) {
        final AtomicReference<IExtendedBlockState> blockState = new AtomicReference<>(
                (IExtendedBlockState) super.getExtendedState(state, world, pos));
        final SignalTileEnity entity = (SignalTileEnity) world.getTileEntity(pos);
        if (entity != null)
            entity.getProperties().forEach((property, value) -> blockState.getAndUpdate(
                    oldState -> oldState.withProperty((SEProperty) (property), value)));
        return blockState.get();
    }

    @Override
    public boolean isTranslucent(final IBlockState state) {
        return true;
    }

    @Override
    public boolean isOpaqueCube(final IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(final IBlockState state) {
        return false;
    }

    private IUnlistedProperty<?>[] propcache = null;

    private void buildCacheIfNull() {
        if (propcache == null) {
            final Collection<IUnlistedProperty<?>> props = ((ExtendedBlockState) this
                    .getBlockState()).getUnlistedProperties();
            propcache = props.toArray(new IUnlistedProperty[props.size()]);
        }
    }

    public int getIDFromProperty(final IUnlistedProperty<?> propertyIn) {
        buildCacheIfNull();
        for (int i = 0; i < propcache.length; i++)
            if (propcache[i].equals(propertyIn))
                return i;
        return -1;
    }

    public IUnlistedProperty<?> getPropertyFromID(final int id) {
        buildCacheIfNull();
        return propcache[id];
    }

    @SuppressWarnings("rawtypes")
    private ArrayList<IUnlistedProperty> signalProperties;

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
                        this.signalProperties.add((IUnlistedProperty) f.get(null));
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
        }, this.signalProperties.toArray(new IUnlistedProperty[signalProperties.size()]));
    }

    @SuppressWarnings("rawtypes")
    public ImmutableList<IUnlistedProperty> getProperties() {
        return ImmutableList.copyOf(this.signalProperties);
    }

    @Override
    public boolean eventReceived(final IBlockState state, final World worldIn, final BlockPos pos,
            final int id, final int param) {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta) {
        return new SignalTileEnity();
    }

    public String getSignalTypeName() {
        return this.getRegistryName().getResourcePath();
    }

    public int getID() {
        return id;
    }

    @Override
    public void breakBlock(final World worldIn, final BlockPos pos, final IBlockState state) {
        super.breakBlock(worldIn, pos, state);

        if (!worldIn.isRemote)
            GhostBlock.destroyUpperBlock(worldIn, pos);
    }

    @SuppressWarnings("unchecked")
    public int getHeight(final Map<SEProperty<?>, Object> map) {
        for (final HeightProperty property : this.prop.signalHeights) {
            if (property.predicate.test(map))
                return property.height;
        }
        return this.prop.defaultHeight;
    }

    public boolean canHaveCustomname(final Map<SEProperty<?>, Object> map) {
        return this.prop.customNameRenderHeight != -1;
    }

    @Override
    public String toString() {
        return this.getUnlocalizedName();
    }

    public final boolean canBeLinked() {
        return this.prop.canLink;
    }

    @SideOnly(Side.CLIENT)
    public int colorMultiplier(final IBlockState state, final IBlockAccess worldIn,
            final BlockPos pos, final int tintIndex) {
        return this.prop.colors.get(tintIndex);
    }

    @SideOnly(Side.CLIENT)
    public boolean hasCostumColor() {
        return !this.prop.colors.isEmpty();
    }

    @SideOnly(Side.CLIENT)
    public void renderOverlay(final double x, final double y, final double z,
            final SignalTileEnity te, final FontRenderer font) {
        this.renderOverlay(x, y, z, te, font, this.prop.customNameRenderHeight);
    }

    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    public void renderOverlay(final double x, final double y, final double z,
            final SignalTileEnity te, final FontRenderer font, final float renderHeight) {
        float customRenderHeight = renderHeight;
        final Map<SEProperty<?>, Object> map = te.getProperties();
        for (final FloatProperty property : this.prop.customRenderHeights) {
            if (property.predicate.test(map)) {
                customRenderHeight = property.height;
            }
        }
        if (customRenderHeight == -1)
            return;
        final World world = te.getWorld();
        final BlockPos pos = te.getPos();
        final IBlockState state = world.getBlockState(pos);
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

    @SideOnly(Side.CLIENT)
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

    @Override
    public void updateConfigValues() {
        setLightLevel(OpenSignalsConfig.signalLightValue / 15.0f);
    }

    public static <T extends Comparable<T>> Predicate<Map<SEProperty<?>, Object>> check(
            final SEProperty<T> property, final T type) {
        return map -> map.containsKey(property) ? map.get(property).equals(type) : true;
    }

    public ISignalAutoconfig getConfig() {
        return this.prop.config;
    }

    @SuppressWarnings("rawtypes")
    private SEProperty powerProperty = null;

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    @Override
    public boolean onBlockActivated(final World worldIn, final BlockPos pos,
            final IBlockState state, final EntityPlayer playerIn, final EnumHand hand,
            final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        final TileEntity tile = worldIn.getTileEntity(pos);
        boolean customname = false;
        if (tile instanceof SignalTileEnity) {
            final SignalTileEnity signaltile = (SignalTileEnity) tile;
            customname = canHaveCustomname(signaltile.getProperties());
        }
        if (!playerIn.getHeldItemMainhand().getItem().equals(OSItems.LINKING_TOOL)
                && (canBeLinked() || customname)) {
            OpenSignalsMain.handler.invokeGui(Signal.class, playerIn, worldIn, pos);
            return true;
        }
        if (!this.prop.redstoneOutputs.isEmpty()) {
            if (worldIn.isRemote) {
                return true;
            }
            final SignalTileEnity signalTE = (SignalTileEnity) tile;
            final Map<SEProperty<?>, Object> properties = signalTE.getProperties();
            for (final ValuePack pack : this.prop.redstoneOutputs) {
                if (pack.predicate.test(properties)) {
                    final SEProperty seProperty = (SEProperty) pack.property;
                    this.powerProperty = seProperty;
                    signalTE.getProperty(seProperty)
                            .ifPresent(power -> signalTE.setProperty(seProperty, !(Boolean) power));
                    break;
                }
            }
            worldIn.setBlockState(pos, state, 3);
            worldIn.notifyNeighborsOfStateChange(pos, this, false);
            worldIn.markAndNotifyBlock(pos, null, state, state, 3);
            return true;
        }
        return false;
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return !this.prop.redstoneOutputs.isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos,
            EnumFacing side) {
        if (this.prop.redstoneOutputs.isEmpty() || this.powerProperty == null)
            return 0;

        final SignalTileEnity tile = (SignalTileEnity) blockAccess.getTileEntity(pos);
        if (tile.getProperty(powerProperty).filter(power -> !(Boolean) power).isPresent()) {
            return 0;
        }
        final Map<SEProperty<?>, Object> properties = tile.getProperties();
        for (final ValuePack pack : this.prop.redstoneOutputs) {
            if (pack.predicate.test(properties)) {
                return 15;
            }
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    public void getUpdate(final World world, final BlockPos pos) {
        if (this.prop.sounds.isEmpty())
            return;

        final SignalTileEnity tile = (SignalTileEnity) world.getTileEntity(pos);
        final Map<SEProperty<?>, Object> properties = tile.getProperties();
        final SoundProperty sound = getSound(properties);
        if (sound.duration < 1)
            return;

        if (sound.duration == 1) {
            world.playSound(null, pos, sound.sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
        } else {
            if (world.isUpdateScheduled(pos, this)) {
                return;
            } else {
                if (sound.predicate.test(properties)) {
                    world.scheduleUpdate(pos, this, 1);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public SoundProperty getSound(final Map<SEProperty<?>, Object> map) {
        for (final SoundProperty property : this.prop.sounds) {
            if (property.predicate.test(map)) {
                return property;
            }
        }
        return new SoundProperty();
    }

    @Override
    public void updateTick(final World world, final BlockPos pos, final IBlockState state,
            final Random rand) {
        if (this.prop.sounds.isEmpty() || world.isRemote) {
            return;
        }
        final SignalTileEnity tile = (SignalTileEnity) world.getTileEntity(pos);
        final SoundProperty sound = getSound(tile.getProperties());
        if (sound.duration <= 1) {
            return;
        }
        world.playSound(null, pos, sound.sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
        world.scheduleUpdate(pos, this, sound.duration);
    }
}