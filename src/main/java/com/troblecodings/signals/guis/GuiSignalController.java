package com.troblecodings.signals.guis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.DrawUtil.DisableIntegerable;
import com.troblecodings.guilib.ecs.DrawUtil.EnumIntegerable;
import com.troblecodings.guilib.ecs.DrawUtil.SizeIntegerables;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.entitys.UIBlockRender;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIEnumerable;
import com.troblecodings.guilib.ecs.entitys.input.UIClickable;
import com.troblecodings.guilib.ecs.entitys.input.UIDrag;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UIScissor;
import com.troblecodings.guilib.ecs.entitys.render.UITexture;
import com.troblecodings.guilib.ecs.entitys.render.UIToolTip;
import com.troblecodings.guilib.ecs.entitys.transform.UIIndependentTranslate;
import com.troblecodings.guilib.ecs.entitys.transform.UIRotate;
import com.troblecodings.guilib.ecs.entitys.transform.UIScale;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.WriteBuffer;
import com.troblecodings.signals.enums.EnumMode;
import com.troblecodings.signals.enums.EnumState;
import com.troblecodings.signals.enums.SignalControllerNetwork;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSignalController extends GuiBase {

    private final ContainerSignalController controller;
    private final UIEntity lowerEntity = new UIEntity();
    private boolean previewMode = false;
    private boolean loaded = false;
    private final EntityPlayer player;
    private EnumMode currentMode;
    private int currentProfile = 0;
    private final BlockPos pos;

    public GuiSignalController(final GuiInfo info) {
        super(info);
        this.controller = (ContainerSignalController) info.base;
        this.player = info.player;
        this.pos = info.pos;
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
    private void createPageForSide(final EnumFacing face, final UIEntity leftSide,
            final UIBlockRender bRender) {
        final UIEntity middlePart = new UIEntity();

        final IIntegerable<String> profile = SizeIntegerables.of("profile", 32,
                in -> String.valueOf(in));
        final UIEnumerable profileEnum = new UIEnumerable(32, "profile");
        leftSide.add(GuiElements.createEnumElement(profileEnum, profile, x -> {
            currentProfile = x;
            sendRSProfile(x);
            updateProfileProperties(middlePart, bRender, profileEnum);
            applyModelChange(bRender);
        }, this.controller.lastProfile));
        currentProfile = this.controller.lastProfile;

        middlePart.setInheritHeight(true);
        middlePart.setInheritWidth(true);
        final UIBox boxMode = new UIBox(UIBox.VBOX, 1);
        middlePart.add(boxMode);

        updateProfileProperties(middlePart, bRender, profileEnum);
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

    private void updateProfileProperties(final UIEntity middlePart, final UIBlockRender bRender,
            final UIEnumerable profile) {
        middlePart.clearChildren();
        profile.setIndex(currentProfile);
        final Map<SEProperty, String> properties = controller.allRSStates
                .computeIfAbsent(currentProfile, _u -> new HashMap<>());
        controller.getReference().forEach((property, value) -> {
            if (!properties.containsKey(property)) {
                properties.put(property, "DISABLED");
            }
        });
        properties.forEach((property, value) -> {
            final UIEntity entity = GuiElements
                    .createEnumElement(new DisableIntegerable<>(property), e -> {
                        applyModelChange(bRender);
                        sendPropertyToServer(property, e);
                        final Map<SEProperty, String> map = controller.allRSStates
                                .computeIfAbsent(currentProfile, _u -> new HashMap<>());
                        if (e == -1) {
                            map.remove(property);
                        } else {
                            map.put(property, property.getObjFromID(e));
                        }
                    }, property.getParent().getIDFromValue(value));
            middlePart.add(entity);
        });
    }

    private void addSingleRSMode() {
        this.lowerEntity.add(new UIBox(UIBox.HBOX, 2));

        final UIEntity leftSide = new UIEntity();
        leftSide.setInheritHeight(true);
        leftSide.setInheritWidth(true);
        leftSide.add(new UIBox(UIBox.VBOX, 2));
        this.lowerEntity.add(leftSide);

        final UIBlockRender bRender = new UIBlockRender();
        this.lowerEntity.add(createPreview(bRender));

        final UIEntity rightSide = new UIEntity();
        rightSide.setInheritHeight(true);
        rightSide.setWidth(30);
        rightSide.add(new UIBox(UIBox.VBOX, 4));

        currentProfile = controller.lastProfile;
        createPageForSide(EnumFacing.DOWN, leftSide, bRender);

        final Minecraft mc = Minecraft.getMinecraft();
        final IBlockState state = mc.player.world.getBlockState(pos);
        final IBakedModel model = mc.getBlockRendererDispatcher().getModelForState(state);
        final UIEnumerable toggle = new UIEnumerable(EnumFacing.values().length, "singleModeFace");
        toggle.setOnChange(e -> {
            final EnumFacing faceing = EnumFacing.values()[e];

            final List<UIColor> colors = rightSide.findRecursive(UIColor.class);
            colors.forEach(c -> c.setColor(0x70000000));
            colors.get(e).setColor(0x70FF0000);
            leftSide.clearChildren();
            createPageForSide(faceing, leftSide, bRender);
        });
        rightSide.add(toggle);
        for (final EnumFacing face : EnumFacing.VALUES) {
            final List<BakedQuad> quad = model.getQuads(state, face, 0);
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
        final BlockPos link = controller.linkedRSInput;
        final String posString = link == null ? "not linked"
                : link.getX() + ", " + link.getY() + ", " + link.getZ();
        final UILabel label = new UILabel("Linked To: " + posString);
        lowerEntity.add(label);
        final IIntegerable<String> profile = new DisableIntegerable<>(
                SizeIntegerables.of("profile", 32, in -> String.valueOf(in)));
        lowerEntity.add(GuiElements.createEnumElement(profile, e -> sendRSInputProfileToServer(e),
                controller.linkedRSInputProfile));
        lowerEntity.add(GuiElements.createButton(I18n.format("gui.unlink"), e -> {
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
        lowerEntity.setInheritHeight(true);
        lowerEntity.setInheritWidth(true);

        final String name = I18n
                .format("tile." + signal.getRegistryName().getResourcePath() + ".name");

        final UILabel titlelabel = new UILabel(name);
        titlelabel.setCenterX(false);

        final UIEntity titel = new UIEntity();
        titel.add(new UIScale(1.2f, 1.2f, 1));
        titel.add(titlelabel);
        titel.setInheritHeight(true);
        titel.setInheritWidth(true);

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
        middlePart.setInheritHeight(true);
        middlePart.setInheritWidth(true);
        middlePart.add(new UIBox(UIBox.VBOX, 4));
        middlePart.add(header);
        middlePart.add(lowerEntity);

        this.entity.add(GuiElements.createSpacerH(10));
        this.entity.add(middlePart);
        this.entity.add(GuiElements.createSpacerH(10));
        this.entity.add(new UIBox(UIBox.HBOX, 1));
    }

    private UIEntity createPreview(final UIBlockRender blockRender) {
        final UIToolTip tooltip = new UIToolTip(I18n.format("controller.preview", previewMode));

        final UIEntity rightSide = new UIEntity();
        rightSide.setWidth(60);
        rightSide.setInheritHeight(true);
        final UIRotate rotation = new UIRotate();
        rotation.setRotateY(180);
        rightSide.add(new UIClickable(e -> {
            previewMode = !previewMode;
            applyModelChange(blockRender);
            tooltip.setDescripton(I18n.format("controller.preview", previewMode));
        }, 1));
        rightSide.add(
                new UIDrag((x, y) -> rotation.setRotateY((float) (rotation.getRotateY() + x))));
        rightSide.add(tooltip);

        rightSide.add(new UIScissor());
        rightSide.add(new UIIndependentTranslate(35, 150, 40));
        rightSide.add(rotation);
        rightSide.add(new UIIndependentTranslate(-0.5, -3.5, -0.5));
        rightSide.add(new UIScale(20, -20, 20));
        rightSide.add(blockRender);
        return rightSide;
    }

    private void addManuellMode() {
        final UIEntity list = new UIEntity();
        list.setInheritHeight(true);
        list.setInheritWidth(true);
        final UIBox vbox = new UIBox(UIBox.VBOX, 1);
        list.add(vbox);

        final UIEntity leftSide = new UIEntity();
        leftSide.setInheritHeight(true);
        leftSide.setInheritWidth(true);
        leftSide.add(list);
        leftSide.add(GuiElements.createPageSelect(vbox));
        leftSide.add(new UIBox(UIBox.VBOX, 5));
        lowerEntity.add(leftSide);

        final UIBlockRender blockRender = new UIBlockRender();
        lowerEntity.add(createPreview(blockRender));
        lowerEntity.add(new UIBox(UIBox.HBOX, 1));

        final Map<SEProperty, String> map = this.controller.getReference();
        if (map == null)
            return;
        map.forEach((property, value) -> {
            final UIEnumerable enumarable = new UIEnumerable(property.count(), property.getName());
            final int index = property.getParent().getIDFromValue(value);
            list.add(GuiElements.createEnumElement(enumarable, property, e -> {
                if (loaded) {
                    sendPropertyToServer(property, enumarable.getIndex());
                }
                applyModelChange(blockRender);
            }, index));
        });
        applyModelChange(blockRender);
    }

    private void sendAndSetProfile(final EnumFacing facing, final int profile,
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
        buffer.putByte((byte) (profile == -1 ? SignalControllerNetwork.REMOVE_PROFILE.ordinal()
                : SignalControllerNetwork.SET_PROFILE.ordinal()));
        buffer.putByte((byte) state.ordinal());
        buffer.putByte((byte) facing.ordinal());
        buffer.putByte((byte) profile);
        OpenSignalsMain.network.sendTo(player, buffer.build());
    }

    private void sendCurrentMode() {
        if (!loaded) {
            return;
        }
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte((byte) SignalControllerNetwork.SEND_MODE.ordinal());
        buffer.putByte((byte) currentMode.ordinal());
        OpenSignalsMain.network.sendTo(player, buffer.build());
    }

    private void sendRSProfile(final int profile) {
        if (!loaded) {
            return;
        }
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte((byte) SignalControllerNetwork.SEND_RS_PROFILE.ordinal());
        buffer.putByte((byte) profile);
        OpenSignalsMain.network.sendTo(player, buffer.build());
    }

    private void sendPropertyToServer(final SEProperty property, final int value) {
        if (!loaded) {
            return;
        }
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte((byte) (value == -1 ? SignalControllerNetwork.REMOVE_PROPERTY.ordinal()
                : SignalControllerNetwork.SEND_PROPERTY.ordinal()));
        buffer.putByte((byte) controller.getSignal().getIDFromProperty(property));
        buffer.putByte((byte) value);
        OpenSignalsMain.network.sendTo(player, buffer.build());
    }

    private void sendRSInputProfileToServer(final int profile) {
        if (!loaded) {
            return;
        }
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte(
                (byte) (profile == -1 ? SignalControllerNetwork.REMOVE_RS_INPUT_PROFILE.ordinal()
                        : SignalControllerNetwork.SET_RS_INPUT_PROFILE.ordinal()));
        buffer.putByte((byte) profile);
        OpenSignalsMain.network.sendTo(player, buffer.build());
    }

    private void unlinkInputPos() {
        controller.linkedRSInput = null;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte((byte) SignalControllerNetwork.UNLINK_INPUT_POS.ordinal());
        OpenSignalsMain.network.sendTo(player, buffer.build());
    }

    @Override
    public void updateFromContainer() {
        initInternal();
        loaded = true;
    }

    private void applyModelChange(final UIBlockRender blockRender) {
        // TODO new model system
    }

    @Override
    public ContainerBase getNewGuiContainer(final GuiInfo info) {
        return new ContainerSignalController(info);
    }

}