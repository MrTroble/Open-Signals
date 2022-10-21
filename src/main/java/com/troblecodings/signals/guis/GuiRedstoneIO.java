package com.troblecodings.signals.guis;

import com.troblecodings.signals.init.SignalBlocks;
import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;

import eu.gir.guilib.ecs.GuiBase;
import eu.gir.guilib.ecs.GuiElements;
import eu.gir.guilib.ecs.GuiSyncNetwork;
import eu.gir.guilib.ecs.entitys.UIBox;
import eu.gir.guilib.ecs.entitys.UIEntity;
import eu.gir.guilib.ecs.entitys.UITextInput;
import eu.gir.guilib.ecs.entitys.render.UILabel;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;

public class GuiRedstoneIO extends GuiBase {

    private final RedstoneIOTileEntity tile;
    private UILabel labelComp;

    public GuiRedstoneIO(final RedstoneIOTileEntity tile) {
        this.tile = tile;
        init();
    }

    private void init() {
        this.entity.clear();
        this.entity.add(new UIBox(UIBox.HBOX, 5));

        final UIEntity inner = new UIEntity();
        inner.setInheritHeight(true);
        inner.setInheritWidth(true);
        inner.add(new UIBox(UIBox.VBOX, 2));
        this.entity.add(GuiElements.createSpacerH(10));
        this.entity.add(inner);
        this.entity.add(GuiElements.createSpacerH(10));

        final UIEntity label = GuiElements.createLabel(tile.getName(), 0x7678a0);
        label.setScaleX(1.5f);
        label.setScaleY(1.5f);
        label.findRecursive(UILabel.class).forEach(l -> labelComp = l);
        labelComp.setCenterX(false);
        inner.add(GuiElements.createSpacerV(10));
        inner.add(label);
        inner.add(GuiElements.createSpacerV(8));

        final UIEntity hbox = new UIEntity();
        hbox.add(new UIBox(UIBox.HBOX, 2));
        hbox.setHeight(25);
        hbox.setInheritWidth(true);

        final UIEntity textField = new UIEntity();
        textField.setHeight(20);
        textField.setInheritWidth(true);

        final UITextInput input = new UITextInput(RedstoneIOTileEntity.NAME_NBT);
        input.setText(tile.getName());
        textField.add(input);
        hbox.add(textField);
        final UIEntity apply = GuiElements.createButton(I18n.format("btn.apply"),
                _u -> this.updateText(input.getText()));
        apply.setInheritWidth(false);
        apply.setWidth(80);
        hbox.add(apply);

        inner.add(hbox);

        inner.add(GuiElements.createLabel(I18n.format("label.linkedto")));
        final UIEntity list = new UIEntity();
        list.setInheritHeight(true);
        list.setInheritWidth(true);
        final UIBox layout = new UIBox(UIBox.VBOX, 5);
        list.add(layout);
        this.tile.forEach(pos -> list.add(GuiElements.createLabel(
                String.format("%s: x=%d, y=%d, z=%d", SignalBlocks.SIGNAL_BOX.getLocalizedName(),
                        pos.getX(), pos.getY(), pos.getZ()))));
        inner.add(list);
        inner.add(GuiElements.createPageSelect(layout));
    }

    private void updateText(final String input) {
        final NBTTagCompound compound = new NBTTagCompound();
        this.entity.write(compound);
        GuiSyncNetwork.sendToPosServer(compound, tile.getPos());
        labelComp.setText(input);
    }

}
