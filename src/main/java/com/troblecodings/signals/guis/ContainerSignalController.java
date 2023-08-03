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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class ContainerSignalController extends ContainerBase implements UIClientSync, INetworkSync {

    private final AtomicReference<Map<SEProperty, String>> reference = new AtomicReference<>();
    private final AtomicReference<Signal> referenceBlock = new AtomicReference<>();
    private final GuiInfo info;
    private List<SEProperty> propertiesList;
    private BlockPos linkedPos;
    private SignalControllerTileEntity controllerEntity;
    private int currentRSProfile;
    protected final Map<Integer, Map<SEProperty, String>> allRSStates = new HashMap<>();
    protected final Map<EnumFacing, Map<EnumState, Integer>> enabledRSStates = new HashMap<>();
    protected int lastProfile;
    protected EnumMode currentMode = EnumMode.MANUELL;
    protected BlockPos linkedRSInput = null;
    protected int linkedRSInputProfile = -1;

    public ContainerSignalController(final GuiInfo info) {
        super(info);
        info.base = this;
        info.player.openContainer = this;
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
        controllerEntity = (SignalControllerTileEntity) info.world.getTileEntity(info.pos);
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
        final Map<EnumFacing, Map<EnumState, Byte>> enabledStates = controllerEntity
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
            final EnumFacing direction = EnumFacing.values()[buffer.getByteAsInt()];
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
                    final SEProperty property = propertiesList.get(buffer.getByteAsInt());
                    controllerEntity.removePropertyFromProfile((byte) currentRSProfile, property);
                }
                break;
            }
            case REMOVE_PROFILE: {
                final EnumState state = EnumState.of(buffer);
                final EnumFacing direction = deserializeDirection(buffer);
                controllerEntity.removeProfileFromDirection(direction, state);
                break;
            }
            case SET_PROFILE: {
                final EnumState state = EnumState.of(buffer);
                final EnumFacing direction = deserializeDirection(buffer);
                final int profile = buffer.getByteAsInt();
                controllerEntity.updateEnabledStates(direction, state, profile);
                break;
            }
            default:
                break;
        }
    }

    private static EnumFacing deserializeDirection(final ReadBuffer buffer) {
        return EnumFacing.values()[buffer.getByteAsInt()];
    }

    public Map<SEProperty, String> getReference() {
        return reference.get();
    }

    public Signal getSignal() {
        return referenceBlock.get();
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