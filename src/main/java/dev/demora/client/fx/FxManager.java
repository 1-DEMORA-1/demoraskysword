package dev.demora.client.fx;

import dev.demora.net.Fx;
import dev.demora.net.FxPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
public final class FxManager {
	public static final Particles PARTICLES = new Particles();
	private static final List<FxEffect> EFFECTS = new ArrayList<>();
	private static final List<FxEffect> ADDED = new ArrayList<>();
	private static final Map<Integer, Effects.Charge> CHARGES = new HashMap<>();
	private static float trauma;
	private static float flash;
	private static float flashR = 1, flashG = 1, flashB = 1;
	private static long ticks;
	public static void add(FxEffect e) {
		ADDED.add(e);
	}
	public static ThreadLocalRandom rnd() {
		return ThreadLocalRandom.current();
	}
	public static double r(double min, double max) {
		return min + rnd().nextDouble() * (max - min);
	}
	public static Vec3d randomDir() {
		double u = r(-1, 1), t = r(0, Math.PI * 2), s = Math.sqrt(1 - u * u);
		return new Vec3d(s * Math.cos(t), u, s * Math.sin(t));
	}
	public static Entity entity(int id) {
		MinecraftClient mc = MinecraftClient.getInstance();
		return mc.world == null ? null : mc.world.getEntityById(id);
	}
	public static float clamp01(float v) {
		return MathHelper.clamp(v, 0, 1);
	}
	private static double distToPlayer(Vec3d p) {
		MinecraftClient mc = MinecraftClient.getInstance();
		return mc.player == null ? 1.0E9 : mc.player.getPos().distanceTo(p);
	}

	public static void shake(Vec3d at, float amount, double range) {
		double k = 1 - distToPlayer(at) / range;
		if (k > 0) {
			trauma = Math.min(1.2F, trauma + amount * (float) k);
		}
	}
	public static void flash(Vec3d at, float amount, double range, float r, float g, float b) {
		double k = 1 - distToPlayer(at) / range;
		if (k > 0 && amount * k > flash) {
			flash = Math.min(0.75F, amount * (float) k);
			flashR = r;
			flashG = g;
			flashB = b;
		}
	}
	public static float[] shakeAngles(float tickDelta) {
		float t = (ticks + tickDelta) * 0.9F;
		float s = trauma * trauma * 3.2F;
		return new float[]{
				s * (float) (Math.sin(t * 1.7) + Math.sin(t * 3.1 + 1.3) * 0.5),
				s * (float) (Math.sin(t * 2.3 + 0.7) + Math.sin(t * 4.3) * 0.5),
				s * (float) Math.sin(t * 1.3 + 2.1) * 0.8F
		};
	}
	public static float flash() {
		return flash;
	}
	public static float[] flashColor() {
		return new float[]{flashR, flashG, flashB};
	}
	public static void handle(FxPayload p) {
		switch (p.type()) {
			case Fx.SLASH -> add(new Effects.Slash(p));
			case Fx.SPARK -> add(new Effects.Spark(p));
			case Fx.RITUAL -> add(new Effects.Ritual(p));
			case Fx.CHARGE -> {
				int caster = p.ids()[0];
				Effects.Charge existing = CHARGES.get(caster);
				if (existing != null && existing.alive()) {
					existing.update(p);
				} else {
					Effects.Charge c = new Effects.Charge(p);
					CHARGES.put(caster, c);
					add(c);
				}
			}
			case Fx.STRIKE -> {
				Effects.Charge c = CHARGES.remove(p.ids()[0]);
				if (c != null) {
					c.release();
				}
				add(new Effects.Strike(p));
			}
			default -> {
			}
		}
	}
	public static void tick() {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.world == null) {
			EFFECTS.clear();
			CHARGES.clear();
			PARTICLES.clear();
			trauma = flash = 0;
			return;
		}
		if (mc.isPaused()) {
			return;
		}
		ticks++;
		EFFECTS.addAll(ADDED);
		ADDED.clear();
		EFFECTS.removeIf(e -> !e.tickEffect());
		CHARGES.values().removeIf(e -> !e.alive());
		PARTICLES.tick(false);
		trauma = Math.max(0, trauma - 0.045F);
		flash *= 0.78F;
	}
	public static void render(Camera camera, float tickDelta) {
		MinecraftClient mc = MinecraftClient.getInstance();
		Draw d = new Draw(mc.getBufferBuilders().getEffectVertexConsumers(), camera, tickDelta);
		for (FxEffect e : EFFECTS) {
			e.render(d);
		}
		PARTICLES.render(d);
		d.buffers.draw();
	}

	private FxManager() {
	}
}
