package com.troblecodings.signals.guis;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.troblecodings.core.I18Wrapper;
import com.troblecodings.core.VectorWrapper;
import com.troblecodings.guilib.ecs.DrawUtil.SizeIntegerables;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.entitys.UIBlockRenderInfo;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIMultiBlockRender;
import com.troblecodings.guilib.ecs.entitys.input.UIClickable;
import com.troblecodings.guilib.ecs.entitys.render.UIButton;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.signals.blocks.Monitor;
import com.troblecodings.signals.models.ModelInfoWrapper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.client.model.data.EmptyModelData;

public class GuiMonitorSelection extends GuiBase {

    private static final ModelInfoWrapper EMPTY_WRAPPER =
            new ModelInfoWrapper(EmptyModelData.INSTANCE);
    private static final SoundManager HANDLER = Minecraft.getInstance().getSoundManager();
    private static final int BACKGROUND_COLOR = 0xFF8B8B8B;

    private final UIMultiBlockRender renderer = new UIMultiBlockRender(25, -5.5f);
    private final ContainerMonitorSelection container;
    private final UIEntity sizeSelection = new UIEntity();

    private Monitor monitor;

    public GuiMonitorSelection(final GuiInfo info) {
        super(info);
        this.container = (ContainerMonitorSelection) info.base;
    }

    private void initInternal() {
        final UIEntity list = new UIEntity();
        list.setInherits(true);
        list.add(new UIBox(UIBox.VBOX, 5));
        this.entity.add(new UIBox(UIBox.HBOX, 5));
        this.entity.add(GuiElements.createSpacerH(10));
        this.entity.add(list);
        this.entity.add(GuiElements.createSpacerH(10));

        monitor = Monitor.MONITORS.get(0);
        final UIEntity selection =
                GuiElements.createEnumElement(
                        SizeIntegerables.of("monitor_selection", Monitor.MONITORS.size(),
                                i -> Monitor.MONITORS.get(i).getMonitorProperties().getName()),
                        i -> {
                            monitor = Monitor.MONITORS.get(i);
                            container.sizeX = container.sizeY = 0;
                            container.sendSelectedMonitorToServer(monitor);
                            rebuildSizeSelection();
                        }, container.selectedMonitor);
        setUpSizeSelections();

        list.add(GuiElements.createSpacerV(10));
        list.add(selection);
        list.add(sizeSelection);
        list.add(getBlockRenderEntity());
    }

    private void rebuildSizeSelection() {
        sizeSelection.clear();
        setUpSizeSelections();
    }

    private void setUpSizeSelections() {
        sizeSelection.setInheritWidth(true);
        sizeSelection.setHeight(40);
        sizeSelection.add(new UIBox(UIBox.HBOX, 5));

        sizeSelection.add(getSizeSelection(I18Wrapper.format("gui.monitor.x_size"),
                monitor.getMonitorProperties().getMaxX(), Direction.Axis.X));
        sizeSelection.add(getSizeSelection(I18Wrapper.format("gui.monitor.y_size"),
                monitor.getMonitorProperties().getMaxY(), Direction.Axis.Y));
    }

    private UIEntity getSizeSelection(final String name, final int max, final Direction.Axis axis) {
        final UIEntity list = new UIEntity();
        list.setInheritWidth(true);
        list.setHeight(50);
        list.add(new UIBox(UIBox.VBOX, 5));

        list.add(GuiElements.createLabel(name));
        list.add(getUserSelectionEntity(max, axis));

        return list;
    }

    private UIEntity getUserSelectionEntity(final int max, final Direction.Axis axis) {
        final UIEntity list = new UIEntity();
        list.setInherits(true);
        list.add(new UIBox(UIBox.HBOX, 2));

        AtomicInteger selectedValue = new AtomicInteger(
                axis.equals(Direction.Axis.X) ? container.sizeX : container.sizeY);
        final UIButton leftButton = new UIButton("<");
        final UIButton middleButton = new UIButton(String.valueOf(selectedValue.get()));
        final UIButton rightButton = new UIButton(">");

        final Consumer<Integer> updateSideButtons = value -> {
            rightButton.setEnabled(value < max);
            leftButton.setEnabled(value > 0);
            if (axis.equals(Direction.Axis.X)) {
                container.sizeX = value;
            } else if (axis.equals(Direction.Axis.Y)) {
                container.sizeY = value;
            }
            updateBlockRenderer();
            container.sendSizeToServer(axis, value);
        };

        final UIEntity leftButtonEntity = getButton(leftButton, e -> {
            final int oldValue = selectedValue.get();
            if (oldValue == 0)
                return;
            selectedValue.set(oldValue - 1);
            final int newValue = selectedValue.get();
            updateSideButtons.accept(newValue);
            middleButton.setText(String.valueOf(newValue));
        }, 20);

        list.add(leftButtonEntity);

        list.add(getButton(middleButton, e -> {
        }, Integer.MAX_VALUE));

        final UIEntity rightButtonEntity = getButton(rightButton, e -> {
            final int oldValue = selectedValue.get();
            if (oldValue == max)
                return;
            selectedValue.set(oldValue + 1);
            final int newValue = selectedValue.get();
            updateSideButtons.accept(newValue);
            middleButton.setText(String.valueOf(newValue));
        }, 20);
        list.add(rightButtonEntity);

        leftButton.setEnabled(selectedValue.get() > 0);
        rightButton.setEnabled(selectedValue.get() < max);
        updateBlockRenderer();
        return list;
    }

    private static UIEntity getButton(final UIButton button, final Consumer<UIEntity> consumer,
            final int width) {
        final UIEntity entity = new UIEntity();
        entity.setHeight(20);
        if (width == Integer.MAX_VALUE) {
            entity.setInheritWidth(true);
        } else {
            entity.setWidth(width);
        }
        entity.add(button);
        entity.add(new UIClickable(consumer.andThen(
                e -> HANDLER.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f)))));
        return entity;

    }

    private UIEntity getBlockRenderEntity() {
        final UIEntity renderEntity = new UIEntity();
        renderEntity.setInherits(true);
        renderEntity.add(new UIColor(BACKGROUND_COLOR));
        renderEntity.add(renderer);
        return renderEntity;
    }

    private void updateBlockRenderer() {
        renderer.clear();
        for (int i = 0; i < container.sizeX; i++) {
            for (int j = 0; j < container.sizeY; j++) {
                renderer.setBlockState(
                        new UIBlockRenderInfo(monitor.defaultBlockState(), EMPTY_WRAPPER,
                                new VectorWrapper(-i + 0.5f * container.sizeX - 4.8f, j, 0)));
            }
        }
    }

    @Override
    public void updateFromContainer() {
        initInternal();
    }

}
