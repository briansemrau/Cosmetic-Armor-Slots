package com.github.briansemrau.cosmeticarmorslots;

import net.fabricmc.api.ClientModInitializer;

public class CosmeticArmorSlotsClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		CosmeticArmorSlotsNetwork.onInitializeClient();
	}

}
