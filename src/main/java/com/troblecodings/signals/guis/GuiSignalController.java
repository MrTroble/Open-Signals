package com.troblecodings.signals.guis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.common.collect.Maps;
import com.troblecodings.core.I18Wrapper;
import com.troblecodings.guilib.ecs.DrawUtil.DisableIntegerable;
import com.troblecodings.guilib.ecs.DrawUtil.EnumIntegerable;
import com.troblecodings.guilib.ecs.DrawUtil.SizeIntegerables;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIEnumerable;
import com.troblecodings.guilib.ecs.entitys.UIScrollBox;
import com.troblecodings.guilib.ecs.entitys.input.UIClickable;
import com.troblecodings.guilib.ecs.entitys.input.UIScroll;
import com.troblecodings.guilib.ecs.entitys.render.UIBorder;
import com.troblecodings.guilib.ecs.entitys.render.UIButton;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UIScissor;
import com.troblecodings.guilib.ecs.entitys.render.UITexture;
import com.troblecodings.guilib.ecs.entitys.render.UIToolTip;
import com.troblecodings.guilib.ecs.entitys.transform.UIScale;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.JsonEnum;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.enums.EnumMode;
import com.troblecodings.signals.enums.EnumState;
import com.troblecodings.signals.enums.SignalBoxIcons;
import com.troblecodings.signals.enums.SignalBoxIcons.SignalBoxSymbols;
import com.troblecodings.signals.handler.ClientNameHandler;
import com.troblecodings.signals.handler.ClientSignalStateHandler;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.models.SignalCustomModel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

@OnlyIn(Dist.CLIENT)
public class GuiSignalController extends GuiBase {

    public static final int MAX_PROFILES = 30;

    private final ContainerSignalController controller;
    private final UIEntity lowerEntity = new UIEntity();
    private boolean loaded = false;
    private int currentProfile = -1;
    private final PreviewSideBar previewSidebar = new PreviewSideBar(-8);
    private final PreviewSideBar previewRedstone = new PreviewSideBar(-8);

    private final UIButton[] buttonsRSMode = new UIButton[MAX_PROFILES + 1];

    public GuiSignalController(final GuiInfo info) {
        super(info);
        this.controller = (ContainerSignalController) info.base;
        initInternal();
    }

    private void initMode(final EnumMode mode) {
        lowerEntity.clear();
        controller.currentMode = mode;
        controller.sendCurrentMode();
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

    private void updateProfileProperties(final UIEntity middlePart, final int profile) {
        this.updateProfileProperties(middlePart, profile, false);
    }

    private void updateProfileProperties(final UIEntity middlePart, final int profile,
            final boolean onlyUpdatePreview) {
        middlePart.clearChildren();
        if (profile == -1)
            return;
        final Map<SEProperty, String> properties =
                controller.allRSStates.computeIfAbsent(profile, _u -> new HashMap<>());
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
            if (!onlyUpdatePreview) {
                final UIEntity entity =
                        GuiElements.createEnumElement(new DisableIntegerable<>(property), e -> {
                            controller.sendPropertyToServer(property, e);
                            previewRedstone.addToRenderNormal(property, e);
                            if (e == -1) {
                                properties.remove(property);
                            } else {
                                properties.put(property, property.getObjFromID(e));
                            }
                            previewRedstone.update(controller.getSignal());
                        }, property.getParent().getIDFromValue(value));
                middlePart.add(entity);
            }
        });
        previewRedstone.update(controller.getSignal());
    }

    private void addProfileSelection(final UIEntity mainEntity) {
        final UIEntity entity = new UIEntity();
        entity.setInheritWidth(true);
        entity.setHeight(55);
        entity.add(new UIBorder(0x70000000, 2));
        entity.add(new UIBox(UIBox.VBOX, 0));
        entity.add(GuiElements.createSpacerV(2));

        mainEntity.add(entity);
        mainEntity.add(GuiElements.createSpacerV(5));

        final UIEntity firstRow = new UIEntity();
        firstRow.setInheritWidth(true);
        firstRow.setHeight(20);
        firstRow.add(new UIBox(UIBox.HBOX, 5));
        entity.add(firstRow);

        entity.add(GuiElements.createSpacerV(5));

        final UIEntity selectionEntity = new UIEntity();
        selectionEntity.setInherits(true);
        selectionEntity.add(new UIBox(UIBox.HBOX, 2));
        selectionEntity.setX(2);
        entity.add(selectionEntity);

        final UIEntity listWithScroll = new UIEntity();
        listWithScroll.setInheritWidth(true);
        listWithScroll.setHeight(30);
        listWithScroll.add(new UIBox(UIBox.VBOX, 2));
        listWithScroll.add(new UIScissor());

        final UIEntity list = new UIEntity();
        listWithScroll.add(list);
        list.setInherits(true);

        final UIScrollBox scrollbox = new UIScrollBox(UIBox.HBOX, 2);
        list.add(scrollbox);

        final UIEntity propertyEntity = new UIEntity();
        propertyEntity.setInherits(true);
        propertyEntity.add(new UIBox(UIBox.VBOX, 1));
        mainEntity.add(propertyEntity);

        firstRow.add(getRedstoneModeLabel());
        firstRow.add(getAddButton(list, propertyEntity));
        firstRow.add(getRemoveIcon());

        selectionEntity.add(listWithScroll);

        addProfilesToList(list, propertyEntity);

        if (buttonsRSMode.length > 0) {
            for (final UIButton b : buttonsRSMode) {
                if (b != null) {
                    enableButton(b);
                    break;
                }
            }
            updateProfileProperties(propertyEntity, currentProfile);
        }
        final UIScroll scroll = new UIScroll();
        final UIEntity scrollBar = GuiElements.createScrollBar(scrollbox, 5, scroll);
        scrollbox.setConsumer(i -> {
            if (i > list.getWidth()) {
                listWithScroll.add(scroll);
                listWithScroll.add(scrollBar);
            } else {
                listWithScroll.remove(scrollBar);
                listWithScroll.remove(scroll);
            }
        });
    }

    private UIEntity getRemoveIcon() {
        final UIEntity entity = new UIEntity();
        entity.setHeight(20);
        entity.setWidth(20);
        // TODO Add remove texture
        entity.add(new UIButton("x"));
        entity.add(new UIClickable(e -> {
            controller.allRSStates.remove(currentProfile);
            final int profileToRemove = currentProfile;
            initMode(EnumMode.SINGLE);
            controller.sendRSProfileRemove(profileToRemove, currentProfile);
        }));
        entity.add(new UIToolTip(I18Wrapper.format("gui.controller.remove_profile")));
        return entity;
    }

    private UIEntity getAddButton(final UIEntity list, final UIEntity propertyEntity) {
        final UIEntity addButtonEntity = getButtonPatternEntity(e -> {
            final int newProfileID = getNewProfileID();
            if (newProfileID == -1)
                return;

            controller.allRSStates.put(newProfileID, new HashMap<>());
            addProfilesToList(list, propertyEntity);
            final int profileToLoad = currentProfile == -1 ? newProfileID : currentProfile;
            enableButton(buttonsRSMode[profileToLoad]);
            updateProfileProperties(propertyEntity, profileToLoad);
            if (currentProfile == -1) {
                currentProfile = newProfileID;
            }
        }, "gui.controller.add_profile");
        final UIButton addButton = new UIButton("+");
        addButtonEntity.add(addButton);
        return addButtonEntity;
    }

    private static UIEntity getRedstoneModeLabel() {
        final UIEntity entity = new UIEntity();
        final UILabel label = new UILabel(I18Wrapper.format("gui.controller.profile"));
        entity.setHeight(20);
        entity.setInheritWidth(true);
        label.setCenterX(true);
        label.setCenterY(true);
        entity.add(label);
        return entity;
    }

    private static UIEntity getButtonPatternEntity(final Consumer<UIEntity> consumer,
            final String desc) {
        final SoundManager handler = Minecraft.getInstance().getSoundManager();
        final UIEntity buttonEntity = new UIEntity();
        buttonEntity.setHeight(20);
        buttonEntity.setWidth(20);
        buttonEntity.add(new UIClickable(consumer.andThen(
                e -> handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f)))));
        if (!desc.isEmpty()) {
            buttonEntity.add(new UIToolTip(I18Wrapper.format(desc)));
        }
        return buttonEntity;
    }

    private void addProfilesToList(final UIEntity list, final UIEntity propertyEntity) {
        list.clearChildren();
        Arrays.fill(buttonsRSMode, null);
        controller.allRSStates.forEach(
                (profile, map) -> buttonsRSMode[profile] = new UIButton(String.valueOf(profile)));
        for (int i = 0; i < buttonsRSMode.length; i++) {
            final UIButton button = buttonsRSMode[i];
            if (button == null) {
                continue;
            }
            final int profile = i;
            final UIEntity buttonEntity = getButtonPatternEntity(e -> {
                currentProfile = profile;
                updateProfileProperties(propertyEntity, profile);
                enableButton(button);
            }, "");
            buttonEntity.add(button);
            list.add(buttonEntity);
        }
    }

    private int getNewProfileID() {
        int newProfile = 0;
        while (controller.allRSStates.containsKey(newProfile)) {
            newProfile++;
            if (newProfile > MAX_PROFILES)
                return -1;
        }
        return newProfile;
    }

    private void enableButton(final UIButton button) {
        for (final UIButton b : buttonsRSMode) {
            if (b != null) {
                b.setEnabled(false);
            }
        }
        if (button != null) {
            button.setEnabled(true);
        }
    }

    private void addSideAndIDSelection(final UIEntity rightSide) {
        final Minecraft mc = Minecraft.getInstance();
        final BlockState state = OSBlocks.HV_SIGNAL_CONTROLLER.defaultBlockState();
        final BakedModel model = mc.getBlockRenderer().getBlockModel(state);

        rightSide.add(getRowOfDirectionSelectionInfo());
        for (final Direction face : Direction.values()) {
            final UIEntity redstoneEntity = new UIEntity();
            redstoneEntity.setInheritWidth(true);
            redstoneEntity.setHeight(20);
            redstoneEntity.add(new UIBox(UIBox.HBOX, 2));

            final List<BakedQuad> quad =
                    model.getQuads(state, face, SignalCustomModel.RANDOM, EmptyModelData.INSTANCE);
            final UIEntity faceEntity = new UIEntity();
            faceEntity.setWidth(20);
            faceEntity.setHeight(20);
            faceEntity.add(new UITexture(quad.get(0).getSprite()));
            final UIColor color = new UIColor(0x70000000);
            faceEntity.add(color);
            final UILabel label = new UILabel(face.getName().substring(0, 1).toUpperCase());
            label.setTextColor(0xFFFFFFFF);
            faceEntity.add(label);
            redstoneEntity.add(faceEntity);

            addIDSelection(redstoneEntity, face);

            rightSide.add(redstoneEntity);
        }
        rightSide.add(getRowOfDirectionSelectionInfo());
    }

    private UIEntity getRowOfDirectionSelectionInfo() {
        final UIEntity entity = new UIEntity();
        entity.setHeight(20);
        entity.setInheritWidth(true);
        entity.add(new UIBox(UIBox.HBOX, 2));

        final UIEntity label =
                GuiElements.createLabel(I18Wrapper.format("gui.controller.mode"), 0xFF000000, 0.8f);
        label.setInheritWidth(false);
        label.setWidth(20);
        label.setY(5);
        entity.add(label);
        entity.add(new UIToolTip(I18Wrapper.format("gui.controller.mode.desc")));

        final UIEntity redstoneOn =
                getTexturedIcon(SignalBoxIcons.SYMBOLS, SignalBoxSymbols.REDSTONE_ON.ordinal());
        entity.add(redstoneOn);

        final UIEntity redstoneOff =
                getTexturedIcon(SignalBoxIcons.SYMBOLS, SignalBoxSymbols.REDSTONE_OFF.ordinal());
        redstoneOff.add(new UIBorder(0xFF000000, 1));

        entity.add(redstoneOff);
        return entity;
    }

    private UIEntity getTexturedIcon(final SignalBoxIcons icons, final int id) {
        final UIEntity icon = new UIEntity();
        icon.setHeight(20);
        icon.setWidth(20);
        icon.add(new UIColor(0x6F000000));
        icon.add(new UITexture(icons.getResourceLocation(), icons.getX(id), 0, icons.getMX(id), 1));
        icon.add(new UIBorder(0xFF000000, 1));
        return icon;
    }

    private void addIDSelection(final UIEntity redstoneEntity, final Direction face) {
        int onIndex = controller.enabledRSStates.getOrDefault(face, new HashMap<>())
                .getOrDefault(EnumState.ONSTATE, -1);
        int offIndex = controller.enabledRSStates.getOrDefault(face, new HashMap<>())
                .getOrDefault(EnumState.OFFSTATE, -1);

        redstoneEntity.add(getRedstoneModeButton(onIndex, face, EnumState.ONSTATE));
        redstoneEntity.add(getRedstoneModeButton(offIndex, face, EnumState.OFFSTATE));
    }

    private UIEntity getRedstoneModeButton(final int defaultIndex, final Direction face,
            final EnumState state) {
        final UIButton mainButton =
                new UIButton(defaultIndex != -1 ? String.valueOf(defaultIndex) : "");
        final UIEntity redstoneButton = getButtonPatternEntity(e -> {
            push(GuiElements.createScreenBack(screenEntity -> {
                final UIBox box = new UIBox(UIBox.VBOX, 1);
                final UIEntity list = new UIEntity();
                list.setInherits(true);
                list.add(box);

                list.add(
                        GuiElements
                                .createButton(
                                        I18Wrapper
                                                .format("gui.controller." + face.getName() + "."
                                                        + state.getNameWrapper())
                                                + ": "
                                                + I18Wrapper.format("property.disabled.name"),
                                        _u -> {
                                            controller.sendAndSetProfile(face, -1, state);
                                            mainButton.setText("");
                                            pop();
                                        }));
                controller.allRSStates.forEach((i, _u) -> {
                    final UIEntity button = GuiElements.createButton(I18Wrapper.format(
                            "gui.controller." + face.getName() + "." + state.getNameWrapper())
                            + " : " + String.valueOf(i), _u2 -> {
                                controller.sendAndSetProfile(face, i, state);
                                mainButton.setText(i != -1 ? String.valueOf(i) : "");
                                pop();
                            });
                    list.add(button);
                });
                screenEntity.add(list);
                screenEntity.add(GuiElements.createPageSelect(box));
            }));
        }, "gui.controller." + face.getName() + "." + state.getNameWrapper() + ".desc");
        redstoneButton.add(mainButton);
        return redstoneButton;
    }

    private void addSingleRSMode() {
        this.lowerEntity.add(new UIBox(UIBox.HBOX, 2));
        Arrays.fill(buttonsRSMode, null);

        final UIEntity leftSide = new UIEntity();
        leftSide.setInherits(true);
        leftSide.add(new UIBox(UIBox.VBOX, 2));
        this.lowerEntity.add(leftSide);
        this.lowerEntity.add(this.previewRedstone.get());

        final UIEntity rightSide = new UIEntity();
        rightSide.setInheritHeight(true);
        rightSide.setWidth(65);
        rightSide.add(new UIBox(UIBox.VBOX, 2));

        currentProfile = controller.allRSStates.entrySet().stream().findFirst()
                .orElseGet(() -> Maps.immutableEntry(-1, new HashMap<>())).getKey();
        addProfileSelection(leftSide);

        addSideAndIDSelection(rightSide);
        this.lowerEntity.add(rightSide);
    }

    private void addRSInputMode() {
        this.lowerEntity.add(new UIBox(UIBox.HBOX, 5));

        final UIEntity leftSide = new UIEntity();
        leftSide.setInherits(true);
        leftSide.add(new UIBox(UIBox.VBOX, 10));
        this.lowerEntity.add(leftSide);

        this.lowerEntity.add(this.previewRedstone.get());

        final IIntegerable<String> profile = new DisableIntegerable<>(
                SizeIntegerables.of("profile", 30, in -> String.valueOf(in)));
        leftSide.add(GuiElements.createEnumElement(profile, e -> {
            updateProfileProperties(new UIEntity(), e, true);
            controller.sendRSInputProfileToServer(e);
        }, controller.linkedRSInputProfile));
        updateProfileProperties(new UIEntity(), controller.linkedRSInputProfile, true);

        addLinkedPosInfo(leftSide);
    }

    private void addLinkedPosInfo(final UIEntity leftSide) {
        final Minecraft mc = Minecraft.getInstance();
        final BlockState state = OSBlocks.REDSTONE_IN.defaultBlockState();
        final BakedModel model = mc.getBlockRenderer().getBlockModel(state);

        final UIEntity redstoneEntity = new UIEntity();
        redstoneEntity.setInheritWidth(true);
        redstoneEntity.setHeight(30);
        redstoneEntity.add(new UIBox(UIBox.HBOX, 5));
        leftSide.add(redstoneEntity);

        final List<BakedQuad> quad = model.getQuads(state, Direction.DOWN, SignalCustomModel.RANDOM,
                EmptyModelData.INSTANCE);
        final UIEntity faceEntity = new UIEntity();
        faceEntity.setWidth(30);
        faceEntity.setHeight(30);
        faceEntity.add(new UITexture(quad.get(0).getSprite()));
        redstoneEntity.add(faceEntity);

        final String posString =
                controller.linkedRSInput == null ? I18Wrapper.format("gui.controller.not_linked")
                        : controller.linkedRSInput.toShortString();
        final UIEntity labelEntity = GuiElements.createLabel(posString, 0xFF000000, 1.5f);
        final UILabel label = labelEntity.findRecursive(UILabel.class).stream().findFirst()
                .orElseGet(() -> new UILabel(posString));
        label.setCenterX(false);
        label.setCenterY(true);

        redstoneEntity.add(GuiElements.createSpacerH(30));
        redstoneEntity.add(labelEntity);

        final UIEntity unlinkButton =
                GuiElements.createButton(I18Wrapper.format("gui.controller.unlink"), e -> {
                    controller.unlinkInputPos();
                    label.setText(I18Wrapper.format("gui.controller.not_linked"));
                });
        unlinkButton.add(new UIToolTip(I18Wrapper.format("gui.controller.unlink.desc")));
        leftSide.add(unlinkButton);
    }

    private void initInternal() {
        this.entity.clear();

        final Signal signal = this.controller.getSignal();
        if (signal == null) {
            this.entity.add(new UILabel(I18Wrapper.format("gui.notconnected")));
            return;
        }
        lowerEntity.setInherits(true);

        final String name = I18Wrapper
                .format("block." + OpenSignalsMain.MODID + "." + signal.delegate.name().getPath())
                + "; Name: "
                + ClientNameHandler.getClientName(new StateInfo(mc.level, controller.getPos()))
                        .replace("[n]", " ");

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
        final UIEntity rsMode = GuiElements.createEnumElement(enumModes, enumMode,
                in -> initMode(enumMode.getObjFromID(in)), controller.currentMode.ordinal());
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
                    controller.sendPropertyToServer(property, e);
                    previewSidebar.update(controller.getSignal());
                }
            }, index));
            previewSidebar.addToRenderNormal(property, index);
        });
        previewSidebar.update(controller.getSignal());
    }

    @Override
    public void push(final UIEntity entity) {
        previewSidebar.setDisable(true);
        previewRedstone.setDisable(true);
        super.push(entity);
    }

    @Override
    public UIEntity pop() {
        previewSidebar.setDisable(false);
        previewRedstone.setDisable(false);
        return super.pop();
    }

    @Override
    public void updateFromContainer() {
        initInternal();
        loaded = true;
    }
}