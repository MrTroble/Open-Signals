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
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.enums.EnumMode;
import com.troblecodings.signals.enums.EnumState;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

public class SignalControllerTileEntity extends SyncableTileEntity
        implements ISyncable, ILinkableTile {

    private BlockPos linkedSignalPosition = null;
    private Signal linkedSignal = null;
    private int lastProfile = 0;
    private NBTWrapper copy;
    private EnumMode lastState;
    private final boolean[] currentStates = new boolean[Direction.values().length];
    private final Map<Byte, Map<SEProperty, String>> allStates = new HashMap<>();
    private final Map<Direction, Map<EnumState, Byte>> enabledStates = new HashMap<>();

    public static final String SIGNAL_NAME = "signalname";
    private static final String BLOCK_POS_ID = "blockpos";
    private static final String PROFILE = "profile";
    private static final String PROPERITES = "properties";
    private static final String ALLSTATES = "allstates";
    private static final String LAST_PROFILE = "lastprofile";
    private static final String ENUM_MODE = "enummode";

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

    public void initializeDirection(final Direction direction, final Map<EnumState, Byte> states) {
        enabledStates.put(direction, states);
    }

    public void initializeProfile(final Byte profile, final Map<SEProperty, String> properties) {
        allStates.put(profile, ImmutableMap.copyOf(properties));
    }

    public boolean containsProfile(final Byte profile) {
        return allStates.containsKey(profile);
    }

    public int getProfile() {
        return lastProfile;
    }

    public void setProfile(final int profile) {
        lastProfile = profile;
    }

    public void updateRedstoneProfile(final Byte profile, final SEProperty property,
            final String value) {
        final Map<SEProperty, String> properties = allStates.containsKey(profile)
                ? allStates.get(profile)
                : new HashMap<>();
        final Map<SEProperty, String> setProperties = new HashMap<>();
        setProperties.putAll(properties);
        setProperties.put(property, value);
        allStates.put((byte) profile, ImmutableMap.copyOf(setProperties));
    }

    public void updateEnabledStates(final Direction direction, final EnumState state,
            final int profile) {
        final Map<EnumState, Byte> states = enabledStates.containsKey(direction)
                ? enabledStates.get(direction)
                : new HashMap<>();
        final Map<EnumState, Byte> setStates = new HashMap<>();
        setStates.putAll(states);
        setStates.put(state, (byte) profile);
        enabledStates.put(direction, ImmutableMap.copyOf(setStates));
    }

    public Map<Byte, Map<SEProperty, String>> getAllStates() {
        return ImmutableMap.copyOf(allStates);
    }

    public Map<Direction, Map<EnumState, Byte>> getEnabledStates() {
        return ImmutableMap.copyOf(enabledStates);
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
        System.out.println();
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
        if (wrapper.contains(SIGNAL_NAME)) {
            linkedSignal = Signal.SIGNALS.get(wrapper.getString(SIGNAL_NAME));
        }
        if (wrapper.contains(LAST_PROFILE)) {
            lastProfile = wrapper.getInteger(LAST_PROFILE);
        }
        if (wrapper.contains(ENUM_MODE)) {
            lastState = EnumMode.values()[wrapper.getInteger(ENUM_MODE)];
        }
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
        System.out.println();
    }

    @Override
    public void onLoad() {
        if (!level.isClientSide && copy != null) {
            if (copy.contains(BLOCK_POS_ID))
                linkedSignalPosition = copy.getBlockPos(BLOCK_POS_ID);
            readFromWrapper(copy);
            if (linkedSignalPosition != null && linkedSignal != null)
                SignalStateHandler
                        .loadSignal(new SignalStateInfo(level, linkedSignalPosition, linkedSignal));
        }
    }

    public void unloadSignal() {
        if (linkedSignalPosition != null & linkedSignal != null)
            SignalStateHandler
                    .unloadSignal(new SignalStateInfo(level, linkedSignalPosition, linkedSignal));
    }

    public BlockPos getLinkedPosition() {
        if (SignalStateHandler
                .containsStates(new SignalStateInfo(level, linkedSignalPosition, linkedSignal)))
            return linkedSignalPosition;
        unlink();
        return null;
    }

    public Signal getLinkedSignal() {
        if (SignalStateHandler
                .containsStates(new SignalStateInfo(level, linkedSignalPosition, linkedSignal)))
            return linkedSignal;
        unlink();
        return null;
    }

    @Override
    public boolean hasLink() {
        return linkedSignalPosition != null;
    }

    @Override
    public boolean link(final BlockPos pos, final CompoundTag tag) {
        @SuppressWarnings("deprecation")
        final Block block = Registry.BLOCK
                .get(new ResourceLocation(OpenSignalsMain.MODID, tag.getString(SIGNAL_NAME)));
        if (block != null && block instanceof Signal) {
            linkedSignalPosition = pos;
            linkedSignal = (Signal) block;
            return true;
        }
        return false;
    }

    @Override
    public boolean unlink() {
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

    @Override
    public boolean isValid(final Player player) {
        return true;
    }
}