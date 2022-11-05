package eu.gir.girsignals.init;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.items.ItemArmorTemplate;
import eu.gir.girsignals.items.ItemConductorTrowel;
import eu.gir.girsignals.items.Placementtool;
import eu.gir.linkableapi.Linkingtool;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public final class GIRItems {

    private GIRItems() {
    }

    public static final Linkingtool LINKING_TOOL = new Linkingtool(GIRTabs.TAB, (world, pos) -> {
        final IBlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        return block == GIRBlocks.REDSTONE_IN || block == GIRBlocks.REDSTONE_OUT
                || (block instanceof Signal && ((Signal) block).canBeLinked());
    });
    public static final Placementtool PLACEMENT_TOOL = new Placementtool();
    public static final Placementtool SIGN_PLACEMENT_TOOL = new Placementtool();
    public static final ItemConductorTrowel CONDUCTOR_TROWEL_GREEN = new ItemConductorTrowel();
    public static final ItemConductorTrowel CONDUCTOR_TROWEL_RED = new ItemConductorTrowel();
    public static final ItemConductorTrowel WARNING_FLAG = new ItemConductorTrowel();
    public static final ItemConductorTrowel K_BOARD = new ItemConductorTrowel();
    public static final ItemConductorTrowel L_BOARD = new ItemConductorTrowel();
    public static final ItemArmorTemplate REFLECTIVE_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.reflectiveArmorMaterial, 1, EntityEquipmentSlot.HEAD);
    public static final ItemArmorTemplate REFLECTIVE_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.reflectiveArmorMaterial, 1, EntityEquipmentSlot.CHEST);
    public static final ItemArmorTemplate REFLECTIVE_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.reflectiveArmorMaterial, 1, EntityEquipmentSlot.LEGS);
    public static final ItemArmorTemplate REFLECTIVE_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.reflectiveArmorMaterial, 1, EntityEquipmentSlot.FEET);
    public static final ItemArmorTemplate DISPATCHER_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.dispatcherArmorMaterial, 1, EntityEquipmentSlot.HEAD);
    public static final ItemArmorTemplate DISPATCHER_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.dispatcherArmorMaterial, 1, EntityEquipmentSlot.CHEST);
    public static final ItemArmorTemplate DISPATCHER_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.dispatcherArmorMaterial, 1, EntityEquipmentSlot.LEGS);
    public static final ItemArmorTemplate DISPATCHER_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.dispatcherArmorMaterial, 1, EntityEquipmentSlot.FEET);
    public static final ItemArmorTemplate STATION_MANAGER_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.stationManagerArmorMaterial, 1, EntityEquipmentSlot.HEAD);
    public static final ItemArmorTemplate STATION_MANAGER_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.stationManagerArmorMaterial, 1, EntityEquipmentSlot.CHEST);
    public static final ItemArmorTemplate STATION_MANAGER_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.stationManagerArmorMaterial, 1, EntityEquipmentSlot.LEGS);
    public static final ItemArmorTemplate STATION_MANAGER_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.stationManagerArmorMaterial, 1, EntityEquipmentSlot.FEET);
    public static final ItemArmorTemplate TRAIN_DRIVER_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.trainDriverArmorMaterial, 1, EntityEquipmentSlot.HEAD);
    public static final ItemArmorTemplate TRAIN_DRIVER_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.trainDriverArmorMaterial, 1, EntityEquipmentSlot.CHEST);
    public static final ItemArmorTemplate TRAIN_DRIVER_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.trainDriverArmorMaterial, 1, EntityEquipmentSlot.LEGS);
    public static final ItemArmorTemplate TRAIN_DRIVER_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.trainDriverArmorMaterial, 1, EntityEquipmentSlot.FEET);
    public static final ItemArmorTemplate CONDUCTOR_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.conductorArmorMaterial, 1, EntityEquipmentSlot.HEAD);
    public static final ItemArmorTemplate CONDUCTOR_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.conductorArmorMaterial, 1, EntityEquipmentSlot.CHEST);
    public static final ItemArmorTemplate CONDUCTOR_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.conductorArmorMaterial, 1, EntityEquipmentSlot.LEGS);
    public static final ItemArmorTemplate CONDUCTOR_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.conductorArmorMaterial, 1, EntityEquipmentSlot.FEET);

    public static ArrayList<Item> registeredItems = new ArrayList<>();

    public static void init() {
        final Field[] fields = GIRItems.class.getFields();
        for (final Field field : fields) {
            final int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)
                    && Modifier.isPublic(modifiers)) {
                final String name = field.getName().toLowerCase().replace("_", "");
                try {
                    final Item item = (Item) field.get(null);
                    item.setRegistryName(new ResourceLocation(GirsignalsMain.MODID, name));
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
