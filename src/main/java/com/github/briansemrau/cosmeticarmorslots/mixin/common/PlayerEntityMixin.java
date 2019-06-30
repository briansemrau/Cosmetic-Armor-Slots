package com.github.briansemrau.cosmeticarmorslots.mixin.common;

import com.github.briansemrau.cosmeticarmorslots.CosmeticArmorSlotsNetwork;
import com.github.briansemrau.cosmeticarmorslots.interfaces.IPlayerEntityMixin;
import com.github.briansemrau.cosmeticarmorslots.interfaces.IPlayerInventoryMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DefaultedList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements IPlayerEntityMixin {

    @Shadow
    @Final
    private PlayerInventory inventory;

    @Unique
    private boolean[] useCosmeticArmorSlot = new boolean[4];

    @Environment(EnvType.SERVER)
    @Unique
    private final DefaultedList<ItemStack> equippedCosmeticArmor = DefaultedList.create(4, ItemStack.EMPTY);
    @Environment(EnvType.SERVER)
    @Unique
    private final boolean[] storedUsingCosmeticArmorSlot = new boolean[4];

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }

    @Override
    public void setUseCosmeticArmorSlot(int entitySlotId, boolean use) {
        useCosmeticArmorSlot[entitySlotId] = use;
    }

    @Override
    public boolean getUseCosmeticArmorSlot(int entitySlotId) {
        return useCosmeticArmorSlot[entitySlotId] || !((IPlayerInventoryMixin) this.inventory).getCosmeticArmor().get(entitySlotId).isEmpty();
    }

    @Override
    public ItemStack getEquippedCosmeticArmor(EquipmentSlot equipmentSlot) {
        switch (equipmentSlot.getType()) {
            case ARMOR:
                return ((IPlayerInventoryMixin) this.inventory).getCosmeticArmor().get(equipmentSlot.getEntitySlotId());
            case HAND:
            default:
                return ItemStack.EMPTY;
        }
    }

    @Override
    public void setEquippedCosmeticArmor(EquipmentSlot equipmentSlot, ItemStack itemStack) {
        if (equipmentSlot.getType() == EquipmentSlot.Type.ARMOR) {
            ((IPlayerInventoryMixin) this.inventory).getCosmeticArmor().set(equipmentSlot.getEntitySlotId(), itemStack);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (!this.world.isClient) {
            for (int i = 0; i < EquipmentSlot.values().length; ++i) {
                EquipmentSlot equipmentSlot = EquipmentSlot.values()[i];
                if (equipmentSlot.getType() != EquipmentSlot.Type.ARMOR) {
                    continue;
                }
                int slotId = equipmentSlot.getEntitySlotId();

                // Get previous cosmetic armor equipped/visible state
                ItemStack storedEquippedStack;
                storedEquippedStack = this.equippedCosmeticArmor.get(slotId);
                boolean storedVisibility = this.storedUsingCosmeticArmorSlot[slotId];

                // Get current equipped/visible state
                ItemStack actualEquippedStack = this.getEquippedCosmeticArmor(equipmentSlot);
                boolean actualVisibility = this.getUseCosmeticArmorSlot(slotId);

                // If either state changed, send packet to update other clients
                if (!ItemStack.areEqualIgnoreDamage(actualEquippedStack, storedEquippedStack) || actualVisibility != storedVisibility) {
                    ((ServerWorld) this.world).method_14178().sendToOtherNearbyPlayers(this, CosmeticArmorSlotsNetwork.createEquipCosmeticArmorPacket(this.getEntityId(), equipmentSlot, actualEquippedStack, actualVisibility));
                    this.equippedCosmeticArmor.set(slotId, actualEquippedStack.isEmpty() ? ItemStack.EMPTY : actualEquippedStack.copy());
                    this.storedUsingCosmeticArmorSlot[slotId] = actualVisibility;
                }
            }
        }
    }

    @Inject(method = "readCustomDataFromTag", at = @At("TAIL"))
    private void onReadCustomDataFromTag(CompoundTag compoundTag, CallbackInfo ci) {
        if (compoundTag.containsKey("ArmorVisibility", 10)) {
            CompoundTag tag = compoundTag.getCompound("ArmorVisibility");
            useCosmeticArmorSlot[0] = tag.getBoolean("feetvisible");
            useCosmeticArmorSlot[1] = tag.getBoolean("legsvisible");
            useCosmeticArmorSlot[2] = tag.getBoolean("chestvisible");
            useCosmeticArmorSlot[3] = tag.getBoolean("headvisible");
        }
    }

    @Inject(method = "writeCustomDataToTag", at = @At("TAIL"))
    private void onWriteCustomDataFromTag(CompoundTag compoundTag, CallbackInfo ci) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("feetvisible", useCosmeticArmorSlot[0]);
        tag.putBoolean("legsvisible", useCosmeticArmorSlot[1]);
        tag.putBoolean("chestvisible", useCosmeticArmorSlot[2]);
        tag.putBoolean("headvisible", useCosmeticArmorSlot[3]);
        compoundTag.put("ArmorVisibility", tag);
    }

}
