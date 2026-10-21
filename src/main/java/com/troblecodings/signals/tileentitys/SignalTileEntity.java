package com.troblecodings.signals.tileentitys;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.interfaces.NamableWrapper;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.animation.SignalAnimationHandler;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.RenderOverlayInfo;
import com.troblecodings.signals.core.SignalStateListener;
import com.troblecodings.signals.core.SignalTextRenderCache;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.handler.ClientSignalStateHandler;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.models.ModelInfoWrapper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SignalTileEntity extends SyncableTileEntity implements NamableWrapper, ISyncable {

    protected final SignalAnimationHandler handler;
    private final SignalTextRenderCache textRenderCache = new SignalTextRenderCache();

    public static final String LINKED_LIST = "linkedList";

    public SignalTileEntity() {
        this.handler = new SignalAnimationHandler(this);
    }

    private final Map<SEProperty, String> properties = new HashMap<>();

    private final SignalStateListener listener = (info, states, changed) -> {
        switch (changed) {
            case ADDED_TO_CACHE: {
                properties.clear();
                properties.putAll(SignalStateHandler.getStates(info));
                break;
            }
            case REMOVED_FROM_FILE:
            case REMOVED_FROM_CACHE: {
                properties.clear();
                break;
            }
            case UPDATED: {
                properties.putAll(states);
                break;
            }
            default:
                break;
        }
    };

    @Override
    public boolean isValid(final Player player) {
        return true;
    }

    public void renderOverlay(final RenderOverlayInfo info) {
        final Signal signal = getSignal();
        if (signal == null)
            return;
        signal.renderOverlay(info.with(this));
    }

    @Override
    public String getNameWrapper() {
        final String name = super.getNameWrapper();
        final Signal signal = getSignal();
        return name == null || name.isEmpty()
                ? signal != null ? signal.getSignalTypeName() : "signal"
                : name;
    }

    @Override
    public void saveWrapper(final NBTWrapper wrapper) {
        wrapper.putList(LINKED_LIST, linkedPositions.stream().map(NBTWrapper::getBlockPosWrapper)
                .collect(Collectors.toList()));
    }

    @Override
    public void loadWrapper(final NBTWrapper wrapper) {
        linkedPositions.clear();
        wrapper.getList(LINKED_LIST).stream().map(NBTWrapper::getAsPos)
                .forEach(linkedPositions::add);
        System.out.println(pos + " - " + linkedPositions);
    }

    public void addLinkedSignalReader(final BlockPos pos) {
        if (!linkedPositions.contains(pos)) {
            linkedPositions.add(pos);
            System.out.println("Adding " + pos);
        }
    }

    public void removeLinkedSignalReader(final BlockPos pos) {
        System.out.println("Removeing " + pos);
        linkedPositions.remove(pos);
    }

    public Signal getSignal() {
        final Block block = getBlockState().getBlock();
        return block instanceof Signal ? (Signal) block : null;
    }

    public Map<SEProperty, String> getProperties() {
        return ImmutableMap.copyOf(properties);
    }

    public SignalTextRenderCache getTextRenderCache() {
        return textRenderCache;
    }

    public SignalAnimationHandler getAnimationHandler() {
        return handler;
    }

    @Override
    public @Nonnull IModelData getModelData() {
        return new ModelInfoWrapper(properties);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void requestModelDataUpdate() {
        final Map<SEProperty, String> newProperties =
                ClientSignalStateHandler.getClientStates(new StateInfo(level, worldPosition));
        final boolean wasEmpty = properties.isEmpty();
        handler.updateStates(newProperties, wasEmpty);
        this.properties.clear();
        this.properties.putAll(newProperties);
        super.requestModelDataUpdate();
    }

    @Override
    public void onLoad() {
        if (!world.isRemote) {
            final IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
            world.markBlockRangeForRenderUpdate(pos, pos);
            markDirty();
            SignalStateHandler.addListener(new SignalStateInfo(world, pos, getSignal()), listener);
            linkedPositions.forEach(pos -> loadChunkAndGetTile(SignalReaderTileEntity.class, world,
                    pos, (tile, chunk) -> {
                        tile.onLoad();
                    }));
        } else if (hasAnimation()) {
            handler.updateAnimationListFromBlock();
            handler.updateStates(
                    ClientSignalStateHandler.getClientStates(new StateInfo(level, worldPosition)),
                    true);
        }
    }

    @Override
    public void onChunkUnloaded() {
        if (!level.isClientSide) {
            SignalStateHandler.removeListener(
                    new SignalStateInfo(level, worldPosition, getSignal()), listener);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return getSignal().getRenderBox().map(box -> box.move(worldPosition))
                .orElse(super.getRenderBoundingBox());
    }
}