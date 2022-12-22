package com.troblecodings.signals.guis;

import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiSyncNetwork;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UITextInput;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.signals.tileentitys.SignalTileEnity;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;

public class GuiSignal extends GuiBase {

    private final SignalTileEnity tile;
    private UILabel labelComp;

    public GuiSignal(final SignalTileEnity tile) {
        this.tile = tile;
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

        final UIEntity textfield = new UIEntity();
        textfield.setHeight(20);
        textfield.setInheritWidth(true);

        final UITextInput input = new UITextInput(SignalTileEnity.CUSTOMNAME);
        input.setText(tile.getName());
        textfield.add(input);

        hbox.add(textfield);
        final UIEntity apply = GuiElements.createButton(I18n.get("btn.apply"),
                _u -> this.updateText(input.getText()));
        apply.setInheritWidth(false);
        apply.setWidth(60);
        hbox.add(apply);

        inner.add(hbox);
    }

    private void updateText(final String input) {
        final CompoundTag compound = new CompoundTag();
        this.entity.write(compound);
        GuiSyncNetwork.sendToPosServer(compound, tile.getBlockPos());
        labelComp.setText(input);
    }

}
