package com.troblecodings.signals.guis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.troblecodings.core.I18Wrapper;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.DrawUtil.DisableIntegerable;
import com.troblecodings.guilib.ecs.DrawUtil.EnumIntegerable;
import com.troblecodings.guilib.ecs.DrawUtil.SizeIntegerables;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIEnumerable;
import com.troblecodings.guilib.ecs.entitys.input.UIClickable;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UITexture;
import com.troblecodings.guilib.ecs.entitys.transform.UIScale;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.JsonEnum;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.enums.EnumMode;
import com.troblecodings.signals.enums.EnumState;
import com.troblecodings.signals.enums.SignalControllerNetwork;
import com.troblecodings.signals.handler.ClientNameHandler;
import com.troblecodings.signals.handler.ClientSignalStateHandler;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.models.SignalCustomModel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

@OnlyIn(Dist.CLIENT)
public class GuiSignalController extends GuiBase {

    private final ContainerSignalController controller;
    private final UIEntity lowerEntity = new UIEntity();
    private boolean loaded = false;
    private final Player player;
    private EnumMode currentMode;
    private int currentProfile = 0;
    private final PreviewSideBar previewSidebar = new PreviewSideBar(-8);
    private final PreviewSideBar previewRedstone = new PreviewSideBar(-8);

    public GuiSignalController(final GuiInfo info) {
        super(info);
        this.controller = (ContainerSignalController) info.base;
        this.player = info.player;
        initInternal();
    }

    private void initMode(final EnumMode mode) {
        lowerEntity.clear();
        this.currentMode = mode;
        sendCurrentMode();
        switch (mode) {
            case MANUELL:
                addManuellMode();
                break;
            case SINGLE:
                addSingleRSMode();
                break;
            case RS_INPUT:
                addRSInputMode();
                break;
            default:
                break;
        }
    }

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    private void createPageForSide(final Direction face, final UIEntity leftSide) {
        final UIEntity middlePart = new UIEntity();

        final IIntegerable<String> profile = SizeIntegerables.of("profile", 32,
                in -> String.valueOf(in));
        final UIEnumerable profileEnum = new UIEnumerable(32, "profile");
        leftSide.add(GuiElements.createEnumElement(profileEnum, profile, x -> {
            currentProfile = x;
            sendRSProfile(x);
            updateProfileProperties(middlePart, profileEnum);
        }, this.controller.lastProfile));
        currentProfile = this.controller.lastProfile;

        middlePart.setInherits(true);
        final UIBox boxMode = new UIBox(UIBox.VBOX, 1);
        middlePart.add(boxMode);

        updateProfileProperties(middlePart, profileEnum);
        leftSide.add(middlePart);

        int onIndex = -1;
        int offIndex = -1;
        if (controller.enabledRSStates.containsKey(face)) {
            final Map<EnumState, Integer> states = controller.enabledRSStates.get(face);
            if (states.containsKey(EnumState.ONSTATE)) {
                onIndex = states.get(EnumState.ONSTATE);
            }
            if (states.containsKey(EnumState.OFFSTATE)) {
                offIndex = states.get(EnumState.OFFSTATE);
            }
        }
        final IIntegerable<Object> offProfile = new DisableIntegerable(
                SizeIntegerables.of("profileOff." + face.getName(), 32, in -> String.valueOf(in)));
        final IIntegerable<Object> onProfile = new DisableIntegerable(
                SizeIntegerables.of("profileOn." + face.getName(), 32, in -> String.valueOf(in)));
        final UIEntity offElement = GuiElements.createEnumElement(offProfile,
                e -> sendAndSetProfile(face, e, EnumState.OFFSTATE), offIndex);
        final UIEntity onElement = GuiElements.createEnumElement(onProfile,
                e -> sendAndSetProfile(face, e, EnumState.ONSTATE), onIndex);
        leftSide.add(offElement);
        leftSide.add(onElement);
        leftSide.add(GuiElements.createPageSelect(boxMode));
    }

    private void updateProfileProperties(final UIEntity middlePart, final UIEnumerable profile) {
        middlePart.clearChildren();
        profile.setIndex(currentProfile);
        final Map<SEProperty, String> properties = controller.allRSStates
                .computeIfAbsent(currentProfile, _u -> new HashMap<>());
        controller.getProperties().forEach((property, value) -> {
            if (!properties.containsKey(property)) {
                properties.put(property, "DISABLED");
            }
        });
        ClientSignalStateHandler.getClientStates(new StateInfo(mc.level, controller.getPos()))
                .forEach((property, value) -> {
                    previewRedstone.addToRenderNormal(property,
                            property.getParent().getIDFromValue(value));
                });

        properties.forEach((property, value) -> {
            final JsonEnum enumJson = property.getParent();
            previewRedstone.addToRenderNormal(property, enumJson.getIDFromValue(value));
            final UIEntity entity = GuiElements
                    .createEnumElement(new DisableIntegerable<>(property), e -> {
                        sendPropertyToServer(property, e);
                        previewRedstone.addToRenderNormal(property, e);
                        if (e == -1) {
                            properties.remove(property);
                        } else {
                            properties.put(property, property.getObjFromID(e));
                        }
                        previewRedstone.update(controller.getSignal());
                    }, property.getParent().getIDFromValue(value));
            middlePart.add(entity);
        });
        previewRedstone.update(controller.getSignal());
    }

    private void addSingleRSMode() {
        this.lowerEntity.add(new UIBox(UIBox.HBOX, 2));

        final UIEntity leftSide = new UIEntity();
        leftSide.setInheritHeight(true);
        leftSide.setInheritWidth(true);
        leftSide.add(new UIBox(UIBox.VBOX, 2));
        this.lowerEntity.add(leftSide);
        this.lowerEntity.add(this.previewRedstone.get());

        final UIEntity rightSide = new UIEntity();
        rightSide.setInheritHeight(true);
        rightSide.setWidth(30);
        rightSide.add(new UIBox(UIBox.VBOX, 4));

        currentProfile = controller.lastProfile;
        createPageForSide(Direction.DOWN, leftSide);

        final Minecraft mc = Minecraft.getInstance();
        final BlockState state = OSBlocks.HV_SIGNAL_CONTROLLER.defaultBlockState();
        final BakedModel model = mc.getBlockRenderer().getBlockModel(state);
        final UIEnumerable toggle = new UIEnumerable(Direction.values().length, "singleModeFace");
        toggle.setOnChange(e -> {
            final Direction faceing = Direction.values()[e];

            final List<UIColor> colors = rightSide.findRecursive(UIColor.class);
            colors.forEach(c -> c.setColor(0x70000000));
            colors.get(e).setColor(0x70FF0000);
            leftSide.clearChildren();
            createPageForSide(faceing, leftSide);
        });
        rightSide.add(toggle);
        for (final Direction face : Direction.values()) {
            final List<BakedQuad> quad = model.getQuads(state, face, SignalCustomModel.RANDOM,
                    EmptyModelData.INSTANCE);
            final UIEntity faceEntity = new UIEntity();
            faceEntity.setWidth(20);
            faceEntity.setHeight(20);
            faceEntity.add(new UITexture(quad.get(0).getSprite()));
            final UIColor color = new UIColor(0x70000000);
            faceEntity.add(color);
            faceEntity.add(new UIClickable(e -> toggle.setIndex(face.ordinal())));
            final UILabel label = new UILabel(face.getName().substring(0, 1).toUpperCase());
            label.setTextColor(0xFFFFFFFF);
            faceEntity.add(label);
            rightSide.add(faceEntity);
        }
        this.lowerEntity.add(rightSide);
    }

    private void addRSInputMode() {
        this.lowerEntity.add(new UIBox(UIBox.VBOX, 5));
        final String posString = controller.linkedRSInput == null ? "not linked"
                : controller.linkedRSInput.toShortString();
        final UILabel label = new UILabel("Linked To: " + posString);
        lowerEntity.add(label);
        final IIntegerable<String> profile = new DisableIntegerable<>(
                SizeIntegerables.of("profile", 32, in -> String.valueOf(in)));
        lowerEntity.add(GuiElements.createEnumElement(profile, e -> sendRSInputProfileToServer(e),
                controller.linkedRSInputProfile));
        lowerEntity.add(GuiElements.createButton(I18Wrapper.format("gui.unlink"), e -> {
            unlinkInputPos();
            label.setText("Linked To: not linked");
        }));
    }

    private void initInternal() {
        this.entity.clear();

        final Signal signal = this.controller.getSignal();
        if (signal == null) {
            this.entity.add(new UILabel("Not connected"));
            return;
        }
        lowerEntity.setInherits(true);

        final String name = I18Wrapper.format("tile." + signal.delegate.name().getPath() + ".name")
                + "; Name: "
                + ClientNameHandler.getClientName(new StateInfo(mc.level, controller.getPos()));

        final UILabel titlelabel = new UILabel(name);
        titlelabel.setCenterX(false);

        final UIEntity titel = new UIEntity();
        titel.add(new UIScale(1.2f, 1.2f, 1));
        titel.add(titlelabel);
        titel.setInherits(true);

        final UIEntity header = new UIEntity();
        header.setInheritWidth(true);
        header.setHeight(45);
        header.add(new UIBox(UIBox.VBOX, 1));
        header.add(titel);
        final EnumIntegerable<EnumMode> enumMode = new EnumIntegerable<>(EnumMode.class);
        final UIEnumerable enumModes = new UIEnumerable(enumMode.count(), enumMode.getName());
        final UIEntity rsMode = GuiElements.createEnumElement(enumModes, enumMode, in -> {
            lowerEntity.clearChildren();
            initMode(enumMode.getObjFromID(in));
        }, controller.currentMode.ordinal());
        initMode(controller.currentMode);
        header.add(rsMode);

        final UIEntity middlePart = new UIEntity();
        middlePart.setInherits(true);
        middlePart.add(new UIBox(UIBox.VBOX, 4));
        middlePart.add(header);
        middlePart.add(lowerEntity);

        this.entity.add(GuiElements.createSpacerH(10));
        this.entity.add(middlePart);
        this.entity.add(GuiElements.createSpacerH(10));
        this.entity.add(new UIBox(UIBox.HBOX, 1));
    }

    private void addManuellMode() {
        final UIEntity list = new UIEntity();
        list.setInherits(true);
        final UIBox vbox = new UIBox(UIBox.VBOX, 1);
        list.add(vbox);

        final UIEntity leftSide = new UIEntity();
        leftSide.setInherits(true);
        leftSide.add(list);
        leftSide.add(GuiElements.createPageSelect(vbox));
        leftSide.add(new UIBox(UIBox.VBOX, 5));
        lowerEntity.add(leftSide);

        previewSidebar.clear();
        lowerEntity.add(previewSidebar.get());
        lowerEntity.add(new UIBox(UIBox.HBOX, 1));

        ClientSignalStateHandler.getClientStates(new StateInfo(mc.level, controller.getPos()))
                .forEach((property, value) -> previewSidebar.addToRenderNormal(property,
                        property.getParent().getIDFromValue(value)));

        final Map<SEProperty, String> map = this.controller.getProperties();
        if (map == null)
            return;
        map.forEach((property, value) -> {
            final UIEnumerable enumarable = new UIEnumerable(property.count(), property.getName());
            final int index = property.getParent().getIDFromValue(value);
            list.add(GuiElements.createEnumElement(enumarable, property, e -> {
                previewSidebar.addToRenderNormal(property, e);
                if (loaded) {
                    sendPropertyToServer(property, e);
                    previewSidebar.update(controller.getSignal());
                }
            }, index));
            previewSidebar.addToRenderNormal(property, index);
        });
        previewSidebar.update(controller.getSignal());
    }

    private void sendAndSetProfile(final Direction facing, final int profile,
            final EnumState state) {
        if (!loaded) {
            return;
        }
        final Map<EnumState, Integer> map = controller.enabledRSStates.computeIfAbsent(facing,
                _u -> new HashMap<>());
        if (profile == -1) {
            map.remove(state);
        } else {
            map.put(state, profile);
        }
        final WriteBuffer buffer = new WriteBuffer();
        if (profile == -1) {
            buffer.putEnumValue(SignalControllerNetwork.REMOVE_PROFILE);
        } else {
            buffer.putEnumValue(SignalControllerNetwork.SET_PROFILE);
        }
        buffer.putByte((byte) state.ordinal());
        buffer.putByte((byte) facing.ordinal());
        buffer.putByte((byte) profile);
        OpenSignalsMain.network.sendTo(player, buffer);
    }

    private void sendCurrentMode() {
        if (!loaded) {
            return;
        }
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalControllerNetwork.SEND_MODE);
        buffer.putByte((byte) currentMode.ordinal());
        OpenSignalsMain.network.sendTo(player, buffer);
    }

    private void sendRSProfile(final int profile) {
        if (!loaded) {
            return;
        }
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalControllerNetwork.SEND_RS_PROFILE);
        buffer.putByte((byte) profile);
        OpenSignalsMain.network.sendTo(player, buffer);
    }

    private void sendPropertyToServer(final SEProperty property, final int value) {
        if (!loaded) {
            return;
        }
        final WriteBuffer buffer = new WriteBuffer();
        if (value == -1) {
            buffer.putEnumValue(SignalControllerNetwork.REMOVE_PROPERTY);
        } else {
            buffer.putEnumValue(SignalControllerNetwork.SEND_PROPERTY);
        }
        buffer.putByte((byte) controller.getSignal().getIDFromProperty(property));
        buffer.putByte((byte) value);
        OpenSignalsMain.network.sendTo(player, buffer);
    }

    private void sendRSInputProfileToServer(final int profile) {
        if (!loaded) {
            return;
        }
        final WriteBuffer buffer = new WriteBuffer();
        if (profile == -1) {
            buffer.putEnumValue(SignalControllerNetwork.REMOVE_RS_INPUT_PROFILE);
        } else {
            buffer.putEnumValue(SignalControllerNetwork.SET_RS_INPUT_PROFILE);
        }
        buffer.putByte((byte) profile);
        OpenSignalsMain.network.sendTo(player, buffer);
    }

    private void unlinkInputPos() {
        controller.linkedRSInput = null;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalControllerNetwork.UNLINK_INPUT_POS);
        OpenSignalsMain.network.sendTo(player, buffer);
    }

    @Override
    public void updateFromContainer() {
        initInternal();
        loaded = true;
    }
}