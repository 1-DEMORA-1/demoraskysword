package dev.demora;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import net.minecraft.world.chunk.WorldChunk;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
public final class VillageShards extends PersistentState {
	private static final long NONE = Long.MIN_VALUE;
	private static final float CHANCE = 0.5F;
	private static final Type<VillageShards> TYPE = new Type<>(VillageShards::new, VillageShards::read, null);
	public static final LootConditionType CONDITION = Registry.register(Registries.LOOT_CONDITION_TYPE,
			DemoraSkySword.id("village_shard_chest"), new LootConditionType(MapCodec.unit(Condition.INSTANCE)));
	private final Map<Long, Long> chosen = new HashMap<>();
	private static VillageShards read(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		VillageShards state = new VillageShards();
		NbtList list = nbt.getList("villages", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < list.size(); i++) {
			NbtCompound c = list.getCompound(i);
			state.chosen.put(c.getLong("village"), c.getLong("chest"));
		}
		return state;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		NbtList list = new NbtList();
		chosen.forEach((village, chest) -> {
			NbtCompound c = new NbtCompound();
			c.putLong("village", village);
			c.putLong("chest", chest);
			list.add(c);
		});
		nbt.put("villages", list);
		return nbt;
	}
	private static VillageShards get(ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate(TYPE, DemoraSkySword.MOD_ID + "_villages");
	}
	private static boolean isVillageChest(LootableContainerBlockEntity be) {
		return be.getLootTable() != null && be.getLootTable().getValue().getNamespace().equals("minecraft")
				&& be.getLootTable().getValue().getPath().startsWith("chests/village/");
	}
	private static List<BlockPos> villageChests(ServerWorld world, BlockBox box, BlockPos current) {
		List<BlockPos> out = new ArrayList<>();
		out.add(current);
		if (box == null) {
			return out;
		}
		for (int cx = box.getMinX() >> 4; cx <= box.getMaxX() >> 4; cx++) {
			for (int cz = box.getMinZ() >> 4; cz <= box.getMaxZ() >> 4; cz++) {
				if (!world.getChunkManager().isChunkLoaded(cx, cz)) {
					continue;
				}
				WorldChunk chunk = world.getChunk(cx, cz);
				for (BlockEntity be : chunk.getBlockEntities().values()) {
					if (be instanceof LootableContainerBlockEntity lootable && isVillageChest(lootable)
							&& box.contains(be.getPos()) && !be.getPos().equals(current)) {
						out.add(be.getPos().toImmutable());
					}
				}
			}
		}
		return out;
	}
	private static boolean test(LootContext ctx) {
		ServerWorld world = ctx.getWorld();
		Vec3d origin = ctx.get(LootContextParameters.ORIGIN);
		if (origin == null) {
			return false;
		}
		BlockPos pos = BlockPos.ofFloored(origin);
		StructureStart village = world.getStructureAccessor().getStructureContaining(pos, StructureTags.VILLAGE);
		boolean inVillage = village != null && village.hasChildren();
		long key;
		BlockBox box = null;
		if (inVillage) {
			key = village.getPos().toLong();
			box = village.getBoundingBox();
		} else {
			Optional<BlockPos> bell = world.getPointOfInterestStorage().getNearestPosition(
					type -> type.matchesKey(PointOfInterestTypes.MEETING), pos, 96, PointOfInterestStorage.OccupationStatus.ANY);
			if (bell.isPresent()) {
				BlockPos b = bell.get();
				key = b.asLong() ^ 0x2545F4914F6CDD1DL;
				box = new BlockBox(b.getX() - 80, b.getY() - 32, b.getZ() - 80, b.getX() + 80, b.getY() + 32, b.getZ() + 80);
			} else {
				key = pos.asLong() ^ 0x5DEECE66DL;
			}
		}
		VillageShards state = get(world);
		Long chest = state.chosen.get(key);
		if (chest == null) {
			chest = NONE;
			if (ctx.getRandom().nextFloat() < CHANCE) {
				List<BlockPos> chests = villageChests(world, box, pos);
				chest = chests.get(ctx.getRandom().nextInt(chests.size())).asLong();
			}
			state.chosen.put(key, chest);
			state.markDirty();
		}
		return chest == pos.asLong();
	}

	public static void init() {
		LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
			if (!source.isBuiltin() || !key.getValue().getNamespace().equals("minecraft")
					|| !key.getValue().getPath().startsWith("chests/village/")) {
				return;
			}
			builder.pool(LootPool.builder()
					.rolls(ConstantLootNumberProvider.create(1))
					.conditionally(() -> Condition.INSTANCE)
					.with(ItemEntry.builder(DemoraSkySword.SHARD_BLADE))
					.with(ItemEntry.builder(DemoraSkySword.SHARD_GUARD))
					.with(ItemEntry.builder(DemoraSkySword.SHARD_HILT)));
		});
	}
	public enum Condition implements LootCondition {
		INSTANCE;
		@Override
		public LootConditionType getType() {
			return CONDITION;
		}
		@Override
		public boolean test(LootContext ctx) {
			return VillageShards.test(ctx);
		}
	}
}
