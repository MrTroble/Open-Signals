package com.troblecodings.signals.guis;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.interfaces.UIClientSync;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.enums.EnumMode;
import com.troblecodings.signals.enums.EnumState;
import com.troblecodings.signals.enums.SignalControllerNetwork;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.tileentitys.SignalControllerTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public class ContainerSignalController extends ContainerBase implements UIClientSync, INetworkSync {

    private final AtomicReference<Map<SEProperty, String>> reference = new AtomicReference<>();
    private final AtomicReference<Signal> referenceBlock = new AtomicReference<>();
    protected final Map<Integer, Map<SEProperty, String>> allRSStates = new HashMap<>();
    protected final Map<Direction, Map<EnumState, Integer>> enabledRSStates = new HashMap<>();
    private List<SEProperty> propertiesList;
    private final GuiInfo info;
    private BlockPos linkedPos;
    protected int lastProfile;
    protected EnumMode currentMode = EnumMode.MANUELL;
    private SignalControllerTileEntity controllerEntity;
    private int currentRSProfile;

    public ContainerSignalController(final GuiInfo info) {
        super(info);
        info.base = this;
        info.player.containerMenu = this;
        this.info = info;
    }

    @Override
    public void sendAllDataToRemote() {
        sendProperitesToClient();
    }

    private void sendProperitesToClient() {
        if (info.pos == null) {
            return;
        }
        controllerEntity = (SignalControllerTileEntity) info.world.getBlockEntity(info.pos);
        linkedPos = controllerEntity.getLinkedPosition();
        if (linkedPos == null) {
            return;
        }
        final SignalStateInfo stateInfo = new SignalStateInfo(info.world, linkedPos);
        referenceBlock.set((Signal) info.world.getBlockState(linkedPos).getBlock());
        final Map<SEProperty, String> properties = SignalStateHandler.getStates(stateInfo);
        if (properties == null || properties.isEmpty())
            return;
        reference.set(properties);
        final Map<SEProperty, String> propertiesToSend = new HashMap<>();
        properties.forEach((property, value) -> {
            if ((property.isChangabelAtStage(ChangeableStage.APISTAGE)
                    || property.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG))
                    && property.testMap(properties)) {
                propertiesToSend.put(property, value);
            }
        });
        final Map<Byte, Map<SEProperty, String>> allStates = new HashMap<>(
                controllerEntity.getAllStates());
        final Map<Byte, Map<SEProperty, String>> allStatesToSend = new HashMap<>();
        allStates.forEach((profile, props) -> {
            final Map<SEProperty, String> propsForProfile = new HashMap<>();
            props.forEach((property, value) -> {
                if ((property.isChangabelAtStage(ChangeableStage.APISTAGE)
                        || property.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG))
                        && property.testMap(properties)) {
                    propsForProfile.put(property, value);
                }
            });
            allStatesToSend.put(profile, propsForProfile);
        });
        final Map<Direction, Map<EnumState, Byte>> enabledStates = controllerEntity
                .getEnabledStates();
        final List<Byte> allBytes = new ArrayList<>();

        allBytes.add((byte) propertiesToSend.size());
        propertiesToSend.forEach((property, value) -> {
            packPropertyToBuffer(allBytes, stateInfo, property, value);
        });
        allBytes.add((byte) controllerEntity.getProfile());
        allBytes.add((byte) allStatesToSend.size());
        allStatesToSend.forEach((profile, props) -> {
            allBytes.add(profile);
            allBytes.add((byte) props.size());
            props.forEach((property, value) -> {
                packPropertyToBuffer(allBytes, stateInfo, property, value);
            });
        });

        allBytes.add((byte) enabledStates.size());
        enabledStates.forEach((direction, states) -> {
            allBytes.add((byte) direction.ordinal());
            allBytes.add((byte) states.size());
            states.forEach((mode, profile) -> {
                allBytes.add((byte) mode.ordinal());
                allBytes.add(profile);
            });
        });
        final ByteBuffer buffer = ByteBuffer.allocate(13 + allBytes.size());
        buffer.putInt(stateInfo.pos.getX());
        buffer.putInt(stateInfo.pos.getY());
        buffer.putInt(stateInfo.pos.getZ());
        currentMode = controllerEntity.getLastMode();
        buffer.put((byte) currentMode.ordinal());
        allBytes.forEach(entry -> buffer.put(entry));
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    private void packPropertyToBuffer(final List<Byte> allBytes, final SignalStateInfo stateInfo,
            final SEProperty property, final String value) {
        allBytes.add((byte) stateInfo.signal.getIDFromProperty(property));
        allBytes.add((byte) property.getParent().getIDFromValue(value));
    }

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        linkedPos = new BlockPos(buf.getInt(), buf.getInt(), buf.getInt());
        currentMode = EnumMode.values()[Byte.toUnsignedInt(buf.get())];
        final Signal signal = (Signal) info.world.getBlockState(linkedPos).getBlock();
        referenceBlock.set(signal);
        final int size = Byte.toUnsignedInt(buf.get());
        final Map<SEProperty, String> properites = new HashMap<>();
        propertiesList = signal.getProperties();
        for (int i = 0; i < size; i++) {
            final SEProperty property = propertiesList.get(Byte.toUnsignedInt(buf.get()));
            properites.put(property, property.getObjFromID(Byte.toUnsignedInt(buf.get())));
        }
        reference.set(properites);
        lastProfile = Byte.toUnsignedInt(buf.get());
        final int allStatesSize = Byte.toUnsignedInt(buf.get());
        for (int i = 0; i < allStatesSize; i++) {
            final int profile = Byte.toUnsignedInt(buf.get());
            final int propertySize = Byte.toUnsignedInt(buf.get());
            final Map<SEProperty, String> profileProps = new HashMap<>();
            for (int j = 0; j < propertySize; j++) {
                final SEProperty property = propertiesList.get(Byte.toUnsignedInt(buf.get()));
                final String value = property.getObjFromID(Byte.toUnsignedInt(buf.get()));
                profileProps.put(property, value);
            }
            allRSStates.put(profile, profileProps);
        }
        final int enabledStatesSize = Byte.toUnsignedInt(buf.get());
        for (int i = 0; i < enabledStatesSize; i++) {
            final Direction direction = Direction.values()[Byte.toUnsignedInt(buf.get())];
            final int propSize = Byte.toUnsignedInt(buf.get());
            final Map<EnumState, Integer> states = new HashMap<>();
            for (int j = 0; j < propSize; j++) {
                final EnumState mode = EnumState.values()[Byte.toUnsignedInt(buf.get())];
                states.put(mode, Byte.toUnsignedInt(buf.get()));
            }
            enabledRSStates.put(direction, states);
        }
        update();
    }

    @Override
    public void deserializeServer(final ByteBuffer buf) {
        if (propertiesList == null) {
            propertiesList = getSignal().getProperties();
        }
        final SignalControllerNetwork mode = SignalControllerNetwork.values()[Byte
                .toUnsignedInt(buf.get())];
        if (mode.equals(SignalControllerNetwork.SEND_MODE)) {
            currentMode = EnumMode.values()[Byte.toUnsignedInt(buf.get())];
            controllerEntity.setLastMode(currentMode);
            return;
        }
        if (mode.equals(SignalControllerNetwork.SEND_RS_PROFILE)) {
            currentRSProfile = Byte.toUnsignedInt(buf.get());
            return;
        }
        if (mode.equals(SignalControllerNetwork.SEND_PROPERTY)) {
            final SEProperty property = propertiesList.get(Byte.toUnsignedInt(buf.get()));
            final String value = property.getObjFromID(Byte.toUnsignedInt(buf.get()));
            if (currentMode.equals(EnumMode.MANUELL)) {
                SignalStateHandler.setState(new SignalStateInfo(info.world, linkedPos, getSignal()),
                        property, value);
            } else if (currentMode.equals(EnumMode.SINGLE)) {
                if (!controllerEntity.containsProfile((byte) currentRSProfile)) {
                    controllerEntity.initializeProfile((byte) currentRSProfile, getReference());
                }
                controllerEntity.updateRedstoneProfile((byte) currentRSProfile, property, value);
            }
            return;
        }
        if (mode.equals(SignalControllerNetwork.SET_PROFILE)) {
            final EnumState state = EnumState.values()[Byte.toUnsignedInt(buf.get())];
            final Direction direction = Direction.values()[Byte.toUnsignedInt(buf.get())];
            final int profile = Byte.toUnsignedInt(buf.get());
            controllerEntity.updateEnabledStates(direction, state, profile);
            return;
        }
        if (mode.equals(SignalControllerNetwork.INITIALIZE_DIRECTION)) {
            final Direction direction = Direction.values()[Byte.toUnsignedInt(buf.get())];
            final Map<EnumState, Byte> states = new HashMap<>();
            states.put(EnumState.OFFSTATE, (byte) 0);
            states.put(EnumState.ONSTATE, (byte) 0);
            controllerEntity.initializeDirection(direction, states);
        }
    }

    public Map<SEProperty, String> getReference() {
        return reference.get();
    }

    public Signal getSignal() {
        return referenceBlock.get();
    }

    @Override
    public Player getPlayer() {
        return info.player;
    }

    @Override
    public boolean stillValid(final Player playerIn) {
        if (playerIn instanceof Player) {
            this.info.player = playerIn;
        }
        return true;
    }

    public BlockPos getPos() {
        return linkedPos;
    }
}