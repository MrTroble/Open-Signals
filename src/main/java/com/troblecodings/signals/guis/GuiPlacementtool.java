package com.troblecodings.signals.guis;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.IntConsumer;

import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiSyncNetwork;
import com.troblecodings.guilib.ecs.entitys.UIBlockRender;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UICheckBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIEnumerable;
import com.troblecodings.guilib.ecs.entitys.input.UIDrag;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UIScissor;
import com.troblecodings.guilib.ecs.entitys.transform.UIIndependentTranslate;
import com.troblecodings.guilib.ecs.entitys.transform.UIRotate;
import com.troblecodings.guilib.ecs.entitys.transform.UIScale;
import com.troblecodings.signals.ChangeableStage;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.items.Placementtool;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPlacementtool extends GuiBase {

    private final UIEntity list = new UIEntity();
    private final UIBlockRender blockRender = new UIBlockRender();
    private final HashMap<String, IUnlistedProperty<?>> lookup = new HashMap<String, IUnlistedProperty<?>>();
    private Signal currentSelectedBlock;
    private final Placementtool tool;
    public static final int GUI_PLACEMENTTOOL = 0;

    public GuiPlacementtool(final ItemStack stack) {
        this.compound = stack.getTagCompound();
        if (this.compound == null)
            this.compound = new NBTTagCompound();
        tool = (Placementtool) stack.getItem();
        final int usedBlock = this.compound.hasKey(Placementtool.BLOCK_TYPE_ID)
                ? this.compound.getInteger(Placementtool.BLOCK_TYPE_ID)
                : tool.getObjFromID(0).getID();
        currentSelectedBlock = Signal.SIGNALLIST.get(usedBlock);

        init();
    }

    private void initList() {
        final ExtendedBlockState hVExtendedBlockState = (ExtendedBlockState) currentSelectedBlock
                .getBlockState();
        final Collection<IUnlistedProperty<?>> unlistedProperties = hVExtendedBlockState
                .getUnlistedProperties();
        for (final IUnlistedProperty<?> property : unlistedProperties) {
            final SEProperty<?> prop = SEProperty.cst(property);
            of(prop, inp -> applyModelChanges());
        }
        if (currentSelectedBlock.canHaveCustomname(new HashMap<>()))
            list.add(GuiElements.createInputElement(Signal.CUSTOMNAME, in -> applyModelChanges()));
        this.list.read(compound);
    }

    private void init() {
        initList();
        final UIBox vbox = new UIBox(UIBox.VBOX, 5);
        this.list.add(vbox);
        this.list.setInheritHeight(true);
        this.list.setInheritWidth(true);

        final UIEntity lowerEntity = new UIEntity();
        lowerEntity.add(GuiElements.createSpacerH(10));

        final UIEntity selectBlockEntity = GuiElements.createEnumElement(tool, input -> {
            currentSelectedBlock = tool.getObjFromID(input);
            final ExtendedBlockState bsc = (ExtendedBlockState) currentSelectedBlock
                    .getBlockState();
            lookup.clear();
            bsc.getUnlistedProperties().forEach(p -> lookup.put(p.getName(), p));
            this.list.clearChildren();
            initList();
            this.entity.update();
            applyModelChanges();
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
        blockRenderEntity.add(new UIDrag((x, y) -> rotation.setRotateY(rotation.getRotateY() + x)));

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
        this.entity.read(compound);
    }

    public void of(final SEProperty<?> property, final IntConsumer consumer) {
        if (property == null)
            return;
        if (property.isChangabelAtStage(ChangeableStage.GUISTAGE)) {
            if (property.getType().equals(Boolean.class)) {
                list.add(GuiElements.createBoolElement(property, consumer));
                return;
            }
            list.add(GuiElements.createEnumElement(property, consumer));
        } else if (property.isChangabelAtStage(ChangeableStage.APISTAGE)) {
            list.add(GuiElements.createBoolElement(property, consumer));
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        applyModelChanges();
    }

    @Override
    public void onGuiClosed() {
        compound.setInteger(Placementtool.BLOCK_TYPE_ID, currentSelectedBlock.getID());
        super.onGuiClosed();
        GuiSyncNetwork.sendToItemServer(compound);
    }

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    public void applyModelChanges() {
        IExtendedBlockState ebs = (IExtendedBlockState) currentSelectedBlock.getDefaultState();

        final List<UIEnumerable> enumerables = this.list.findRecursive(UIEnumerable.class);
        for (final UIEnumerable enumerable : enumerables) {
            final SEProperty sep = (SEProperty) lookup.get(enumerable.getID());
            if (sep == null)
                return;
            ebs = ebs.withProperty(sep, sep.getObjFromID(enumerable.getIndex()));
        }

        final List<UICheckBox> checkbox = this.list.findRecursive(UICheckBox.class);
        for (final UICheckBox checkb : checkbox) {
            final SEProperty sep = (SEProperty) lookup.get(checkb.getID());
            if (sep == null)
                return;
            if (sep.isChangabelAtStage(ChangeableStage.GUISTAGE)) {
                ebs = ebs.withProperty(sep, checkb.isChecked());
            } else if (checkb.isChecked()) {
                ebs = ebs.withProperty(sep, sep.getDefault());
            }
        }

        for (final Entry<IUnlistedProperty<?>, Optional<?>> prop : ebs.getUnlistedProperties()
                .entrySet()) {
            final SEProperty property = SEProperty.cst(prop.getKey());
            if (property.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG)) {
                ebs = ebs.withProperty(property, property.getDefault());
            }
        }

        blockRender.setBlockState(ebs);
    }
}