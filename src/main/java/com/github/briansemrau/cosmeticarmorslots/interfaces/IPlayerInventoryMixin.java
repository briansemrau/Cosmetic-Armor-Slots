package com.github.briansemrau.cosmeticarmorslots.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;

public interface IPlayerInventoryMixin {

    DefaultedList<ItemStack> getCosmeticArmor();

}
