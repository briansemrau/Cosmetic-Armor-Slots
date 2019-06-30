package com.github.briansemrau.cosmeticarmorslots.interfaces;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public interface IPlayerEntityMixin {

    boolean getUseCosmeticArmorSlot(int entitySlotId);

    void setUseCosmeticArmorSlot(int entitySlotId, boolean use);

    ItemStack getEquippedCosmeticArmor(EquipmentSlot equipmentSlot);

    void setEquippedCosmeticArmor(EquipmentSlot equipmentSlot, ItemStack itemStack);

}
