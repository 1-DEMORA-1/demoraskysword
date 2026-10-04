package dev.demora;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.List;
public final class Targets {
	public static boolean valid(PlayerEntity player, Entity e) {
		return e instanceof LivingEntity living && e != player && living.isAlive() && !(e instanceof ArmorStandEntity)
				&& !e.isSpectator() && !player.isTeammate(e) && !(e instanceof PlayerEntity p && p.isCreative());
	}
	public static List<LivingEntity> around(ServerWorld world, PlayerEntity player, Vec3d c, double r) {
		return world.getEntitiesByClass(LivingEntity.class, new Box(c, c).expand(r),
				e -> valid(player, e) && e.getPos().add(0, e.getHeight() / 2, 0).squaredDistanceTo(c) <= r * r);
	}
	public static Vec3d groundPoint(PlayerEntity player, double range) {
		HitResult hit = player.raycast(range, 1.0F, false);
		Vec3d p = hit.getPos();
		if (hit.getType() == HitResult.Type.BLOCK) {
			return p;
		}
		World world = player.getWorld();
		BlockPos.Mutable m = BlockPos.ofFloored(p).mutableCopy();
		for (int i = 0; i < 32 && m.getY() > world.getBottomY(); i++) {
			if (!world.getBlockState(m.down()).getCollisionShape(world, m.down()).isEmpty()) {
				return new Vec3d(p.x, m.getY(), p.z);
			}
			m.move(0, -1, 0);
		}
		return p;
	}

	public static void hurt(ServerWorld world, LivingEntity target, DamageSource source, float amount) {
		target.timeUntilRegen = 0;
		target.damage(world, source, amount);
	}
	public static void knockAway(LivingEntity target, Vec3d from, double strength, double up) {
		Vec3d dir = target.getPos().subtract(from).multiply(1, 0, 1);
		if (dir.lengthSquared() < 1.0E-4) {
			dir = new Vec3d(0, 0, 1);
		}
		dir = dir.normalize().multiply(strength);
		target.addVelocity(dir.x, up, dir.z);
		target.velocityModified = true;
	}
	private Targets() {
	}
}
