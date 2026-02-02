package com.troblecodings.signals.guis;

import com.troblecodings.core.I18Wrapper;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.DrawUtil.BoolIntegerables;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIEntity.MouseEvent;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UIToolTip;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.guis.UISignalBoxRendering.BoxEntity;
import com.troblecodings.signals.guis.UISignalBoxRendering.SelectionType;

import net.minecraft.entity.player.EntityPlayer;

public class GuiPathwayRequester extends GuiBase {

    private final ContainerPathwayRequester container;
    private final EntityPlayer player;

    public GuiPathwayRequester(final GuiInfo info) {
        super(info);
        this.container = (ContainerPathwayRequester) info.base;
        this.player = info.player;
        entity.clear();
        entity.add(new UILabel(I18Wrapper.format("gui.notconnected")));
    }

    private void initOwn() {
        entity.clear();
        entity.add(new UIBox(UIBox.VBOX, 5));

        final UIEntity higherEntity = new UIEntity();
        higherEntity.setInheritWidth(true);
        higherEntity.setHeight(20);
        higherEntity.add(new UIBox(UIBox.HBOX, 5));

        final UIEntity label = GuiElements.createLabel(
                I18Wrapper.format("tile.pathwayrequester.name"), 0x7678a0,
                1f);
        higherEntity.add(label);

        final BoxEntity boxEntity = UISignalBoxRendering.createSignalBoxEntity(container.grid,
                false, (rendering, point, mouseKey) -> {
                    if (mouseKey != MouseEvent.LEFT_MOUSE)
                        return;
                    container.grid.getNodeChecked(point).ifPresent(node -> {
                        if (container.start == null && node.isValidStart()) {
                            container.start = point;
                            rendering.addSelection(GuiSignalBox.SELECTION_COLOR, point,
                                    SelectionType.FIRST);
                        } else if (container.start != null && container.end == null
                                && node.isValidEnd()) {
                            container.end = point;
                            rendering.addSelection(GuiSignalBox.SELECTION_COLOR, point,
                                    SelectionType.SECOND);
                            sendPWToServer();
                            infoUpdate(I18Wrapper.format("gui.pwr.saved"));
                        }
                    });
                });
        if (container.start != null) {
            boxEntity.rendering.addSelection(GuiSignalBox.SELECTION_COLOR, container.start,
                    SelectionType.FIRST);
        }
        if (container.end != null) {
            boxEntity.rendering.addSelection(GuiSignalBox.SELECTION_COLOR, container.end,
                    SelectionType.SECOND);
        }

        final UIEntity newPathButton = GuiElements
                .createButton(I18Wrapper.format("gui.pwr.newpath"), e -> {
                    boxEntity.rendering.clearSelection();
                    container.start = null;
                    container.end = null;
                    infoUpdate(I18Wrapper.format("gui.pwr.newpath.set"));
                });
        newPathButton.add(new UIToolTip(I18Wrapper.format("gui.pwr.newpath.desc")));
        higherEntity.add(newPathButton);

        final UIEntity checkbox = GuiElements.createBoolElement(
                BoolIntegerables.of(I18Wrapper.format("gui.pwr.addtosave")),
                i -> updateAddToSaverOnServer(i), container.addToPWToSavedPW);
        higherEntity.add(checkbox);

        final UIEntity middleEntity = new UIEntity();
        middleEntity.setInherits(true);
        middleEntity.add(new UIBox(UIBox.HBOX, 5));
        middleEntity.add(GuiElements.createSpacerH(20));
        middleEntity.add(boxEntity.entity);
        middleEntity.add(GuiElements.createSpacerH(20));

        final UIEntity lowerEntity = new UIEntity();
        lowerEntity.setHeight(20);
        lowerEntity.setInheritWidth(true);
        lowerEntity.add(new UIBox(UIBox.HBOX, 5));
        lowerEntity.add(GuiElements.createLabel("Linked SignalBox: "
                + (container.linkedPos == null ? "Not linked!" : container.linkedPos.toString()),
                1.3f));

        entity.add(higherEntity);
        entity.add(middleEntity);
        entity.add(lowerEntity);
    }

    @Override
    public void updateFromContainer() {
        initOwn();
    }

    private void sendPWToServer() {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte((byte) 0);
        container.start.writeNetwork(buffer);
        container.end.writeNetwork(buffer);
        OpenSignalsMain.network.sendTo(player, buffer);
    }

    private void updateAddToSaverOnServer(final int value) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte((byte) 1);
        buffer.putByte((byte) value);
        OpenSignalsMain.network.sendTo(player, buffer);
    }

    private void infoUpdate(final String tip) {
        final UIToolTip tooltip = new UIToolTip(tip, true);
        entity.add(tooltip);
        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            entity.remove(tooltip);
        }).start();
    }

    @Override
    public ContainerBase getNewGuiContainer(final GuiInfo info) {
        return new ContainerPathwayRequester(info);
    }
}