package com.troblecodings.signals.tileentitys;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.openjdk.nashorn.internal.runtime.regexp.joni.constants.Arguments;

import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.guilib.ecs.interfaces.NamableWrapper;
import com.troblecodings.linkableapi.ILinkableTile;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.enums.EnumMode;
import com.troblecodings.signals.enums.EnumState;

import li.cil.oc.api.machine.Callback;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Direction;
import net.minecraft.world.ILevelNameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class SignalControllerTileEntity extends SyncableTileEntity
        implements ISyncable, ILinkableTile, IChunkloadable {

    private BlockPos linkedSignalPosition = null;
    private int[] listOfSupportedIndicies;
    private Map<String, Integer> tableOfSupportedSignalTypes;
    private int signalTypeCache = -1;
    private CompoundTag compound = new CompoundTag();
    private final HashMap<Direction, Map<EnumState, String>> statesEnabled //
            = new HashMap<Direction, Map<EnumState, String>>();
    private final boolean[] currentStates = new boolean[Direction.values().length];

    private static final String ID_X = "xLinkedPos";
    private static final String ID_Y = "yLinkedPos";
    private static final String ID_Z = "zLinkedPos";

    private static final String ID_COMP = "COMP";

    public SignalControllerTileEntity() {
    }

    @Override
    public void readFromNBT(final CompoundTag compound) {
        linkedSignalPosition = readBlockPosFromNBT(compound);
        this.compound = compound.getCompoundTag(ID_COMP);
        super.readFromNBT(compound);
        this.updateRSProfiles();
        if (world != null && world.isRemote && linkedSignalPosition != null)
            onLink();
    }

    @Override
    public CompoundTag writeToNBT(final CompoundTag compound) {
        writeBlockPosToNBT(linkedSignalPosition, compound);
        super.writeToNBT(compound);
        compound.put(ID_COMP, this.compound);
        return compound;
    }

    public static Map<String, Integer> getSupportedSignalStates(final SignalTileEnity signaltile) {
        final Signal signalBlock = Signal.SIGNALLIST.get(signaltile.getBlockID());
        final Map<SEProperty, Object> properties = signaltile.getProperties();
        final Builder<String, Integer> nameToIDBuilder = new Builder<>();
        properties.keySet().stream().filter((property) -> property.test(properties)
                && (property.isChangabelAtStage(ChangeableStage.APISTAGE)
                        || property.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG)))
                .forEach(property -> nameToIDBuilder.put(property.getName(),
                        signalBlock.getIDFromProperty(property)));
        return nameToIDBuilder.build();
    }

    public void onLink() {
        new Thread(() -> {
            loadChunkAndGetTile(SignalTileEnity.class, world, linkedSignalPosition,
                    (signaltile, _u) -> {
                        while (!world.isBlockLoaded(linkedSignalPosition))
                            continue;
                        signalTypeCache = signaltile.getBlockID();
                        tableOfSupportedSignalTypes = getSupportedSignalStates(signaltile);
                        listOfSupportedIndicies = tableOfSupportedSignalTypes.values().stream()
                                .mapToInt(Integer::intValue).toArray();
                        syncClient();
                    });
        }).start();
    }

    @Override
    public void onLoad() {
        if (linkedSignalPosition != null) {
            onLink();
            syncClient();
        }
    }

    public int[] getSupportedSignalTypesImpl() {
        return listOfSupportedIndicies;
    }

    public static boolean find(final int[] arr, final int i) {
        return Arrays.stream(arr).anyMatch(x -> i == x);
    }

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    public boolean changeSignalImpl(final int type, final int newSignal) {
        if (!find(getSupportedSignalTypesImpl(), type))
            return false;
        final AtomicBoolean rtc = new AtomicBoolean(true);
        loadChunkAndGetTile(SignalTileEnity.class, world, linkedSignalPosition, (tile, chunk) -> {
            final BlockState state = chunk.getBlockState(linkedSignalPosition);
            final Signal block = (Signal) state.getBlock();
            final SEProperty prop = SEProperty.cst(block.getPropertyFromID(type));
            if (!prop.isValid(newSignal)) {
                rtc.set(false);
                return;
            }
            tile.setProperty(prop, prop.getObjFromID(newSignal));
            world.markAndNotifyBlock(linkedSignalPosition, chunk, state, state, 3);
        });
        return rtc.get();
    }

    public int getSignalTypeImpl() {
        return signalTypeCache;
    }

    @SuppressWarnings("rawtypes")
    public int getSignalStateImpl(final int type) {
        if (!find(getSupportedSignalTypesImpl(), type))
            return -1;
        final AtomicReference<SignalTileEnity> entity = new AtomicReference<SignalTileEnity>();
        loadChunkAndGetTile(SignalTileEnity.class, world, linkedSignalPosition,
                (sig, ch) -> entity.set(sig));
        final SignalTileEnity tile = entity.get();
        if (tile == null)
            return -1;
        final Signal block = Signal.SIGNALLIST.get(tile.getBlockID());
        final SEProperty prop = SEProperty.cst(block.getPropertyFromID(type));
        final java.util.Optional bool = tile.getProperty(prop);
        if (bool.isPresent())
            return (boolean) bool.get() ? 1 : 0;
        return -1;
    }

    public BlockPos getLinkedPosition() {
        return linkedSignalPosition;
    }

    @Override
    public boolean hasLink() {
        if (linkedSignalPosition == null)
            return false;
        if (loadChunkAndGetTile(SignalTileEnity.class, level, linkedSignalPosition, (x, y) -> {
        }))
            return true;
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
        tableOfSupportedSignalTypes = null;
        listOfSupportedIndicies = null;
        return true;
    }

    private boolean inMode(final EnumMode mode) {
        return this.compound.getInt(EnumMode.class.getSimpleName().toLowerCase()) == mode.ordinal();
    }

    @Override
    public void updateTag(final CompoundTag compound) {
        if (compound == null || tableOfSupportedSignalTypes == null)
            return;
        this.compound = compound;

        if (inMode(EnumMode.MANUELL)) {
            compound.getKeySet().forEach(str -> {
                if (tableOfSupportedSignalTypes.containsKey(str)) {
                    final int type = compound.getInt(str);
                    final int id = tableOfSupportedSignalTypes.get(str);
                    changeSignalImpl(id, type);
                }
            });
        }
        updateRSProfiles();
        syncClient();
    }

    private void changeProfile(final String onProfile, final Direction face,
            final EnumState state) {
        if (compound.hasKey(onProfile)) {
            if (!this.statesEnabled.containsKey(face))
                this.statesEnabled.put(face, Maps.newHashMap());
            final int value = compound.getInt(onProfile);
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
        for (final Direction face : Direction.VALUES) {
            final String offProfile = "profileOff." + face.getName();
            final String onProfile = "profileOn." + face.getName();
            changeProfile(onProfile, face, EnumState.ONSTATE);
            changeProfile(offProfile, face, EnumState.OFFSTATE);
        }
    }

    public void redstoneUpdate() {
        if (compound == null || tableOfSupportedSignalTypes == null)
            return;
        for (final Direction face : Direction.VALUES) {
            if (!this.statesEnabled.containsKey(face))
                continue;
            final boolean state = this.world.isSidePowered(pos.offset(face), face);
            final boolean old = this.currentStates[face.ordinal()];
            if (state == old)
                continue;
            this.currentStates[face.ordinal()] = state;
            final EnumState currenState = state ? EnumState.ONSTATE : EnumState.OFFSTATE;
            final String profile = this.statesEnabled.get(face).get(currenState);
            if (profile == null)
                continue;
            compound.getKeySet().stream().filter(key -> key.endsWith(profile)).forEach(e -> {
                final int value = compound.getInt(e);
                if (value < 0)
                    return;
                final String name = e.split("\\.")[0];
                if (tableOfSupportedSignalTypes.containsKey(name)) {
                    final int id = tableOfSupportedSignalTypes.get(name);
                    changeSignalImpl(id, value);
                }
            });
        }
    }

    @Override
    public CompoundTag getTag() {
        return this.compound;
    }

    @Override
    public boolean isValid(final Player player) {
        return true;
    }

}
