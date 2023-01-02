package com.troblecodings.signals.items;

import com.troblecodings.signals.OpenSignalsMain;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public class EnumHelper {

    private static class ImplArmorMat implements ArmorMaterial {

        int[] defense;
        int durability;
        int enchant;
        SoundEvent event;
        Ingredient ingrediant;
        String name;
        float toughness;
        float knock;

        public ImplArmorMat(final int[] defense, final int durability, final int enchant,
                final SoundEvent event, final Ingredient ingrediant, final String name,
                final float toughness, final float knock) {
            super();
            this.defense = defense;
            this.durability = durability;
            this.enchant = enchant;
            this.event = event;
            this.ingrediant = ingrediant;
            this.name = name;
            this.toughness = toughness;
            this.knock = knock;
        }

        @Override
        public int getDurabilityForSlot(final EquipmentSlot slot) {
            return durability;
        }

        @Override
        public int getDefenseForSlot(final EquipmentSlot slot) {
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

        @Override
        public float getKnockbackResistance() {
            return this.knock;
        }

    }

    public static ArmorMaterial addArmorMaterial(final String name, final int durability,
            final int[] defense, final int enchant, final SoundEvent event, final float toughness) {
        return new ImplArmorMat(defense, durability, enchant, event, Ingredient.EMPTY, name,
                toughness, toughness);
    }
}
