package com.troblecodings.signals.guis;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.interfaces.UIClientSync;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.NetworkBufferWrappers;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.enums.EnumMode;
import com.troblecodings.signals.enums.EnumState;
import com.troblecodings.signals.enums.SignalControllerNetwork;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.tileentitys.IChunkLoadable;
import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;
import com.troblecodings.signals.tileentitys.SignalControllerTileEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class ContainerSignalController extends ContainerBase
        implements UIClientSync, IChunkLoadable {

    protected final Map<Integer, Map<SEProperty, String>> allRSStates = new HashMap<>();
    protected final Map<Direction, Map<EnumState, Integer>> enabledRSStates = new HashMap<>();
    protected int lastProfile;
    protected EnumMode currentMode = EnumMode.MANUELL;
    protected BlockPos linkedRSInput = null;
    protected int linkedRSInputProfile = -1;
    private final Map<SEProperty, String> properties = new HashMap<>();
    private Signal currentSignal = null;
    private List<SEProperty> propertiesList;
    private BlockPos linkedPos;
    private SignalControllerTileEntity controllerEntity;
    private int currentRSProfile;

    public ContainerSignalController(final GuiInfo info) {
        super(info);
    }

    @Override
    public void sendAllDataToRemote() {
        sendProperitesToClient();
    }

    private void sendProperitesToClient() {
        // TODO redo the SignalController
        if (info.pos == null) {
            return;
            controllerEntity =
                    (SignalControllerTileEntity) info.world.getBlockEntity(getInfo().pos);
            linkedPos = controllerEntity.getLinkedPosition();
            if (linkedPos == null)
                return;
            currentSignal = controllerEntity.getLinkedSignal();
            final SignalStateInfo stateInfo =
                    new SignalStateInfo(info.world, linkedPos, getSignal());
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
            final Map<Byte, Map<SEProperty, String>> allStates =
                    new HashMap<>(controllerEntity.getAllStates());
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
            final Map<Direction, Map<EnumState, Byte>> enabledStates =
                    controllerEntity.getEnabledStates();
            currentMode = controllerEntity.getLastMode();

            final WriteBuffer buffer = new WriteBuffer();
            buffer.putBlockPos(stateInfo.pos);
            buffer.putInt(getSignal().getID());
            buffer.putByte((byte) currentMode.ordinal());
            buffer.putMapWithCombinedValueConsumer(propertiesToSend,
                    NetworkBufferWrappers.getSEPropertyConsumer(getSignal()), (buf, prop,
                            value) -> buf.putByte((byte) prop.getParent().getIDFromValue(value)));
            buffer.putByte((byte) controllerEntity.getProfile());
            buffer.putMap(allStatesToSend, WriteBuffer.BYTE_CONSUMER,
                    (buf, props) -> buf.putMapWithCombinedValueConsumer(props,
                            NetworkBufferWrappers.getSEPropertyConsumer(getSignal()),
                            (mapBuf, prop, value) -> mapBuf
                                    .putByte((byte) prop.getParent().getIDFromValue(value))));

            buffer.putMap(enabledStates, WriteBuffer.getEnumConsumer(), (buf, map) -> buf
                    .putMap(map, WriteBuffer.getEnumConsumer(), WriteBuffer.BYTE_CONSUMER));
            final BlockPos linkedRSInput = controllerEntity.getLinkedRSInput();
            buffer.putBoolean(linkedRSInput != null);
            if (linkedRSInput != null) {
                buffer.putBlockPos(linkedRSInput);
            }
            buffer.putBoolean(controllerEntity.getProfileRSInput() != -1);
            if (controllerEntity.getProfileRSInput() != -1) {
                buffer.putByte(controllerEntity.getProfileRSInput());
            }
            OpenSignalsMain.network.sendTo(info.player, buffer.build());
        }
    }

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        final ReadBuffer buffer = new ReadBuffer(buf);
        linkedPos = buffer.getBlockPos();
        final int signalID = buffer.getInt();
        currentSignal = Signal.SIGNAL_IDS.get(signalID);
        currentMode = EnumMode.values()[buffer.getByteToUnsignedInt()];
        this.properties.clear();
        this.properties.putAll(buffer.getMapWithCombinedValueFunc(
                NetworkBufferWrappers.getSEPropertyFunc(currentSignal),
                (buf, prop) -> prop.getObjFromID(buffer.getByteToUnsignedInt())));
        propertiesList = currentSignal.getProperties();
        lastProfile = buffer.getByteToUnsignedInt();
        allRSStates.clear();
        allRSStates.putAll(buffer.getMap(ReadBuffer.BYTE_TO_INT_FUNCTION,
                (buf) -> buf.getMapWithCombinedValueFunc(
                        NetworkBufferWrappers.getSEPropertyFunc(currentSignal),
                        (mapBuf, prop) -> prop.getObjFromID(mapBuf.getByteToUnsignedInt()))));
        enabledRSStates.clear();
        enabledRSStates.putAll(buffer.getMap(ReadBuffer.getEnumFunction(Direction.class),
                buf -> buf.getMap(ReadBuffer.getEnumFunction(EnumState.class),
                        ReadBuffer.BYTE_TO_INT_FUNCTION)));
        final boolean isInputConnected = buffer.getBoolean();
        if (isInputConnected) {
            linkedRSInput = buffer.getBlockPos();
        }
    }

    @Override
    public void deserializeServer(final ByteBuffer buf) {
        final ReadBuffer buffer = new ReadBuffer(buf);
        if (propertiesList == null) {
            propertiesList = getSignal().getProperties();
        }
        final SignalControllerNetwork mode = buffer.getEnumValue(SignalControllerNetwork.class);
        switch (mode) {
            case SEND_MODE: {
                currentMode = EnumMode.of(buffer);
                controllerEntity.setLastMode(currentMode);
                break;
            }
            case SEND_RS_PROFILE: {
                currentRSProfile = buffer.getByteToUnsignedInt();
                break;
            }
            case SEND_PROPERTY: {
                final SEProperty property = propertiesList.get(buffer.getByteToUnsignedInt());
                final String value = property.getObjFromID(buffer.getByteToUnsignedInt());
                if (currentMode.equals(EnumMode.MANUELL)) {
                    SignalStateHandler.setState(
                            new SignalStateInfo(info.world, linkedPos, getSignal()), property,
                            value);
                } else if (currentMode.equals(EnumMode.SINGLE)) {
                    controllerEntity.updateRedstoneProfile((byte) currentRSProfile, property,
                            value);
                }
                break;
            }
            case REMOVE_PROPERTY: {
                if (currentMode.equals(EnumMode.SINGLE)) {
                    final SEProperty property = propertiesList.get(buffer.getByteToUnsignedInt());
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
                final int profile = buffer.getByteToUnsignedInt();
                controllerEntity.updateEnabledStates(direction, state, profile);
                break;
            }
            case SET_RS_INPUT_PROFILE: {
                final int profile = buffer.getByteToUnsignedInt();
                controllerEntity.setProfileRSInput((byte) profile);
                break;
            }
            case REMOVE_RS_INPUT_PROFILE: {
                controllerEntity.setProfileRSInput((byte) -1);
                break;
            }
            case UNLINK_INPUT_POS: {
                final BlockPos linkedInput = controllerEntity.getLinkedRSInput();
                loadChunkAndGetTile(RedstoneIOTileEntity.class, (ServerWorld) info.world,
                        linkedInput, (tile, _u) -> tile.unlinkController(info.pos));
                controllerEntity.setLinkedRSInput(null);
                break;
            }
            default:
                break;
        }
        controllerEntity.setChanged();
    }

    private static Direction deserializeDirection(final ReadBuffer buffer) {
        return Direction.values()[buffer.getByteToUnsignedInt()];
    }

    public Map<SEProperty, String> getProperties() {
        return properties;
    }

    public Signal getSignal() {
        return currentSignal;
    }

    @Override
    public PlayerEntity getPlayer() {
        return getInfo().player;
    }

    @Override
    public boolean stillValid(final PlayerEntity playerIn) {
        if (playerIn instanceof PlayerEntity) {
            this.getInfo().player = playerIn;
        }
        return true;
    }

    public BlockPos getPos() {
        return linkedPos;
    }
}