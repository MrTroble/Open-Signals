package com.troblecodings.signals.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.core.JsonEnum;
import com.troblecodings.signals.core.PosIdentifier;
import com.troblecodings.signals.core.RenderOverlayInfo;
import com.troblecodings.signals.core.SignalAngel;
import com.troblecodings.signals.core.SignalProperties;
import com.troblecodings.signals.core.TileEntitySupplierWrapper;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.handler.ClientSignalStateHandler;
import com.troblecodings.signals.handler.ClientSignalStateInfo;
import com.troblecodings.signals.handler.NameHandler;
import com.troblecodings.signals.handler.NameStateInfo;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.items.Placementtool;
import com.troblecodings.signals.parser.ValuePack;
import com.troblecodings.signals.properties.BooleanProperty;
import com.troblecodings.signals.properties.FloatProperty;
import com.troblecodings.signals.properties.HeightProperty;
import com.troblecodings.signals.properties.SoundProperty;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ITickList;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Signal extends BasicBlock {

    public static Consumer<List<SEProperty>> nextConsumer = _u -> {
    };

    public static final Map<String, Signal> SIGNALS = new HashMap<>();
    public static final EnumProperty<SignalAngel> ANGEL = EnumProperty.create("angel",
            SignalAngel.class);
    public static final SEProperty CUSTOMNAME = new SEProperty("customname", JsonEnum.BOOLEAN,
            "false", ChangeableStage.AUTOMATICSTAGE, t -> true, 0);
    public static final TileEntitySupplierWrapper SUPPLIER = SignalTileEntity::new;

    protected final SignalProperties prop;
    private List<SEProperty> signalProperties;
    private final Map<SEProperty, Integer> signalPropertiesToInt = new HashMap<>();
    private SEProperty powerProperty = null;

    public Signal(final SignalProperties prop) {
        super(Properties.of(Material.STONE));
        this.prop = prop;
        registerDefaultState(defaultBlockState().setValue(ANGEL, SignalAngel.ANGEL0));
        prop.placementtool.addSignal(this);
        for (int i = 0; i < signalProperties.size(); i++) {
            final SEProperty property = signalProperties.get(i);
            signalPropertiesToInt.put(property, i);
        }
    }
    
    @Override
    public int getLightEmission(final BlockState state) {
        return 1;
    }
    
    @Override
    public boolean canOcclude(final BlockState state) {
        return false;
    }

    @Override
    public boolean propagatesSkylightDown(final BlockState state, final IBlockReader getter,
            final BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(final BlockState state, final IBlockReader getter,
            final BlockPos pos) {
        return 1.0F;
    }

    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context) {
        final int angel = Integer
                .valueOf(MathHelper.floor(context.getRotation() * 16.0F / 360.0F + 0.5D) & 15);
        return defaultBlockState().setValue(ANGEL, SignalAngel.values()[angel]);
    }

    public int getIDFromProperty(final SEProperty property) {
        return this.signalPropertiesToInt.get(property);
    }

    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader source,
            final BlockPos pos, final ISelectionContext context) {
        final SignalTileEntity te = (SignalTileEntity) source.getBlockEntity(pos);
        if (te == null)
            return VoxelShapes.block();
        final World world = te.getLevel();
        final SignalStateInfo info = new SignalStateInfo(world, pos, this);
        final Map<SEProperty, String> properties = world.isClientSide
                ? ClientSignalStateHandler.getClientStates(new ClientSignalStateInfo(info))
                : SignalStateHandler.getStates(info);
        return VoxelShapes
                .create(VoxelShapes.block().bounds().expandTowards(0, getHeight(properties), 0));
    }

    @Override
    public VoxelShape getCollisionShape(final BlockState blockState, final IBlockReader worldIn,
            final BlockPos pos, final ISelectionContext context) {
        return getShape(blockState, worldIn, pos, context);
    }

    @Override
    public ItemStack getCloneItemStack(final IBlockReader getter, final BlockPos pos,
            final BlockState state) {
        return getPlacementtool().getDefaultInstance();
    }

    @Override
    protected void createBlockStateDefinition(final Builder<Block, BlockState> builder) {
        final List<SEProperty> properties = new ArrayList<>();
        nextConsumer.accept(properties);
        nextConsumer = _u -> {
        };
        properties.add(CUSTOMNAME);
        this.signalProperties = ImmutableList.copyOf(properties);
        builder.add(ANGEL);
    }

    public List<SEProperty> getProperties() {
        return this.signalProperties;
    }

    public String getSignalTypeName() {
        return this.getRegistryName().getPath();
    }

    @Override
    public void destroy(final IWorld worldIn, final BlockPos pos, final BlockState state) {
        super.destroy(worldIn, pos, state);
        GhostBlock.destroyUpperBlock(worldIn, pos);
        if (!worldIn.isClientSide() && worldIn instanceof World) {
            SignalStateHandler.setRemoved(new SignalStateInfo((World) worldIn, pos, this));
            NameHandler.setRemoved(new NameStateInfo((World) worldIn, pos));
            SignalBoxHandler.onPosRemove(new PosIdentifier(pos, (World) worldIn));
        }
    }

    @SuppressWarnings("unchecked")
    public int getHeight(final Map<SEProperty, String> map) {
        for (final HeightProperty property : this.prop.signalHeights) {
            if (property.predicate.test(map))
                return property.height;
        }
        return this.prop.defaultHeight;
    }

    public boolean canHaveCustomname(final Map<SEProperty, String> map) {
        return this.prop.customNameRenderHeight != -1 || !this.prop.customRenderHeights.isEmpty();
    }

    @Override
    public Optional<String> getSupplierWrapperName() {
        return Optional.of("signal");
    }

    @Override
    public String toString() {
        return this.getDescriptionId();
    }

    public final boolean canBeLinked() {
        return this.prop.canLink;
    }

    @OnlyIn(Dist.CLIENT)
    public int colorMultiplier(final int tintIndex) {
        return this.prop.colors.get(tintIndex);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean hasCostumColor() {
        return !this.prop.colors.isEmpty();
    }

    @OnlyIn(Dist.CLIENT)
    public void renderOverlay(final RenderOverlayInfo info) {
        if (this.prop.autoscale) {
            renderScaleOverlay(info, this.prop.customNameRenderHeight);
            return;
        }
        this.renderOverlay(info, this.prop.customNameRenderHeight);
    }

    @SuppressWarnings("unchecked")
    @OnlyIn(Dist.CLIENT)
    public void renderScaleOverlay(final RenderOverlayInfo info, final float renderHeight) {
        float customRenderHeight = renderHeight;
        final Map<SEProperty, String> map = ClientSignalStateHandler
                .getClientStates(new ClientSignalStateInfo(info.tileEntity.getLevel(),
                        info.tileEntity.getBlockPos()));
        final String customNameState = map.get(CUSTOMNAME);
        if (customNameState == null || customNameState.equalsIgnoreCase("FALSE"))
            return;
        for (final FloatProperty property : this.prop.customRenderHeights) {
            if (property.predicate.test(map)) {
                customRenderHeight = property.height;
            }
            if (customRenderHeight == -1)
                return;
        }
        final World world = info.tileEntity.getLevel();
        final BlockPos pos = info.tileEntity.getBlockPos();
        final BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof Signal)) {
            return;
        }
        final String name = info.tileEntity.getNameWrapper();
        final SignalAngel face = state.getValue(Signal.ANGEL);

        final String[] display = name.split("\\[n\\]");

        final float width = info.font.width(name);
        final float scale = Math.min(1 / (22 * (width / 56)), 0.1f);

        GlStateManager.pushMatrix();
        GlStateManager.translated(info.x + 0.5f, info.y + 0.75f, info.z + 0.5f);
        GlStateManager.rotatef(face.getDregree(), 1, 1, 1); // TODO 0, 1, 0
        GlStateManager.scalef(-scale, -scale, 1);
        GlStateManager.translatef(-1.3f / scale, 0, -0.32f);

        int k = 0;
        for (int i = 0; i < display.length; i++) {
            final List<String> splittedList = info.font
                    .split(display[i], (int) this.prop.signWidth);
            for (int j = 0; j < splittedList.size(); j++) {
                info.font.draw(splittedList.get(j), 0, (k * 10), this.prop.textColor);
                k++;
            }
        }
        GlStateManager.popMatrix();
    }

    @SuppressWarnings("unchecked")
    @OnlyIn(Dist.CLIENT)
    public void renderOverlay(final RenderOverlayInfo info, final float renderHeight) {
        float customRenderHeight = renderHeight;
        boolean doubleSidedText = false;
        final Map<SEProperty, String> map = ClientSignalStateHandler
                .getClientStates(new ClientSignalStateInfo(info.tileEntity.getLevel(),
                        info.tileEntity.getBlockPos()));
        final String customNameState = map.get(CUSTOMNAME);
        if (customNameState == null || customNameState.equalsIgnoreCase("FALSE"))
            return;
        for (final FloatProperty property : this.prop.customRenderHeights) {
            if (property.predicate.test(map)) {
                customRenderHeight = property.height;
            }
        }
        for (final BooleanProperty boolProp : this.prop.doubleSidedText) {
            if (boolProp.predicate.test(map)) {
                doubleSidedText = boolProp.doubleSided;
            }
        }
        if (customRenderHeight == -1)
            return;
        final World world = info.tileEntity.getLevel();
        final BlockPos pos = info.tileEntity.getBlockPos();
        final BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof Signal)) {
            return;
        }
        final String name = info.tileEntity.getNameWrapper();
        final SignalAngel face = state.getValue(Signal.ANGEL);

        final String[] display = name.split("\\[n\\]");

        final float scale = this.prop.signScale;

        GlStateManager.pushMatrix();
        GlStateManager.translated(info.x + 0.5f, info.y + customRenderHeight, info.z + 0.5f);
        GlStateManager.rotatef(face.getDregree(), 1, 1, 1); // TODO 0, 1, 0
        GlStateManager.scalef(0.015f * scale, -0.015f * scale, 0.015f * scale);

        renderSingleOverlay(info, display);

        if (doubleSidedText) {
                    // UIRotate.fromXYZ(0, (float) (-face.getRadians() + Math.PI), 0));
            GlStateManager.rotated(-face.getRadians() + Math.PI, 0, 1, 0);
            GlStateManager.rotatef(face.getDregree(), 1, 1, 1); // TODO 0, 1, 0
            GlStateManager.translated(info.x - 0.5f, info.y + customRenderHeight - 2, info.z - 0.5f);
            renderSingleOverlay(info, display);
        }

        GlStateManager.popMatrix();
    }

    @OnlyIn(Dist.CLIENT)
    public void renderSingleOverlay(final RenderOverlayInfo info, final String[] display) {
        final float width = this.prop.signWidth;
        final float offsetX = this.prop.offsetX;
        final float offsetZ = this.prop.offsetY;
        GlStateManager.pushMatrix();
        GlStateManager.translatef(width / 2 + offsetX, 0, -4.2f + offsetZ);
        GlStateManager.scalef(-1f, 1f, 1f);

        int k = 0;
        for (int i = 0; i < display.length; i++) {
            final List<String> splittedList = info.font
                    .split(display[i], (int) width);
            for (int j = 0; j < splittedList.size(); j++) {
                info.font.draw(splittedList.get(j), 0, (k * 10), this.prop.textColor);
                k++;
            }
        }
        GlStateManager.popMatrix();
    }

    public Placementtool getPlacementtool() {
        return this.prop.placementtool;
    }

    public int getDefaultDamage() {
        return this.prop.defaultItemDamage;
    }

    @Override
    public boolean use(final BlockState state, final World world, final BlockPos pos,
            final PlayerEntity player, final Hand hand, final BlockRayTraceResult result) {
        if (!(state.getBlock() instanceof Signal)) {
            return false;
        }
        final SignalStateInfo stateInfo = new SignalStateInfo(world, pos, this);
        if (loadRedstoneOutput(world, stateInfo)) {
            world.blockUpdated(pos, state.getBlock());
            return true;
        }
        final boolean customname = canHaveCustomname(SignalStateHandler.getStates(stateInfo));
        if (!player.getItemInHand(Hand.MAIN_HAND).getItem().equals(OSItems.LINKING_TOOL)
                && (canBeLinked() || customname)) {
            OpenSignalsMain.handler.invokeGui(Signal.class, player, world, pos, "signal");
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean loadRedstoneOutput(final World worldIn, final SignalStateInfo info) {
        if (!this.prop.redstoneOutputs.isEmpty()) {
            final Map<SEProperty, String> properties = SignalStateHandler.getStates(info);
            this.powerProperty = null;
            for (final ValuePack pack : this.prop.redstoneOutputs) {
                if (pack.predicate.test(properties)) {
                    this.powerProperty = pack.property;
                    SignalStateHandler.getState(info, pack.property).ifPresent(power -> {
                        SignalStateHandler.setState(info, pack.property,
                                Boolean.toString(!Boolean.valueOf(power)));
                    });
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isSignalSource(final BlockState state) {
        return !this.prop.redstoneOutputs.isEmpty();
    }

    @Override
    public int getSignal(final BlockState state, final IBlockReader getter, final BlockPos pos,
            final Direction direction) {
        return getDirectSignal(state, getter, pos, direction);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int getDirectSignal(final BlockState blockState, final IBlockReader blockAccess,
            final BlockPos pos, final Direction side) {
        if (this.prop.redstoneOutputs.isEmpty() || this.powerProperty == null
                || !(blockAccess instanceof World)) {
            return 0;
        }
        final SignalStateInfo stateInfo = new SignalStateInfo((World) blockAccess, pos, this);
        if (SignalStateHandler.getState(stateInfo, powerProperty)
                .filter(power -> power.equalsIgnoreCase("false")).isPresent()) {
            return 0;
        }
        final Map<SEProperty, String> properties = SignalStateHandler.getStates(stateInfo);
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

        final SignalStateInfo stateInfo = new SignalStateInfo(world, pos, this);
        final Map<SEProperty, String> properties = SignalStateHandler.getStates(stateInfo);
        final SoundProperty sound = getSound(properties);
        if (sound.duration < 1)
            return;

        if (sound.duration == 1) {
            world.playSound(null, pos, sound.sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
        } else {
            if (world.getBlockTicks().hasScheduledTick(pos, this)) {
                return;
            } else {
                if (sound.predicate.test(properties)) {
                    ((ITickList<Block>) world).scheduleTick(pos, this, 1);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public SoundProperty getSound(final Map<SEProperty, String> map) {
        for (final SoundProperty property : this.prop.sounds) {
            if (property.predicate.test(map)) {
                return property;
            }
        }
        return new SoundProperty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void tick(final BlockState state, final World world, final BlockPos pos,
            final Random rand) {
        if (this.prop.sounds.isEmpty() || world.isClientSide) {
            return;
        }
        final SignalStateInfo stateInfo = new SignalStateInfo(world, pos, this);
        final SoundProperty sound = getSound(SignalStateHandler.getStates(stateInfo));
        if (sound.duration <= 1) {
            return;
        }
        world.playSound(null, pos, sound.sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
        ((ITickList<Block>) world).scheduleTick(pos, this, sound.duration);
    }

    @Override
    public Optional<TileEntitySupplierWrapper> getSupplierWrapper() {
        return Optional.of(SUPPLIER);
    }
}