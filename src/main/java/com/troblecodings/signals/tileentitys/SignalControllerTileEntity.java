package com.troblecodings.signals.tileentitys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.linkableapi.ILinkableTile;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.RedstoneInput;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.SignalStateListener;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.enums.EnumMode;
import com.troblecodings.signals.enums.EnumState;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.init.OSItems;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.server.ServerWorld;

public class SignalControllerTileEntity extends SyncableTileEntity
        implements ISyncable, ILinkableTile {

    private BlockPos linkedSignalPosition = null;
    private Signal linkedSignal = null;
    private int lastProfile = 0;
    private NBTWrapper copy;
    private EnumMode lastState;
    private BlockPos linkedRSInput = null;
    private Byte profileRSInput = -1;
    private final boolean[] currentStates = new boolean[Direction.values().length];
    private final Map<Byte, Map<SEProperty, String>> allStates = new HashMap<>();
    private final Map<Direction, Map<EnumState, Byte>> enabledStates = new HashMap<>();
    private final SignalStateListener listener = (_u, removed) -> {
        if (removed) {
            linkedSignalPosition = null;
            linkedSignal = null;
            allStates.clear();
            enabledStates.clear();
        }
    };

    public static final String SIGNAL_NAME = "signalname";
    private static final String BLOCK_POS_ID = "blockpos";
    private static final String PROFILE = "profile";
    private static final String PROPERITES = "properties";
    private static final String ALLSTATES = "allstates";
    private static final String LAST_PROFILE = "lastprofile";
    private static final String ENUM_MODE = "enummode";
    private static final String LINKED_RS_INPUT = "linkedrsinput";
    private static final String RS_INPUT_PROFILE = "rsinputprofile";

    public SignalControllerTileEntity(final TileEntityInfo info) {
        super(info);
    }

    public void setLastMode(final EnumMode state) {
        lastState = state;
    }

    public EnumMode getLastMode() {
        if (lastState == null)
            return EnumMode.MANUELL;
        return lastState;
    }

    public int getProfile() {
        return lastProfile;
    }

    public void setProfile(final int profile) {
        lastProfile = profile;
    }

    public void removePropertyFromProfile(final Byte profile, final SEProperty property) {
        final Map<SEProperty, String> properties = allStates.get(profile);
        if (properties != null)
            properties.remove(property);
    }

    public void removeProfileFromDirection(final Direction direction, final EnumState state) {
        final Map<EnumState, Byte> properties = enabledStates.get(direction);
        if (properties != null)
            properties.remove(state);
    }

    public void updateRedstoneProfile(final Byte profile, final SEProperty property,
            final String value) {
        allStates.computeIfAbsent(profile, _u -> new HashMap<>()).put(property, value);
    }

    public void updateEnabledStates(final Direction direction, final EnumState state,
            final int profile) {
        enabledStates.computeIfAbsent(direction, _u -> new HashMap<>()).put(state, (byte) profile);
    }

    public Map<Byte, Map<SEProperty, String>> getAllStates() {
        return ImmutableMap.copyOf(allStates);
    }

    public Map<Direction, Map<EnumState, Byte>> getEnabledStates() {
        return ImmutableMap.copyOf(enabledStates);
    }

    public void setLinkedRSInput(final BlockPos inputPos) {
        this.linkedRSInput = inputPos;
    }

    public BlockPos getLinkedRSInput() {
        return linkedRSInput;
    }

    public byte getProfileRSInput() {
        return profileRSInput;
    }

    public void setProfileRSInput(final byte profileRSInput) {
        this.profileRSInput = profileRSInput;
    }

    @Override
    public void saveWrapper(final NBTWrapper wrapper) {
        if (level == null || level.isClientSide)
            return;
        if (linkedSignalPosition != null && linkedSignal != null) {
            wrapper.putBlockPos(BLOCK_POS_ID, linkedSignalPosition);
            wrapper.putString(SIGNAL_NAME, linkedSignal.getSignalTypeName());
        } else {
            return;
        }
        wrapper.putInteger(LAST_PROFILE, lastProfile);
        if (lastState != null)
            wrapper.putInteger(ENUM_MODE, lastState.ordinal());
        for (final Direction direction : Direction.values()) {
            if (!enabledStates.containsKey(direction))
                continue;

            final NBTWrapper comp = new NBTWrapper();
            enabledStates.get(direction).forEach((state, profile) -> {
                comp.putInteger(state.getNameWrapper(), profile);
            });
            wrapper.putWrapper(direction.getName(), comp);
        }
        final List<NBTWrapper> list = new ArrayList<>();
        allStates.forEach((profile, properties) -> {
            final NBTWrapper comp = new NBTWrapper();
            comp.putInteger(PROFILE, profile);
            final NBTWrapper props = new NBTWrapper();
            properties.forEach((property, value) -> {
                property.writeToNBT(props, value);
            });
            comp.putWrapper(PROPERITES, props);
            list.add(comp);
        });
        wrapper.putList(ALLSTATES, list);
        if (linkedRSInput != null) {
            wrapper.putBlockPos(LINKED_RS_INPUT, linkedRSInput);
            wrapper.putInteger(RS_INPUT_PROFILE, profileRSInput);
        }
    }

    @Override
    public void loadWrapper(final NBTWrapper wrapper) {
        if (wrapper.contains(BLOCK_POS_ID))
            linkedSignalPosition = wrapper.getBlockPos(BLOCK_POS_ID);
        copy = wrapper.copy();
        if (level == null) {
            return;
        }
        readFromWrapper(copy);
    }

    private void readFromWrapper(final NBTWrapper wrapper) {
        if (level == null || level.isClientSide || linkedSignalPosition == null)
            return;
        linkedSignal = Signal.SIGNALS.get(wrapper.getString(SIGNAL_NAME));
        lastProfile = wrapper.getInteger(LAST_PROFILE);
        lastState = EnumMode.values()[wrapper.getInteger(ENUM_MODE)];
        for (final Direction direction : Direction.values()) {
            if (!wrapper.contains(direction.getName()))
                continue;
            final NBTWrapper comp = wrapper.getWrapper(direction.getName());
            final Map<EnumState, Byte> map = new HashMap<>();
            comp.keySet().stream().forEach(str -> {
                final EnumState state = EnumState.of(str);
                map.put(state, (byte) comp.getInteger(state.getNameWrapper()));
            });
            enabledStates.put(direction, map);
        }
        final List<NBTWrapper> list = wrapper.getList(ALLSTATES);
        final List<SEProperty> properites = linkedSignal == null ? new ArrayList<>()
                : linkedSignal.getProperties();
        list.forEach(compund -> {
            final int profile = compund.getInteger(PROFILE);
            final NBTWrapper comp = compund.getWrapper(PROPERITES);
            final Map<SEProperty, String> properties = new HashMap<>();
            properites.forEach(property -> {
                final Optional<String> value = property.readFromNBT(comp);
                if (value.isPresent()) {
                    properties.put(property, value.get());
                }
            });
            allStates.put((byte) profile, properties);
        });
        if (wrapper.contains(LINKED_RS_INPUT))
            linkedRSInput = wrapper.getBlockPos(LINKED_RS_INPUT);
        profileRSInput = (byte) (wrapper.contains(RS_INPUT_PROFILE)
                ? wrapper.getInteger(RS_INPUT_PROFILE)
                : -1);
    }

    @Override
    public void onLoad() {
        if (!level.isClientSide && copy != null) {
            if (copy.contains(BLOCK_POS_ID))
                linkedSignalPosition = copy.getBlockPos(BLOCK_POS_ID);
            readFromWrapper(copy);
            final SignalStateInfo info = new SignalStateInfo(level, linkedSignalPosition,
                    linkedSignal);
            if (linkedSignalPosition != null && linkedSignal != null) {
                SignalStateHandler.loadSignal(info);
                SignalStateHandler.addListener(info, listener);
            }
        }
    }

    public void unloadSignal() {
        if (linkedSignalPosition != null & linkedSignal != null)
            SignalStateHandler
                    .unloadSignal(new SignalStateInfo(level, linkedSignalPosition, linkedSignal));
    }

    public BlockPos getLinkedPosition() {
        return linkedSignalPosition;
    }

    public Signal getLinkedSignal() {
        return linkedSignal;
    }

    @Override
    public boolean hasLink() {
        return linkedSignalPosition != null;
    }

    @Override
    public boolean link(final BlockPos pos, final CompoundNBT tag) {
        @SuppressWarnings("deprecation")
        final Block block = Registry.BLOCK.get(
                new ResourceLocation(OpenSignalsMain.MODID, tag.getString(OSItems.readStringFromPos(pos))));
        if (block != null && block instanceof Signal) {
            unlink();
            linkedSignalPosition = pos;
            linkedSignal = (Signal) block;
            SignalStateHandler.addListener(new SignalStateInfo(level, pos, linkedSignal), listener);
            return true;
        } else if (block instanceof RedstoneInput) {
            linkedRSInput = pos;
            loadChunkAndGetTile(RedstoneIOTileEntity.class, (ServerWorld) level, pos,
                    (tile, _u) -> tile.linkController(getBlockPos()));
            return true;
        }
        return false;
    }

    @Override
    public boolean unlink() {
        SignalStateHandler.removeListener(
                new SignalStateInfo(level, linkedSignalPosition, linkedSignal), listener);
        linkedSignalPosition = null;
        linkedSignal = null;
        allStates.clear();
        enabledStates.clear();
        return true;
    }

    public void redstoneUpdate() {
        if (level.isClientSide || linkedSignalPosition == null)
            return;
        for (final Direction face : Direction.values()) {
            if (!this.enabledStates.containsKey(face))
                continue;
            final boolean state = this.level.hasSignal(worldPosition.relative(face), face);
            final boolean old = this.currentStates[face.ordinal()];
            if (state == old)
                continue;
            this.currentStates[face.ordinal()] = state;
            final EnumState currenState = state ? EnumState.ONSTATE : EnumState.OFFSTATE;
            final Byte profile = this.enabledStates.get(face).get(currenState);
            if (profile == null || !allStates.containsKey(profile)) {
                continue;
            }
            final SignalStateInfo info = new SignalStateInfo(level, linkedSignalPosition,
                    linkedSignal);
            SignalStateHandler.setStates(info, allStates.get(profile));
        }
    }

    public void updateFromRSInput() {
        if (level.isClientSide)
            return;
        final Map<SEProperty, String> properties = allStates.get(profileRSInput);
        if (properties != null)
            SignalStateHandler.setStates(
                    new SignalStateInfo(level, linkedSignalPosition, linkedSignal), properties);
    }

    @Override
    public boolean isValid(final PlayerEntity player) {
        return true;
    }

    @Override
    public void onChunkUnloaded() {
        unloadSignal();
    }
}
