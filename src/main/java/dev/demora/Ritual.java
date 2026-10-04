package dev.demora;

import dev.demora.net.Fx;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
public final class Ritual {
	public static final int ASSEMBLE = 70;
	public static final int DURATION = 112;
	public static void begin(ServerWorld world, PlayerEntity player, Vec3d top) {
		Fx.send(world, Fx.RITUAL, top, Vec3d.ZERO, new float[]{DURATION}, new int[]{player.getId()}, 128);
		Scheduler.run(t -> {
			if (t == 0) {
				world.playSound(null, top.x, top.y, top.z, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 2.0F, 0.8F);
				world.playSound(null, top.x, top.y, top.z, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.PLAYERS, 2.0F, 0.6F);
			}
			if (t == 18) {
				world.playSound(null, top.x, top.y, top.z, DemoraSkySword.CHARGE, SoundCategory.PLAYERS, 3.0F, 1.0F);
			}
			if (t > 18 && t < ASSEMBLE && t % 12 == 0) {
				world.playSound(null, top.x, top.y, top.z, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.PLAYERS,
						1.5F, 0.8F + t / (float) ASSEMBLE);
			}
			if (t == ASSEMBLE) {
				world.playSound(null, top.x, top.y, top.z, DemoraSkySword.IMPACT, SoundCategory.PLAYERS, 4.0F, 1.2F);
				world.playSound(null, top.x, top.y, top.z, SoundEvents.ITEM_TRIDENT_THUNDER.value(), SoundCategory.PLAYERS, 2.0F, 1.0F);
				for (LivingEntity e : Targets.around(world, player, top, 4.0)) {
					Targets.knockAway(e, top, 0.8, 0.4);
				}
			}
			if (t == DURATION - 4) {
				ItemEntity item = new ItemEntity(world, top.x, top.y + 0.6, top.z, new ItemStack(DemoraSkySword.SKY_SWORD));
				item.setVelocity(0, 0.12, 0);
				item.setPickupDelay(15);
				item.setNeverDespawn();
				world.spawnEntity(item);
				world.playSound(null, top.x, top.y, top.z, SoundEvents.ITEM_TRIDENT_RETURN, SoundCategory.PLAYERS, 2.0F, 0.6F);
				world.playSound(null, top.x, top.y, top.z, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 2.0F, 1.2F);
			}
			return t < DURATION;
		});
	}

	private Ritual() {
	}
}
