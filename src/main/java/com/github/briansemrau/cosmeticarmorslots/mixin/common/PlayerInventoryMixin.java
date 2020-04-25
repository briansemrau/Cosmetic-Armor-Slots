package com.github.briansemrau.cosmeticarmorslots.mixin.common;

import com.github.briansemrau.cosmeticarmorslots.interfaces.IPlayerInventoryMixin;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Nameable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin implements Inventory, Nameable, IPlayerInventoryMixin {

    // Shadowed fields

    @Shadow
    @Final
    @Mutable
    private List<DefaultedList<ItemStack>> combinedInventory;

    @Shadow
    @Final
    public DefaultedList<ItemStack> main;
    @Shadow
    @Final
    public DefaultedList<ItemStack> armor;
    @Shadow
    @Final
    public DefaultedList<ItemStack> offHand;

    // Mixin fields

    private DefaultedList<ItemStack> cosmeticArmor;

    // Duck-typing functions

    @Override
    public DefaultedList<ItemStack> getCosmeticArmor() {
        return cosmeticArmor;
    }

    // Injections

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onConstructed(PlayerEntity playerEntity, CallbackInfo ci) {
        cosmeticArmor = DefaultedList.ofSize(4, ItemStack.EMPTY);

        // Change combinedInventory to a mutable type
        combinedInventory = new ArrayList<>(combinedInventory);
        combinedInventory.add(cosmeticArmor);
    }

    @Inject(method = "serialize", at = @At("TAIL"))
    public void onSerialize(ListTag listTag_1, CallbackInfoReturnable<ListTag> cir) {
        // Item slots 0-35, 100-103, 150 claimed by default inventory
        // Cosmetic armor using item slots adjacent to regular armor slots

        for (int i = 0; i < this.cosmeticArmor.size(); i++) {
            if (!this.cosmeticArmor.get(i).isEmpty()) {
                CompoundTag ct = new CompoundTag();
                ct.putByte("Slot", (byte) (i + 104));
                this.cosmeticArmor.get(i).toTag(ct);
                listTag_1.add(ct);
            }
        }

        // If adding an API, additional slots should probably be configured for compatibility or serialization should
        // be changed to use some kind of mod identifier to designate slots
    }

    @Inject(method = "deserialize", at = @At("TAIL"))
    public void onDeserialize(ListTag listTag_1, CallbackInfo ci) {
        this.cosmeticArmor.clear();
        for (int i = 0; i < listTag_1.size(); ++i) {
            CompoundTag ct = listTag_1.getCompound(i);
            int slot = ct.getByte("Slot") & 255;
            ItemStack itemStack = ItemStack.fromTag(ct);
            if (!itemStack.isEmpty()) {
                if (slot >= 104 && slot < this.cosmeticArmor.size() + 104) {
                    this.cosmeticArmor.set(slot - 104, itemStack);
                }
            }
        }
    }

    @Inject(method = "getInvSize", at = @At("HEAD"), cancellable = true)
    public void onGetInvSize(CallbackInfoReturnable<Integer> cir) {
        int size = 0;
        for (DefaultedList list : combinedInventory) {
            size += list.size();
        }
        cir.setReturnValue(size);
    }

    @Inject(method = "isInvEmpty", at = @At("HEAD"), cancellable = true)
    public void onIsInvEmpty(CallbackInfoReturnable<Boolean> cir) {
        for (ItemStack itemStack : this.cosmeticArmor) {
            if (!itemStack.isEmpty()) {
                cir.setReturnValue(false);
            }
        }
    }

    // Add if making API
    // public void damageArmor(float float_1) {...

}
