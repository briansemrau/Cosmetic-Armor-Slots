package io.github.briansemrau.cosmeticarmorslots.mixin;

import io.github.briansemrau.cosmeticarmorslots.interfaces.IPlayerContainerMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.container.ContainerType;
import net.minecraft.container.CraftingContainer;
import net.minecraft.container.PlayerContainer;
import net.minecraft.container.Slot;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerContainer.class)
public abstract class PlayerContainerMixin extends CraftingContainer<CraftingInventory> implements IPlayerContainerMixin {

    @Shadow
    @Final
    private static String[] EMPTY_ARMOR_SLOT_IDS;

    @Shadow
    @Final
    private static EquipmentSlot[] EQUIPMENT_SLOT_ORDER;

    // prefixed to avoid overwriting
//    private static final String[] _EMPTY_ARMOR_SLOT_IDS = new String[]{"cosmeticarmorslots:textures/item/empty_armor_slot_boots", "cosmeticarmorslots:textures/item/empty_armor_slot_leggings", "cosmeticarmorslots:textures/item/empty_armor_slot_chestplate", "cosmeticarmorslots:textures/item/empty_armor_slot_helmet"};
//    private static final EquipmentSlot[] _EQUIPMENT_SLOT_ORDER = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public PlayerContainerMixin(ContainerType<?> containerType_1, int int_1) {
        super(containerType_1, int_1);
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onConstructed(PlayerInventory playerInventory, boolean boolean_1, PlayerEntity playerEntity, CallbackInfo ci) {
        for (int i = 0; i < 4; ++i) {
            final EquipmentSlot equipmentSlot_1 = EQUIPMENT_SLOT_ORDER[i];
            Slot slot = new Slot(playerInventory, 44 - i, -10000, -10000) {
                public int getMaxStackAmount() {
                    return 1;
                }

                public boolean canInsert(ItemStack itemStack_1) {
                    return equipmentSlot_1 == MobEntity.getPreferredEquipmentSlot(itemStack_1);
                }

                public boolean canTakeItems(PlayerEntity playerEntity_1) {
                    ItemStack itemStack_1 = this.getStack();
                    return !itemStack_1.isEmpty() && !playerEntity_1.isCreative() && EnchantmentHelper.hasBindingCurse(itemStack_1) ? false : super.canTakeItems(playerEntity_1);
                }

                @Environment(EnvType.CLIENT)
                public String getBackgroundSprite() {
                    return EMPTY_ARMOR_SLOT_IDS[equipmentSlot_1.getEntitySlotId()];
                }
            };
            this.addSlot(slot);
        }
    }

}
