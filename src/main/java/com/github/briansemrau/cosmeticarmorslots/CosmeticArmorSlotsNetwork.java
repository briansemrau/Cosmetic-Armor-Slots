package com.github.briansemrau.cosmeticarmorslots;

import com.github.briansemrau.cosmeticarmorslots.interfaces.IPlayerEntityMixin;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import static com.github.briansemrau.cosmeticarmorslots.CosmeticArmorSlots.MOD_ID;

public class CosmeticArmorSlotsNetwork {

    public static final Identifier EQUIP_COSMETIC_ARMOR_PACKET = new Identifier(MOD_ID, "equip_cosmetic_armor");
    public static final Identifier VISIBILITY_UPDATE_PACKET = new Identifier(MOD_ID, "visibility_update");

    public static void onInitialize() {
        ServerSidePacketRegistry.INSTANCE.register(VISIBILITY_UPDATE_PACKET, (packetContext, packetByteBuf) -> {
            EquipmentSlot slot = packetByteBuf.readEnumConstant(EquipmentSlot.class);
            boolean visible = packetByteBuf.readBoolean();

            ((IPlayerEntityMixin) packetContext.getPlayer()).setUseCosmeticArmorSlot(slot.getEntitySlotId(), visible);
        });
    }

    public static void onInitializeClient() {
        ClientSidePacketRegistry.INSTANCE.register(EQUIP_COSMETIC_ARMOR_PACKET, (packetContext, packetByteBuf) -> {
            int entityId = packetByteBuf.readVarInt();
            EquipmentSlot slot = packetByteBuf.readEnumConstant(EquipmentSlot.class);
            ItemStack stack = packetByteBuf.readItemStack();
            boolean useCosmeticSlot = packetByteBuf.readBoolean();

            Entity player = packetContext.getPlayer().world.getEntityById(entityId);
            if (player != null) {
                ((IPlayerEntityMixin) player).setEquippedCosmeticArmor(slot, stack);
                ((IPlayerEntityMixin) player).setUseCosmeticArmorSlot(slot.getEntitySlotId(), useCosmeticSlot);
            }
        });
    }

    public static Packet<?> createEquipCosmeticArmorPacket(int entityId, EquipmentSlot slot, ItemStack stack, boolean visible) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(entityId);
        buf.writeEnumConstant(slot);
        buf.writeItemStack(stack);
        buf.writeBoolean(visible);
        return ServerSidePacketRegistry.INSTANCE.toPacket(EQUIP_COSMETIC_ARMOR_PACKET, buf);
    }

    public static Packet<?> createCosmeticSlotVisibilityUpdatePacket(EquipmentSlot slot, boolean visible) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeEnumConstant(slot);
        buf.writeBoolean(visible);
        return ClientSidePacketRegistry.INSTANCE.toPacket(VISIBILITY_UPDATE_PACKET, buf);
    }

}
