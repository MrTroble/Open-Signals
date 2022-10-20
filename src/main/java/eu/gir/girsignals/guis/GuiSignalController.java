package eu.gir.girsignals.guis;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import eu.gir.girsignals.ChangeableStage;
import eu.gir.girsignals.EnumMode;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import eu.gir.guilib.ecs.DrawUtil.DisableIntegerable;
import eu.gir.guilib.ecs.DrawUtil.EnumIntegerable;
import eu.gir.guilib.ecs.DrawUtil.SizeIntegerables;
import eu.gir.guilib.ecs.GuiBase;
import eu.gir.guilib.ecs.GuiElements;
import eu.gir.guilib.ecs.GuiSyncNetwork;
import eu.gir.guilib.ecs.entitys.UIBlockRender;
import eu.gir.guilib.ecs.entitys.UIBox;
import eu.gir.guilib.ecs.entitys.UIEntity;
import eu.gir.guilib.ecs.entitys.UIEnumerable;
import eu.gir.guilib.ecs.entitys.input.UIClickable;
import eu.gir.guilib.ecs.entitys.input.UIDrag;
import eu.gir.guilib.ecs.entitys.render.UIColor;
import eu.gir.guilib.ecs.entitys.render.UILabel;
import eu.gir.guilib.ecs.entitys.render.UIScissor;
import eu.gir.guilib.ecs.entitys.render.UITexture;
import eu.gir.guilib.ecs.entitys.render.UIToolTip;
import eu.gir.guilib.ecs.entitys.transform.UIIndependentTranslate;
import eu.gir.guilib.ecs.entitys.transform.UIRotate;
import eu.gir.guilib.ecs.entitys.transform.UIScale;
import eu.gir.guilib.ecs.interfaces.IIntegerable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSignalController extends GuiBase {

    private final BlockPos pos;
    private final ContainerSignalController controller;
    private final UIEntity lowerEntity = new UIEntity();
    private boolean previewMode = false;
    private String profileName = null;

    public GuiSignalController(final SignalControllerTileEntity tile) {
        this.pos = tile.getPos();
        this.controller = new ContainerSignalController(this::init);
        Minecraft.getMinecraft().player.openContainer = this.controller;
        this.compound = tile.getTag();
        init();
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
    private void createPageForSide(final EnumFacing face, final UIEntity leftSide,
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

        final Map<SEProperty<?>, Object> map = this.controller.getReference();
        if (map == null)
            return;
        for (final SEProperty<?> entry : map.keySet()) {
            if ((entry.isChangabelAtStage(ChangeableStage.APISTAGE)
                    || entry.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG))
                    && entry.test(map)) {
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

        final Minecraft mc = Minecraft.getMinecraft();
        final IBlockState state = mc.player.world.getBlockState(pos);
        final IBakedModel model = mc.getBlockRendererDispatcher().getModelForState(state);
        final UIEnumerable toggle = new UIEnumerable(EnumFacing.VALUES.length, "singleModeFace");
        toggle.setOnChange(e -> {
            final EnumFacing faceing = EnumFacing.VALUES[e];

            final List<UIColor> colors = rightSide.findRecursive(UIColor.class);
            colors.forEach(c -> c.setColor(0x70000000));
            colors.get(e).setColor(0x70FF0000);
            leftSide.write(compound);
            leftSide.clearChildren();
            createPageForSide(faceing, leftSide, bRender);
            leftSide.read(compound);
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

    private void init() {
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
        rightSide.add(new UIDrag((x, y) -> rotation.setRotateY(rotation.getRotateY() + x)));
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

        final Map<SEProperty<?>, Object> map = this.controller.getReference();
        if (map == null)
            return;
        for (final SEProperty<?> entry : map.keySet()) {
            if ((entry.isChangabelAtStage(ChangeableStage.APISTAGE)
                    || entry.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG))
                    && entry.test(map)) {
                list.add(GuiElements.createEnumElement(entry, e -> applyModelChange(blockRender)));
            }
        }
        applyModelChange(blockRender);
    }

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    private void applyModelChange(final UIBlockRender blockRender) {
        IExtendedBlockState currentState = (IExtendedBlockState) this.controller.getSignal()
                .getDefaultState();
        for (final Entry<SEProperty<?>, Object> e : controller.getReference().entrySet()) {
            currentState = currentState.withProperty((SEProperty) e.getKey(), e.getValue());
        }

        if (!previewMode) {
            for (final UIEnumerable property : lowerEntity.findRecursive(UIEnumerable.class)) {
                if (profileName == null || property.getID().endsWith(profileName)) {
                    final SEProperty sep = SEProperty.cst(
                            controller.lookup.get(property.getID().replace("." + profileName, "")));
                    if (sep != null)
                        currentState = currentState.withProperty(sep,
                                sep.getObjFromID(property.getIndex()));
                }
            }
        }
        blockRender.setBlockState(currentState);
    }

    @Override
    public void onGuiClosed() {
        this.entity.write(compound);
        GuiSyncNetwork.sendToPosServer(compound, pos);
    }

}