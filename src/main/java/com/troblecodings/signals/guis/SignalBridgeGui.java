package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.math.Quaternion;
import com.troblecodings.core.I18Wrapper;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.DrawUtil.NamedEnumIntegerable;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.entitys.UIBlockRender;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIMultiBlockRender;
import com.troblecodings.guilib.ecs.entitys.UIScrollBox;
import com.troblecodings.guilib.ecs.entitys.input.UIClickable;
import com.troblecodings.guilib.ecs.entitys.input.UIDrag;
import com.troblecodings.guilib.ecs.entitys.input.UIScroll;
import com.troblecodings.guilib.ecs.entitys.render.UIBorder;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UIScissor;
import com.troblecodings.guilib.ecs.entitys.transform.UIScale;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.BasicBlock;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.contentpacks.SignalBridgeBlockParser;
import com.troblecodings.signals.enums.SignalBridgeNetwork;
import com.troblecodings.signals.enums.SignalBridgeType;
import com.troblecodings.signals.models.ModelInfoWrapper;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbridge.SignalBridgeBasicBlock;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;

public class SignalBridgeGui extends GuiBase {

    private static final ModelInfoWrapper EMPTY_WRAPPER = new ModelInfoWrapper(new HashMap<>());
    private static final UIBorder SELECTED_BORDER = new UIBorder(0xFF00FF00, 1);
    private static final int TILE_WIDTH = 20;
    private static final int TILE_COUNT = 10;

    private final UIEntity leftEntity = new UIEntity();
    private final UIEntity rightEntity = new UIEntity();
    private final Map<SignalBridgeBasicBlock, UIEntity> blockForEntity = new HashMap<>();
    private final SignalBridgeContainer container;
    private final Player player;
    private SignalBridgeBasicBlock currentBlock;

    public SignalBridgeGui(final GuiInfo info) {
        super(info);
        this.container = (SignalBridgeContainer) info.base;
        this.player = info.player;
    }

    private void initInternal() {
        this.entity.add(new UIBox(UIBox.VBOX, 5));
        final UIEntity lowerEntity = new UIEntity();
        lowerEntity.setInherits(true);
        lowerEntity.add(new UIBox(UIBox.HBOX, 20));
        lowerEntity.add(leftEntity);
        lowerEntity.add(rightEntity);
        final UIEntity header = new UIEntity();
        header.setInheritWidth(true);
        header.setHeight(20);
        header.add(new UIBox(UIBox.HBOX, 5));
        header.add(GuiElements.createLabel(I18Wrapper.format("gui.signalbridge.title"),
                header.getBasicTextColor(), 1.1f));
        header.add(GuiElements.createSpacerH(10));
        header.add(GuiElements.createButton(I18Wrapper.format("gui.signalbride.preview"),
                e -> buildBridgePreview()));
        header.add(GuiElements.createButton("?", 20, e -> {
            final UIEntity screen = GuiElements.createScreen(screenEntity -> {
                screenEntity.add(
                        GuiElements.createButton(I18Wrapper.format("gui.return"), _u -> pop()));
                screenEntity.add(GuiElements.createSpacerV(50));
                screenEntity.add(GuiElements.createLabel(I18Wrapper.format("gui.signalbridge.info"),
                        screenEntity.getBasicTextColor(), 1.1f));
            });
            push(screen);
        }));
        entity.add(header);
        entity.add(lowerEntity);
        updateAvailableBridgeParts(SignalBridgeType.BASE);
        buildGrid();
    }

    private void updateAvailableBridgeParts(final SignalBridgeType type) {
        currentBlock = null;
        leftEntity.clear();
        leftEntity.setInheritHeight(true);
        leftEntity.setWidth(80);
        leftEntity.add(new UIBox(UIBox.VBOX, 1));

        final IIntegerable<SignalBridgeType> integerable = new NamedEnumIntegerable<>(
                I18Wrapper.format("gui.bridge.type"), SignalBridgeType.class);
        leftEntity.add(GuiElements.createEnumElement(integerable,
                i -> updateAvailableBridgeParts(integerable.getObjFromID(i)), type.ordinal()));

        final UIEntity scroll = new UIEntity();
        scroll.setInherits(true);
        scroll.add(new UIBox(UIBox.HBOX, 1));
        scroll.add(new UIScissor());
        leftEntity.add(scroll);
        final UIEntity list = new UIEntity();
        scroll.add(list);
        list.setInherits(true);
        list.add(new UIBox(UIBox.VBOX, 1));

        final List<SignalBridgeBasicBlock> typeBlocks = SignalBridgeBlockParser.SIGNAL_BRIDGE_BLOCKS
                .getOrDefault(type, new ArrayList<>());
        typeBlocks.forEach(block -> {
            final UIEntity blockEntity = createPreviewForBlock(block, 14, -2, 1.9f, 80, 60, true, 0,
                    0, true);
            blockEntity.add(new UIClickable(e -> {
                if (currentBlock != null)
                    removeUISelection(currentBlock);
                if (currentBlock == block) {
                    currentBlock = null;
                    return;
                }
                addUISelection(block);
                currentBlock = block;
            }));
            list.add(blockEntity);
            blockForEntity.put(block, blockEntity);
        });

        final UIScrollBox scrollBox = new UIScrollBox(UIBox.VBOX, 2);
        final UIScroll scrolling = new UIScroll();
        final UIEntity scrollBar = GuiElements.createScrollBar(scrollBox, 7, scrolling);
        scrollBox.setConsumer(size -> {
            if (size > list.getHeight()) {
                scroll.add(scrolling);
                scroll.add(scrollBar);
            } else {
                scroll.remove(scrollBar);
                scroll.remove(scrolling);
            }
        });
        list.add(scrollBox);
        entity.update();
    }

    private void buildGrid() {
        rightEntity.clear();
        rightEntity.setInherits(true);
        rightEntity.add(new UIBox(UIBox.HBOX, 1));
        final UIEntity plane = new UIEntity();
        plane.setHeight(TILE_COUNT * TILE_WIDTH);
        plane.setWidth(TILE_COUNT * TILE_WIDTH);
        plane.add(new UIBorder(GuiSignalBox.GRID_COLOR, 2));
        plane.add(new UIColor(GuiSignalBox.BACKGROUND_COLOR));
        plane.add(new UIBox(UIBox.VBOX, 0));
        for (int x = 0; x < TILE_COUNT; x++) {
            final UIEntity row = new UIEntity();
            final UIBox hbox = new UIBox(UIBox.HBOX, 0);
            hbox.setPageable(false);
            row.add(hbox);
            row.setHeight(TILE_WIDTH);
            row.setWidth(TILE_WIDTH);
            for (int y = 0; y < TILE_COUNT; y++) {
                final Point point = new Point(y, x);
                final UIEntity tile = new UIEntity();
                tile.setHeight(TILE_WIDTH);
                tile.setWidth(TILE_WIDTH);
                tile.add(new UIBorder(GuiSignalBox.GRID_COLOR, 0.5f));
                row.add(tile);
                final SignalBridgeBasicBlock savedBlock = container.builder.getBlockOnPoint(point);
                if (savedBlock != null) {
                    final UIEntity blockEntity = createPreviewForBlock(savedBlock, 15, -1, 1,
                            TILE_WIDTH, TILE_WIDTH, false, -12.5f, 3, false);
                    tile.add(blockEntity);
                }
                tile.add(new UIClickable(e -> {
                    if (currentBlock == null) {
                        return;
                    }
                    final SignalBridgeBasicBlock block = container.builder.getBlockOnPoint(point);
                    final UIEntity blockEntity = createPreviewForBlock(currentBlock, 15, -1, 1,
                            TILE_WIDTH, TILE_WIDTH, false, -12.5f, 3, false);
                    if (block == null) {
                        tile.add(blockEntity);
                        container.builder.addBlock(point, currentBlock);
                        sendNewBlock(point, currentBlock);
                    } else if (block == currentBlock) {
                        tile.clearChildren();
                        container.builder.removeBridgeBlock(point);
                        sendRemoveBlock(point);
                    }
                    tile.update();
                }));
                tile.add(new UIClickable(e -> {
                    if (Screen.hasControlDown()) {
                        tile.add(new UIBorder(0xFF0000FF, 2));
                        container.builder.changeStartPoint(point);
                    }
                }, 1));
            }
            plane.add(row);
        }
        rightEntity.add(plane);
        entity.update();
    }

    private void buildBridgePreview() {
        rightEntity.clear();
        rightEntity.setInherits(true);
        final UIEntity entity = new UIEntity();
        entity.setHeight(200);
        entity.setWidth(200);
        entity.add(new UIBorder(GuiSignalBox.GRID_COLOR, 2));
        entity.add(new UIColor(GuiSignalBox.BACKGROUND_COLOR));
        final UIEntity renderEntity = new UIEntity();
        renderEntity.setX(140);
        renderEntity.setY(-17);
        final UIMultiBlockRender render = new UIMultiBlockRender(20, -10);
        final List<Map.Entry<Vec3i, BasicBlock>> list = container.builder.getRelativesToStart();
        if (list.isEmpty()) {
            entity.add(new UILabel("gui.signalbridge.noblock"));
            return;
        }
        list.forEach(entry -> {
            final Vec3i vec = entry.getKey();
            render.setBlockState(entry.getValue().defaultBlockState(), EMPTY_WRAPPER, vec.getX(),
                    vec.getY(), vec.getZ());
        });
        renderEntity.add(render);
        entity.add(renderEntity);
        entity.add(new UIDrag(
                (x, y) -> render.updateRotation(Quaternion.fromXYZ(0, (float) x * 0.1f, 0))));
        entity.add(new UIScroll(s -> {
            final float newScale = (float) (entity.getScaleX() + s * 0.05f);
            if (newScale <= 0)
                return;
            entity.setScaleX(newScale);
            entity.setScaleY(newScale);
            entity.update();
        }));
        rightEntity.add(entity);
        entity.update();
    }

    private static UIEntity createPreviewForBlock(final SignalBridgeBasicBlock block,
            final float renderScale, final float renderHeight, final float previewScale,
            final float width, final float height, final boolean showName, final double previewX,
            final double previewY, final boolean enableRotation) {
        final UIEntity blockEntity = new UIEntity();
        blockEntity.setWidth(width);
        blockEntity.setHeight(height);
        blockEntity.add(new UIColor(GuiSignalBox.BACKGROUND_COLOR));
        if (showName) {
            final UILabel label = new UILabel(
                    I18Wrapper.format("block." + block.delegate.name().getPath() + ".name"));
            label.setCenterY(false);
            label.setTextColor(blockEntity.getBasicTextColor());
            blockEntity.add(label);
        }
        final UIEntity preview = new UIEntity();
        final UIBlockRender renderer = new UIBlockRender(renderScale, renderHeight);
        renderer.setBlockState(block.defaultBlockState(), EMPTY_WRAPPER);
        preview.setWidth(60);
        preview.setHeight(60);
        preview.setX(previewX);
        preview.setY(previewY);
        preview.add(new UIScale(previewScale, previewScale, previewScale));

        if (enableRotation)
            preview.add(new UIDrag(
                    (x, y) -> renderer.updateRotation(Quaternion.fromXYZ(0, (float) x * 0.1f, 0)),
                    1));

        preview.add(new UIScissor());
        preview.add(renderer);

        blockEntity.add(preview);
        return blockEntity;
    }

    private void removeUISelection(final SignalBridgeBasicBlock block) {
        final UIEntity blockEntity = blockForEntity.get(block);
        blockEntity.remove(SELECTED_BORDER);
        blockEntity.update();
    }

    private void addUISelection(final SignalBridgeBasicBlock block) {
        final UIEntity blockEntity = blockForEntity.get(block);
        blockEntity.add(SELECTED_BORDER);
        blockEntity.update();
    }

    private void sendNewBlock(final Point point, final SignalBridgeBasicBlock block) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBridgeNetwork.SET_BLOCK);
        point.writeNetwork(buffer);
        buffer.putInt(block.getID());
        OpenSignalsMain.network.sendTo(player, buffer);
    }

    private void sendRemoveBlock(final Point point) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBridgeNetwork.REMOVE_BLOCK);
        point.writeNetwork(buffer);
        OpenSignalsMain.network.sendTo(player, buffer);
    }

    private void sendSignal(final Vec3i vec, final Signal signal) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBridgeNetwork.SET_BLOCK);
        buffer.putInt(vec.getX());
        buffer.putInt(vec.getY());
        buffer.putInt(vec.getZ());
        buffer.putInt(signal.getID());
        OpenSignalsMain.network.sendTo(player, buffer);
    }

    private void sendRemoveSignal(final Vec3i vec) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBridgeNetwork.REMOVE_SIGNAL);
        buffer.putInt(vec.getX());
        buffer.putInt(vec.getY());
        buffer.putInt(vec.getZ());
        OpenSignalsMain.network.sendTo(player, buffer);
    }

    @Override
    public void updateFromContainer() {
        initInternal();
    }

}