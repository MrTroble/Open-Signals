package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.DrawUtil.DisableIntegerable;
import com.troblecodings.guilib.ecs.DrawUtil.EnumIntegerable;
import com.troblecodings.guilib.ecs.DrawUtil.SizeIntegerables;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.GuiSyncNetwork;
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
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.enums.EnumMode;
import com.troblecodings.signals.models.SignalCustomModel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiSignalController extends GuiBase {

    private final ContainerSignalController controller;
    private final UIEntity lowerEntity = new UIEntity();
    private boolean previewMode = false;
    private String profileName = null;
    private final List<UIPropertyEnumHolder> holders = new ArrayList<>();

    public GuiSignalController(final GuiInfo info) {
        super(info);
        this.controller = (ContainerSignalController)info.base;
        info.player.containerMenu = this.controller;
        this.compound = new NBTWrapper();
        initInternal();
    }

    @Override
    public void removed() {
        this.entity.write(compound);
        GuiSyncNetwork.sendToPosServer(compound, controller.getPos());
    }

    private void initMode(final EnumMode mode, final Signal signal) {
        lowerEntity.clear();
        profileName = null;
        switch (mode) {
            case MANUELL:
                addManuellMode();
                break;
            case SINGLE:
                addSingleRSMode();
                break;
            case MUX:
                break;
            default:
                break;
        }
    }

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    private void createPageForSide(final Direction face, final UIEntity leftSide,
            final UIBlockRender bRender) {
        final UIEntity middlePart = new UIEntity();

        final IIntegerable<String> profile = SizeIntegerables.of("profile", 32,
                in -> String.valueOf(in));
        leftSide.add(GuiElements.createEnumElement(profile, x -> {
            this.profileName = "p" + x;
            middlePart.findRecursive(UIEnumerable.class).forEach(e -> {
                final String[] id = e.getID().split("\\.");
                if (id.length > 1) {
                    e.write(compound);
                    e.setID(id[0] + "." + profileName
                            + (id.length > 2 ? ("." + face.getName()) : ""));
                    e.read(compound);
                }
            });
            applyModelChange(bRender);
        }));

        middlePart.setInheritHeight(true);
        middlePart.setInheritWidth(true);
        final UIBox boxMode = new UIBox(UIBox.VBOX, 1);
        middlePart.add(boxMode);

        final Map<SEProperty, String> map = this.controller.getReference();
        if (map == null)
            return;
        for (final SEProperty entry : map.keySet()) {
            if ((entry.isChangabelAtStage(ChangeableStage.APISTAGE)
                    || entry.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG))
                    && entry.testMap(map)) {
                final UIEntity entity = GuiElements.createEnumElement(
                        new DisableIntegerable<>(entry), e -> applyModelChange(bRender));
                entity.findRecursive(UIEnumerable.class).forEach(e -> {
                    e.setID(entry.getName() + ".unknown");
                    e.setMin(-1);
                });
                middlePart.add(entity);
            }
        }
        leftSide.add(middlePart);

        final IIntegerable<Object> offProfile = new DisableIntegerable(
                SizeIntegerables.of("profileOff." + face.getName(), 32, in -> String.valueOf(in)));
        final IIntegerable<Object> onProfile = new DisableIntegerable(
                SizeIntegerables.of("profileOn." + face.getName(), 32, in -> String.valueOf(in)));
        final UIEntity offElement = GuiElements.createEnumElement(offProfile, e -> {
        });
        final UIEntity onElement = GuiElements.createEnumElement(onProfile, e -> {
        });
        offElement.findRecursive(UIEnumerable.class).forEach(e -> e.setMin(-1));
        onElement.findRecursive(UIEnumerable.class).forEach(e -> e.setMin(-1));
        leftSide.add(offElement);
        leftSide.add(onElement);
        leftSide.add(GuiElements.createPageSelect(boxMode));
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

        final Minecraft mc = Minecraft.getInstance();
        final BlockState state = mc.player.level.getBlockState(controller.getPos());
        final BakedModel model = mc.getBlockRenderer().getBlockModel(state);
        final UIEnumerable toggle = new UIEnumerable(Direction.values().length, "singleModeFace");
        toggle.setOnChange(e -> {
            final Direction faceing = Direction.values()[e];

            final List<UIColor> colors = rightSide.findRecursive(UIColor.class);
            colors.forEach(c -> c.setColor(0x70000000));
            colors.get(e).setColor(0x70FF0000);
            leftSide.write(compound);
            leftSide.clearChildren();
            createPageForSide(faceing, leftSide, bRender);
            leftSide.read(compound);
        });
        rightSide.add(toggle);

        for (final Direction face : Direction.values()) {
            final List<BakedQuad> quad = model.getQuads(state, face, SignalCustomModel.RANDOM);
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

    private void initInternal() {
        this.entity.clear();

        final Signal signal = this.controller.getSignal();
        if (signal == null) {
            this.entity.add(new UILabel("Not connected"));
            return;
        }
        lowerEntity.setInheritHeight(true);
        lowerEntity.setInheritWidth(true);

        final String name = I18n.get("tile." + signal.getRegistryName().getPath() + ".name");

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
        final EnumIntegerable<EnumMode> enumMode = new EnumIntegerable<EnumMode>(EnumMode.class);
        final UIEntity rsMode = GuiElements.createEnumElement(enumMode, in -> {
            lowerEntity.write(compound);
            lowerEntity.clearChildren();
            initMode(enumMode.getObjFromID(in), signal);
            this.lowerEntity.read(compound);
        });
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

        this.entity.read(compound);
    }

    private UIEntity createPreview(final UIBlockRender blockRender) {
        final UIToolTip tooltip = new UIToolTip(I18n.get("controller.preview", previewMode));

        final UIEntity rightSide = new UIEntity();
        rightSide.setWidth(60);
        rightSide.setInheritHeight(true);
        final UIRotate rotation = new UIRotate();
        rotation.setRotateY(180);
        rightSide.add(new UIClickable(e -> {
            previewMode = !previewMode;
            applyModelChange(blockRender);
            tooltip.setDescripton(I18n.get("controller.preview", previewMode));
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

        holders.clear();
        final Map<SEProperty, String> map = this.controller.getReference();
        if (map == null)
            return;
        for (final SEProperty entry : map.keySet()) {
            if ((entry.isChangabelAtStage(ChangeableStage.APISTAGE)
                    || entry.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG))
                    && entry.testMap(map)) {
                final UIEnumerable enumarable = new UIEnumerable(entry.count(), entry.getName());
                list.add(GuiElements.createEnumElement(enumarable, entry,
                        e -> applyModelChange(blockRender)));
                holders.add(new UIPropertyEnumHolder(entry, enumarable));
            }
        }
        applyModelChange(blockRender);
    }

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    private void applyModelChange(final UIBlockRender blockRender) {
        // TODO new model system
    }

}