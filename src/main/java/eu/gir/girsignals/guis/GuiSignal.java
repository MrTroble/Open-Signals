package eu.gir.girsignals.guis;

import eu.gir.girsignals.tileentitys.SignalTileEnity;
import eu.gir.guilib.ecs.GuiBase;
import eu.gir.guilib.ecs.GuiElements;
import eu.gir.guilib.ecs.GuiSyncNetwork;
import eu.gir.guilib.ecs.entitys.UIBox;
import eu.gir.guilib.ecs.entitys.UIEntity;
import eu.gir.guilib.ecs.entitys.UITextInput;
import eu.gir.guilib.ecs.entitys.render.UILabel;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;

public class GuiSignal extends GuiBase {

    private final SignalTileEnity tile;
    private UILabel labelComp;

    public GuiSignal(final SignalTileEnity tile) {
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
        final UIEntity apply = GuiElements.createButton(I18n.format("btn.apply"),
                _u -> this.updateText(input.getText()));
        apply.setInheritWidth(false);
        apply.setWidth(60);
        hbox.add(apply);

        inner.add(hbox);
    }

    private void updateText(final String input) {
        final NBTTagCompound compound = new NBTTagCompound();
        this.entity.write(compound);
        GuiSyncNetwork.sendToPosServer(compound, tile.getPos());
        labelComp.setText(input);
        tile.setCustomName(input);
    }

}
