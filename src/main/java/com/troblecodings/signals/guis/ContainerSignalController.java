package com.troblecodings.signals.guis;

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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class ContainerSignalController extends ContainerBase
        implements UIClientSync, IChunkLoadable {

    protected final Map<Integer, Map<SEProperty, String>> allRSStates = new HashMap<>();
    protected final Map<Direction, Map<EnumState, Integer>> enabledRSStates = new HashMap<>();
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
        if (info.pos == null)
            return;
        }
        controllerEntity = (SignalControllerTileEntity) info.world.getTileEntity(info.pos);
        linkedPos = controllerEntity.getLinkedPosition();
        if (linkedPos == null)
            return;
        currentSignal = controllerEntity.getLinkedSignal();
        final SignalStateInfo stateInfo = new SignalStateInfo(info.world, linkedPos, getSignal());
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
        buffer.putEnumValue(currentMode);
        buffer.putMapWithCombinedValueConsumer(propertiesToSend,
                NetworkBufferWrappers.getSEPropertyConsumer(getSignal()),
                (buf, prop, value) -> buf.putByte((byte) prop.getParent().getIDFromValue(value)));
        buffer.putMap(allStatesToSend, WriteBuffer.BYTE_CONSUMER,
                (buf, props) -> buf.putMapWithCombinedValueConsumer(props,
                        NetworkBufferWrappers.getSEPropertyConsumer(getSignal()),
                        (mapBuf, prop, value) -> mapBuf
                                .putByte((byte) prop.getParent().getIDFromValue(value))));

        buffer.putMap(enabledStates, WriteBuffer.getEnumConsumer(), (buf, map) -> buf.putMap(map,
                WriteBuffer.getEnumConsumer(), WriteBuffer.BYTE_CONSUMER));
        final BlockPos linkedRSInput = controllerEntity.getLinkedRSInput();
        buffer.putBoolean(linkedRSInput != null);
        if (linkedRSInput != null) {
            buffer.putBlockPos(linkedRSInput);
        }
        buffer.putBoolean(controllerEntity.getProfileRSInput() != -1);
        if (controllerEntity.getProfileRSInput() != -1) {
            buffer.putByte(controllerEntity.getProfileRSInput());
        }
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    @Override
    public void deserializeClient(final ReadBuffer buffer) {
        linkedPos = buffer.getBlockPos();
        final int signalID = buffer.getInt();
        this.currentSignal = Signal.SIGNAL_IDS.get(signalID);
        currentMode = buffer.getEnumValue(EnumMode.class);
        this.properties.clear();
        this.properties.putAll(buffer.getMapWithCombinedValueFunc(
                NetworkBufferWrappers.getSEPropertyFunc(currentSignal),
                (buf, prop) -> prop.getObjFromID(buffer.getByteToUnsignedInt())));
        propertiesList = currentSignal.getProperties();
        allRSStates.clear();
        allRSStates.putAll(buffer.getMap(ReadBuffer.BYTE_TO_INT_FUNCTION,
                (buf) -> buf.getMapWithCombinedValueFunc(
                        NetworkBufferWrappers.getSEPropertyFunc(currentSignal),
                        (mapBuf, prop) -> prop.getObjFromID(mapBuf.getByteToUnsignedInt()))));
        enabledRSStates.clear();
        enabledRSStates.putAll(buffer.getMap(ReadBuffer.getEnumFunction(EnumFacing.class),
                buf -> buf.getMap(ReadBuffer.getEnumFunction(EnumState.class),
                        ReadBuffer.BYTE_TO_INT_FUNCTION)));
        final boolean isInputConnected = buffer.getBoolean();
        if (isInputConnected) {
            linkedRSInput = buffer.getBlockPos();
        }
        final boolean isProfileInputenabled = buffer.getBoolean();
        if (isProfileInputenabled) {
            linkedRSInputProfile = buffer.getByteToUnsignedInt();
        }
        update();
    }

    @Override
    public void deserializeServer(final ReadBuffer buffer) {
        if (propertiesList == null) {
            propertiesList = getSignal().getProperties();
        }
        final SignalControllerNetwork mode = buffer.getEnumValue(SignalControllerNetwork.class);
        switch (mode) {
            case SEND_MODE: {
                currentMode = buffer.getEnumValue(EnumMode.class);
                controllerEntity.setLastMode(currentMode);
                break;
            }
            case SEND_RS_PROFILE: {
                if (buffer.getBoolean()) {
                    controllerEntity.removeProfile(buffer.getByte());
                }
                currentRSProfile = buffer.getByteToUnsignedInt();
                break;
            }
            case SEND_PROPERTY: {
                final SEProperty property = propertiesList.get(buffer.getByteToUnsignedInt());
                final int valueID = buffer.getByteToUnsignedInt();
                final String value = property.getObjFromID(valueID);
                if (currentMode.equals(EnumMode.MANUELL)) {
                    SignalStateHandler.setState(
                            new SignalStateInfo(info.world, linkedPos, getSignal()), property,
                            value);
                } else if (currentMode.equals(EnumMode.SINGLE)) {
                    if (valueID == -1) {
                        controllerEntity.removePropertyFromProfile((byte) currentRSProfile,
                                property);
                        break;
                    }
                    controllerEntity.updateRedstoneProfile((byte) currentRSProfile, property,
                            value);
                }
                break;
            }
            case SET_PROFILE: {
                final Direction direction = buffer.getEnumValue(Direction.class);
                final EnumState state = buffer.getEnumValue(EnumState.class);
                final int profile = buffer.getByteToUnsignedInt();
                if (profile == -1) {
                    controllerEntity.removeProfileFromDirection(direction, state);
                } else {
                    controllerEntity.updateEnabledStates(direction, state, profile);
                }
                break;
            }
            case SET_RS_INPUT_PROFILE: {
                final int profile = buffer.getByteToUnsignedInt();
                controllerEntity.setProfileRSInput((byte) profile);
                break;
            }
            case UNLINK_INPUT_POS: {
                final BlockPos linkedInput = controllerEntity.getLinkedRSInput();
                if (info.pos == null || linkedInput == null) {
                    break;
                }
                loadChunkAndGetTile(RedstoneIOTileEntity.class, (ServerLevel) info.world,
                        linkedInput, (tile, _u) -> tile.unlinkController(info.pos));
                controllerEntity.setLinkedRSInput(null);
                break;
            }
            default:
                break;
        }
        controllerEntity.markDirty();
    }

    protected void sendAndSetProfile(final Direction facing, final int profile,
            final EnumState state) {
        final Map<EnumState, Integer> map =
                enabledRSStates.computeIfAbsent(facing, _u -> new HashMap<>());
        if (profile == -1) {
            map.remove(state);
        } else {
            map.put(state, profile);
        }
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalControllerNetwork.SET_PROFILE);
        buffer.putEnumValue(facing);
        buffer.putEnumValue(state);
        buffer.putByte((byte) profile);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void sendCurrentMode() {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalControllerNetwork.SEND_MODE);
        buffer.putEnumValue(currentMode);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void sendRSProfileRemove(final int profileToRemove, final int newProfile) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalControllerNetwork.SEND_RS_PROFILE);
        buffer.putBoolean(true);
        buffer.putByte((byte) profileToRemove);
        buffer.putByte((byte) newProfile);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void sendRSProfile(final int profile) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalControllerNetwork.SEND_RS_PROFILE);
        buffer.putBoolean(false);
        buffer.putByte((byte) profile);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void sendPropertyToServer(final SEProperty property, final int value) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalControllerNetwork.SEND_PROPERTY);
        buffer.putByte((byte) currentSignal.getIDFromProperty(property));
        buffer.putByte((byte) value);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void sendRSInputProfileToServer(final int profile) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalControllerNetwork.SET_RS_INPUT_PROFILE);
        buffer.putByte((byte) profile);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void unlinkInputPos() {
        linkedRSInput = null;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalControllerNetwork.UNLINK_INPUT_POS);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    public Map<SEProperty, String> getProperties() {
        return properties;
    }

    public Signal getSignal() {
        return currentSignal;
    }

    @Override
    public EntityPlayer getPlayer() {
        return info.player;
    }

    @Override
    public boolean canInteractWith(final EntityPlayer playerIn) {
        if (playerIn instanceof EntityPlayerMP) {
            this.info.player = playerIn;
        }
        return true;
    }

    public BlockPos getPos() {
        return linkedPos;
    }
}