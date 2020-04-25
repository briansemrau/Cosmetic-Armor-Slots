package com.github.briansemrau.cosmeticarmorslots.client.gui.screen.ingame;

import com.github.briansemrau.cosmeticarmorslots.CosmeticArmorSlotsNetwork;
import com.github.briansemrau.cosmeticarmorslots.interfaces.IPlayerEntityMixin;
import com.github.briansemrau.cosmeticarmorslots.interfaces.IPlayerInventoryMixin;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

@Environment(EnvType.CLIENT)
public class CosmeticArmorInventoryScreen extends ContainerScreen<CosmeticArmorInventoryScreen.CosmeticArmorSlotsContainer> {

    private static final Identifier TEXTURE = new Identifier("cosmeticarmorslots", "textures/gui/container/cosmeticarmorslots.png");
    private static final Identifier COSMETIC_BUTTON_TEX = new Identifier("cosmeticarmorslots", "textures/gui/cosmetic_armor_button.png");
    private static final Identifier VISIBILITY_BUTTON_TEX = new Identifier("cosmeticarmorslots", "textures/gui/visibility.png");

    private float mouseX;
    private float mouseY;

    public CosmeticArmorInventoryScreen(PlayerEntity playerEntity) {
        super(new CosmeticArmorSlotsContainer(playerEntity.inventory), playerEntity.inventory, new TranslatableText("container.cosmeticarmorslots"));
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(new TexturedButtonWidget(this.x + 66, this.height / 2 - 14, 8, 8, 0, 0, 8, COSMETIC_BUTTON_TEX, 8, 16, (buttonWidget) -> {
            this.minecraft.openScreen(new InventoryScreen(this.playerInventory.player));
        }));
        for (int i = 0; i < 4; ++i) {
            int slotIndex = 3 - i;
            IPlayerEntityMixin player = ((IPlayerEntityMixin) this.playerInventory.player);
            this.addButton(new ButtonWidget(this.x + 94, this.y + 12 + i * 18, 8, 8, "", (buttonWidget) -> {
                player.setUseCosmeticArmorSlot(slotIndex, !player.getUseCosmeticArmorSlot(slotIndex));
                this.minecraft.getNetworkHandler().getConnection().send(CosmeticArmorSlotsNetwork.createCosmeticSlotVisibilityUpdatePacket(EquipmentSlot.fromTypeIndex(EquipmentSlot.Type.ARMOR, slotIndex), player.getUseCosmeticArmorSlot(slotIndex)));
            }) {
                @Override
                public void render(int int_1, int int_2, float float_1) {
                    this.visible = ((IPlayerInventoryMixin)playerInventory).getCosmeticArmor().get(slotIndex).isEmpty();
                    super.render(int_1, int_2, float_1);
                }

                @Override
                public void renderButton(int int_1, int int_2, float float_1) {
                    MinecraftClient.getInstance().getTextureManager().bindTexture(VISIBILITY_BUTTON_TEX);
                    GlStateManager.disableDepthTest();
                    GlStateManager.enableBlend();
                    int u = 0;
                    int v = 0;
                    if (!player.getUseCosmeticArmorSlot(slotIndex)) {
                        v = 16;
                    }
                    if (isHovered()) {
                        u = 16;
                    }

                    blit(this.x, this.y, this.width, this.height, u, v, 16, 16, 32, 32);
                    GlStateManager.disableBlend();
                    GlStateManager.enableDepthTest();
                }
            });
        }
    }

    @Override
    public void render(int int_1, int int_2, float float_1) {
        this.renderBackground();
        super.render(int_1, int_2, float_1);
        this.drawMouseoverTooltip(int_1, int_2);

        this.mouseX = (float) int_1;
        this.mouseY = (float) int_2;
    }

    @Override
    protected void drawForeground(int int_1, int int_2) {
        super.drawForeground(int_1, int_2);
    }

    @Override
    protected void drawBackground(float var1, int var2, int var3) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(TEXTURE);
        this.blit(this.x, this.y, 0, 0, this.containerWidth, this.containerHeight);
        InventoryScreen.drawEntity(this.x + 51, this.y + 75, 30, (float) (this.x + 51) - this.mouseX, (float) (this.y + 75 - 50) - this.mouseY, this.minecraft.player);
    }

    //    @Environment(EnvType.CLIENT)
    static class ProxySlot extends Slot {
        private final Slot slot;

        ProxySlot(Slot slot_1, int int_1, int xPos, int yPos) {
            super(slot_1.inventory, int_1, xPos, yPos);
            this.slot = slot_1;
        }

        public ItemStack onTakeItem(PlayerEntity playerEntity_1, ItemStack itemStack_1) {
            this.slot.onTakeItem(playerEntity_1, itemStack_1);
            return itemStack_1;
        }

        public boolean canInsert(ItemStack itemStack_1) {
            return this.slot.canInsert(itemStack_1);
        }

        public ItemStack getStack() {
            return this.slot.getStack();
        }

        public boolean hasStack() {
            return this.slot.hasStack();
        }

        public void setStack(ItemStack itemStack_1) {
            this.slot.setStack(itemStack_1);
        }

        public void markDirty() {
            this.slot.markDirty();
        }

        public int getMaxStackAmount() {
            return this.slot.getMaxStackAmount();
        }

        public int getMaxStackAmount(ItemStack itemStack_1) {
            return this.slot.getMaxStackAmount(itemStack_1);
        }

        @Nullable
        public Pair<Identifier, Identifier> getBackgroundSprite() {
            return this.slot.getBackgroundSprite();
        }

        public ItemStack takeStack(int int_1) {
            return this.slot.takeStack(int_1);
        }

        public boolean doDrawHoveringEffect() {
            return this.slot.doDrawHoveringEffect();
        }

        public boolean canTakeItems(PlayerEntity playerEntity_1) {
            return this.slot.canTakeItems(playerEntity_1);
        }
    }

    //    @Environment(EnvType.CLIENT)
    static class CosmeticArmorSlotsContainer extends Container {
        CosmeticArmorSlotsContainer(PlayerInventory playerInventory) {
            super(null, 0);

            for (int i = 0; i < playerInventory.player.playerContainer.slots.size(); ++i) {
                Slot originalSlot = playerInventory.player.playerContainer.slots.get(i);
                //Slot slot = new ProxySlot(originalSlot, i);
                int xPos = originalSlot.xPosition;
                int yPos = originalSlot.yPosition;
                if (i < 5) {
                    // crafting slots
                    xPos = -10000;
                    yPos = -10000;
                } else if (i == 45) {
                    // offhand slot
                    xPos = 152;
                    yPos = 62;
                } else if (i >= 46 && i < 50) {
                    // cosmetic armor slots
                    xPos = 77;
                    yPos = 8 + (i - 46) * 18;
                } else {
                    // everything else
                    xPos = originalSlot.xPosition;
                    yPos = originalSlot.yPosition;
                }
                this.addSlot(new ProxySlot(originalSlot,i,xPos,yPos));
            }
        }

        public boolean canUse(PlayerEntity playerEntity_1) {
            return true;
        }

        public ItemStack transferSlot(PlayerEntity playerEntity_1, int int_1) {
            ItemStack itemStack_1 = ItemStack.EMPTY;
            Slot slot_1 = (Slot) this.slots.get(int_1);
            if (slot_1 != null && slot_1.hasStack()) {
                ItemStack itemStack_2 = slot_1.getStack();
                itemStack_1 = itemStack_2.copy();
                EquipmentSlot equipmentSlot_1 = MobEntity.getPreferredEquipmentSlot(itemStack_1);
                if (int_1 == 0) {
                    if (!this.insertItem(itemStack_2, 9, 45, true)) {
                        return ItemStack.EMPTY;
                    }

                    slot_1.onStackChanged(itemStack_2, itemStack_1);
                } else if (int_1 >= 1 && int_1 < 5) {
                    if (!this.insertItem(itemStack_2, 9, 45, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (int_1 >= 5 && int_1 < 9) {
                    if (!this.insertItem(itemStack_2, 9, 45, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (equipmentSlot_1.getType() == EquipmentSlot.Type.ARMOR && !((Slot) this.slots.get(8 - equipmentSlot_1.getEntitySlotId())).hasStack()) {
                    int int_2 = 8 - equipmentSlot_1.getEntitySlotId();
                    if (!this.insertItem(itemStack_2, int_2, int_2 + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (equipmentSlot_1 == EquipmentSlot.OFFHAND && !((Slot) this.slots.get(45)).hasStack()) {
                    if (!this.insertItem(itemStack_2, 45, 46, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (int_1 >= 9 && int_1 < 36) {
                    if (!this.insertItem(itemStack_2, 36, 45, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (int_1 >= 36 && int_1 < 45) {
                    if (!this.insertItem(itemStack_2, 9, 36, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.insertItem(itemStack_2, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }

                if (itemStack_2.isEmpty()) {
                    slot_1.setStack(ItemStack.EMPTY);
                } else {
                    slot_1.markDirty();
                }

                if (itemStack_2.getCount() == itemStack_1.getCount()) {
                    return ItemStack.EMPTY;
                }

                ItemStack itemStack_3 = slot_1.onTakeItem(playerEntity_1, itemStack_2);
                if (int_1 == 0) {
                    playerEntity_1.dropItem(itemStack_3, false);
                }
            }

            return itemStack_1;
        }
    }

}
