package com.troblecodings.signals.guis;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.troblecodings.core.I18Wrapper;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.DrawUtil.DisableIntegerable;
import com.troblecodings.guilib.ecs.GuiBase;
import com.troblecodings.guilib.ecs.GuiElements;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.entitys.UIBox;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.guilib.ecs.entitys.UIEnumerable;
import com.troblecodings.guilib.ecs.entitys.input.UIClickable;
import com.troblecodings.guilib.ecs.entitys.render.UIButton;
import com.troblecodings.guilib.ecs.entitys.render.UIColor;
import com.troblecodings.guilib.ecs.entitys.render.UILabel;
import com.troblecodings.guilib.ecs.entitys.render.UITexture;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.core.JsonEnum;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.handler.ClientNameHandler;
import com.troblecodings.signals.handler.ClientSignalStateHandler;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.parser.interm.LogicalSymbols;
import com.troblecodings.signals.tileentitys.SignalReaderTileEntity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;

public class GuiSignalReader extends GuiBase {

    private final ContainerSignalReader container;
    private final PreviewSideBar previewSidebar = new PreviewSideBar(-8.5f);
    private final UIEntity lowerEntity = new UIEntity();

    public GuiSignalReader(final GuiInfo info) {
        super(info);
        this.container = (ContainerSignalReader) info.base;
        initInternal();
    }

    private void initInternal() {
        this.entity.clear();

        if (container.signal == null) {
            this.entity.add(new UILabel(I18Wrapper.format("gui.notconnected")));
            return;
        }
        final UIEntity topEntity = new UIEntity();
        topEntity.setInheritWidth(true);
        topEntity.setHeight(30);
        topEntity.add(new UIBox(UIBox.HBOX, 0));

        lowerEntity.setInherits(true);
        lowerEntity.add(new UIBox(UIBox.HBOX, 5));

        entity.add(new UIBox(UIBox.VBOX, 5));
        entity.add(topEntity);
        entity.add(lowerEntity);

        topEntity.add(getLabelEntity());
        addPropertyAndSideSelection();
    }

    private void addPropertyAndSideSelection() {
        final UIEntity leftEntity = new UIEntity();
        leftEntity.setInherits(true);
        leftEntity.add(new UIBox(UIBox.VBOX, 5));

        lowerEntity.add(leftEntity);
        lowerEntity.add(previewSidebar.get());

        final UIEntity sideSelection = new UIEntity();
        sideSelection.setInheritWidth(true);
        sideSelection.setHeight(30);
        sideSelection.add(new UIBox(UIBox.HBOX, 10));
        sideSelection.setX(20);

        final UIEntity propertyList = new UIEntity();
        propertyList.setInherits(true);
        final UIBox vbox = new UIBox(UIBox.VBOX, 1);
        propertyList.add(vbox);

        leftEntity.add(sideSelection);
        leftEntity.add(propertyList);
        addSideSelection(sideSelection, propertyList);
        leftEntity.add(GuiElements.createPageSelect(vbox));
    }

    private void addSideSelection(final UIEntity sideSelection, final UIEntity propertyList) {
        final Minecraft mc = Minecraft.getMinecraft();
        final IBlockState state = OSBlocks.SIGNALREADER.getDefaultState();
        final IBakedModel model = mc.getBlockRendererDispatcher().getModelForState(state);

        final UIEnumerable toggle = new UIEnumerable(EnumFacing.values().length, "Face");
        toggle.setOnChange(e -> {
            final EnumFacing faceing = EnumFacing.values()[e];

            final List<UIColor> colors = sideSelection.findRecursive(UIColor.class);
            colors.forEach(c -> c.setColor(0x70000000));
            colors.get(e).setColor(0x70FF0000);
            setUpPropertiesForDirection(faceing, propertyList);
        });
        sideSelection.add(toggle);
        for (final EnumFacing face : EnumFacing.values()) {
            final List<BakedQuad> quad = model.getQuads(state, face, 0);
            final UIEntity faceEntity = new UIEntity();
            faceEntity.setWidth(30);
            faceEntity.setHeight(30);
            faceEntity.add(new UITexture(quad.get(0).getSprite()));
            final UIColor color = new UIColor(face.ordinal() == 0 ? 0x70FF0000 : 0x70000000);
            faceEntity.add(color);
            faceEntity.add(new UIClickable(e -> toggle.setIndex(face.ordinal())));
            final UILabel label = new UILabel(face.getName().substring(0, 1).toUpperCase());
            label.setTextColor(0xFFFFFFFF);
            faceEntity.add(label);
            sideSelection.add(faceEntity);
        }
        setUpPropertiesForDirection(EnumFacing.values()[0], propertyList);
    }

    private void setUpPropertiesForDirection(final EnumFacing dir, final UIEntity list) {
        list.clearChildren();
        @SuppressWarnings("unchecked")
        final Entry<LogicalSymbols[], Entry<SEProperty, String>[]> entryForDirection =
                container.statesForFace.computeIfAbsent(dir,
                        _u -> Maps.immutableEntry(
                                new LogicalSymbols[SignalReaderTileEntity.MAX_LOGICAL_VALUES_SIZE],
                                new Entry[SignalReaderTileEntity.MAX_PROPERTIES_SIZE]));
        final LogicalSymbols[] logicSymbols = entryForDirection.getKey();
        final Entry<SEProperty, String>[] propertyEntries = entryForDirection.getValue();
        for (int i = 0; i < propertyEntries.length; i++) {
            final Map.Entry<SEProperty, String> entry = propertyEntries[i];
            if (entry == null) {
                continue;
            }
            final int index = i;
            final UIEntity row = new UIEntity();
            row.setHeight(20);
            row.setInheritWidth(true);
            row.add(new UIBox(UIBox.HBOX, 5));

            final int logicSymbolID = i - 1;
            if (logicSymbolID >= 0) {
                list.add(getCenterdXUIEntity(getEntityFromSymbol(dir, logicSymbols, logicSymbolID),
                        20));
            }

            list.add(row);

            final SEProperty property = entry.getKey();
            final String value = entry.getValue();
            final JsonEnum enumJson = property.getParent();
            previewSidebar.addToRenderNormal(property, enumJson.getIDFromValue(value));
            final UIEntity entity =
                    GuiElements.createEnumElement(new DisableIntegerable<>(entry.getKey()), e -> {
                        if (e == -1) {
                            propertyEntries[index] = null;
                            if (logicSymbolID >= 0) {
                                logicSymbols[logicSymbolID] = null;
                            } else if (index == 0) {
                                logicSymbols[0] = null;
                            }
                            reorderArray(propertyEntries);
                            reorderArray(logicSymbols);
                        } else {
                            propertyEntries[index] =
                                    Maps.immutableEntry(property, property.getObjFromID(e));
                        }
                        setUpPropertiesForDirection(dir, list);
                        container.sendToServerForDirection(dir);
                    }, property.getParent().getIDFromValue(value));

            row.add(entity);

            row.add(GuiElements.createButton("x", 20, e -> {
                propertyEntries[index] = null;
                if (logicSymbolID >= 0) {
                    logicSymbols[logicSymbolID] = null;
                } else if (index == 0) {
                    logicSymbols[0] = null;
                }
                reorderArray(propertyEntries);
                reorderArray(logicSymbols);

                setUpPropertiesForDirection(dir, list);
                container.sendToServerForDirection(dir);
            }));
        }
        if (!isArrayFull(propertyEntries)) {
            list.add(GuiElements.createSpacerV(10));
            list.add(getCenterdXUIEntity(GuiElements.createButton("+", 20, e -> {
                push(GuiElements.createScreen(screen -> {
                    final UIEntity propertyList = new UIEntity();
                    propertyList.setInherits(true);
                    final UIBox vbox = new UIBox(UIBox.VBOX, 2);
                    propertyList.add(vbox);
                    screen.add(propertyList);
                    propertyList.add(GuiElements.createButton("<", 20, e1 -> pop()));
                    container.selectableProperties.forEach(property -> {
                        propertyList
                                .add(GuiElements.createButton(property.getLocalizedName(), e1 -> {
                                    if (propertyEntries[0] != null) {
                                        logicSymbols[getNextFreeIndex(logicSymbols)] =
                                                LogicalSymbols.OR;
                                    }
                                    propertyEntries[getNextFreeIndex(propertyEntries)] =
                                            Maps.immutableEntry(property, property.getDefault());
                                    pop();
                                    setUpPropertiesForDirection(dir, list);
                                    container.sendToServerForDirection(dir);
                                }));
                    });
                    screen.add(GuiElements.createPageSelect(vbox));
                }));
            }), 20));
        }

        ClientSignalStateHandler.getClientStates(new StateInfo(mc.world, container.pos))
                .forEach((property, value) -> {
                    previewSidebar.addToRenderNormal(property,
                            property.getParent().getIDFromValue(value));
                });

        previewSidebar.update(container.signal);
    }

    private UIEntity getEntityFromSymbol(final EnumFacing dir, final LogicalSymbols[] logicSymbols,
            final int logicSymbolID) {
        final LogicalSymbols symbol = logicSymbols[logicSymbolID];
        return GuiElements.createButton(getNameForSymbol(symbol), 50, buttonEntity -> {
            final LogicalSymbols newSymbol =
                    logicSymbols[logicSymbolID].equals(LogicalSymbols.AND) ? LogicalSymbols.OR
                            : LogicalSymbols.AND;
            buttonEntity.findRecursive(UIButton.class)
                    .forEach(b -> b.setText(getNameForSymbol(newSymbol)));
            logicSymbols[logicSymbolID] = newSymbol;
            container.sendToServerForDirection(dir);
        });
    }

    private static UIEntity getCenterdXUIEntity(final UIEntity entity, final int height) {
        final UIEntity row = new UIEntity();
        row.setInheritWidth(true);
        row.setHeight(height);
        row.add(new UIBox(UIBox.HBOX, 0));
        row.add(GuiElements.createSpacerV(height));
        row.add(entity);
        row.add(GuiElements.createSpacerV(height));
        return row;
    }

    private static String getNameForSymbol(final LogicalSymbols symbol) {
        return symbol.equals(LogicalSymbols.AND) ? I18Wrapper.format("gui.signalreader.and")
                : I18Wrapper.format("gui.signalreader.or");
    }

    private static int getNextFreeIndex(final Object[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == null)
                return i;
        }
        return -1;
    }

    private static void reorderArray(final Object[] array) {
        int toMove = 0;
        for (int i = 0; i < array.length; i++) {
            final Object obj = array[i];
            if (obj == null) {
                toMove++;
            } else {
                array[i] = null;
                array[i - toMove] = obj;
            }
        }
    }

    private boolean isArrayFull(final Object[] array) {
        for (final Object obj : array) {
            if (obj == null)
                return false;
        }
        return true;
    }

    private UIEntity getLabelEntity() {
        final UIEntity labelEntity = new UIEntity();
        labelEntity.setInherits(true);
        labelEntity.setScale(1.5f);

        final UILabel label = new UILabel(
                I18Wrapper.format("tile.signalreader.name") + "; Name: "
                        + ClientNameHandler.getClientName(new StateInfo(mc.world, container.pos))
                                .replace("[n]", " "));
        label.setCenterX(false);
        label.setCenterY(true);
        labelEntity.add(label);
        return labelEntity;
    }

    @Override
    public void updateFromContainer() {
        initInternal();
    }

    @Override
    public void push(final UIEntity entity) {
        previewSidebar.setDisable(true);
        super.push(entity);
    }

    @Override
    public UIEntity pop() {
        previewSidebar.setDisable(false);
        return super.pop();
    }

    @Override
    public ContainerBase getNewGuiContainer(final GuiInfo info) {
        return new ContainerSignalReader(info);
    }

}
