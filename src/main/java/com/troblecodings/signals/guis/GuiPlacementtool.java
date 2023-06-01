package com.troblecodings.signals.guis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.entitys.UIBlockRender;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIEnumerable;
import com.troblecodings.guilib.ecs.entitys.UITextInput;
import com.troblecodings.guilib.ecs.entitys.input.UIDrag;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UIScissor;
import com.troblecodings.guilib.ecs.entitys.transform.UIIndependentTranslate;
import com.troblecodings.guilib.ecs.entitys.transform.UIRotate;
import com.troblecodings.guilib.ecs.entitys.transform.UIScale;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.JsonEnum;
import com.troblecodings.signals.core.WriteBuffer;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.items.Placementtool;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPlacementtool extends GuiBase {

    public static final int GUI_PLACEMENTTOOL = 0;

    private final UIEntity list = new UIEntity();
    private final UIBlockRender blockRender = new UIBlockRender();
    private Signal currentSelectedBlock;
    private final Placementtool tool;
    private final EntityPlayer player;
    private final ContainerPlacementtool container;
    private UIEnumerable enumerable;
    private boolean loaded = false;

    public GuiPlacementtool(final GuiInfo info) {
        super(info);
        this.player = info.player;
        this.container = (ContainerPlacementtool) info.base;
        final ItemStack stack = info.player.getHeldItemMainhand();
        tool = (Placementtool) stack.getItem();
        final int usedBlock = NBTWrapper.getOrCreateWrapper(stack)
                .getInteger(Placementtool.BLOCK_TYPE_ID);
        currentSelectedBlock = tool.getObjFromID(usedBlock);
        initInternal();
    }

    @Override
    public ContainerBase getNewGuiContainer(final GuiInfo info) {
        return new ContainerPlacementtool(info);
    }

    private void initInternal() {
        final UIBox vbox = new UIBox(UIBox.VBOX, 5);
        this.list.add(vbox);
        this.list.setInheritHeight(true);
        this.list.setInheritWidth(true);

        final UIEntity lowerEntity = new UIEntity();
        lowerEntity.add(GuiElements.createSpacerH(10));

        enumerable = new UIEnumerable(tool.count(), tool.getName());

        final UIEntity selectBlockEntity = GuiElements.createEnumElement(enumerable, tool,
                input -> {
                    currentSelectedBlock = tool.getObjFromID(input);
                    this.list.clearChildren();
                    if (container.signalID != input) {
                        sendSignalId(input);
                    }
                });
        final UIEntity leftSide = new UIEntity();
        leftSide.setInheritHeight(true);
        leftSide.setInheritWidth(true);
        leftSide.add(new UIBox(UIBox.VBOX, 5));

        leftSide.add(selectBlockEntity);
        leftSide.add(list);
        leftSide.add(GuiElements.createPageSelect(vbox));

        final UIEntity blockRenderEntity = new UIEntity();
        blockRenderEntity.setInheritHeight(true);
        blockRenderEntity.setWidth(60);

        final UIRotate rotation = new UIRotate();
        rotation.setRotateY(180);
        blockRenderEntity.add(
                new UIDrag((x, y) -> rotation.setRotateY((float) (rotation.getRotateY() + x))));

        blockRenderEntity.add(new UIScissor());
        blockRenderEntity.add(new UIIndependentTranslate(35, 150, 40));
        blockRenderEntity.add(rotation);
        blockRenderEntity.add(new UIIndependentTranslate(-0.5, -3.5, -0.5));
        blockRenderEntity.add(new UIScale(20, -20, 20));
        blockRenderEntity.add(blockRender);

        lowerEntity.add(new UIBox(UIBox.HBOX, 5));

        lowerEntity.add(leftSide);
        lowerEntity.add(blockRenderEntity);
        lowerEntity.setInheritHeight(true);
        lowerEntity.setInheritWidth(true);

        final UILabel titlelabel = new UILabel(I18n.format("property.signal.name"));
        titlelabel.setCenterX(false);

        final UIEntity titel = new UIEntity();
        titel.add(new UIScale(1.2f, 1.2f, 1));
        titel.add(titlelabel);
        titel.setInheritHeight(true);
        titel.setInheritWidth(true);

        final UIEntity topPart = new UIEntity();
        topPart.setInheritWidth(true);
        topPart.setHeight(20);
        topPart.add(new UIBox(UIBox.HBOX, 5));
        topPart.add(GuiElements.createSpacerH(10));
        topPart.add(titel);
        this.entity.add(topPart);

        this.entity.add(new UIBox(UIBox.VBOX, 5));
        this.entity.add(lowerEntity);
    }

    public void of(final SEProperty property, final IntConsumer consumer, final int value) {
        if (property == null)
            return;
        if (property.isChangabelAtStage(ChangeableStage.GUISTAGE)) {
            if (property.getParent().equals(JsonEnum.BOOLEAN)) {
                list.add(GuiElements.createBoolElement(property, consumer, value));
                return;
            }
            list.add(GuiElements.createEnumElement(property, consumer, value));
        } else if (property.isChangabelAtStage(ChangeableStage.APISTAGE)) {
            list.add(GuiElements.createBoolElement(property, consumer, value));
        }
    }

    @Override
    public void updateFromContainer() {
        enumerable.setIndex(container.signalID);
        final List<SEProperty> originalProperties = currentSelectedBlock.getProperties();
        final Map<SEProperty, Integer> savedProperties = container.properties;
        originalProperties.forEach(property -> {
            of(property,
                    inp -> applyPropertyChanges(currentSelectedBlock.getIDFromProperty(property),
                            inp),
                    savedProperties.get(property));
        });
        final UIEntity textfield = new UIEntity();
        textfield.setHeight(20);
        textfield.setInheritWidth(true);
        if (currentSelectedBlock.canHaveCustomname(new HashMap<>())) {
            final UITextInput name = new UITextInput(container.signalName);
            name.setOnTextUpdate(this::sendName);
            textfield.add(name);
            list.add(textfield);
        }
        loaded = true;
    }

    private void applyPropertyChanges(final int propertyId, final int valueId) {
        if (loaded) {
            final WriteBuffer buffer = new WriteBuffer();
            buffer.putByte((byte) propertyId);
            buffer.putByte((byte) valueId);
            OpenSignalsMain.network.sendTo(player, buffer.build());
        }
        applyModelChanges();
    }

    private void sendSignalId(final int id) {
        if (!loaded)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte((byte) 255);
        buffer.putInt(id);
        OpenSignalsMain.network.sendTo(player, buffer.build());
    }

    private void sendName(final String name) {
        if (!loaded)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte((byte) 255);
        buffer.putInt(-1);
        final byte[] signalName = name.getBytes();
        buffer.putByte((byte) signalName.length);
        for (final byte b : signalName) {
            buffer.putByte(b);
        }
        OpenSignalsMain.network.sendTo(player, buffer.build());
    }

    public void applyModelChanges() {
        @SuppressWarnings("unused")
        final IBlockState ebs = currentSelectedBlock.getDefaultState();
        /*
         * final List<UIEnumerable> enumerables =
         * this.list.findRecursive(UIEnumerable.class); for (final UIEnumerable
         * enumerable : enumerables) { final SEProperty sep = (SEProperty)
         * lookup.get(enumerable.getID()); if (sep == null) return; ebs =
         * ebs.withProperty(sep, sep.getObjFromID(enumerable.getIndex())); }
         *
         * final List<UICheckBox> checkbox = this.list.findRecursive(UICheckBox.class);
         * for (final UICheckBox checkb : checkbox) { final SEProperty sep =
         * (SEProperty) lookup.get(checkb.getID()); if (sep == null) return; if
         * (sep.isChangabelAtStage(ChangeableStage.GUISTAGE)) { ebs =
         * ebs.withProperty(sep, checkb.isChecked()); } else if (checkb.isChecked()) {
         * ebs = ebs.withProperty(sep, sep.getDefault()); } }
         *
         * for (final Entry<IUnlistedProperty<?>, Optional<?>> prop :
         * ebs.getUnlistedProperties() .entrySet()) { final SEProperty property =
         * SEProperty.cst(prop.getKey()); if
         * (property.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG)) { ebs =
         * ebs.withProperty(property, property.getDefault()); } }
         *
         * blockRender.setBlockState(ebs);
         */
    }
}