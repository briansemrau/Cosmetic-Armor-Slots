package com.github.briansemrau.cosmeticarmorslots.mixin;

import com.github.briansemrau.cosmeticarmorslots.interfaces.IPlayerEntityMixin;
import com.github.briansemrau.cosmeticarmorslots.interfaces.IPlayerInventoryMixin;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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

    @Unique
    private boolean[] useCosmeticArmorSlot = new boolean[4];

    @Shadow
    @Final
    private PlayerInventory inventory;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }

    @Override
    public void setUseCosmeticArmorSlot(int entitySlotId, boolean use) {
        useCosmeticArmorSlot[entitySlotId] = use;
    }

    @Override
    public boolean useCosmeticArmorSlot(int entitySlotId) {
        return useCosmeticArmorSlot[entitySlotId] || !((IPlayerInventoryMixin) this.inventory).getCosmeticArmor().get(entitySlotId).isEmpty();
    }

    @Inject(method = "readCustomDataFromTag", at=@At("TAIL"))
    public void onReadCustomDataFromTag(CompoundTag compoundTag, CallbackInfo ci) {
        ListTag listTag = new ListTag();
        for (int i = 0; i < 4; ++i) {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("Visible", useCosmeticArmorSlot[i]);
            listTag.add(tag);
        }
        compoundTag.put("VisibleCosmeticArmor", listTag);
    }

    @Inject(method = "writeCustomDataToTag", at=@At("TAIL"))
    public void onWriteCustomDataFromTag(CompoundTag compoundTag, CallbackInfo ci) {
        if (compoundTag.containsKey("VisibleCosmeticArmor")) {
            ListTag listTag = compoundTag.getList("VisibleCosmeticArmor", 4);
            for (int i = 0; i < 4; ++i) {
                useCosmeticArmorSlot[i] = listTag.getCompoundTag(i).getBoolean("Visible");
            }
        }
    }

}
