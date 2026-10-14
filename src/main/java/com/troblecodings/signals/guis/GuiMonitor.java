package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.troblecodings.core.I18Wrapper;
import com.troblecodings.guilib.ecs.ContainerBase;
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
    private UILabel ratioInfo;
    private BoxEntity box;
    private float monitorRatio = 0;
    private UIMouseUpdate mouseUpdate;

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
        mouseUpdate = getUserSelectionGrid(box, uiProfile);

        boxEntity.add(mouseUpdate);

        upperEntity.add(GuiElements.createLabel(I18Wrapper.format("tile.monitor.name")));
        upperEntity.add(
                GuiElements.createButton(I18Wrapper.format("gui.monitor.new_section"), 70, e -> {
                    box.rendering.clearColoredPoints();
                    selectedPoints.clear();
                    container.renderStart = new Point(-1, -1);
                    container.renderEnd = new Point(-1, -1);
                    ratioInfo.setText(I18Wrapper.format("gui.monitor.current_ratio") + ":   :  ");
                    mouseUpdate.enable();
                }));

        final UIEntity ratioEntity = new UIEntity();
        ratioEntity.setHeight(40);
        ratioEntity.setWidth(100);
        ratioEntity.add(new UIBox(UIBox.VBOX, 2));

        ratioEntity.add(GuiElements.createLabel(I18Wrapper.format("gui.monitor.monitor_ratio")
                + ": " + container.monitorSizeX + " : " + container.monitorSizeY));

        final UIEntity currentRatio =
                GuiElements.createLabel(I18Wrapper.format("gui.monitor.current_ratio") + ": "
                        + (container.renderEnd.getX() - container.renderStart.getX()) + " : "
                        + (container.renderEnd.getY() - container.renderStart.getY()));
        ratioInfo = currentRatio.findRecursive(UILabel.class).stream().findFirst()
                .orElse(new UILabel(""));
        ratioEntity.add(currentRatio);

        upperEntity.add(ratioEntity);

        list.add(boxEntity);
    }

    private UIMouseUpdate getUserSelectionGrid(final BoxEntity box,
            final UISignalBoxProfile uiProfile) {
        final int color = uiProfile.getOperationModeSettings().getUserSelectionColor();
        return new UIMouseUpdate(current -> {
            if (current.equals(container.renderEnd))
                return;
            final Point start = getStartPoint(current);
            if (current.getX() < start.getX() || current.getY() < start.getY())
                return;

            setUpSelectionFromTo(start, current, color);
            updateRatioInfo(start, current);
            container.renderEnd = current;
        }, (update) -> {
            if (selectedPoints.size() == 1) {
                update.enable();
                return;
            }
            container.sendNewPointsToServer();
        }, (hoveredPoint) -> {
            if (selectedPoints.isEmpty())
                return;
            updateRatioInfo(selectedPoints.get(0), hoveredPoint);
        }, box.rendering);
    }

    private void setUpSelectionFromTo(final Point start, final Point end, final int color) {
        selectedPoints.clear();
        box.rendering.clearColoredPoints();
        for (int i = start.getX(); i <= end.getX(); i++) {
            for (int j = start.getY(); j <= end.getY(); j++) {
                final Point newPoint = new Point(i, j);
                selectedPoints.add(newPoint);
                box.rendering.addColoredPoint(color, newPoint);
            }
        }
    }

    private void updateRatioInfo(final Point start, final Point end) {
        final int distX = end.getX() <= start.getX() ? 1 : end.getX() - start.getX() + 1;
        final int distY = end.getY() <= start.getY() ? 1 : end.getY() - start.getY() + 1;
        final boolean isInRatio = ((float) distX / (float) distY) == monitorRatio;
        ratioInfo.setText(
                I18Wrapper.format("gui.monitor.current_ratio") + ": " + distX + " : " + distY);
        ratioInfo.setTextColor(isInRatio ? 0xFF00FF00 : 0xFFFF0000);
    }

    private Point getStartPoint(final Point defaultPoint) {
        if (container.renderStart == null || container.renderStart.equals(new Point(-1, -1))) {
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
        monitorRatio = (float) container.monitorSizeX / (float) container.monitorSizeY;
        updateRatioInfo(renderStart, renderEnd);
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

    @Override
    protected void mouseClickMove(final int mouseX, final int mouseY, final int clickedMouseButton,
            final long timeSinceLastClick) {
        if (mouseUpdate != null) {
            mouseUpdate.mouseEvent(new MouseEvent(mouseX, mouseY, 0, EnumMouseState.MOVE));
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    private static class UIMouseUpdate extends UIComponent {

        private final UISignalBoxRendering rendering;
        private final Consumer<Point> consumer;
        private final Consumer<UIMouseUpdate> onRelease;
        private final Consumer<Point> ratioUpdate;
        private boolean enable;

        public UIMouseUpdate(final Consumer<Point> consumer,
                final Consumer<UIMouseUpdate> onRelease, final Consumer<Point> ratioUpdate,
                final UISignalBoxRendering rendering) {
            this.consumer = consumer;
            this.onRelease = onRelease;
            this.rendering = rendering;
            this.ratioUpdate = ratioUpdate;
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
            if (event.state != null) {
                switch (event.state) {
                    case CLICKED:
                        this.consumer.accept(point);
                        break;
                    case RELEASE:
                        enable = false;
                        onRelease.accept(this);
                        break;
                    case MOVE:
                        ratioUpdate.accept(point);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public ContainerBase getNewGuiContainer(final GuiInfo info) {
        return new ContainerMonitor(info);
    }
}