package com.troblecodings.signals.guis;

import java.util.concurrent.TimeUnit;

import com.troblecodings.core.I18Wrapper;
import com.troblecodings.guilib.ecs.DrawUtil.EnumIntegerable;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UITextInput;
import com.troblecodings.guilib.ecs.entitys.input.UIKeyUpdate;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UIToolTip;
import com.troblecodings.signals.blocks.CombinedRedstoneInput;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.guis.NamableContainer.NamableContainerNetwork;
import com.troblecodings.signals.handler.ClientNameHandler;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;

import net.minecraft.world.level.block.Block;

public class NamableGui extends GuiBase {

    private UILabel labelComp;
    private final NamableContainer container;

    public NamableGui(final GuiInfo info) {
        super(info);
        this.container = (NamableContainer) info.base;
    }

    private void initOwn() {
        this.entity.clear();
        this.entity.add(new UIBox(UIBox.HBOX, 5));

        final UIEntity inner = new UIEntity();
        inner.setInherits(true);
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
        textfield.add(new UIToolTip(I18Wrapper.format("property.customname.desc")));
        textfield.add(new UIKeyUpdate(_u -> this.updateText(input.getText())));

        hbox.add(textfield);
        final UIEntity apply = GuiElements.createButton(I18Wrapper.format("btn.apply"),
                _u -> this.updateText(input.getText()));
        apply.setInheritWidth(false);
        apply.setWidth(60);
        hbox.add(apply);
        inner.add(hbox);
        if (!(container.tile instanceof RedstoneIOTileEntity))
            return;

        final Block block = container.tile.getBlockState().getBlock();
        if (block instanceof RedstoneInput) {
            addDelayEntityFor(inner, NamableContainerNetwork.BLOCKING_TIME,
                    NamableContainerNetwork.BLOCKING_TIME_UNIT,
                    I18Wrapper.format("gui.namable.blocking_delay"), container.blockingDelay,
                    container.blockingTimeUnit);
            if (block instanceof CombinedRedstoneInput) {
                addDelayEntityFor(inner, NamableContainerNetwork.RESET_TIME,
                        NamableContainerNetwork.RESET_TIME_UNIT,
                        I18Wrapper.format("gui.namable.reset_delay"), container.resetDelay,
                        container.resetTimeUnit);
            }
        }

        inner.add(GuiElements.createSpacerV(10));

        inner.add(GuiElements.createLabel(I18Wrapper.format("label.linkedto")));
        final UIEntity list = new UIEntity();
        list.setInherits(true);
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

    private void addDelayEntityFor(final UIEntity inner, final NamableContainerNetwork timeMode,
            final NamableContainerNetwork timeUnitMode, final String label, final int defaultTime,
            final TimeUnit defaultTimeUnit) {
        final UIEntity hentity = new UIEntity();
        hentity.setHeight(20);
        hentity.setInheritWidth(true);
        hentity.add(new UIBox(UIBox.HBOX, 5));

        inner.add(hentity);

        final UITextInput resetInput = new UITextInput(String.valueOf(defaultTime));
        resetInput.setValidator(input -> {
            if (input.isEmpty())
                return true;
            try {
                Integer.valueOf(input);
            } catch (final NumberFormatException e) {
                return false;
            }
            return true;
        });
        resetInput.setOnTextUpdate(input -> container
                .sendDelayTimeToServer(input.isEmpty() ? 0 : Integer.valueOf(input), timeMode));

        final UIEntity resetInputEntity = new UIEntity();
        resetInputEntity.setInherits(true);
        resetInputEntity.add(resetInput);

        final UIEntity labelEntity = GuiElements.createLabel(label, 1f);
        labelEntity.setInheritWidth(false);
        labelEntity.setWidth(100);
        labelEntity.setY(2);

        hentity.add(labelEntity);
        hentity.add(resetInputEntity);

        final UIEntity timeUnitSelection = GuiElements.createEnumElement(
                new EnumIntegerable<>(TimeUnit.class),
                i -> container.sendDelayTimeUnitToServer(TimeUnit.values()[i], timeUnitMode),
                defaultTimeUnit.ordinal());
        timeUnitSelection.setInheritWidth(false);
        timeUnitSelection.setWidth(130);
        hentity.add(timeUnitSelection);
    }

    private void updateText(final String input) {
        if (input.isEmpty() || input
                .equals(ClientNameHandler.getClientName(new StateInfo(mc.world, container.pos))))
            return;
        container.sendNameToServer(input);
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