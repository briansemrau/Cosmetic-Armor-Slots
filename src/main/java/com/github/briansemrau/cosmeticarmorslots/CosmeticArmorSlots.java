package com.github.briansemrau.cosmeticarmorslots;

import net.fabricmc.api.ModInitializer;

public class CosmeticArmorSlots implements ModInitializer {

	public static final String MOD_ID = "cosmeticarmorslots";

	@Override
	public void onInitialize() {
		CosmeticArmorSlotsNetwork.onInitialize();
	}

}
