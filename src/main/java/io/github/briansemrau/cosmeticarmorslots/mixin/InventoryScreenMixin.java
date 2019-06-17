package io.github.briansemrau.cosmeticarmorslots.mixin;

import io.github.briansemrau.cosmeticarmorslots.client.gui.screen.ingame.CosmeticArmorInventoryScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.container.PlayerContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerContainer> implements RecipeBookProvider {

    private static final Identifier COSMETIC_BUTTON_TEX = new Identifier("cosmeticarmorslots", "textures/gui/cosmetic_armor_button.png");

    @Shadow
    private boolean isMouseDown;

    public InventoryScreenMixin(PlayerContainer container_1, PlayerInventory playerInventory_1, Component component_1) {
        super(container_1, playerInventory_1, component_1);
    }

    @Inject(method = "init", at = @At("TAIL"))
    protected void onInit(CallbackInfo ci) {
        if (!this.minecraft.interactionManager.hasCreativeInventory()) {
            this.addButton(new TexturedButtonWidget(this.left + 66, this.height / 2 - 14, 8, 8, 0, 0, 8, COSMETIC_BUTTON_TEX, 8, 16, (buttonWidget) -> {
                this.minecraft.openScreen(new CosmeticArmorInventoryScreen(this.playerInventory.player));
                this.isMouseDown = true;
            }) {
                @Override
                public void renderButton(int int_1, int int_2, float float_1) {
                    super.renderButton(int_1, int_2, float_1);
                    this.setPos(InventoryScreenMixin.this.left + 66, InventoryScreenMixin.this.height / 2 - 14);
                }
            });
        }
    }

}
