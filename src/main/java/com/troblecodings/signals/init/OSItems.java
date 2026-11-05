package com.troblecodings.signals.init;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.linkableapi.Linkingtool;
import com.troblecodings.linkableapi.MultiLinkingTool;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.items.ItemArmorTemplate;
import com.troblecodings.signals.items.Placementtool;
import com.troblecodings.signals.items.SignalBridgeItem;
import com.troblecodings.signals.items.ToolParser;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

public final class OSItems {

    private OSItems() {
    }

    /**
     * Items that sat in a vanilla creative tab through {@code Properties.tab(...)}
     * on 1.18. 1.20.1 removed that, so {@link OSTabs} puts them back via
     * BuildCreativeModeTabContentsEvent.
     */
    public static final Map<Item, ResourceKey<CreativeModeTab>> CREATIVE_TAB_ITEMS =
            new LinkedHashMap<>();

    public static final Linkingtool LINKING_TOOL = assignTab(new Linkingtool(null, (world, pos) -> {
        final BlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        final boolean isRedstoneBlock = block == OSBlocks.REDSTONE_IN
                || block == OSBlocks.REDSTONE_OUT || block == OSBlocks.COMBI_REDSTONE_INPUT;
        return isRedstoneBlock || (block instanceof Signal && ((Signal) block).canBeLinked())
                || block == OSBlocks.SIGNAL_BOX;
    }, _u -> true, (level, pos, tag) -> {
        final BlockState state = level.getBlockState(pos);
        final NBTWrapper wrapper = new NBTWrapper(tag);
        wrapper.putString(pos.toShortString(),
                ForgeRegistries.BLOCKS.getKey(state.getBlock()).getPath());
    }), OSTabs.TAB.getKey());
    public static final MultiLinkingTool MULTI_LINKING_TOOL =
            assignTab(new MultiLinkingTool(null, (world, pos) -> {
                final BlockState state = world.getBlockState(pos);
                final Block block = state.getBlock();
                final boolean isRedstoneBlock = block == OSBlocks.REDSTONE_IN
                        || block == OSBlocks.REDSTONE_OUT || block == OSBlocks.COMBI_REDSTONE_INPUT;
                return isRedstoneBlock
                        || (block instanceof Signal && ((Signal) block).canBeLinked())
                        || block == OSBlocks.SIGNAL_BOX;
            }, _u -> true, (level, pos, tag) -> {
                final BlockState state = level.getBlockState(pos);
                final NBTWrapper wrapper = new NBTWrapper(tag);
                wrapper.putString(pos.toShortString(),
                        ForgeRegistries.BLOCKS.getKey(state.getBlock()).getPath());
            }), OSTabs.TAB.getKey());
    public static final Item CONDUCTOR_TROWEL_GREEN =
            assignTab(new Item(new Properties()), CreativeModeTabs.COMBAT);
    public static final Item CONDUCTOR_TROWEL_RED =
            assignTab(new Item(new Properties()), CreativeModeTabs.COMBAT);
    public static final Item WARNING_FLAG =
            assignTab(new Item(new Properties()), CreativeModeTabs.COMBAT);
    public static final Item K_BOARD =
            assignTab(new Item(new Properties()), CreativeModeTabs.COMBAT);
    public static final Item L_BOARD =
            assignTab(new Item(new Properties()), CreativeModeTabs.COMBAT);
    public static final ItemArmorTemplate REFLECTIVE_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.REFLECTIVE_ARMOR_MATERIAL, ArmorItem.Type.HELMET);
    public static final ItemArmorTemplate REFLECTIVE_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.REFLECTIVE_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE);
    public static final ItemArmorTemplate REFLECTIVE_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.REFLECTIVE_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS);
    public static final ItemArmorTemplate REFLECTIVE_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.REFLECTIVE_ARMOR_MATERIAL, ArmorItem.Type.BOOTS);
    public static final ItemArmorTemplate DISPATCHER_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.DISPATCHER_ARMOR_MATERIAL, ArmorItem.Type.HELMET);
    public static final ItemArmorTemplate DISPATCHER_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.DISPATCHER_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE);
    public static final ItemArmorTemplate DISPATCHER_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.DISPATCHER_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS);
    public static final ItemArmorTemplate DISPATCHER_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.DISPATCHER_ARMOR_MATERIAL, ArmorItem.Type.BOOTS);
    public static final ItemArmorTemplate STATION_MANAGER_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.STATIONMANAGER_ARMOR_MATERIAL, ArmorItem.Type.HELMET);
    public static final ItemArmorTemplate STATION_MANAGER_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.STATIONMANAGER_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE);
    public static final ItemArmorTemplate STATION_MANAGER_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.STATIONMANAGER_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS);
    public static final ItemArmorTemplate STATION_MANAGER_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.STATIONMANAGER_ARMOR_MATERIAL, ArmorItem.Type.BOOTS);
    public static final ItemArmorTemplate TRAIN_DRIVER_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.TRAINDRIVER_ARMOR_MATERIAL, ArmorItem.Type.HELMET);
    public static final ItemArmorTemplate TRAIN_DRIVER_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.TRAINDRIVER_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE);
    public static final ItemArmorTemplate TRAIN_DRIVER_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.TRAINDRIVER_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS);
    public static final ItemArmorTemplate TRAIN_DRIVER_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.TRAINDRIVER_ARMOR_MATERIAL, ArmorItem.Type.BOOTS);
    public static final ItemArmorTemplate CONDUCTOR_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.CONDUCTOR_ARMOR_MATERIAL, ArmorItem.Type.HELMET);
    public static final ItemArmorTemplate CONDUCTOR_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.CONDUCTOR_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE);
    public static final ItemArmorTemplate CONDUCTOR_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.CONDUCTOR_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS);
    public static final ItemArmorTemplate CONDUCTOR_SHOES =
            new ItemArmorTemplate(ItemArmorTemplate.CONDUCTOR_ARMOR_MATERIAL, ArmorItem.Type.BOOTS);
    public static final Item SIGNAL_PLATE =
            assignTab(new Item(new Properties()), CreativeModeTabs.INGREDIENTS);
    public static final Item SIGNAL_SHIELD =
            assignTab(new Item(new Properties()), CreativeModeTabs.INGREDIENTS);
    public static final Item LAMPS =
            assignTab(new Item(new Properties()), CreativeModeTabs.INGREDIENTS);
    public static final Item ELECTRIC_PARTS =
            assignTab(new Item(new Properties()), CreativeModeTabs.INGREDIENTS);
    public static final Item MANIPULATOR =
            assignTab(new Item(new Properties()), OSTabs.TAB.getKey());
    public static final SignalBridgeItem SIGNAL_BRIDGE_ITEM = new SignalBridgeItem();

    public static ArrayList<Item> registeredItems = new ArrayList<>();

    public static ArrayList<Placementtool> placementtools = new ArrayList<>();

    /**
     * Registry names used to live on the item itself; {@code setRegistryName} was
     * removed in 1.19, so they are held here until {@link RegisterEvent} fires.
     */
    public static final Map<Item, ResourceLocation> ITEM_NAMES = new LinkedHashMap<>();

    public static <T extends Item> T assignTab(final T item,
            final ResourceKey<CreativeModeTab> tab) {
        CREATIVE_TAB_ITEMS.put(item, tab);
        return item;
    }

    public static void init() {
        synchronized (LINKING_TOOL) {
            if (!registeredItems.isEmpty())
                return;
            final Field[] fields = OSItems.class.getFields();
            for (final Field field : fields) {
                final int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)
                        && Modifier.isPublic(modifiers)) {
                    final String name = field.getName().toLowerCase().replace("_", "");
                    try {
                        final Object object = field.get(null);
                        if (!(object instanceof Item)) {
                            continue;
                        }
                        final Item item = (Item) object;
                        ITEM_NAMES.put(item, new ResourceLocation(OpenSignalsMain.MODID, name));
                        registeredItems.add(item);
                    } catch (final IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            loadTools();
        }
    }

    private static final Gson GSON = new Gson();

    private static void loadTools() {
        OpenSignalsMain.contentPacks.getFiles("tools").forEach(entry -> {
            final ToolParser tools = GSON.fromJson(entry.getValue(), ToolParser.class);
            tools.getPlacementTools().forEach(placementtool -> {
                final Placementtool tool = new Placementtool();
                final String name = placementtool.toLowerCase().replace("_", "").trim();
                ITEM_NAMES.put(tool, new ResourceLocation(OpenSignalsMain.MODID, name));
                placementtools.add(tool);
                registeredItems.add(tool);
            });
        });
    }

    @SubscribeEvent
    public static void registerItem(final RegisterEvent event) {
        event.register(Registries.ITEM, helper -> {
            OSItems.init();
            registeredItems.forEach(item -> helper.register(ITEM_NAMES.get(item), item));
        });
    }
}