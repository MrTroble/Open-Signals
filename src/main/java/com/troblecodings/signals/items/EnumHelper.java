package com.troblecodings.signals.items;

import com.troblecodings.signals.OpenSignalsMain;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;

public final class EnumHelper {

    private EnumHelper() {
    }

    private static class ImplArmorMat implements IArmorMaterial {

        private final int[] defense;
        private final int durability;
        private final int enchant;
        private final SoundEvent event;
        private final Ingredient ingrediant;
        private final String name;
        private final float toughness;

        public ImplArmorMat(final int[] defense, final int durability, final int enchant,
                final SoundEvent event, final Ingredient ingrediant, final String name,
                final float toughness) {
            super();
            this.defense = defense;
            this.durability = durability;
            this.enchant = enchant;
            this.event = event;
            this.ingrediant = ingrediant;
            this.name = name;
            this.toughness = toughness;
        }

        @Override
        public int getDurabilityForSlot(final EquipmentSlotType slot) {
            return durability;
        }

        @Override
        public int getDefenseForSlot(final EquipmentSlotType slot) {
            return this.defense[slot.getIndex()];
        }

        @Override
        public int getEnchantmentValue() {
            return this.enchant;
        }

        @Override
        public SoundEvent getEquipSound() {
            return this.event;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return this.ingrediant;
        }

        @Override
        public String getName() {
            return OpenSignalsMain.MODID + ":" + this.name;
        }

        @Override
        public float getToughness() {
            return this.toughness;
        }
    }

    public static IArmorMaterial addArmorMaterial(final String name, final int durability,
            final int[] defense, final int enchant, final SoundEvent event, final float toughness) {
        return new ImplArmorMat(defense, durability, enchant, event, Ingredient.EMPTY, name,
                toughness);
    }
}