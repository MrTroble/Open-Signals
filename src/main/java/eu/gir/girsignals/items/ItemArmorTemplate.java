package eu.gir.girsignals.items;

import eu.gir.girsignals.GirsignalsMain;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraftforge.common.util.EnumHelper;

public class ItemArmorTemplate extends ItemArmor{

    public ItemArmorTemplate(final ArmorMaterial materialIn, final int renderIndexIn,
            final EntityEquipmentSlot equipmentSlotIn) {
        super(materialIn, renderIndexIn, equipmentSlotIn);
        setCreativeTab(CreativeTabs.COMBAT);
    }
    
    public static final ArmorMaterial reflectiveArmorMaterial = EnumHelper
            .addArmorMaterial("reflective", GirsignalsMain.MODID + ":reflective", 1000, new int[] {
                    1, 1, 1, 1
            }, 30, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F);

    public static final ArmorMaterial dispatcherArmorMaterial = EnumHelper
            .addArmorMaterial("dispatcher", GirsignalsMain.MODID + ":dispatcher", 1000, new int[] {
                    1, 1, 1, 1
            }, 30, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F);
    
    public static final ArmorMaterial stationManagerArmorMaterial = EnumHelper
            .addArmorMaterial("station_manager", GirsignalsMain.MODID + ":station_manager", 1000, new int[] {
                    1, 1, 1, 1
            }, 30, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F);
    
    public static final ArmorMaterial trainDriverArmorMaterial = EnumHelper
            .addArmorMaterial("train_driver", GirsignalsMain.MODID + ":train_driver", 1000, new int[] {
                    1, 1, 1, 1
            }, 30, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F);
}
