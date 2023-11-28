package com.troblecodings.signals.guis;

import com.troblecodings.core.I18Wrapper;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UITextInput;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.handler.ClientNameHandler;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;

import net.minecraft.entity.player.EntityPlayer;

public class NamableGui extends GuiBase {

    private UILabel labelComp;
    private final NamableContainer container;
    private final EntityPlayer player;

    public NamableGui(final GuiInfo info) {
        super(info);
        this.container = (NamableContainer) info.base;
        this.player = info.player;
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

        final UIEntity label = GuiElements.createLabel(container.tile.getNameWrapper(), 0x7678a0);
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

        final UITextInput input = new UITextInput(container.tile.getNameWrapper());
        textfield.add(input);

        hbox.add(textfield);
        final UIEntity apply = GuiElements.createButton(I18Wrapper.format("btn.apply"),
                _u -> this.updateText(input.getText()));
        apply.setInheritWidth(false);
        apply.setWidth(60);
        hbox.add(apply);
        inner.add(hbox);
        if (!(container.tile instanceof RedstoneIOTileEntity)) {
            return;
        }
        inner.add(GuiElements.createLabel(I18Wrapper.format("label.linkedto")));
        final UIEntity list = new UIEntity();
        list.setInheritHeight(true);
        list.setInheritWidth(true);
        final UIBox layout = new UIBox(UIBox.VBOX, 5);
        list.add(layout);
        this.container.linkedPos.forEach(pos -> list.add(GuiElements.createLabel(
                String.format("%s: x = %d, y = %d, z = %d", OSBlocks.SIGNAL_BOX.getLocalizedName(),
                        pos.getX(), pos.getY(), pos.getZ()))));
        this.container.linkedController.forEach(
                pos -> list.add(GuiElements.createLabel(String.format("%s: x = %d, y = %d, z = %d",
                        OSBlocks.HV_SIGNAL_CONTROLLER.getLocalizedName(), pos.getX(), pos.getY(),
                        pos.getZ()))));
        inner.add(list);
        inner.add(GuiElements.createPageSelect(layout));
    }

    private void updateText(final String input) {
        if (input.isEmpty() || input
                .equals(ClientNameHandler.getClientName(new StateInfo(mc.world, container.pos))))
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putString(input);
        OpenSignalsMain.network.sendTo(player, buffer.build());
        labelComp.setText(input);
    }

    @Override
    public void updateFromContainer() {
        initOwn();
    }

    @Override
    public ContainerBase getNewGuiContainer(final GuiInfo info) {
        return new NamableContainer(info);
    }
}