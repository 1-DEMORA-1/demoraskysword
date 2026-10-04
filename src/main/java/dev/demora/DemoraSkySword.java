package dev.demora;

import dev.demora.net.FxPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
public class DemoraSkySword implements ModInitializer {
	public static final String MOD_ID = "demoraskysword";
	public static final ToolMaterial MATERIAL = new ToolMaterial(
			BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 4062, 10.0F, 5.0F, 22, ItemTags.NETHERITE_TOOL_MATERIALS);
	public static final SoundEvent CHARGE = sound("judgement_charge");
	public static final SoundEvent DESCEND = sound("judgement_descend");
	public static final SoundEvent IMPACT = sound("judgement_impact");
	public static final SoundEvent SLASH = sound("slash");
	public static final Item SKY_SWORD = item("sky_sword");
	public static final Item SHARD_BLADE = shard("sky_shard_blade");
	public static final Item SHARD_GUARD = shard("sky_shard_guard");
	public static final Item SHARD_HILT = shard("sky_shard_hilt");
	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
	private static SoundEvent sound(String name) {
		Identifier id = id(name);
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}
	private static Item item(String name) {
		RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id(name));
		Item.Settings settings = new Item.Settings().registryKey(key).rarity(Rarity.COMMON).fireproof();
		return Registry.register(Registries.ITEM, key, new SkySwordItem(settings));
	}

	private static Item shard(String name) {
		RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id(name));
		Item.Settings settings = new Item.Settings().registryKey(key).rarity(Rarity.COMMON).fireproof().maxCount(16);
		return Registry.register(Registries.ITEM, key, new ShardItem(settings));
	}
	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playS2C().register(FxPayload.ID, FxPayload.CODEC);
		Scheduler.init();
		VillageShards.init();
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> entries.add(SKY_SWORD));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
			entries.add(SHARD_BLADE);
			entries.add(SHARD_GUARD);
			entries.add(SHARD_HILT);
		});
	}
}
