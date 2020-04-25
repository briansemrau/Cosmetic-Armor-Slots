package com.github.briansemrau.cosmeticarmorslots.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
//import org.spongepowered.asm.lib
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

import static org.objectweb.asm.Opcodes.PUTFIELD;

/**
 * This mixin removes the bug where extra slots mixed into PlayerContainer show up on the hotbar.
 */
@Environment(EnvType.CLIENT)
@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeContainer> {

    public CreativeInventoryScreenMixin(CreativeInventoryScreen.CreativeContainer container_1, PlayerInventory playerInventory_1, Text text_1) {
        super(container_1, playerInventory_1, text_1);
    }

    @Inject(method = "setSelectedTab",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/gui/screen/ingame/CreativeInventoryScreen;deleteItemSlot:Lnet/minecraft/container/Slot;",
                    opcode = PUTFIELD,
                    shift = At.Shift.BEFORE))
    private void onSetDeleteItemSlot(CallbackInfo ci) {
        for (int i = 0; i < this.minecraft.player.playerContainer.slots.size(); ++i) {
            if (i > 45) {
                Slot slot = new Slot(this.container.slots.get(i).inventory,i,-10000,-10000);
                this.container.slots.set(i,slot);
                /*try {
                    Field xPosField = slot.getClass().getDeclaredField("xPosition");
                    Field yPosField = slot.getClass().getDeclaredField("yPosition");
                    xPosField.setAccessible(true);
                    yPosField.setAccessible(true);
                    xPosField.set(slot,-10000);
                    yPosField.set(slot,-10000);  // Let's reflect on our decision to modify final variables
                    //slot.xPosition = -10000;
                    //slot.yPosition = -10000;
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }*/

            }
        }
    }

}
