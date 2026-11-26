package com.troblecodings.signals.tileentitys;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.interfaces.NamableWrapper;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.animation.SignalAnimationHandler;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.config.ConfigHandler;
import com.troblecodings.signals.core.RenderOverlayInfo;
import com.troblecodings.signals.core.SignalStateListener;
import com.troblecodings.signals.core.SignalTextRenderCache;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.enums.ChangedState;
import com.troblecodings.signals.handler.ClientSignalStateHandler;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SignalTileEntity extends SyncableTileEntity implements NamableWrapper, ISyncable {

    protected final SignalAnimationHandler handler;
    private final int renderDistance;

    private final Map<SEProperty, String> properties = new HashMap<>();
    private final SignalTextRenderCache textRenderCache = new SignalTextRenderCache();

    public static final String LINKED_LIST = "linkedList";

    public SignalTileEntity() {
        this.handler = new SignalAnimationHandler(this);
        renderDistance = ConfigHandler.renderDistance * ConfigHandler.renderDistance;
    }

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
    public boolean isValid(final EntityPlayer player) {
        return true;
    }

    public void renderOverlay(final RenderOverlayInfo info) {
        final Signal signal = getSignal();
        if (signal == null)
            return;
        signal.renderOverlay(info.with(this));
    }

    public SignalTextRenderCache getTextRenderCache() {
        return textRenderCache;
    }

    public SignalAnimationHandler getAnimationHandler() {
        return handler;
    }

    @SideOnly(Side.CLIENT)
    public void updateAnimationState(final Map<SEProperty, String> properties,
            final ChangedState state) {
        if (hasAnimation()) {
            handler.updateStates(properties, state.equals(ChangedState.ADDED_TO_CACHE)
                    || state.equals(ChangedState.ADDED_TO_FILE));
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return getSignal().getRenderBox().map(box -> box.offset(pos))
                .orElse(super.getRenderBoundingBox());
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
    }

    public void addLinkedSignalReader(final BlockPos pos) {
        if (!linkedPositions.contains(pos))
            linkedPositions.add(pos);
    }

    public void removeLinkedSignalReader(final BlockPos pos) {
        linkedPositions.remove(pos);
    }

    public Signal getSignal() {
        final Block block = getBlockType();
        return block instanceof Signal ? (Signal) block : null;
    }

    public boolean hasAnimation() {
        final Signal signal = getSignal();
        if (signal == null)
            return false;
        return signal.hasAnimation();
    }

    public Map<SEProperty, String> getProperties() {
        return ImmutableMap.copyOf(properties);
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
                    pos, (tile, chunk) -> tile.onLoad()));
        } else if (hasAnimation()) {
            handler.updateAnimationListFromBlock();
            handler.updateStates(
                    ClientSignalStateHandler.getClientStates(new StateInfo(world, pos)), true);

        }
    }

    @Override
    public void onChunkUnload() {
        if (!world.isRemote) {
            SignalStateHandler.removeListener(new SignalStateInfo(world, pos, getSignal()),
                    listener);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public double getMaxRenderDistanceSquared() {
        if (hasAnimation())
            return renderDistance;
        return super.getMaxRenderDistanceSquared();
    }

}