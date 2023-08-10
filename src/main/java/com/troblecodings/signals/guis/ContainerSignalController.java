package com.troblecodings.signals.guis;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.troblecodings.signals.tileentitys.IChunkLoadable;
import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;
import com.troblecodings.signals.tileentitys.SignalControllerTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public class ContainerSignalController extends ContainerBase
        implements UIClientSync, INetworkSync, IChunkLoadable {

    private final Map<SEProperty, String> properties = new HashMap<>();
    private Signal currentSignal = null;
    private List<SEProperty> propertiesList;
    private BlockPos linkedPos;
    private SignalControllerTileEntity controllerEntity;
    private int currentRSProfile;
    protected final Map<Integer, Map<SEProperty, String>> allRSStates = new HashMap<>();
    protected final Map<Direction, Map<EnumState, Integer>> enabledRSStates = new HashMap<>();
    protected int lastProfile;
    protected EnumMode currentMode = EnumMode.MANUELL;
    protected BlockPos linkedRSInput = null;
    protected int linkedRSInputProfile = -1;

    public ContainerSignalController(final GuiInfo info) {
        super(info);
        info.base = this;
        info.player.containerMenu = this;
    }

    @Override
    public void sendAllDataToRemote() {
        sendProperitesToClient();
    }

    private void sendProperitesToClient() {
        if (getInfo().pos == null) {
            return;
        }
        controllerEntity = (SignalControllerTileEntity) getInfo().world
                .getBlockEntity(getInfo().pos);
        linkedPos = controllerEntity.getLinkedPosition();
        if (linkedPos == null) {
            return;
        }
        currentSignal = controllerEntity.getLinkedSignal();
        final SignalStateInfo stateInfo = new SignalStateInfo(getInfo().world, linkedPos,
                getSignal());
        final Map<SEProperty, String> properties = SignalStateHandler.getStates(stateInfo);
        if (properties == null || properties.isEmpty())
            return;
        this.properties.clear();
        this.properties.putAll(properties);
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
        buffer.putInt(getSignal().getID());
        buffer.putByte((byte) currentMode.ordinal());
        buffer.putByte((byte) propertiesToSend.size());
        propertiesToSend.forEach(
                (property, value) -> packPropertyToBuffer(buffer, stateInfo, property, value));
        buffer.putByte((byte) controllerEntity.getProfile());
        buffer.putByte((byte) allStatesToSend.size());
        allStatesToSend.forEach((profile, props) -> {
            buffer.putByte(profile);
            buffer.putByte((byte) props.size());
            props.forEach(
                    (property, value) -> packPropertyToBuffer(buffer, stateInfo, property, value));
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
        final BlockPos linkedRSInput = controllerEntity.getLinkedRSInput();
        buffer.putByte((byte) (linkedRSInput != null ? 1 : 0));
        if (linkedRSInput != null)
            buffer.putBlockPos(linkedRSInput);
        buffer.putByte((byte) (controllerEntity.getProfileRSInput() != -1 ? 1 : 0));
        if (controllerEntity.getProfileRSInput() != -1)
            buffer.putByte(controllerEntity.getProfileRSInput());
        OpenSignalsMain.network.sendTo(getInfo().player, buffer.build());
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
        final int signalID = buffer.getInt();
        this.currentSignal = Signal.SIGNAL_IDS.get(signalID);
        currentMode = EnumMode.values()[buffer.getByteAsInt()];
        final int size = buffer.getByteAsInt();
        this.properties.clear();
        propertiesList = currentSignal.getProperties();
        for (int i = 0; i < size; i++) {
            final SEProperty property = propertiesList.get(buffer.getByteAsInt());
            properties.put(property, property.getObjFromID(buffer.getByteAsInt()));
        }
        lastProfile = buffer.getByteAsInt();
        allRSStates.clear();
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
        enabledRSStates.clear();
        final int enabledStatesSize = buffer.getByteAsInt();
        for (int i = 0; i < enabledStatesSize; i++) {
            final Direction direction = Direction.values()[buffer.getByteAsInt()];
            final int propSize = buffer.getByteAsInt();
            final Map<EnumState, Integer> states = new HashMap<>();
            for (int j = 0; j < propSize; j++) {
                final EnumState mode = EnumState.of(buffer);
                states.put(mode, buffer.getByteAsInt());
            }
            enabledRSStates.put(direction, states);
        }
        final boolean isInputConnected = buffer.getByte() == 1 ? true : false;
        if (isInputConnected)
            linkedRSInput = buffer.getBlockPos();
        final boolean isProfileInputenabled = buffer.getByte() == 1 ? true : false;
        if (isProfileInputenabled)
            linkedRSInputProfile = buffer.getByteAsInt();
        update();
    }

    @Override
    public void deserializeServer(final ByteBuffer buf) {
        final ReadBuffer buffer = new ReadBuffer(buf);
        if (propertiesList == null) {
            propertiesList = getSignal().getProperties();
        }
        final SignalControllerNetwork mode = SignalControllerNetwork.of(buffer);
        switch (mode) {
            case SEND_MODE: {
                currentMode = EnumMode.of(buffer);
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
                            new SignalStateInfo(getInfo().world, linkedPos, getSignal()), property,
                            value);
                } else if (currentMode.equals(EnumMode.SINGLE)) {
                    controllerEntity.updateRedstoneProfile((byte) currentRSProfile, property,
                            value);
                }
                break;
            }
            case REMOVE_PROPERTY: {
                if (currentMode.equals(EnumMode.SINGLE)) {
                    final SEProperty property = propertiesList.get(buffer.getByteAsInt());
                    controllerEntity.removePropertyFromProfile((byte) currentRSProfile, property);
                }
                break;
            }
            case REMOVE_PROFILE: {
                final EnumState state = EnumState.of(buffer);
                final Direction direction = deserializeDirection(buffer);
                controllerEntity.removeProfileFromDirection(direction, state);
                break;
            }
            case SET_PROFILE: {
                final EnumState state = EnumState.of(buffer);
                final Direction direction = deserializeDirection(buffer);
                final int profile = buffer.getByteAsInt();
                controllerEntity.updateEnabledStates(direction, state, profile);
                break;
            }
            case SET_RS_INPUT_PROFILE: {
                final int profile = buffer.getByteAsInt();
                controllerEntity.setProfileRSInput((byte) profile);
                break;
            }
            case REMOVE_RS_INPUT_PROFILE: {
                controllerEntity.setProfileRSInput((byte) -1);
                break;
            }
            case UNLINK_INPUT_POS: {
                final BlockPos linkedInput = controllerEntity.getLinkedRSInput();
                loadChunkAndGetTile(RedstoneIOTileEntity.class, (ServerLevel) getInfo().world,
                        linkedInput, (tile, _u) -> tile.unlinkController(getInfo().pos));
                controllerEntity.setLinkedRSInput(null);
                break;
            }
            default:
                break;
        }
    }

    private static Direction deserializeDirection(final ReadBuffer buffer) {
        return Direction.values()[buffer.getByteAsInt()];
    }

    public Map<SEProperty, String> getProperties() {
        return properties;
    }

    public Signal getSignal() {
        return currentSignal;
    }

    @Override
    public Player getPlayer() {
        return getInfo().player;
    }

    @Override
    public boolean stillValid(final Player playerIn) {
        if (playerIn instanceof Player) {
            this.getInfo().player = playerIn;
        }
        return true;
    }

    public BlockPos getPos() {
        return linkedPos;
    }
}