package com.troblecodings.signals.items;

import com.troblecodings.signals.OpenSignalsMain;

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
    
    public static final ArmorMaterial REFLECTIVE_ARMOR_MATERIAL = EnumHelper
            .addArmorMaterial("reflective", OpenSignalsMain.MODID + ":reflective", 1000, new int[] {
                    1, 1, 1, 1
            }, 30, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F); //conductorArmorMaterial

    public static final ArmorMaterial DISPATCHER_ARMOR_MATERIAL = EnumHelper
            .addArmorMaterial("dispatcher", OpenSignalsMain.MODID + ":dispatcher", 1000, new int[] {
                    1, 1, 1, 1
            }, 30, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F);
    
    public static final ArmorMaterial STATIONMANAGER_ARMOR_MATERIAL = EnumHelper
            .addArmorMaterial("station_manager", OpenSignalsMain.MODID + ":station_manager", 1000, new int[] {
                    1, 1, 1, 1
            }, 30, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F);
    
    public static final ArmorMaterial TRAINDRIVER_ARMOR_MATERIAL = EnumHelper
            .addArmorMaterial("train_driver", OpenSignalsMain.MODID + ":train_driver", 1000, new int[] {
                    1, 1, 1, 1
            }, 30, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F);
    
    public static final ArmorMaterial CONDUCTOR_ARMOR_MATERIAL = EnumHelper
            .addArmorMaterial("conductor", OpenSignalsMain.MODID + ":conductor", 1000, new int[] {
                    1, 1, 1, 1
            }, 30, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F);
}
