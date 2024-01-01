package com.troblecodings.signals.init;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.linkableapi.Linkingtool;
import com.troblecodings.linkableapi.MultiLinkingTool;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.GhostBlock;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.items.ItemArmorTemplate;
import com.troblecodings.signals.items.Placementtool;
import com.troblecodings.signals.items.ToolParser;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

public final class OSItems {

    private OSItems() {
    }

    public static final Linkingtool LINKING_TOOL = new Linkingtool(OSTabs.TAB, (world, pos) -> {
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
    });
    public static final MultiLinkingTool MULTI_LINKING_TOOL = new MultiLinkingTool(OSTabs.TAB,
            (world, pos) -> {
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
            });
    public static final Item CONDUCTOR_TROWEL_GREEN = new Item(new Properties());
    public static final Item CONDUCTOR_TROWEL_RED = new Item(new Properties());
    public static final Item WARNING_FLAG = new Item(new Properties());
    public static final Item K_BOARD = new Item(new Properties());
    public static final Item L_BOARD = new Item(new Properties());
    public static final ItemArmorTemplate REFLECTIVE_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.REFLECTIVE_ARMOR_MATERIAL, Type.HELMET);
    public static final ItemArmorTemplate REFLECTIVE_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.REFLECTIVE_ARMOR_MATERIAL, Type.CHESTPLATE);
    public static final ItemArmorTemplate REFLECTIVE_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.REFLECTIVE_ARMOR_MATERIAL, Type.LEGGINGS);
    public static final ItemArmorTemplate REFLECTIVE_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.REFLECTIVE_ARMOR_MATERIAL, Type.BOOTS);
    public static final ItemArmorTemplate DISPATCHER_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.DISPATCHER_ARMOR_MATERIAL, Type.HELMET);
    public static final ItemArmorTemplate DISPATCHER_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.DISPATCHER_ARMOR_MATERIAL, Type.CHESTPLATE);
    public static final ItemArmorTemplate DISPATCHER_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.DISPATCHER_ARMOR_MATERIAL, Type.LEGGINGS);
    public static final ItemArmorTemplate DISPATCHER_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.DISPATCHER_ARMOR_MATERIAL, Type.BOOTS);
    public static final ItemArmorTemplate STATION_MANAGER_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.STATIONMANAGER_ARMOR_MATERIAL, Type.HELMET);
    public static final ItemArmorTemplate STATION_MANAGER_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.STATIONMANAGER_ARMOR_MATERIAL, Type.CHESTPLATE);
    public static final ItemArmorTemplate STATION_MANAGER_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.STATIONMANAGER_ARMOR_MATERIAL, Type.LEGGINGS);
    public static final ItemArmorTemplate STATION_MANAGER_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.STATIONMANAGER_ARMOR_MATERIAL, Type.BOOTS);
    public static final ItemArmorTemplate TRAIN_DRIVER_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.TRAINDRIVER_ARMOR_MATERIAL, Type.HELMET);
    public static final ItemArmorTemplate TRAIN_DRIVER_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.TRAINDRIVER_ARMOR_MATERIAL, Type.CHESTPLATE);
    public static final ItemArmorTemplate TRAIN_DRIVER_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.TRAINDRIVER_ARMOR_MATERIAL, Type.LEGGINGS);
    public static final ItemArmorTemplate TRAIN_DRIVER_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.TRAINDRIVER_ARMOR_MATERIAL, Type.BOOTS);
    public static final ItemArmorTemplate CONDUCTOR_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.CONDUCTOR_ARMOR_MATERIAL, Type.HELMET);
    public static final ItemArmorTemplate CONDUCTOR_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.CONDUCTOR_ARMOR_MATERIAL, Type.CHESTPLATE);
    public static final ItemArmorTemplate CONDUCTOR_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.CONDUCTOR_ARMOR_MATERIAL, Type.LEGGINGS);
    public static final ItemArmorTemplate CONDUCTOR_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.CONDUCTOR_ARMOR_MATERIAL, Type.BOOTS);
    public static final Item SIGNAL_PLATE = new Item(new Properties());
    public static final Item SIGNAL_SHIELD = new Item(new Properties());
    public static final Item LAMPS = new Item(new Properties());
    public static final Item ELECTRIC_PARTS = new Item(new Properties());
    public static final Item MANIPULATOR = new Item(new Properties());

    public static ArrayList<Item> registeredItems = new ArrayList<>();

    public static ArrayList<Placementtool> placementtools = new ArrayList<>();

    private static final Gson GSON = new Gson();

    @SubscribeEvent
    public static void registerItem(final RegisterEvent event) {
        event.register(Registries.ITEM, holder -> {
            final Field[] fields = OSItems.class.getFields();
            for (final Field field : fields) {
                final int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)
                        && Modifier.isPublic(modifiers)) {
                    final String name = field.getName().toLowerCase().replace("_", "");
                    try {
                        final Item item = (Item) field.get(null);
                        holder.register(new ResourceLocation(OpenSignalsMain.MODID, name), item);
                        registeredItems.add(item);
                    } catch (final IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            OpenSignalsMain.contentPacks.getFiles("tools").forEach(entry -> {
                final ToolParser tools = GSON.fromJson(entry.getValue(), ToolParser.class);
                tools.getPlacementTools().forEach(placementtool -> {
                    final Placementtool tool = new Placementtool();
                    final String name = placementtool.toLowerCase().replace("_", "").trim();
                    holder.register(new ResourceLocation(OpenSignalsMain.MODID, name), tool);
                    placementtools.add(tool);
                    registeredItems.add(tool);
                });
            });
        });
    }

    @SubscribeEvent
    public static void tab(final CreativeModeTabEvent.BuildContents event) {
        registeredItems.forEach(item -> {
            if (event.getTab().equals(OSTabs.TAB))
                event.accept(() -> item);
        });
        OSBlocks.BLOCKS_TO_REGISTER.stream()
                .filter(block -> !(block instanceof Signal && block instanceof GhostBlock))
                .forEach(block -> {
                    if (event.getTab().equals(OSTabs.TAB))
                        event.accept(() -> new BlockItem(block, new Properties()));
                });
    }
}