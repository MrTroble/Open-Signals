package com.troblecodings.signals.guis;

import java.util.HashMap;
import java.util.Map;

import com.troblecodings.core.I18Wrapper;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.DrawUtil.BoolIntegerables;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.input.UIClickable;
import com.troblecodings.guilib.ecs.entitys.input.UIDrag;
import com.troblecodings.guilib.ecs.entitys.input.UIScroll;
import com.troblecodings.guilib.ecs.entitys.render.UIBorder;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UIScissor;
import com.troblecodings.guilib.ecs.entitys.render.UIToolTip;
import com.troblecodings.guilib.ecs.entitys.transform.UIScale;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxNode;

import net.minecraft.world.entity.player.Player;

public class PathwayRequesterGui extends GuiBase {

    private final PathwayRequesterContainer container;
    private final Player player;
    private final Map<Point, UIEntity> allTiles = new HashMap<>();

    public PathwayRequesterGui(final GuiInfo info) {
        super(info);
        this.container = (PathwayRequesterContainer) info.base;
        this.player = info.player;
        entity.clear();
        entity.add(new UILabel("Not connected"));
    }

    private void initOwn() {
        entity.clear();
        entity.add(new UIBox(UIBox.VBOX, 5));

        final UIEntity higherEntity = new UIEntity();
        higherEntity.setInheritWidth(true);
        higherEntity.setHeight(20);
        higherEntity.add(new UIBox(UIBox.HBOX, 5));

        final UIEntity label = GuiElements.createLabel(I18Wrapper.format("tile.pathwayrequester"),
                0x7678a0, 1.2f);
        higherEntity.add(label);

        final UIEntity newPathButton = GuiElements
                .createButton(I18Wrapper.format("gui.pwr.newpath"), e -> {
                    final UIEntity start = allTiles.getOrDefault(container.start, new UIEntity());
                    start.findRecursive(UIColor.class).forEach(start::remove);
                    final UIEntity end = allTiles.getOrDefault(container.end, new UIEntity());
                    start.findRecursive(UIColor.class).forEach(end::remove);
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
        initializeGrid(middleEntity);
        middleEntity.add(GuiElements.createSpacerH(20));

        final UIEntity lowerEntity = new UIEntity();
        lowerEntity.setHeight(20);
        lowerEntity.setInheritWidth(true);
        lowerEntity.add(new UIBox(UIBox.HBOX, 5));
        lowerEntity.add(GuiElements
                .createLabel("Linked SignalBox: " + (container.linkedPos == null ? "Not linked!"
                        : container.linkedPos.toShortString()), 1.3f));

        entity.add(higherEntity);
        entity.add(middleEntity);
        entity.add(lowerEntity);
    }

    private void initializeGrid(final UIEntity lowerEntity) {
        final UIEntity splitter = new UIEntity();
        splitter.setInherits(true);
        allTiles.clear();
        final UIEntity plane = new UIEntity();
        plane.setWidth(GuiSignalBox.TILE_COUNT * GuiSignalBox.TILE_WIDTH);
        plane.setHeight(GuiSignalBox.TILE_COUNT * GuiSignalBox.TILE_WIDTH);
        splitter.add(new UIScroll(s -> {
            final float newScale = (float) (plane.getScaleX() + s * 0.05f);
            if (newScale <= 0)
                return;
            plane.setScaleX(newScale);
            plane.setScaleY(newScale);
            plane.update();
        }));
        splitter.add(new UIDrag((x, y) -> {
            plane.setX(plane.getX() + x);
            plane.setY(plane.getY() + y);
            plane.update();
        }, 2));
        plane.add(new UIBox(UIBox.VBOX, 0).setPageable(false));
        for (int x = 0; x < GuiSignalBox.TILE_COUNT; x++) {
            final UIEntity row = new UIEntity();
            row.add(new UIBox(UIBox.HBOX, 0).setPageable(false));
            row.setHeight(GuiSignalBox.TILE_WIDTH);
            row.setWidth(GuiSignalBox.TILE_WIDTH);
            for (int y = 0; y < GuiSignalBox.TILE_COUNT; y++) {
                final UIEntity tile = new UIEntity();
                tile.setHeight(GuiSignalBox.TILE_WIDTH);
                tile.setWidth(GuiSignalBox.TILE_WIDTH);
                final Point name = new Point(y, x);
                SignalBoxNode node = container.grid.getNode(name);
                if (node == null) {
                    node = new SignalBoxNode(name);
                }
                final UISignalBoxTile sbt = new UISignalBoxTile(node);
                tile.add(sbt);
                if (!node.getCustomText().isEmpty()) {
                    final UIEntity inputEntity = new UIEntity();
                    inputEntity.add(new UIScale(0.7f, 0.7f, 0.7f));
                    final UILabel label = new UILabel(node.getCustomText());
                    label.setTextColor(0xFFFFFFFF);
                    inputEntity.add(label);
                    inputEntity.setX(5);
                    tile.add(inputEntity);
                }
                tile.add(new UIClickable(e -> {
                    if (container.start == null && sbt.isValidStart()) {
                        container.start = name;
                        e.add(new UIColor(GuiSignalBox.SELECTION_COLOR));
                    } else if (container.start != null && container.end == null
                            && sbt.isValidEnd()) {
                        container.end = name;
                        e.add(new UIColor(GuiSignalBox.SELECTION_COLOR));
                        sendPWToServer();
                        infoUpdate(I18Wrapper.format("gui.saved"));
                    }
                }));
                if (name.equals(container.start) || name.equals(container.end)) {
                    tile.add(new UIColor(GuiSignalBox.SELECTION_COLOR));
                }
                row.add(tile);
                allTiles.put(name, tile);
            }
            plane.add(row);
        }
        splitter.add(new UIScissor());
        splitter.add(new UIColor(GuiSignalBox.BACKGROUND_COLOR));
        splitter.add(new UIBorder(0xFF000000, 4));
        splitter.add(plane);
        lowerEntity.add(splitter);
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
}