package com.troblecodings.signals.guis;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.input.UIDrag;
import com.troblecodings.guilib.ecs.entitys.input.UIScroll;
import com.troblecodings.guilib.ecs.entitys.render.UIBorder;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UIScissor;
import com.troblecodings.guilib.ecs.entitys.transform.UIScale;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;

public final class SignalBoxUIHelper {

    private SignalBoxUIHelper() {
    }

    public static void initializeGrid(final UIEntity entity, final SignalBoxGrid grid,
            final BiConsumer<UIEntity, UISignalBoxTile> consumer) {
        final Map<Point, UIEntity> allTiles = new HashMap<>();
        final UIEntity splitter = new UIEntity();
        splitter.setInherits(true);
        allTiles.clear();
        final UIEntity plane = new UIEntity();
        plane.setWidth(GuiSignalBox.TILE_COUNT * GuiSignalBox.TILE_WIDTH);
        plane.setHeight(GuiSignalBox.TILE_COUNT * GuiSignalBox.TILE_WIDTH);
        plane.setScale(0.5f);
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
                SignalBoxNode node = grid.getNode(name);
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
                consumer.accept(tile, sbt);
                row.add(tile);
                allTiles.put(name, tile);
            }
            plane.add(row);
        }
        splitter.add(new UIScissor());
        splitter.add(new UIColor(GuiSignalBox.BACKGROUND_COLOR));
        splitter.add(new UIBorder(0xFF000000, 4));
        splitter.add(plane);
        entity.add(splitter);
    }
}