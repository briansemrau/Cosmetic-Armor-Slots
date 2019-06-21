package com.github.briansemrau.cosmeticarmorslots.mixin;

import com.github.briansemrau.cosmeticarmorslots.interfaces.IPlayerEntityMixin;
import com.github.briansemrau.cosmeticarmorslots.interfaces.IPlayerInventoryMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.World;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements IPlayerEntityMixin {

    private static String renderStackCompareString;
    private static int renderStackCompareStringLength;
    private static int renderStackCompareStringHashCode;

    static {
        renderStackCompareString = LivingEntityRenderer.class.getName();//FabricLoader.getInstance().getMappingResolver().mapClassName(FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace(), "net.minecraft.client.render.entity.LivingEntityRenderer");
        renderStackCompareStringLength = renderStackCompareString.length();
        renderStackCompareStringHashCode = renderStackCompareString.hashCode();
    }

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

    @Inject(method = "readCustomDataFromTag", at = @At("TAIL"))
    public void onReadCustomDataFromTag(CompoundTag compoundTag, CallbackInfo ci) {
        ListTag listTag = new ListTag();
        for (int i = 0; i < 4; ++i) {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("Visible", useCosmeticArmorSlot[i]);
            listTag.add(tag);
        }
        compoundTag.put("VisibleCosmeticArmor", listTag);
    }

    @Inject(method = "writeCustomDataToTag", at = @At("TAIL"))
    public void onWriteCustomDataFromTag(CompoundTag compoundTag, CallbackInfo ci) {
        if (compoundTag.containsKey("VisibleCosmeticArmor")) {
            ListTag listTag = compoundTag.getList("VisibleCosmeticArmor", 4);
            for (int i = 0; i < 4; ++i) {
                useCosmeticArmorSlot[i] = listTag.getCompoundTag(i).getBoolean("Visible");
            }
        }
    }

    /**
     * This is a really hacky way to enable compatibility with other mods. We check the call stack to see if we're
     * being called by LivingEntityRenderer to decide whether or not to return cosmetic armor.
     *
     * Any optimization suggestions are welcome from other modders.
     */
    @Environment(EnvType.CLIENT)
    @Inject(method = "getEquippedStack",
            at = @At(value = "FIELD",
                    shift = At.Shift.BEFORE,
                    opcode = Opcodes.GETFIELD,
                    target = "Lnet/minecraft/entity/player/PlayerInventory;armor:Lnet/minecraft/util/DefaultedList;"),
            cancellable = true)
    public void onGetEquippedStack(EquipmentSlot equipmentSlot, CallbackInfoReturnable<ItemStack> cir) {
        // Check if is client player
        if (((PlayerEntity) (Object) this) instanceof AbstractClientPlayerEntity) {
            // Check if cosmetic armor is enabled for this equipment slot
            if (equipmentSlot.getType() == EquipmentSlot.Type.ARMOR) {
                if (this.useCosmeticArmorSlot(equipmentSlot.getEntitySlotId())) {

                    // Due to the lack of compatibility features, we check the call stack to see if getEquippedStack
                    // is being called in the render stack.
                    // This is incredibly hacky, but due to the dynamic nature of FeatureRenderers in PlayerEntityRenderer,
                    // this is the only way to do this. Unless there were an API. But I don't have the time to maintain that.

                    StackTraceElement[] stackTrace = new Exception().getStackTrace();
                    // Checking to the 8th element is arbitrary. *Hopefully* no mod manages to have more than 8 trace
                    // elements to get to LivingEntityRenderer
                    for (int i = 4; i < Math.min(8, stackTrace.length); i++) {
                        String className = stackTrace[i].getClassName();

                        // Very-optimized™ version of String.startsWith(..)
                        if (new String(Arrays.copyOfRange(className.getBytes(), 0, renderStackCompareStringLength)).hashCode() == renderStackCompareStringHashCode) {
                            cir.setReturnValue(((IPlayerInventoryMixin) this.inventory).getCosmeticArmor().get(equipmentSlot.getEntitySlotId()));
                            // implicit return
                        }
                    }
                }
            }
        }
    }

}
