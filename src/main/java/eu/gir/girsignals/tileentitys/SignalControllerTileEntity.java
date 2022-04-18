package eu.gir.girsignals.tileentitys;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;

import eu.gir.girsignals.EnumSignals.EnumMode;
import eu.gir.girsignals.EnumSignals.EnumState;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.guilib.ecs.interfaces.ISyncable;
import eu.gir.linkableapi.ILinkableTile;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldNameable;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public class SignalControllerTileEntity extends SyncableTileEntity
        implements ISyncable, SimpleComponent, IWorldNameable, ILinkableTile, IChunkloadable {

    private BlockPos linkedSignalPosition = null;
    private int[] listOfSupportedIndicies;
    private Map<String, Integer> tableOfSupportedSignalTypes;
    private int signalTypeCache = -1;
    private NBTTagCompound compound = new NBTTagCompound();
    private final HashMap<EnumFacing, Map<EnumState, String>> statesEnabled = new HashMap<EnumFacing, Map<EnumState, String>>();
    private final boolean[] currentStates = new boolean[EnumFacing.values().length];

    private static final String ID_X = "xLinkedPos";
    private static final String ID_Y = "yLinkedPos";
    private static final String ID_Z = "zLinkedPos";

    private static final String ID_COMP = "COMP";

    public SignalControllerTileEntity() {
    }

    public static BlockPos readBlockPosFromNBT(NBTTagCompound compound) {
        if (compound != null && compound.hasKey(ID_X) && compound.hasKey(ID_Y)
                && compound.hasKey(ID_Z)) {
            return new BlockPos(compound.getInteger(ID_X), compound.getInteger(ID_Y),
                    compound.getInteger(ID_Z));
        }
        return null;
    }

    public static void writeBlockPosToNBT(BlockPos pos, NBTTagCompound compound) {
        if (pos != null && compound != null) {
            compound.setInteger(ID_X, pos.getX());
            compound.setInteger(ID_Y, pos.getY());
            compound.setInteger(ID_Z, pos.getZ());
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        linkedSignalPosition = readBlockPosFromNBT(compound);
        this.compound = compound.getCompoundTag(ID_COMP);
        super.readFromNBT(compound);
        this.updateRSProfiles();
        if (world != null && world.isRemote && linkedSignalPosition != null)
            onLink();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        writeBlockPosToNBT(linkedSignalPosition, compound);
        super.writeToNBT(compound);
        compound.setTag(ID_COMP, this.compound);
        return compound;
    }

    public static Map<String, Integer> getSupportedSignalStates(final SignalTileEnity signaltile) {
        final Signal signalBlock = Signal.SIGNALLIST.get(signaltile.getBlockID());
        final Map<SEProperty<?>, Object> properties = signaltile.getProperties();
        final Builder<String, Integer> nameToIDBuilder = new Builder<>();
        properties.keySet().stream().filter((property) -> property.test(properties.entrySet())
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

    @Callback
    @Optional.Method(modid = "opencomputers")
    public Object[] hasLink(Context context, Arguments args) {
        return new Object[] {
                hasLink()
        };
    }

    @Callback
    @Optional.Method(modid = "opencomputers")
    public Object[] getSupportedSignalTypes(Context context, Arguments args) {
        return new Object[] {
                tableOfSupportedSignalTypes
        };
    }

    public int[] getSupportedSignalTypesImpl() {
        return listOfSupportedIndicies;
    }

    public static boolean find(int[] arr, int i) {
        return Arrays.stream(arr).anyMatch(x -> i == x);
    }

    @Callback
    @Optional.Method(modid = "opencomputers")
    public Object[] changeSignal(Context context, Arguments args) {
        return new Object[] {
                changeSignalImpl(args.checkInteger(0), args.checkInteger(1))
        };
    }

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    public boolean changeSignalImpl(int type, int newSignal) {
        if (!find(getSupportedSignalTypesImpl(), type))
            return false;
        final AtomicBoolean rtc = new AtomicBoolean(true);
        loadChunkAndGetTile(SignalTileEnity.class, world, linkedSignalPosition, (tile, chunk) -> {
            IBlockState state = chunk.getBlockState(linkedSignalPosition);
            Signal block = (Signal) state.getBlock();
            SEProperty prop = SEProperty.cst(block.getPropertyFromID(type));
            if (!prop.isValid(newSignal)) {
                rtc.set(false);
                return;
            }
            tile.setProperty(prop, prop.getObjFromID(newSignal));
            world.markAndNotifyBlock(linkedSignalPosition, chunk, state, state, 3);
        });
        return rtc.get();
    }

    @Callback
    @Optional.Method(modid = "opencomputers")
    public Object[] getSignalType(Context context, Arguments args) {
        return new Object[] {
                Signal.SIGNALLIST.get(getSignalTypeImpl()).getSignalTypeName()
        };
    }

    public int getSignalTypeImpl() {
        return signalTypeCache;
    }

    @Callback
    @Optional.Method(modid = "opencomputers")
    public Object[] getSignalState(Context context, Arguments args) {
        return new Object[] {
                getSignalStateImpl(args.checkInteger(0))
        };
    }

    @SuppressWarnings("rawtypes")
    public int getSignalStateImpl(int type) {
        if (!find(getSupportedSignalTypesImpl(), type))
            return -1;
        final AtomicReference<SignalTileEnity> entity = new AtomicReference<SignalTileEnity>();
        loadChunkAndGetTile(SignalTileEnity.class, world, linkedSignalPosition,
                (sig, ch) -> entity.set(sig));
        final SignalTileEnity tile = entity.get();
        if (tile == null)
            return -1;
        Signal block = (Signal) Signal.SIGNALLIST.get(tile.getBlockID());
        SEProperty prop = SEProperty.cst(block.getPropertyFromID(type));
        java.util.Optional bool = tile.getProperty(prop);
        if (bool.isPresent())
            return SEProperty.getIDFromObj(bool.get());
        return -1;
    }

    public BlockPos getLinkedPosition() {
        return linkedSignalPosition;
    }

    @Override
    public String getComponentName() {
        return "signalcontroller";
    }

    @Override
    public String getName() {
        return ""; // TODO Replace with loading variant
    }

    @Override
    public boolean hasCustomName() {
        return false; // TODO Replace with loading variant
    }

    @Override
    public boolean hasLink() {
        if (linkedSignalPosition == null)
            return false;
        if (loadChunkAndGetTile(SignalTileEnity.class, world, linkedSignalPosition, (x, y) -> {
        }))
            return true;
        if (!world.isRemote)
            unlink();
        return false;
    }

    @Override
    public boolean link(final BlockPos pos) {
        final IBlockState state = world.getBlockState(pos);
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

    private boolean inMode(EnumMode mode) {
        return this.compound.getInteger(EnumMode.class.getSimpleName().toLowerCase()) == mode
                .ordinal();
    }

    @Override
    public void updateTag(NBTTagCompound compound) {
        if (compound == null || tableOfSupportedSignalTypes == null)
            return;
        this.compound = compound;

        if (inMode(EnumMode.MANUELL)) {
            compound.getKeySet().forEach(str -> {
                if (tableOfSupportedSignalTypes.containsKey(str)) {
                    int type = compound.getInteger(str);
                    int id = tableOfSupportedSignalTypes.get(str);
                    changeSignalImpl(id, type);
                }
            });
        }
        updateRSProfiles();
        syncClient();
    }

    private void changeProfile(String onProfile, EnumFacing face, EnumState state) {
        if (compound.hasKey(onProfile)) {
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
        for (EnumFacing face : EnumFacing.VALUES) {
            final String offProfile = "profileOff." + face.getName();
            final String onProfile = "profileOn." + face.getName();
            changeProfile(onProfile, face, EnumState.ONSTATE);
            changeProfile(offProfile, face, EnumState.OFFSTATE);
        }
    }

    public void redstoneUpdate() {
        if (compound == null || tableOfSupportedSignalTypes == null)
            return;
        for (EnumFacing face : EnumFacing.VALUES) {
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
                final int value = compound.getInteger(e);
                if (value < 0)
                    return;
                final String name = e.split("\\.")[0];
                if (tableOfSupportedSignalTypes.containsKey(name)) {
                    int id = tableOfSupportedSignalTypes.get(name);
                    changeSignalImpl(id, value);
                }
            });
        }
    }

    @Override
    public NBTTagCompound getTag() {
        return this.compound;
    }

}
