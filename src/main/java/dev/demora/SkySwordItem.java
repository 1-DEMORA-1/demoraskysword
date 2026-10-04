package dev.demora;

import dev.demora.mixin.CraftingScreenHandlerAccessor;
import dev.demora.net.Fx;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.consume.UseAction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.Map;
import java.util.WeakHashMap;
public class SkySwordItem extends SwordItem {
	private static final int FULL_CHARGE = 30;
	private static final int MIN_CHARGE = 10;
	private static final int COOLDOWN = 16 * 20;
	public static final int SINK_START = 88;
	public static final int SINK_END = 128;
	private static final Map<LivingEntity, int[]> MARKS = new WeakHashMap<>();
	public SkySwordItem(Settings settings) {
		super(DemoraSkySword.MATERIAL, 4.0F, -2.4F, settings);
	}
	@Override
	public float getBonusAttackDamage(Entity target, float baseAttackDamage, DamageSource source) {
		return target instanceof LivingEntity living && living.hasInvertedHealingAndHarm() ? 6.0F : 0.0F;
	}
	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (attacker.getWorld() instanceof ServerWorld world && attacker instanceof PlayerEntity player) {
			Vec3d look = player.getRotationVec(1.0F);
			Fx.send(world, Fx.SLASH, player.getEyePos().add(look.multiply(1.1)).add(0, -0.25, 0), look,
					new float[]{1.0F, 0.84F, 0.36F, 1.0F, world.random.nextFloat()}, new int[]{player.getId()}, 64);
			world.playSound(null, player.getX(), player.getY(), player.getZ(), DemoraSkySword.SLASH,
					SoundCategory.PLAYERS, 0.6F, 0.9F + world.random.nextFloat() * 0.25F);
			mark(world, target, player);
		}
		return super.postHit(stack, target, attacker);
	}

	private static void mark(ServerWorld world, LivingEntity target, PlayerEntity attacker) {
		int[] mark = MARKS.computeIfAbsent(target, t -> new int[2]);
		if (attacker.age - mark[1] > 100) {
			mark[0] = 0;
		}
		mark[0]++;
		mark[1] = attacker.age;
		if (mark[0] < 3) {
			return;
		}
		mark[0] = 0;
		Vec3d p = target.getPos();
		Fx.send(world, Fx.SPARK, p, Vec3d.ZERO, new float[0], new int[0], 96);
		Scheduler.later(4, () -> {
			if (!target.isAlive()) {
				return;
			}
			Targets.hurt(world, target, world.getDamageSources().indirectMagic(attacker, attacker),
					target.hasInvertedHealingAndHarm() ? 14.0F : 8.0F);
			attacker.heal(3.0F);
			world.playSound(null, p.x, p.y, p.z, SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1.2F, 1.8F);
			world.playSound(null, p.x, p.y, p.z, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.PLAYERS, 2.0F, 1.4F);
		});
	}
	@Override
	public void onCraftByPlayer(ItemStack stack, World world, PlayerEntity player) {
		stack.setCount(0);
		if (!(world instanceof ServerWorld sw)) {
			return;
		}
		Vec3d top = player.getPos().add(player.getRotationVec(1.0F).multiply(1, 0, 1).normalize().multiply(1.5)).add(0, 1, 0);
		if (player.currentScreenHandler instanceof CraftingScreenHandlerAccessor crafting) {
			top = crafting.demora$getContext().get((w, pos) -> Vec3d.ofBottomCenter(pos.up()), top);
		}
		if (player instanceof net.minecraft.server.network.ServerPlayerEntity sp) {
			Scheduler.later(1, sp::closeHandledScreen);
		}
		Ritual.begin(sw, player, top);
	}
	@Override
	public Text getName(ItemStack stack) {
		return super.getName(stack).copy().formatted(Formatting.YELLOW, Formatting.BOLD);
	}
	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.SPEAR;
	}
	@Override
	public int getMaxUseTime(ItemStack stack, LivingEntity user) {
		return 72000;
	}
	@Override
	public ActionResult use(World world, PlayerEntity player, Hand hand) {
		if (player.getItemCooldownManager().isCoolingDown(player.getStackInHand(hand))) {
			return ActionResult.FAIL;
		}
		player.setCurrentHand(hand);
		return ActionResult.CONSUME;
	}

	@Override
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
		if (!(world instanceof ServerWorld sw) || !(user instanceof PlayerEntity player)) {
			return;
		}
		int charge = getMaxUseTime(stack, user) - remainingUseTicks;
		if (charge == 1) {
			world.playSound(null, player.getX(), player.getY(), player.getZ(), DemoraSkySword.CHARGE,
					SoundCategory.PLAYERS, 1.4F, 1.0F);
		}
		if (charge == FULL_CHARGE) {
			world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT,
					SoundCategory.PLAYERS, 1.5F, 1.5F);
		}
		if (charge % 2 == 0) {
			float progress = Math.min(1.0F, charge / (float) FULL_CHARGE);
			Fx.send(sw, Fx.CHARGE, Targets.groundPoint(player, 42), Vec3d.ZERO, new float[]{progress},
					new int[]{player.getId()}, 128);
		}
	}
	@Override
	public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		int charge = getMaxUseTime(stack, user) - remainingUseTicks;
		if (!(world instanceof ServerWorld sw) || !(user instanceof PlayerEntity player) || charge < MIN_CHARGE) {
			return false;
		}
		float power = Math.min(1.0F, (charge - MIN_CHARGE) / (float) (FULL_CHARGE - MIN_CHARGE)) * 0.5F + 0.5F;
		player.getItemCooldownManager().set(stack, COOLDOWN);
		stack.damage(3, player, LivingEntity.getSlotForHand(player.getActiveHand()));
		strike(sw, player, Targets.groundPoint(player, 42), power);
		return true;
	}
	private static void strike(ServerWorld world, PlayerEntity player, Vec3d pos, float power) {
		Fx.send(world, Fx.STRIKE, pos, Vec3d.ZERO, new float[]{power}, new int[]{player.getId()}, 192);
		world.playSound(null, pos.x, pos.y + 10, pos.z, DemoraSkySword.DESCEND, SoundCategory.PLAYERS, 5.0F, 1.0F);
		double radius = 6.5 + power * 2.0;
		Scheduler.run(t -> {
			if (t == 10) {
				world.playSound(null, pos.x, pos.y, pos.z, DemoraSkySword.IMPACT, SoundCategory.PLAYERS, 8.0F, 1.0F);
				world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 3.0F, 0.6F);
				for (LivingEntity e : Targets.around(world, player, pos, radius)) {
					double d = e.getPos().distanceTo(pos);
					float falloff = (float) (1.0 - 0.5 * Math.min(1.0, d / radius));
					float dmg = (16.0F + 16.0F * power) * falloff;
					if (e.hasInvertedHealingAndHarm()) {
						dmg *= 1.5F;
						e.setOnFireFor(6);
					}
					Targets.hurt(world, e, world.getDamageSources().playerAttack(player), dmg);
					Targets.knockAway(e, pos, 1.1 * falloff, 0.9 * falloff + 0.3);
					e.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 120, 0), player);
				}
			}
			if (t > 10 && t < SINK_START && t % 10 == 0) {
				for (LivingEntity e : Targets.around(world, player, pos, radius - 0.5)) {
					if (e instanceof Monster || e.hasInvertedHealingAndHarm()) {
						Targets.hurt(world, e, world.getDamageSources().indirectMagic(player, player),
								e.hasInvertedHealingAndHarm() ? 5.0F : 3.0F);
					}
				}
				if (player.isAlive() && player.getPos().squaredDistanceTo(pos) < radius * radius) {
					player.heal(1.5F);
				}
			}
			if (t == SINK_START) {
				world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.PLAYERS, 3.0F, 0.4F);
				world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.BLOCK_ROOTED_DIRT_BREAK, SoundCategory.PLAYERS, 3.0F, 0.5F);
			}
			return t < SINK_END;
		});
	}
}
