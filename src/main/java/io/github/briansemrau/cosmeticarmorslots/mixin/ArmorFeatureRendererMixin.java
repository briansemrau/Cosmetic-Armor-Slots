package io.github.briansemrau.cosmeticarmorslots.mixin;

import io.github.briansemrau.cosmeticarmorslots.interfaces.IPlayerEntityMixin;
import io.github.briansemrau.cosmeticarmorslots.interfaces.IPlayerInventoryMixin;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {

    // Constructor required to compile mixin
    public ArmorFeatureRendererMixin(FeatureRendererContext<T, M> featureRendererContext_1) {
        super(featureRendererContext_1);
    }

    @Redirect(method = "renderArmor",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;getEquippedStack(Lnet/minecraft/entity/EquipmentSlot;)Lnet/minecraft/item/ItemStack;"))
    public ItemStack getEquippedStackRedirect(LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
        if (livingEntity instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity) livingEntity;
            if (((IPlayerEntityMixin) playerEntity).useCosmeticArmorSlot(equipmentSlot.getEntitySlotId())) {
                return ((IPlayerInventoryMixin) playerEntity.inventory).getCosmeticArmor().get(equipmentSlot.getEntitySlotId());
            }
        }
        return livingEntity.getEquippedStack(equipmentSlot);
    }

}
