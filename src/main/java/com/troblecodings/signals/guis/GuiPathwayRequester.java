package com.troblecodings.signals.guis;

import com.troblecodings.core.I18Wrapper;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.DrawUtil.DisableIntegerable;
import com.troblecodings.guilib.ecs.DrawUtil.SizeIntegerables;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.signalbox.Point;

import net.minecraft.entity.player.PlayerEntity;

public class GuiPathwayRequester extends GuiBase {

    private final ContainerPathwayRequester container;
    private final PlayerEntity player;

    public GuiPathwayRequester(final GuiInfo info) {
        super(info);
        this.container = (ContainerPathwayRequester) info.base;
        this.player = info.player;
        this.entity.clear();
        this.entity.add(new UILabel("Not connected"));
    }

    private void initOwn() {
        this.entity.clear();
        this.entity.add(new UIBox(UIBox.VBOX, 5));

        final UIEntity inner = new UIEntity();
        inner.setWidth(200);
        inner.setInheritHeight(true);
        inner.setX(70);
        inner.add(new UIBox(UIBox.VBOX, 5));
        inner.add(GuiElements.createSpacerV(10));

        final UIEntity label = GuiElements.createLabel(I18Wrapper.format("tile.pathwayrequester"),
                0x7678a0);
        label.setScaleX(1.5f);
        label.setScaleY(1.5f);
        label.setX(-6);
        inner.add(label);
        inner.add(GuiElements.createSpacerV(20));

        final IIntegerable<String> start = SizeIntegerables.of("StartPoint",
                container.validStarts.size(), e -> {
                    if (e == -1)
                        return "Disabled";
                    return container.validStarts.get(e).toString();
                });
        final IIntegerable<String> end = SizeIntegerables.of("EndPoint", container.validEnds.size(),
                e -> {
                    if (e == -1)
                        return "Disabled";
                    return container.validEnds.get(e).toString();
                });

        if (!container.validStarts.isEmpty()) {
            inner.add(GuiElements.createEnumElement(new DisableIntegerable<>(start), e -> {
                if (e == -1) {
                    container.start = new Point(-1, -1);
                    return;
                }
                container.start = container.validStarts.get(e);
            }, container.start != null ? container.start.equals(new Point(-1, -1)) ? -1
                    : container.validStarts.indexOf(container.start) : -1));
        } else {
            final UIEntity infoLabelEntity = new UIEntity();
            infoLabelEntity.setInheritWidth(true);
            infoLabelEntity.setHeight(20);
            final UILabel infoLabel = new UILabel("No start to set!");
            infoLabel.setCenterY(false);
            infoLabelEntity.add(infoLabel);
            inner.add(infoLabelEntity);
        }

        if (!container.validEnds.isEmpty()) {
            inner.add(GuiElements.createEnumElement(new DisableIntegerable<>(end), e -> {
                if (e == -1) {
                    container.end = new Point(-1, -1);
                    return;
                }
                container.end = container.validEnds.get(e);
            }, container.end != null ? container.end.equals(new Point(-1, -1)) ? -1
                    : container.validEnds.indexOf(container.end) : -1));
        } else {
            final UIEntity infoLabelEntity = new UIEntity();
            infoLabelEntity.setInheritWidth(true);
            infoLabelEntity.setHeight(20);
            final UILabel infoLabel = new UILabel("No end to set!");
            infoLabel.setCenterY(false);
            infoLabelEntity.add(infoLabel);
            inner.add(infoLabelEntity);
        }

        if (!container.validStarts.isEmpty() && !container.validEnds.isEmpty()) {
            inner.add(GuiElements.createButton(I18Wrapper.format("btn.save"), e -> sendToServer()));
        } else {
            final UIEntity infoLabelEntity = new UIEntity();
            infoLabelEntity.setInheritWidth(true);
            infoLabelEntity.setHeight(20);
            final UILabel infoLabel = new UILabel("Nothing to save!");
            infoLabel.setCenterY(false);
            infoLabelEntity.add(infoLabel);
            inner.add(infoLabelEntity);
        }

        inner.add(GuiElements.createSpacerV(5));

        final UILabel linkedlabel = new UILabel("Linked SignalBox: "
                + (container.linkedPos == null ? "Not linked!" : container.linkedPos.toString()));
        linkedlabel.setCenterY(false);

        final UIEntity posLabel = new UIEntity();
        posLabel.setHeight(20);
        posLabel.setInheritWidth(true);
        posLabel.add(linkedlabel);

        inner.add(posLabel);

        entity.add(inner);
    }

    @Override
    public void updateFromContainer() {
        initOwn();
    }

    private void sendToServer() {
        final WriteBuffer buffer = new WriteBuffer();
        container.start.writeNetwork(buffer);
        container.end.writeNetwork(buffer);
        OpenSignalsMain.network.sendTo(player, buffer);
    }
}
