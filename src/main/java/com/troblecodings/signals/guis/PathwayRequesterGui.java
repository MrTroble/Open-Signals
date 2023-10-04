package com.troblecodings.signals.guis;

import com.troblecodings.core.I18Wrapper;
import com.troblecodings.core.WriteBuffer;
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

import net.minecraft.world.entity.player.Player;

public class PathwayRequesterGui extends GuiBase {

    private final PathwayRequesterContainer container;
    private final Player player;

    public PathwayRequesterGui(GuiInfo info) {
        super(info);
        this.container = (PathwayRequesterContainer) info.base;
        this.player = info.player;
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
                container.validStarts.size(), e -> container.validStarts.get(e).toShortString());
        final IIntegerable<String> end = SizeIntegerables.of("EndPoint",
                container.validStarts.size(), e -> container.validEnds.get(e).toShortString());

        inner.add(GuiElements.createEnumElement(start, e -> {
            container.start = container.validStarts.get(e);
        }, container.start != null ? container.start.equals(new Point()) ? 0
                : container.validStarts.indexOf(container.start) : 0));
        inner.add(GuiElements.createEnumElement(end, e -> {
            container.end = container.validStarts.get(e);
        }, container.end != null
                ? container.end.equals(new Point()) ? 0 : container.validEnds.indexOf(container.end)
                : 0));

        inner.add(GuiElements.createButton(I18Wrapper.format("btn.save"), e -> sendToServer()));
        inner.add(GuiElements.createSpacerV(5));

        final UILabel Linkedlabel = new UILabel("Linked SignalBox: "
                + (container.linkedPos == null ? "Not linked!" : container.linkedPos.toString()));
        Linkedlabel.setCenterY(false);

        final UIEntity posLabel = new UIEntity();
        posLabel.setHeight(20);
        posLabel.setInheritWidth(true);
        posLabel.add(Linkedlabel);

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
