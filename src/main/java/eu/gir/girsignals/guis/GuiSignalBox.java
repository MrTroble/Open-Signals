package eu.gir.girsignals.guis;

import static eu.gir.girsignals.signalbox.SignalBoxUtil.POINT1;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.POINT2;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.RESET_WAY;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.toNBT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

import eu.gir.girsignals.EnumSignals.SortOptions;
import eu.gir.girsignals.enums.EnumGuiMode;
import eu.gir.girsignals.enums.EnumPathUsage;
import eu.gir.girsignals.enums.LinkType;
import eu.gir.girsignals.signalbox.ModeSet;
import eu.gir.girsignals.signalbox.Point;
import eu.gir.girsignals.signalbox.SignalBoxGrid;
import eu.gir.girsignals.signalbox.SignalBoxNode;
import eu.gir.girsignals.signalbox.SignalBoxTileEntity;
import eu.gir.girsignals.signalbox.SignalBoxUtil;
import eu.gir.girsignals.signalbox.entrys.PathEntryType;
import eu.gir.girsignals.signalbox.entrys.PathOptionEntry;
import eu.gir.guilib.ecs.DrawUtil.DisableIntegerable;
import eu.gir.guilib.ecs.DrawUtil.SizeIntegerables;
import eu.gir.guilib.ecs.GuiBase;
import eu.gir.guilib.ecs.GuiElements;
import eu.gir.guilib.ecs.GuiSyncNetwork;
import eu.gir.guilib.ecs.entitys.UIBox;
import eu.gir.guilib.ecs.entitys.UIEntity;
import eu.gir.guilib.ecs.entitys.UIEnumerable;
import eu.gir.guilib.ecs.entitys.UIStack;
import eu.gir.guilib.ecs.entitys.input.UIClickable;
import eu.gir.guilib.ecs.entitys.input.UIDrag;
import eu.gir.guilib.ecs.entitys.input.UIScroll;
import eu.gir.guilib.ecs.entitys.render.UIBorder;
import eu.gir.guilib.ecs.entitys.render.UIButton;
import eu.gir.guilib.ecs.entitys.render.UIColor;
import eu.gir.guilib.ecs.entitys.render.UILabel;
import eu.gir.guilib.ecs.entitys.render.UIScissor;
import eu.gir.guilib.ecs.entitys.render.UITexture;
import eu.gir.guilib.ecs.entitys.render.UIToolTip;
import eu.gir.guilib.ecs.entitys.transform.UIScale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public class GuiSignalBox extends GuiBase {

    private static final int SELECTION_COLOR = 0x2900FF00;
    private static final int BACKGROUND_COLOR = 0xFF8B8B8B;

    private final UIEntity lowerEntity = new UIEntity();
    private final SignalBoxTileEntity box;
    private final ContainerSignalBox container;
    private UISignalBoxTile lastTile = null;

    public GuiSignalBox(final SignalBoxTileEntity box) {
        this.box = box;
        this.container = new ContainerSignalBox(this::update);
        Minecraft.getMinecraft().player.openContainer = this.container;
        initializeBasicUI();
        if (this.box.getTag() != null)
            this.compound = this.box.getTag();
        this.entity.read(this.compound);
    }

    private void update(final NBTTagCompound compound) {
        this.resetTileSelection();
        if (compound.hasKey(SignalBoxGrid.ERROR_STRING)) {
            final String error = I18n.format(compound.getString(SignalBoxGrid.ERROR_STRING));
            final UIToolTip tooltip = new UIToolTip(error);
            entity.add(tooltip);
            new Thread(() -> {
                try {
                    Thread.sleep(4000);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                entity.remove(tooltip);
            }).start();
            return;
        }
        this.entity.read(compound);
    }

    private void resetTileSelection() {
        this.entity.findRecursive(UIColor.class).stream()
                .filter(color -> color.getColor() == SELECTION_COLOR)
                .forEach(color -> color.getParent().remove(color));
    }

    private void selectLink(final UIEntity parent, final SignalBoxNode node,
            final PathOptionEntry option, final ImmutableSet<Entry<BlockPos, LinkType>> entrySet,
            final LinkType type, final PathEntryType<BlockPos> entryType) {
        final List<BlockPos> positions = entrySet.stream().filter(e -> e.getValue().equals(type))
                .map(e -> e.getKey()).collect(Collectors.toList());
        if (!positions.isEmpty()) {
            final DisableIntegerable<String> blockPos = new DisableIntegerable<String>(
                    SizeIntegerables.of("prop." + type.name(), positions.size(), id -> {
                        final BlockPos pos = positions.get(id);
                        return getSignalInfo(pos, type);
                    }));
            final UIEntity blockSelect = GuiElements.createEnumElement(blockPos, id -> {
                option.setEntry(entryType, id >= 0 ? positions.get(id) : null);
                node.write(compound);
            });
            blockSelect.findRecursive(UIEnumerable.class).forEach(e -> {
                e.setMin(-1);
                final int index = option.getEntry(entryType).map(entry -> positions.indexOf(entry))
                        .orElse(-1);
                e.setIndex(index);
                e.setID(null);
            });
            parent.add(blockSelect);
        }
    }

    private String getSignalInfo(final BlockPos signalPos, final LinkType type) {
        final Map<BlockPos, String> names = this.container.getNames();
        final String customName = names == null ? null : names.get(signalPos);
        return String.format("%s (x=%d, y=%d. z=%d)",
                customName == null
                        ? (type.equals(LinkType.SIGNAL) ? "" : I18n.format("type." + type.name()))
                        : customName,
                signalPos.getX(), signalPos.getY(), signalPos.getZ());
    }

    private void setupModeSettings(final UIEntity parent, final EnumGuiMode mode,
            final Rotation rotation, final SignalBoxNode node, final PathOptionEntry option) {
        final String modeName = I18n.format("property." + mode.name());
        final String rotationName = I18n.format("property." + rotation.name() + ".rotation");
        final UIEntity entity = new UIEntity();
        entity.setInheritWidth(true);
        entity.setHeight(20);
        entity.add(new UIColor(BACKGROUND_COLOR));
        entity.add(new UIScale(1.1f, 1.1f, 1));
        final UILabel modeLabel = new UILabel(modeName + " - " + rotationName);
        modeLabel.setCenterX(false);
        entity.add(modeLabel);
        parent.add(entity);

        final ImmutableSet<Entry<BlockPos, LinkType>> entrySet = box.getPositions().entrySet();

        switch (mode) {
            case CORNER:
            case STRAIGHT: {
                final EnumPathUsage path = option.getEntry(PathEntryType.PATHUSAGE)
                        .orElse(EnumPathUsage.FREE);
                final UIEntity stateEntity = new UIEntity();
                stateEntity.setInheritWidth(true);
                stateEntity.setHeight(15);
                final String pathUsageName = I18n.format("property.status") + ": ";
                final String pathUsage = I18n.format("property." + path);
                stateEntity.add(new UILabel(pathUsageName + pathUsage));
                parent.add(stateEntity);

                final SizeIntegerables<Integer> size = new SizeIntegerables<>("speed", 15, i -> i);
                final UIEntity speedSelection = GuiElements.createEnumElement(size, id -> {
                    option.setEntry(PathEntryType.SPEED, id > 0 ? id : Integer.MAX_VALUE);
                    node.write(compound);
                });
                final int speed = option.getEntry(PathEntryType.SPEED).filter(n -> n < 16)
                        .orElse(Integer.MAX_VALUE);
                speedSelection.findRecursive(UIEnumerable.class).forEach(e -> {
                    e.setID(null);
                    e.setIndex(speed);
                    option.tryHook(PathEntryType.SPEED, e::setOnChange);
                });
                parent.add(speedSelection);

                selectLink(parent, node, option, entrySet, LinkType.OUTPUT, PathEntryType.OUTPUT);
                selectLink(parent, node, option, entrySet, LinkType.INPUT, PathEntryType.BLOCKING);
                selectLink(parent, node, option, entrySet, LinkType.INPUT, PathEntryType.RESETING);
            }
                break;
            case VP:
                selectLink(parent, node, option, entrySet, LinkType.SIGNAL, PathEntryType.SIGNAL);
                break;
            case HP:
            case RS: {
                selectLink(parent, node, option, entrySet, LinkType.SIGNAL, PathEntryType.SIGNAL);
                selectLink(parent, node, option, entrySet, LinkType.INPUT, PathEntryType.BLOCKING);
                parent.add(GuiElements.createButton(I18n.format("button.reset"), e -> {
                    this.lowerEntity.clear();
                    GuiSyncNetwork.sendToPosServer(compound, this.box.getPos());
                    initializeFieldTemplate(this::tileNormal);
                    final NBTTagCompound compound = new NBTTagCompound();
                    final NBTTagCompound wayComp = new NBTTagCompound();
                    toNBT(wayComp, POINT1, node.getPoint());
                    compound.setTag(RESET_WAY, wayComp);
                    GuiSyncNetwork.sendToPosServer(compound, this.box.getPos());
                }));
            }
                break;
            default:
                break;
        }

    }

    private void tileEdit(final UIEntity tile, final UIMenu menu, final UISignalBoxTile sbt) {
        tile.add(new UIClickable(e -> {
            final SignalBoxNode node = sbt.getNode();
            final EnumGuiMode mode = EnumGuiMode.values()[menu.getSelection()];
            final Rotation rotation = Rotation.values()[menu.getRotation()];
            final ModeSet modeSet = new ModeSet(mode, rotation);
            if (node.has(modeSet)) {
                node.remove(modeSet);
            } else {
                node.add(modeSet);
            }
        }));
    }

    private void tileNormal(final UIEntity tile, final UISignalBoxTile currentTile) {
        tile.add(new UIClickable(c -> {
            final SignalBoxNode currentNode = currentTile.getNode();
            if (!currentNode.isValidStart())
                return;
            c.add(new UIColor(SELECTION_COLOR));
            if (lastTile == null) {
                lastTile = currentTile;
            } else {
                if (lastTile == currentTile) {
                    lastTile = null;
                    this.resetTileSelection();
                    return;
                }
                final NBTTagCompound comp = new NBTTagCompound();
                final NBTTagCompound way = new NBTTagCompound();
                toNBT(way, POINT1, lastTile.getNode().getPoint());
                toNBT(way, POINT2, currentNode.getPoint());
                comp.setTag(SignalBoxUtil.REQUEST_WAY, way);
                GuiSyncNetwork.sendToPosServer(comp, box.getPos());
                lastTile = null;
            }
        }));
        tile.add(new UIClickable(e -> initializePageTileConfig(currentTile.getNode()), 1));
    }

    private void resetSelection(final UIEntity entity) {
        final UIEntity parent = entity.getParent();
        parent.findRecursive(UIClickable.class).forEach(click -> click.setVisible(true));
        parent.findRecursive(UIButton.class).forEach(btn -> btn.setEnabled(true));
        entity.findRecursive(UIButton.class).forEach(btn -> btn.setEnabled(false));
        entity.findRecursive(UIClickable.class).forEach(click -> click.setVisible(false));
    }

    private void initializePageTileConfig(final SignalBoxNode node) {
        if (node.isEmpty())
            return;
        reset();
        final UIEntity list = new UIEntity();
        list.setInheritHeight(true);
        list.setInheritWidth(true);
        final UIBox box = new UIBox(UIBox.VBOX, 1);
        list.add(box);
        lowerEntity.add(new UIBox(UIBox.VBOX, 3));
        lowerEntity.add(list);
        node.forEach(modeSet -> setupModeSettings(list, modeSet.mode, modeSet.rotation, node,
                node.getOption(modeSet).get()));
        lowerEntity.add(GuiElements.createPageSelect(box));
        lowerEntity.add(new UIClickable(e -> {
            reset();
            initializeFieldTemplate(this::tileNormal);
        }, 1));
    }

    @SuppressWarnings({
            "rawtypes", "unchecked", "unused"
    })
    private void sort(final SortOptions sort) {
        final ImmutableSet<Entry<BlockPos, LinkType>> entrySet = box.getPositions().entrySet();
        final ArrayList<Entry<BlockPos, LinkType>> settingslist = new ArrayList(entrySet);
        switch (sort) {
            case DISABLED:
                break;
            case NAME_ASSENDING:
                break;
            case NAME_DESCENIDNG:
                break;
            case TYPE_ASSANDING:
                settingslist.sort((o1, o2) -> Integer.compare(o1.getValue().ordinal(),
                        o2.getValue().ordinal()));
                break;
            case TYPE_DESCENDING:
                settingslist.sort((o1, o2) -> Integer.compare(o1.getValue().ordinal(),
                        o2.getValue().ordinal()));
                break;
            case DISTANCE_ASSANDING:
                break;
            case DISTANCE_DSECENDING:
                break;
            default:
                break;
        }
    }

    private void initializePageSettings(final UIEntity entity) {
        reset();
        lowerEntity.add(new UIBox(UIBox.VBOX, 2));
        lowerEntity.setInheritHeight(true);
        lowerEntity.setInheritWidth(true);
        final UIEntity list = new UIEntity();
        final UIBox uibox = new UIBox(UIBox.VBOX, 2);
        list.setInheritHeight(true);
        list.setInheritWidth(true);
        box.getPositions().forEach((p, t) -> {
            final String name = getSignalInfo(p, t);
            final UIEntity layout = new UIEntity();
            layout.setHeight(20);
            layout.setInheritWidth(true);
            layout.add(new UIBox(UIBox.HBOX, 2));

            final int id = t.ordinal();
            final UIEntity icon = new UIEntity();
            icon.add(new UITexture(UISignalBoxTile.ICON, 0.25 * id, 0.5, 0.25 * id + 0.25, 1));
            icon.setHeight(20);
            icon.setWidth(20);
            icon.add(new UIToolTip(I18n.format("type." + t.name())));
            layout.add(icon);

            layout.add(GuiElements.createButton(name));
            layout.add(GuiElements.createButton("x", 20, e -> {
                final NBTTagCompound resetPos = new NBTTagCompound();
                resetPos.setTag(SignalBoxTileEntity.REMOVE_SIGNAL, NBTUtil.createPosTag(p));
                GuiSyncNetwork.sendToPosServer(resetPos, this.box.getPos());
                list.remove(layout);
            }));
            list.add(layout);
        });
        lowerEntity.add(list);
        lowerEntity.add(GuiElements.createPageSelect(uibox));
        resetSelection(entity);
    }

    private void initializeFieldUsage(final UIEntity entity) {
        reset();
        initializeFieldTemplate(this::tileNormal);
        resetSelection(entity);
    }

    private void initializeFieldEdit(final UIEntity entity) {
        reset();
        final UIMenu menu = new UIMenu();
        initializeFieldTemplate((e, name) -> this.tileEdit(e, menu, name));
        lowerEntity.add(menu);
        resetSelection(entity);
    }

    private void initializeFieldTemplate(final BiConsumer<UIEntity, UISignalBoxTile> consumer) {
        lowerEntity.add(new UIColor(BACKGROUND_COLOR));
        lowerEntity.add(new UIStack());
        lowerEntity.add(new UIScissor());

        final UIEntity plane = new UIEntity();
        plane.setInheritWidth(true);
        plane.setInheritHeight(true);
        lowerEntity.add(new UIScroll(s -> {
            final float newScale = plane.getScaleX() + s * 0.001f;
            if (newScale <= 0)
                return;
            plane.setScaleX(newScale);
            plane.setScaleY(newScale);
            plane.update();
        }));
        lowerEntity.add(new UIDrag((x, y) -> {
            plane.setX(plane.getX() + x);
            plane.setY(plane.getY() + y);
            plane.update();
        }, 2));
        final UIBox vbox = new UIBox(UIBox.VBOX, 0);
        vbox.setPageable(false);
        plane.add(vbox);
        for (int x = 0; x < 50; x++) {
            final UIEntity row = new UIEntity();
            final UIBox hbox = new UIBox(UIBox.HBOX, 0);
            hbox.setPageable(false);
            row.add(hbox);
            row.setHeight(10);
            row.setWidth(10);
            for (int y = 0; y < 50; y++) {
                final UIEntity tile = new UIEntity();
                tile.setHeight(10);
                tile.setWidth(10);
                tile.add(new UIBorder(0xFF7F7F7F, 2));
                final Point name = new Point(y, x);
                final SignalBoxNode node = new SignalBoxNode(name);
                final UISignalBoxTile sbt = new UISignalBoxTile(node);
                tile.add(sbt);
                consumer.accept(tile, sbt);
                row.add(tile);
            }
            plane.add(row);
        }
        lowerEntity.add(plane);

        final UIEntity frame = new UIEntity();
        frame.setInheritHeight(true);
        frame.setInheritWidth(true);
        frame.add(new UIBorder(0xFF000000, 6));
        lowerEntity.add(frame);
        this.entity.read(compound);
    }

    private void initializeBasicUI() {
        final String name = I18n.format("tile.signalbox.name");

        final UILabel titlelabel = new UILabel(name);
        titlelabel.setCenterX(false);

        final UIEntity titel = new UIEntity();
        titel.add(new UIScale(1.2f, 1.2f, 1));
        titel.add(titlelabel);
        titel.setInheritHeight(true);
        titel.setInheritWidth(true);

        final UIEntity header = new UIEntity();
        header.setInheritWidth(true);
        header.setHeight(20);
        header.add(new UIBox(UIBox.HBOX, 4));
        header.add(titel);
        header.add(GuiElements.createSpacerH(80));
        header.add(GuiElements.createButton(I18n.format("btn.settings"),
                this::initializePageSettings));
        header.add(GuiElements.createButton(I18n.format("btn.edit"), this::initializeFieldEdit));
        final UIEntity mainButton = GuiElements.createButton(I18n.format("btn.main"),
                this::initializeFieldUsage);
        header.add(mainButton);
        resetSelection(mainButton);

        final UIEntity middlePart = new UIEntity();
        middlePart.setInheritHeight(true);
        middlePart.setInheritWidth(true);
        middlePart.add(new UIBox(UIBox.VBOX, 4));
        middlePart.add(header);
        middlePart.add(lowerEntity);

        lowerEntity.setInheritHeight(true);
        lowerEntity.setInheritWidth(true);
        initializeFieldTemplate(this::tileNormal);

        this.entity.add(GuiElements.createSpacerH(10));
        this.entity.add(middlePart);
        this.entity.add(GuiElements.createSpacerH(10));
        this.entity.add(new UIBox(UIBox.HBOX, 1));

        this.entity.read(compound);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        GuiSyncNetwork.sendToPosServer(compound, this.box.getPos());
    }

    private void reset() {
        this.entity.write(compound);
        GuiSyncNetwork.sendToPosServer(compound, this.box.getPos());
        lowerEntity.clear();
    }
}
