package com.troblecodings.signals.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.core.RenderOverlayInfo;
import com.troblecodings.signals.core.SignalAngel;
import com.troblecodings.signals.core.SignalProperties;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.items.Placementtool;
import com.troblecodings.signals.parser.ValuePack;
import com.troblecodings.signals.properties.FloatProperty;
import com.troblecodings.signals.properties.HeightProperty;
import com.troblecodings.signals.properties.SoundProperty;
import com.troblecodings.signals.tileentitys.SignalTileEntity;
import com.troblecodings.signals.utils.JsonEnum;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Signal extends BasicBlock {

    public static Consumer<List<SEProperty>> nextConsumer = _u -> {
    };

    public static final Map<String, Signal> SIGNALS = new HashMap<>();
    public static final EnumProperty<SignalAngel> ANGEL = EnumProperty.create("angel",
            SignalAngel.class);
    public static final SEProperty CUSTOMNAME = new SEProperty("customname",
            JsonEnum.PROPERTIES.get("boolean"), "false", ChangeableStage.AUTOMATICSTAGE, t -> true);
    public static final TileEntitySupplierWrapper SUPPLIER = SignalTileEntity::new;

    protected final SignalProperties prop;
    private List<SEProperty> signalProperties;
    private SEProperty powerProperty = null;

    public Signal(final SignalProperties prop) {
        super(Properties.of(Material.STONE));
        this.prop = prop;
        registerDefaultState(defaultBlockState().setValue(ANGEL, SignalAngel.ANGEL0));
        prop.placementtool.addSignal(this);
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter source, final BlockPos pos,
            final CollisionContext context) {
        final SignalTileEntity te = (SignalTileEntity) source.getBlockEntity(pos);
        if (te == null)
            return Shapes.block();
        return Shapes
                .create(Shapes.block().bounds().expandTowards(0, getHeight(te.getProperties()), 0));
    }

    @Override
    public VoxelShape getCollisionShape(final BlockState blockState, final BlockGetter worldIn,
            final BlockPos pos, final CollisionContext context) {
        return getShape(blockState, worldIn, pos, context);
    }

    public static final int HOTBAR_SLOT = 9;

    public static ItemStack pickBlock(final Player player, final Item item) {
        // Compatibility issues with other mods ...
        final Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.options.keyPickItem.isDown())
            return new ItemStack(item);
        for (int k = 0; k < HOTBAR_SLOT; ++k) {
            final ItemStack currentStack = player.inventoryMenu.getSlot(k).getItem();
            if (currentStack.getItem().equals(item)) {
                player.inventoryMenu.setItem(k, k, currentStack);
                return ItemStack.EMPTY;
            }
        }
        return new ItemStack(item);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        List<SEProperty> properties = new ArrayList<>();
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
    public void destroy(final LevelAccessor worldIn, final BlockPos pos, final BlockState state) {
        super.destroy(worldIn, pos, state);

        if (!worldIn.isClientSide())
            GhostBlock.destroyUpperBlock(worldIn, pos);
    }

    @SuppressWarnings("unchecked")
    public int getHeight(final Map<SEProperty, String> map) {
        for (final HeightProperty property : this.prop.signalHeights) {
            if (property.predicate.test(map))
                return property.height;
        }
        return this.prop.defaultHeight;
    }

    public boolean canHaveCustomname(Map<SEProperty, String> map) {
        return this.prop.customNameRenderHeight != -1 || !this.prop.customRenderHeights.isEmpty();
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
        this.renderOverlay(info, this.prop.customNameRenderHeight);
    }

    @SuppressWarnings("unchecked")
    @OnlyIn(Dist.CLIENT)
    public void renderOverlay(final RenderOverlayInfo info, final float renderHeight) {
        float customRenderHeight = renderHeight;
        final Map<SEProperty, String> map = info.tileEntity.getProperties();
        for (final FloatProperty property : this.prop.customRenderHeights) {
            if (property.predicate.test(map)) {
                customRenderHeight = property.height;
            }
        }
        if (customRenderHeight == -1)
            return;
        final Level world = info.tileEntity.getLevel();
        final BlockPos pos = info.tileEntity.getBlockPos();
        final BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof Signal)) {
            return;
        }
        final String name = info.tileEntity.getNameAsStringWrapper();
        final SignalAngel face = state.getValue(Signal.ANGEL);

        final String[] display = name.split("\\[n\\]");

        final float scale = this.prop.signScale;

        info.stack.pushPose();
        info.stack.translate(info.x + 0.5f, info.y + customRenderHeight, info.z + 0.5f);
        info.stack.scale(0.015f * scale, -0.015f * scale, 0.015f * scale);
        info.stack.mulPose(face.getQuaternion());

        renderSingleOverlay(info, display);

        info.stack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public void renderSingleOverlay(final RenderOverlayInfo info, final String[] display) {
        final float width = this.prop.signWidth;
        final float offsetX = this.prop.offsetX;
        final float offsetZ = this.prop.offsetY;
        final float scale = this.prop.signScale;
        info.stack.pushPose();
        info.stack.translate(width / 2 + offsetX, 0, -4.2f + offsetZ);
        info.stack.scale(-1f, 1f, 1f);
        for (int i = 0; i < display.length; i++) {
            info.font.draw(info.stack, display[i], (i * scale * 2.8f), width, 0);
        }
        info.stack.popPose();
    }

    public Placementtool getPlacementtool() {
        return this.prop.placementtool;
    }

    @Override
    public InteractionResult use(final BlockState blockstate, final Level level,
            final BlockPos blockPos, final Player placer, final InteractionHand hand,
            final BlockHitResult blockHit) {
        final BlockEntity tile = level.getBlockEntity(blockPos);
        if (!(tile instanceof SignalTileEntity)) {
            return InteractionResult.FAIL;
        }
        final SignalTileEntity signalTile = (SignalTileEntity) tile;
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

    @SuppressWarnings("unchecked")
    private boolean loadRedstoneOutput(final Level worldIn, final BlockState state,
            final BlockPos pos, final SignalTileEntity tile) {
        if (!this.prop.redstoneOutputs.isEmpty()) {
            final Map<SEProperty, String> properties = tile.getProperties();
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
            worldIn.updateNeighborsAt(pos, this);
            worldIn.markAndNotifyBlock(pos, null, state, state, 3, 0);
            return true;
        }
        return false;
    }

    @Override
    public boolean isSignalSource(final BlockState state) {
        return !this.prop.redstoneOutputs.isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public int getDirectSignal(final BlockState blockState, final BlockGetter blockAccess,
            final BlockPos pos, final Direction side) {
        if (this.prop.redstoneOutputs.isEmpty() || this.powerProperty == null)
            return 0;

        final SignalTileEntity tile = (SignalTileEntity) blockAccess.getBlockEntity(pos);
        if (tile.getProperty(powerProperty).filter(power -> power.equals("false")).isPresent()) {
            return 0;
        }
        final Map<SEProperty, String> properties = tile.getProperties();
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

        final SignalTileEntity tile = (SignalTileEntity) world.getBlockEntity(pos);
        final Map<SEProperty, String> properties = tile.getProperties();
        final SoundProperty sound = getSound(properties);
        if (sound.duration < 1)
            return;

        if (sound.duration == 1) {
            world.playSound(null, pos, sound.sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else {
            if (world.getBlockTicks().hasScheduledTick(pos, this)) {
                return;
            } else {
                if (sound.predicate.test(properties)) {
                    world.scheduleTick(pos, this, 1);
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

    @Override
    public void tick(final BlockState state, final ServerLevel world, final BlockPos pos,
            final Random rand) {
        if (this.prop.sounds.isEmpty() || !world.isClientSide) {
            return;
        }
        final SignalTileEntity tile = (SignalTileEntity) world.getBlockEntity(pos);
        final SoundProperty sound = getSound(tile.getProperties());
        if (sound.duration <= 1) {
            return;
        }
        world.playSound(null, pos, sound.sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        world.scheduleTick(pos, this, sound.duration);
    }

    @Override
    public Optional<TileEntitySupplierWrapper> getSupplierWrapper() {
        return Optional.of(SUPPLIER);
    }
}