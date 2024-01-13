package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.math.Quaternion;
import com.troblecodings.core.I18Wrapper;
import com.troblecodings.guilib.ecs.DrawUtil.NamedEnumIntegerable;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.entitys.UIBlockRender;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
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
import com.troblecodings.signalbridge.SignalBridgeBasicBlock;
import com.troblecodings.signals.contentpacks.SignalBridgeBlockParser;
import com.troblecodings.signals.enums.SignalBridgeType;
import com.troblecodings.signals.models.ModelInfoWrapper;

public class SignalBridgeGui extends GuiBase {

    private static final ModelInfoWrapper EMPTY_WRAPPER = new ModelInfoWrapper(new HashMap<>());
    private static final UIBorder SELECTED_BORDER = new UIBorder(0xFF00FF00, 1);

    private final UIEntity leftEntity = new UIEntity();
    private final UIEntity rightEntity = new UIEntity();
    private final Map<SignalBridgeBasicBlock, UIEntity> entityForBlock = new HashMap<>();
    private SignalBridgeBasicBlock currentBlock;

    public SignalBridgeGui(final GuiInfo info) {
        super(info);
        initInternal();
    }

    private void initInternal() {
        this.entity.add(new UIBox(UIBox.HBOX, 1));
        this.entity.add(leftEntity);
        this.entity.add(rightEntity);
        updateAvailableBridgeParts(SignalBridgeType.BASE);
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
            final UIEntity blockEntity = new UIEntity();
            blockEntity.setWidth(80);
            blockEntity.setHeight(60);
            blockEntity.add(new UIColor(GuiSignalBox.BACKGROUND_COLOR));
            final UILabel label = new UILabel(
                    I18Wrapper.format("block." + block.delegate.name().getPath() + ".name"));
            label.setCenterY(false);
            label.setTextColor(blockEntity.getBasicTextColor());
            blockEntity.add(label);
            blockEntity.add(new UIClickable(e -> {
                if (currentBlock != null)
                    removeUISelection(currentBlock);
                if (currentBlock == block) {
                    currentBlock = null;
                    return;
                }
                addUISelection(block);
                currentBlock = block;
            }, 1));

            final UIEntity preview = new UIEntity();
            final UIBlockRender renderer = new UIBlockRender(14, -2);
            renderer.setBlockState(block.defaultBlockState(), EMPTY_WRAPPER);
            preview.setWidth(60);
            preview.setHeight(60);
            preview.add(new UIScale(1.9f, 1.9f, 1.9f));

            preview.add(new UIDrag(
                    (x, y) -> renderer.updateRotation(Quaternion.fromXYZ(0, (float) x * 0.1f, 0))));

            preview.add(new UIScissor());
            preview.add(renderer);

            blockEntity.add(preview);
            list.add(blockEntity);
            entityForBlock.put(block, blockEntity);
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
    }

    private void removeUISelection(final SignalBridgeBasicBlock block) {
        final UIEntity blockEntity = entityForBlock.get(block);
        blockEntity.remove(SELECTED_BORDER);
        blockEntity.update();
    }

    private void addUISelection(final SignalBridgeBasicBlock block) {
        final UIEntity blockEntity = entityForBlock.get(block);
        blockEntity.add(SELECTED_BORDER);
        blockEntity.update();
    }

    @Override
    public void updateFromContainer() {
        initInternal();
    }

}