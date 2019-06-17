package io.github.briansemrau.cosmeticarmorslots.interfaces;

public interface IPlayerEntityMixin {

    void setUseCosmeticArmorSlot(int entitySlotId, boolean use);

    boolean useCosmeticArmorSlot(int entitySlotId);

}
