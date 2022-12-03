package com.troblecodings.signals.init;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import com.troblecodings.linkableapi.Linkingtool;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.items.ItemArmorTemplate;
import com.troblecodings.signals.items.Placementtool;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public final class OSItems {

    private OSItems() {
    }

    public static final Linkingtool LINKING_TOOL = new Linkingtool(OSTabs.TAB, (world, pos) -> {
        final IBlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        return block == OSBlocks.REDSTONE_IN || block == OSBlocks.REDSTONE_OUT
                || (block instanceof Signal && ((Signal) block).canBeLinked());
    });
    public static final Placementtool PLACEMENT_TOOL = new Placementtool();
    public static final Placementtool SIGN_PLACEMENT_TOOL = new Placementtool();
    public static final Item CONDUCTOR_TROWEL_GREEN = new Item().setCreativeTab(CreativeTabs.COMBAT);
    public static final Item CONDUCTOR_TROWEL_RED = new Item().setCreativeTab(CreativeTabs.COMBAT);
    public static final Item WARNING_FLAG = new Item().setCreativeTab(CreativeTabs.COMBAT);
    public static final Item K_BOARD = new Item().setCreativeTab(CreativeTabs.COMBAT);
    public static final Item L_BOARD = new Item().setCreativeTab(CreativeTabs.COMBAT);
    public static final ItemArmorTemplate REFLECTIVE_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.REFLECTIVE_ARMOR_MATERIAL, 1, EntityEquipmentSlot.HEAD);
    public static final ItemArmorTemplate REFLECTIVE_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.REFLECTIVE_ARMOR_MATERIAL, 1, EntityEquipmentSlot.CHEST);
    public static final ItemArmorTemplate REFLECTIVE_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.REFLECTIVE_ARMOR_MATERIAL, 1, EntityEquipmentSlot.LEGS);
    public static final ItemArmorTemplate REFLECTIVE_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.REFLECTIVE_ARMOR_MATERIAL, 1, EntityEquipmentSlot.FEET);
    public static final ItemArmorTemplate DISPATCHER_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.DISPATCHER_ARMOR_MATERIAL, 1, EntityEquipmentSlot.HEAD);
    public static final ItemArmorTemplate DISPATCHER_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.DISPATCHER_ARMOR_MATERIAL, 1, EntityEquipmentSlot.CHEST);
    public static final ItemArmorTemplate DISPATCHER_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.DISPATCHER_ARMOR_MATERIAL, 1, EntityEquipmentSlot.LEGS);
    public static final ItemArmorTemplate DISPATCHER_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.DISPATCHER_ARMOR_MATERIAL, 1, EntityEquipmentSlot.FEET);
    public static final ItemArmorTemplate STATION_MANAGER_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.STATIONMANAGER_ARMOR_MATERIAL, 1, EntityEquipmentSlot.HEAD);
    public static final ItemArmorTemplate STATION_MANAGER_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.STATIONMANAGER_ARMOR_MATERIAL, 1, EntityEquipmentSlot.CHEST);
    public static final ItemArmorTemplate STATION_MANAGER_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.STATIONMANAGER_ARMOR_MATERIAL, 1, EntityEquipmentSlot.LEGS);
    public static final ItemArmorTemplate STATION_MANAGER_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.STATIONMANAGER_ARMOR_MATERIAL, 1, EntityEquipmentSlot.FEET);
    public static final ItemArmorTemplate TRAIN_DRIVER_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.TRAINDRIVER_ARMOR_MATERIAL, 1, EntityEquipmentSlot.HEAD);
    public static final ItemArmorTemplate TRAIN_DRIVER_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.TRAINDRIVER_ARMOR_MATERIAL, 1, EntityEquipmentSlot.CHEST);
    public static final ItemArmorTemplate TRAIN_DRIVER_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.TRAINDRIVER_ARMOR_MATERIAL, 1, EntityEquipmentSlot.LEGS);
    public static final ItemArmorTemplate TRAIN_DRIVER_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.TRAINDRIVER_ARMOR_MATERIAL, 1, EntityEquipmentSlot.FEET);
    public static final ItemArmorTemplate CONDUCTOR_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.CONDUCTOR_ARMOR_MATERIAL, 1, EntityEquipmentSlot.HEAD);
    public static final ItemArmorTemplate CONDUCTOR_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.CONDUCTOR_ARMOR_MATERIAL, 1, EntityEquipmentSlot.CHEST);
    public static final ItemArmorTemplate CONDUCTOR_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.CONDUCTOR_ARMOR_MATERIAL, 1, EntityEquipmentSlot.LEGS);
    public static final ItemArmorTemplate CONDUCTOR_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.CONDUCTOR_ARMOR_MATERIAL, 1, EntityEquipmentSlot.FEET);

    public static ArrayList<Item> registeredItems = new ArrayList<>();

    public static void init() {
        final Field[] fields = OSItems.class.getFields();
        for (final Field field : fields) {
            final int modifiers = field.getModifiers(); 
            if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)
                    && Modifier.isPublic(modifiers)) {
                final String name = field.getName().toLowerCase().replace("_", "");
                try {
                    final Item item = (Item) field.get(null);
                    item.setRegistryName(new ResourceLocation(OpenSignalsMain.MODID, name));
                    item.setUnlocalizedName(name);
                    registeredItems.add(item);
                } catch (final IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SubscribeEvent
    public static void registerItem(final RegistryEvent.Register<Item> event) {
        final IForgeRegistry<Item> registry = event.getRegistry();
        registeredItems.forEach(registry::register);
    }

}
