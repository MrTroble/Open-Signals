package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.troblecodings.core.I18Wrapper;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.entitys.DrawInfo;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIComponent;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIEntity.EnumMouseState;
import com.troblecodings.guilib.ecs.entitys.UIEntity.MouseEvent;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.signals.guis.UISignalBoxRendering.BoxEntity;
import com.troblecodings.signals.signalbox.Point;

public class GuiMonitor extends GuiBase {

    private final ContainerMonitor container;
    private final List<Point> selectedPoints = new ArrayList<>();
    private BoxEntity box;

    public GuiMonitor(final GuiInfo info) {
        super(info);
        this.container = (ContainerMonitor) info.base;
    }

    private void initInternal() {
        if (container.grid == null) {
            this.entity.add(new UILabel(I18Wrapper.format("gui.notconnected")));
            return;
        }
        final UIEntity list = new UIEntity();
        list.setInherits(true);
        list.add(new UIBox(UIBox.VBOX, 5));

        this.entity.add(new UIBox(UIBox.HBOX, 5));
        this.entity.add(GuiElements.createSpacerH(10));
        this.entity.add(list);
        this.entity.add(GuiElements.createSpacerH(10));

        final UIEntity upperEntity = new UIEntity();
        upperEntity.setInheritWidth(true);
        upperEntity.setHeight(30);
        upperEntity.add(new UIBox(UIBox.HBOX, 5));
        list.add(upperEntity);

        final UIEntity boxEntity = box.entity;

        final UISignalBoxProfile uiProfile = container.grid.getUIProfile();
        final UIMouseUpdate mouseUpdate = getUserSelectionGrid(box, uiProfile);

        boxEntity.add(mouseUpdate);

        upperEntity.add(GuiElements.createLabel(I18Wrapper.format("tile.monitor.name")));
        upperEntity.add(GuiElements.createButton("new_section", 100, e -> {
            box.rendering.clearColoredPoints();
            selectedPoints.clear();
            container.renderStart = null;
            mouseUpdate.enable();
        }));
        list.add(boxEntity);
    }

    private UIMouseUpdate getUserSelectionGrid(final BoxEntity box,
            final UISignalBoxProfile uiProfile) {
        final int maxTileX = 10;
        final int maxTileY = 10;
        final int color = uiProfile.getOperationModeSettings().getUserSelectionColor();
        return new UIMouseUpdate(p -> {
            if (selectedPoints.contains(p))
                return;
            final Point start = getStartPoint(p);
            if (p.getX() < start.getX() || p.getY() < start.getY()
                    || p.getX() >= start.getX() + maxTileX || p.getY() >= start.getY() + maxTileY)
                return;

            selectedPoints.add(p);
            box.rendering.addColoredPoint(color, p);
            if (start.getX() != p.getX() && start.getY() != p.getY()) {
                for (int i = start.getX(); i <= p.getX(); i++) {
                    for (int j = start.getY(); j <= p.getY(); j++) {
                        final Point newPoint = new Point(i, j);
                        if (!selectedPoints.contains(newPoint)) {
                            selectedPoints.add(newPoint);
                            box.rendering.addColoredPoint(color, newPoint);
                        }
                    }
                }
            }
            container.renderEnd = p;
        }, () -> container.sendNewPointsToServer(), box.rendering);
    }

    private Point getStartPoint(final Point defaultPoint) {
        if (container.renderStart == null) {
            container.renderStart = defaultPoint;
        }
        return container.renderStart;
    }

    private void addRenderSelection() {
        if (container.grid == null)
            return;
        final Point defaultPoint = new Point(-1, -1);
        final Point renderStart = container.renderStart;
        final Point renderEnd = container.renderEnd;
        final int color =
                container.grid.getUIProfile().getOperationModeSettings().getUserSelectionColor();
        if (renderStart.equals(defaultPoint) || renderEnd.equals(defaultPoint))
            return;

        for (int i = renderStart.getX(); i <= renderEnd.getX(); i++) {
            for (int j = renderStart.getY(); j <= renderEnd.getY(); j++) {
                box.rendering.addColoredPoint(color, new Point(i, j));
            }
        }
    }

    @Override
    public void updateFromContainer() {
        if (container.grid != null) {
            final UISignalBoxProfile uiProfile = container.grid.getUIProfile();
            box = UISignalBoxRendering.createSignalBoxEntity(container.grid, uiProfile,
                    uiProfile.getOperationModeSettings().getUIBorderSettings(), (_u1, _u2, _u3) -> {
                    });
        }
        initInternal();
        addRenderSelection();
    }

    private static class UIMouseUpdate extends UIComponent {

        private final UISignalBoxRendering rendering;
        private final Consumer<Point> consuner;
        private final Runnable onRelease;
        private boolean enable;

        public UIMouseUpdate(final Consumer<Point> consuner, final Runnable onRelease,
                final UISignalBoxRendering rendering) {
            this.consuner = consuner;
            this.onRelease = onRelease;
            this.rendering = rendering;
        }

        @Override
        public void draw(final DrawInfo info) {
        }

        public void enable() {
            enable = true;
        }

        @Override
        public void mouseEvent(final MouseEvent event) {
            if (!this.visible || !getParent().isHovered() || !enable)
                return;
            final UIEntity parent = rendering.getParent();
            final double x = event.x - parent.getLevelX();
            final double y = event.y - parent.getLevelY();
            final double actualWidth = UISignalBoxRendering.TILE_WIDTH * parent.getScaleX();
            final Point point = new Point((int) (x / actualWidth), (int) (y / actualWidth));
            if (event.state == EnumMouseState.CLICKED) {
                this.consuner.accept(point);
            } else if (event.state == EnumMouseState.RELEASE) {
                onRelease.run();
                enable = false;
            }
        }
    }
}