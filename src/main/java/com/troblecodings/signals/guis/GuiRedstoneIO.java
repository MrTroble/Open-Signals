package com.troblecodings.signals.guis;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.GuiSyncNetwork;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UITextInput;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;

import net.minecraft.client.resources.language.I18n;

public class GuiRedstoneIO extends GuiBase {

    private final RedstoneIOTileEntity tile;
    private UILabel labelComp;

    public GuiRedstoneIO(final GuiInfo info) {
        this.tile = info.getTile();
        initOwn();
    }

    private void initOwn() {
        this.entity.clear();
        this.entity.add(new UIBox(UIBox.HBOX, 5));

        final UIEntity inner = new UIEntity();
        inner.setInheritHeight(true);
        inner.setInheritWidth(true);
        inner.add(new UIBox(UIBox.VBOX, 2));
        this.entity.add(GuiElements.createSpacerH(10));
        this.entity.add(inner);
        this.entity.add(GuiElements.createSpacerH(10));

        final UIEntity label = GuiElements.createLabel(tile.getNameWrapper(), 0x7678a0);
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
        input.setText(tile.getNameWrapper());
        textField.add(input);
        hbox.add(textField);
        final UIEntity apply = GuiElements.createButton(I18n.get("btn.apply"),
                _u -> this.updateText(input.getText()));
        apply.setInheritWidth(false);
        apply.setWidth(80);
        hbox.add(apply);

        inner.add(hbox);

        inner.add(GuiElements.createLabel(I18n.get("label.linkedto")));
        final UIEntity list = new UIEntity();
        list.setInheritHeight(true);
        list.setInheritWidth(true);
        final UIBox layout = new UIBox(UIBox.VBOX, 5);
        list.add(layout);
        this.tile.forEach(pos -> list.add(GuiElements.createLabel(
                String.format("%s: x=%d, y=%d, z=%d", OSBlocks.SIGNAL_BOX.getName().getString(),
                        pos.getX(), pos.getY(), pos.getZ()))));
        inner.add(list);
        inner.add(GuiElements.createPageSelect(layout));
    }

    private void updateText(final String input) {
        final NBTWrapper compound = new NBTWrapper();
        this.entity.write(compound);
        GuiSyncNetwork.sendToPosServer(compound, tile.getBlockPos());
        labelComp.setText(input);
    }

}
