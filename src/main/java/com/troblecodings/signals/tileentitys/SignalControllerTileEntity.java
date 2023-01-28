package com.troblecodings.signals.tileentitys;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.linkableapi.ILinkableTile;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.enums.EnumState;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class SignalControllerTileEntity extends SyncableTileEntity
        implements ISyncable, ILinkableTile {

    private BlockPos linkedSignalPosition = null;
    private NBTWrapper compound = new NBTWrapper();
    private final HashMap<Direction, Map<EnumState, String>> statesEnabled //
            = new HashMap<Direction, Map<EnumState, String>>();
    private final boolean[] currentStates = new boolean[Direction.values().length];

    public static final String BLOCK_POS_ID = "blockpos";
    public static final String GUI_TAG = "guitag";

    public SignalControllerTileEntity(final TileEntityInfo info) {
        super(info);
    }

    @Override
    public void saveWrapper(NBTWrapper wrapper) {
        wrapper.putWrapper(GUI_TAG, compound);
        if (linkedSignalPosition != null) {
            wrapper.putBlockPos(BLOCK_POS_ID, linkedSignalPosition);
        }
    }

    @Override
    public void loadWrapper(NBTWrapper wrapper) {
        if (wrapper.contains(BLOCK_POS_ID)) {
            linkedSignalPosition = wrapper.getBlockPos(BLOCK_POS_ID);
        }
        compound = wrapper.getWrapper(GUI_TAG);
        this.updateRSProfiles();
        if (level != null && level.isClientSide && linkedSignalPosition != null)
            onLink();
    }

    public void onLink() {
    }

    @Override
    public void onLoad() {
        if (linkedSignalPosition != null) {
            onLink();
            syncClient();
        }
    }

    public BlockPos getLinkedPosition() {
        return linkedSignalPosition;
    }

    @Override
    public boolean hasLink() {
        if (linkedSignalPosition == null)
            return false;
        if (!level.isClientSide)
            unlink();
        return false;
    }

    @Override
    public boolean link(final BlockPos pos) {
        final BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof Signal) {
            this.linkedSignalPosition = pos;
            onLink();
            return true;
        }
        return false;
    }

    @Override
    public boolean unlink() {
        linkedSignalPosition = null;
        return true;
    }

    private void changeProfile(final String onProfile, final Direction face,
            final EnumState state) {
        if (compound.contains(onProfile)) {
            if (!this.statesEnabled.containsKey(face))
                this.statesEnabled.put(face, Maps.newHashMap());
            final int value = compound.getInteger(onProfile);
            final Map<EnumState, String> faceMap = this.statesEnabled.get(face);
            if (value < 0) {
                faceMap.remove(state);
            } else {
                faceMap.put(state, "p" + value);
            }
        }
    }

    private void updateRSProfiles() {
        this.statesEnabled.clear();
        for (final Direction face : Direction.values()) {
            final String offProfile = "profileOff." + face.getName();
            final String onProfile = "profileOn." + face.getName();
            changeProfile(onProfile, face, EnumState.ONSTATE);
            changeProfile(offProfile, face, EnumState.OFFSTATE);
        }
    }

    public void redstoneUpdate() {
        if (compound == null)
            return;
        for (final Direction face : Direction.values()) {
            if (!this.statesEnabled.containsKey(face))
                continue;
            final boolean state = this.level.hasSignal(worldPosition.relative(face), face);
            final boolean old = this.currentStates[face.ordinal()];
            if (state == old)
                continue;
            this.currentStates[face.ordinal()] = state;
            final EnumState currenState = state ? EnumState.ONSTATE : EnumState.OFFSTATE;
            final String profile = this.statesEnabled.get(face).get(currenState);
            if (profile == null)
                continue;
        }
    }

    @Override
    public boolean isValid(final Player player) {
        return true;
    }

}
