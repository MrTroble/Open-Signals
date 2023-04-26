package com.troblecodings.signals.guis;

import java.nio.ByteBuffer;
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
import com.troblecodings.signals.core.ReadBuffer;
import com.troblecodings.signals.core.WriteBuffer;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.enums.EnumMode;
import com.troblecodings.signals.enums.EnumState;
import com.troblecodings.signals.enums.SignalControllerNetwork;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.tileentitys.SignalControllerTileEntity;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
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
        referenceBlock.set(controllerEntity.getLinkedSignal());
        final SignalStateInfo stateInfo = new SignalStateInfo(info.world, linkedPos, getSignal());
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
        currentMode = controllerEntity.getLastMode();

        final WriteBuffer buffer = new WriteBuffer();
        buffer.putBlockPos(stateInfo.pos);
        final byte[] signalName = getSignal().getSignalTypeName().getBytes();
        buffer.putByte((byte) signalName.length);
        for (final byte b : signalName) {
            buffer.putByte(b);
        }
        buffer.putByte((byte) currentMode.ordinal());
        buffer.putByte((byte) propertiesToSend.size());
        propertiesToSend.forEach((property, value) -> {
            packPropertyToBuffer(buffer, stateInfo, property, value);
        });
        buffer.putByte((byte) controllerEntity.getProfile());
        buffer.putByte((byte) allStatesToSend.size());
        allStatesToSend.forEach((profile, props) -> {
            buffer.putByte(profile);
            buffer.putByte((byte) props.size());
            props.forEach((property, value) -> {
                packPropertyToBuffer(buffer, stateInfo, property, value);
            });
        });

        buffer.putByte((byte) enabledStates.size());
        enabledStates.forEach((direction, states) -> {
            buffer.putByte((byte) direction.ordinal());
            buffer.putByte((byte) states.size());
            states.forEach((mode, profile) -> {
                buffer.putByte((byte) mode.ordinal());
                buffer.putByte(profile);
            });
        });
        OpenSignalsMain.network.sendTo(info.player, buffer.build());
    }

    private void packPropertyToBuffer(final WriteBuffer buffer, final SignalStateInfo stateInfo,
            final SEProperty property, final String value) {
        buffer.putByte((byte) stateInfo.signal.getIDFromProperty(property));
        buffer.putByte((byte) property.getParent().getIDFromValue(value));
    }

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        final ReadBuffer buffer = new ReadBuffer(buf);
        linkedPos = buffer.getBlockPos();
        final int nameSize = buffer.getByteAsInt();
        final byte[] signalName = new byte[nameSize];
        for (int i = 0; i < nameSize; i++) {
            signalName[i] = buffer.getByte();
        }
        final Signal signal = Signal.SIGNALS.get(new String(signalName));
        referenceBlock.set(signal);
        currentMode = EnumMode.values()[buffer.getByteAsInt()];
        final int size = buffer.getByteAsInt();
        final Map<SEProperty, String> properites = new HashMap<>();
        propertiesList = signal.getProperties();
        for (int i = 0; i < size; i++) {
            final SEProperty property = propertiesList.get(buffer.getByteAsInt());
            properites.put(property, property.getObjFromID(buffer.getByteAsInt()));
        }
        reference.set(properites);
        lastProfile = buffer.getByteAsInt();
        final int allStatesSize = buffer.getByteAsInt();
        for (int i = 0; i < allStatesSize; i++) {
            final int profile = buffer.getByteAsInt();
            final int propertySize = buffer.getByteAsInt();
            final Map<SEProperty, String> profileProps = new HashMap<>();
            for (int j = 0; j < propertySize; j++) {
                final SEProperty property = propertiesList.get(buffer.getByteAsInt());
                final String value = property.getObjFromID(buffer.getByteAsInt());
                profileProps.put(property, value);
            }
            allRSStates.put(profile, profileProps);
        }
        final int enabledStatesSize = buffer.getByteAsInt();
        for (int i = 0; i < enabledStatesSize; i++) {
            final Direction direction = Direction.values()[buffer.getByteAsInt()];
            final int propSize = buffer.getByteAsInt();
            final Map<EnumState, Integer> states = new HashMap<>();
            for (int j = 0; j < propSize; j++) {
                final EnumState mode = EnumState.values()[buffer.getByteAsInt()];
                states.put(mode, buffer.getByteAsInt());
            }
            enabledRSStates.put(direction, states);
        }
        update();
    }

    @Override
    public void deserializeServer(final ByteBuffer buf) {
        final ReadBuffer buffer = new ReadBuffer(buf);
        if (propertiesList == null) {
            propertiesList = getSignal().getProperties();
        }
        final SignalControllerNetwork mode = SignalControllerNetwork.values()[buffer
                .getByteAsInt()];
        switch (mode) {
            case SEND_MODE: {
                currentMode = EnumMode.values()[buffer.getByteAsInt()];
                controllerEntity.setLastMode(currentMode);
                break;
            }
            case SEND_RS_PROFILE: {
                currentRSProfile = buffer.getByteAsInt();
                break;
            }
            case SEND_PROPERTY: {
                final SEProperty property = propertiesList.get(buffer.getByteAsInt());
                final String value = property.getObjFromID(buffer.getByteAsInt());
                if (currentMode.equals(EnumMode.MANUELL)) {
                    SignalStateHandler.setState(
                            new SignalStateInfo(info.world, linkedPos, getSignal()), property,
                            value);
                } else if (currentMode.equals(EnumMode.SINGLE)) {
                    if (!controllerEntity.containsProfile((byte) currentRSProfile)) {
                        controllerEntity.initializeProfile((byte) currentRSProfile, getReference());
                    }
                    controllerEntity.updateRedstoneProfile((byte) currentRSProfile, property,
                            value);
                }
                break;
            }
            case SET_PROFILE: {
                final EnumState state = EnumState.values()[buffer.getByteAsInt()];
                final Direction direction = Direction.values()[buffer.getByteAsInt()];
                final int profile = buffer.getByteAsInt();
                controllerEntity.updateEnabledStates(direction, state, profile);
                break;
            }
            case INITIALIZE_DIRECTION: {
                final Direction direction = Direction.values()[buffer.getByteAsInt()];
                final Map<EnumState, Byte> states = new HashMap<>();
                states.put(EnumState.OFFSTATE, (byte) 0);
                states.put(EnumState.ONSTATE, (byte) 0);
                controllerEntity.initializeDirection(direction, states);
                break;
            }
            default:
                break;
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