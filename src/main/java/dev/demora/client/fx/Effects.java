package dev.demora.client.fx;

import dev.demora.DemoraSkySword;
import dev.demora.SkySwordItem;
import dev.demora.net.FxPayload;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import static dev.demora.client.fx.FxManager.PARTICLES;
import static dev.demora.client.fx.FxManager.r;
import static dev.demora.client.fx.FxManager.randomDir;
public final class Effects {
	static final Identifier MAGIC_CIRCLE = DemoraSkySword.id("textures/fx/magic_circle.png");
	static final Vec3d UP = new Vec3d(0, 1, 0);
	static final float MODEL_TOP = 12.78F / 16.0F;
	static final float MODEL_BOTTOM = 13.2F / 16.0F;
	static final float MODEL_GUARD = 2.4F / 16.0F;
	static float ease(float t) {
		t = FxManager.clamp01(t);
		return 1 - (1 - t) * (1 - t) * (1 - t);
	}
	static float easeInOut(float t) {
		t = FxManager.clamp01(t);
		return t * t * (3 - 2 * t);
	}
	static Vec3d perp(Vec3d dir) {
		Vec3d a = Math.abs(dir.y) < 0.9 ? UP : new Vec3d(1, 0, 0);
		return dir.crossProduct(a).normalize();
	}
	static void spark(Vec3d at, Vec3d vel, float r, float g, float b, float size, int life) {
		PARTICLES.add(Particles.STAR, at, life).vel(vel).size(size, 0).color(r, g, b).drag(0.88F).spin(0.2F);
	}

	static void mote(Vec3d at, Vec3d vel, float r, float g, float b, float size, int life) {
		PARTICLES.add(Particles.GLOW, at, life).vel(vel).size(size, size * 0.3F).color(r, g, b).drag(0.93F);
	}
	static void blockDust(Vec3d at, double radius, int count, double speed) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.world == null) {
			return;
		}
		BlockState ground = mc.world.getBlockState(BlockPos.ofFloored(at).down());
		if (ground.isAir()) {
			return;
		}
		BlockStateParticleEffect fx = new BlockStateParticleEffect(ParticleTypes.BLOCK, ground);
		for (int i = 0; i < count; i++) {
			double a = r(0, Math.PI * 2), d = r(0, radius);
			mc.world.addParticle(fx, at.x + Math.cos(a) * d, at.y + 0.1, at.z + Math.sin(a) * d,
					Math.cos(a) * speed * r(0.5, 1.5), r(0.2, 0.7) * speed * 2, Math.sin(a) * speed * r(0.5, 1.5));
		}
	}
	static void arc(Draw d, Vec3d c, Vec3d fwd, Vec3d side, double radius, double thick, double from, double to,
					float reveal, float r, float g, float b, float a) {
		VertexConsumer vc = d.glow();
		int seg = 28;
		double end = from + (to - from) * reveal;
		for (int i = 0; i < seg; i++) {
			double t0 = from + (end - from) * i / seg, t1 = from + (end - from) * (i + 1) / seg;
			double k0 = Math.sin(Math.PI * (t0 - from) / (to - from)), k1 = Math.sin(Math.PI * (t1 - from) / (to - from));
			Vec3d d0 = side.multiply(Math.sin(t0)).add(fwd.multiply(Math.cos(t0)));
			Vec3d d1 = side.multiply(Math.sin(t1)).add(fwd.multiply(Math.cos(t1)));
			Vec3d o0 = c.add(d0.multiply(radius)), o1 = c.add(d1.multiply(radius));
			Vec3d i0 = c.add(d0.multiply(radius - thick * k0)), i1 = c.add(d1.multiply(radius - thick * k1));
			Vec3d e0 = c.add(d0.multiply(radius + thick * 0.15 * k0)), e1 = c.add(d1.multiply(radius + thick * 0.15 * k1));
			float a0 = a * (float) k0, a1 = a * (float) k1;
			d.v(vc, i0, r, g, b, 0);
			d.v(vc, i1, r, g, b, 0);
			d.v(vc, o1, r, g, b, a1);
			d.v(vc, o0, r, g, b, a0);
			d.v(vc, o0, 1, 1, 1, a0);
			d.v(vc, o1, 1, 1, 1, a1);
			d.v(vc, e1, r, g, b, 0);
			d.v(vc, e0, r, g, b, 0);
		}
	}
	public static final class Slash extends FxEffect {
		final Vec3d pos, fwd, side;
		final float r, g, b, size;
		public Slash(FxPayload p) {
			super(8);
			pos = p.pos();
			fwd = p.vec().normalize();
			r = p.f(0, 1);
			g = p.f(1, 1);
			b = p.f(2, 1);
			size = p.f(3, 1);
			double roll = (p.f(4, 0.5F) - 0.5) * 2.2;
			Vec3d right = perp(fwd);
			Vec3d up = right.crossProduct(fwd);
			side = right.multiply(Math.cos(roll)).add(up.multiply(Math.sin(roll)));
			for (int i = 0; i < 6; i++) {
				spark(pos.add(side.multiply(r(-1, 1))).add(fwd.multiply(0.3)), side.multiply(r(-0.1, 0.1)).add(fwd.multiply(0.12)),
						r, g, b, 0.18F, 10);
			}
		}
		@Override
		public void render(Draw d) {
			float t = time(d);
			float reveal = FxManager.clamp01(t / 2.2F);
			float fade = 1 - FxManager.clamp01((t - 2) / 6);
			double radius = 1.5 * size * (1 + 0.12 * t / 8);
			arc(d, pos.subtract(fwd.multiply(0.9)), fwd, side, radius, 0.55 * size, -1.75, 1.75, reveal, r, g, b, 0.95F * fade);
		}
	}
	public static final class Charge extends FxEffect {
		Vec3d pos, prev;
		float progress;
		int lastUpdate;
		int fadeFrom = -1;
		public Charge(FxPayload p) {
			super(100000);
			pos = prev = p.pos();
			progress = p.f(0, 0);
		}
		void update(FxPayload p) {
			prev = pos;
			pos = p.pos();
			progress = p.f(0, 0);
			lastUpdate = age;
		}
		void release() {
			fadeFrom = age;
			life = age + 6;
		}

		@Override
		protected void tick() {
			if (fadeFrom < 0 && age - lastUpdate > 5) {
				release();
			}
			if (fadeFrom >= 0) {
				return;
			}
			double radius = 1.5 + 5.5 * ease(progress);
			int n = 2 + (int) (6 * progress);
			for (int i = 0; i < n; i++) {
				double a = r(0, Math.PI * 2);
				PARTICLES.add(Particles.RUNES, pos.add(Math.cos(a) * radius, 0.1, Math.sin(a) * radius), 30).rune()
						.vel(0, r(0.04, 0.1), 0).drag(0.97F).size(0.28F, 0.12F).color(1.0F, 0.82F, 0.4F);
			}
			for (int i = 0; i < 3; i++) {
				double a = r(0, Math.PI * 2), rr = r(0, radius);
				mote(pos.add(Math.cos(a) * rr, 0.1, Math.sin(a) * rr), new Vec3d(0, r(0.03, 0.12), 0), 1, 0.9F, 0.6F, 0.15F, 30);
			}
		}
		@Override
		public void render(Draw d) {
			float t = time(d);
			float fade = fadeFrom >= 0 ? 1 - FxManager.clamp01((t - fadeFrom) / 6) : 1;
			float e = ease(progress);
			Vec3d c = prev.lerp(pos, d.tickDelta).add(0, 0.06, 0);
			double radius = 1.5 + 5.5 * e;
			float a = (0.45F + 0.55F * e) * fade;
			VertexConsumer circle = d.tex(MAGIC_CIRCLE);
			d.groundDisc(circle, c, radius, t * 0.02, 1.0F, 0.78F, 0.35F, a);
			d.groundDisc(circle, c.add(0, 0.02, 0), radius * 0.55, -t * 0.045, 1.0F, 0.95F, 0.8F, a * 0.8F);
			d.groundRing(d.glow(), c, radius, 0.3, 1.0F, 0.8F, 0.4F, a * 0.7F);
			if (progress > 0.35F) {
				float k = (progress - 0.35F) / 0.65F;
				d.beam(c, c.add(0, 80, 0), 0.1F + 0.9F * k, 1.0F, 0.85F, 0.5F, k * fade);
			}
		}
	}
	public static final class Strike extends FxEffect {
		static final int IMPACT = 10;
		final Vec3d pos;
		final float power, scale, yaw;
		final double radius;
		final ItemStack sword = new ItemStack(DemoraSkySword.SKY_SWORD);
		final ItemRenderState state = new ItemRenderState();
		public Strike(FxPayload p) {
			super(SkySwordItem.SINK_END + 8);
			pos = p.pos();
			power = p.f(0, 1);
			radius = 6.5 + power * 2.0;
			scale = 13.0F + 5.0F * power;
			Entity caster = p.ids().length > 0 ? FxManager.entity(p.ids()[0]) : null;
			yaw = caster != null ? -caster.getYaw() : (float) r(0, 360);
		}

		double length() {
			return (MODEL_TOP + MODEL_BOTTOM) * scale;
		}
		double offset(float t) {
			if (t < IMPACT) {
				float k = t / IMPACT;
				return 70 * (1 - k * k);
			}
			if (t > SkySwordItem.SINK_START) {
				return -easeInOut((t - SkySwordItem.SINK_START) / (SkySwordItem.SINK_END - SkySwordItem.SINK_START)) * (length() + 0.5);
			}
			return 0;
		}
		@Override
		protected void tick() {
			if (age == IMPACT) {
				FxManager.shake(pos, 1.15F, 70);
				FxManager.flash(pos, 0.7F, 55, 1.0F, 0.96F, 0.82F);
				for (int i = 0; i < 260; i++) {
					Vec3d dir = randomDir();
					Vec3d v = new Vec3d(dir.x, Math.abs(dir.y) * 0.8 + 0.2, dir.z).multiply(r(0.3, 1.4));
					PARTICLES.add(Particles.GLOW, pos.add(0, 0.5, 0), (int) r(25, 60)).vel(v).drag(0.9F).gravity(0.012F)
							.size(0.35F, 0.05F).color(1.0F, (float) r(0.75, 0.95), (float) r(0.35, 0.6));
				}
				for (int i = 0; i < 90; i++) {
					spark(pos.add(0, 1, 0), randomDir().multiply(r(0.4, 1.6)), 1, 0.95F, 0.75F, 0.5F, (int) r(20, 40));
				}
				for (int i = 0; i < 50; i++) {
					PARTICLES.add(Particles.SHARD, pos.add(0, 0.5, 0), (int) r(30, 50))
							.vel(randomDir().multiply(r(0.5, 1.2)).add(0, 0.6, 0)).gravity(0.04F).drag(0.95F)
							.size(0.35F, 0.2F).color(1.0F, 0.85F, 0.45F).spin((float) r(-0.4, 0.4));
				}
				blockDust(pos, radius * 0.7, 160, 0.5);
				MinecraftClient mc = MinecraftClient.getInstance();
				if (mc.world != null) {
					for (int i = 0; i < 6; i++) {
						mc.world.addParticle(ParticleTypes.EXPLOSION, pos.x + r(-3, 3), pos.y + r(0, 2), pos.z + r(-3, 3), 0, 0, 0);
					}
				}
			}
			if (age > IMPACT && age < SkySwordItem.SINK_START) {
				for (int i = 0; i < 4; i++) {
					double a = r(0, Math.PI * 2), rr = Math.sqrt(r(0, 1)) * radius;
					mote(pos.add(Math.cos(a) * rr, 0.1, Math.sin(a) * rr), new Vec3d(0, r(0.04, 0.14), 0), 1, 0.88F, 0.55F, 0.2F, 40);
				}
				if (age % 3 == 0) {
					double a = r(0, Math.PI * 2);
					PARTICLES.add(Particles.RUNES, pos.add(Math.cos(a) * radius, 0.15, Math.sin(a) * radius), 40).rune()
							.vel(0, 0.06, 0).size(0.4F, 0.2F).color(1.0F, 0.85F, 0.45F);
				}
			}
			if (age >= SkySwordItem.SINK_START && age < SkySwordItem.SINK_END) {
				FxManager.shake(pos, 0.05F, 40);
				blockDust(pos, 1.6, 10, 0.18);
				for (int i = 0; i < 6; i++) {
					double a = r(0, Math.PI * 2), rr = r(0.3, 1.6);
					PARTICLES.add(Particles.SMOKE, pos.add(Math.cos(a) * rr, 0.2, Math.sin(a) * rr), (int) r(20, 35))
							.vel(Math.cos(a) * 0.04, r(0.02, 0.07), Math.sin(a) * 0.04).size(0.6F, 1.6F)
							.color(0.55F, 0.5F, 0.42F).alpha(0.35F);
				}
				for (int i = 0; i < 3; i++) {
					mote(pos.add(r(-0.8, 0.8), 0.2, r(-0.8, 0.8)), new Vec3d(0, r(0.06, 0.18), 0), 1, 0.9F, 0.6F, 0.25F, 30);
				}
			}
			if (age == SkySwordItem.SINK_END) {
				for (int i = 0; i < 40; i++) {
					mote(pos.add(0, 0.3, 0), randomDir().multiply(r(0.1, 0.35)).add(0, 0.15, 0), 1, 0.9F, 0.6F, 0.3F, 30);
				}
				blockDust(pos, 2.0, 40, 0.3);
			}
		}
		@Override
		public void render(Draw d) {
			float t = time(d);
			double off = offset(t);
			if (t < SkySwordItem.SINK_END + 1) {
				Vec3d center = pos.add(0, MODEL_TOP * scale - 2.5 + off, 0);
				MinecraftClient mc = MinecraftClient.getInstance();
				mc.getItemModelManager().update(state, sword, ModelTransformationMode.NONE, mc.world, null, 0);
				MatrixStack ms = new MatrixStack();
				ms.translate(center.x - d.cam.x, center.y - d.cam.y, center.z - d.cam.z);
				ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
				ms.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
				ms.scale(scale, scale, scale);
				state.render(ms, d.buffers, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
				float glow = t > SkySwordItem.SINK_START ? 1 - FxManager.clamp01((t - SkySwordItem.SINK_START) / 20) : 1;
				float pulse = 0.8F + 0.2F * (float) Math.sin(t * 0.3);
				Vec3d tip = center.subtract(0, MODEL_TOP * scale, 0);
				Vec3d guard = center.add(0, MODEL_GUARD * scale, 0);
				d.beam(tip, guard, scale * 0.09F, 1.0F, 0.84F, 0.45F, 0.4F * pulse * glow);
				d.billboard(d.tex(Particles.GLOW), guard, scale * 0.35F, 0, 1.0F, 0.9F, 0.6F, 0.35F * glow);
			}
			if (t < IMPACT + 2) {
				Vec3d top = pos.add(0, off + scale * 1.6 + 40, 0);
				d.beam(pos.add(0, off + scale * 0.9, 0), top, 1.2F, 1.0F, 0.85F, 0.5F, 0.6F);
			}
			float circleA = t < IMPACT ? 0.5F + 0.5F * t / IMPACT
					: t > SkySwordItem.SINK_START ? 1 - FxManager.clamp01((t - SkySwordItem.SINK_START) / (SkySwordItem.SINK_END - SkySwordItem.SINK_START)) : 1;
			float pulse = 0.85F + 0.15F * (float) Math.sin(t * 0.25);
			VertexConsumer circle = d.tex(MAGIC_CIRCLE);
			d.groundDisc(circle, pos.add(0, 0.07, 0), radius, t * 0.012, 1.0F, 0.8F, 0.38F, circleA * pulse);
			d.groundDisc(circle, pos.add(0, 0.09, 0), radius * 0.5, -t * 0.03, 1.0F, 0.95F, 0.8F, circleA * 0.8F);
			d.groundRing(d.glow(), pos.add(0, 0.1, 0), radius, 0.35, 1.0F, 0.8F, 0.4F, circleA * 0.7F);
			if (t >= IMPACT) {
				float k = t - IMPACT;
				VertexConsumer glow = d.glow();
				for (int i = 0; i < 3; i++) {
					float w = FxManager.clamp01((k - i * 2.5F) / 16);
					if (w > 0 && w < 1) {
						d.groundRing(glow, pos.add(0, 0.25, 0), w * radius * 2.6, 0.9 * (1 - w) + 0.15, 1, 0.9F - i * 0.1F, 0.6F, (1 - w) * 0.9F);
					}
				}
				float dome = FxManager.clamp01(k / 9);
				if (dome < 1) {
					d.sphere(glow, pos, radius * 1.3 * ease(dome), 1, 0.9F, 0.65F, (1 - dome) * 0.8F, 1.0F);
				}
				float pillar = 1 - FxManager.clamp01(k / 35);
				if (pillar > 0) {
					d.beam(pos, pos.add(0, 120, 0), 3.5F * pillar + 0.3F, 1, 0.9F, 0.6F, pillar);
				}
			}
		}
	}

	public static final class Spark extends FxEffect {
		final Vec3d pos;

		public Spark(FxPayload p) {
			super(18);
			pos = p.pos();
		}
		@Override
		protected void tick() {
			if (age == 4) {
				FxManager.flash(pos, 0.2F, 18, 1, 0.95F, 0.8F);
				for (int i = 0; i < 40; i++) {
					spark(pos.add(0, 1, 0), randomDir().multiply(r(0.2, 0.7)), 1, 0.9F, 0.6F, 0.3F, 20);
				}
				for (int i = 0; i < 30; i++) {
					mote(pos.add(0, 0.5, 0), randomDir().multiply(r(0.1, 0.5)).add(0, 0.2, 0), 1, 0.85F, 0.5F, 0.25F, 30);
				}
			}
		}
		@Override
		public void render(Draw d) {
			float t = time(d);
			float k = t < 4 ? t / 4 : 1 - FxManager.clamp01((t - 4) / 14);
			d.beam(pos.add(0, 50, 0), pos, 0.15F + 0.6F * k, 1, 0.88F, 0.55F, k);
			float w = FxManager.clamp01((t - 4) / 12);
			if (t >= 4) {
				d.groundRing(d.glow(), pos.add(0, 0.1, 0), 0.5 + w * 3.5, 0.35, 1, 0.85F, 0.5F, 1 - w);
			}
			d.groundDisc(d.tex(MAGIC_CIRCLE), pos.add(0, 0.05, 0), 1.6, t * 0.06, 1, 0.8F, 0.4F, k);
		}
	}
	static java.util.List<Vec3d> bolt(Vec3d a, Vec3d b, double jag, int depth) {
		java.util.List<Vec3d> pts = new java.util.ArrayList<>();
		pts.add(a);
		subdivide(pts, a, b, jag, depth);
		pts.add(b);
		return pts;
	}
	private static void subdivide(java.util.List<Vec3d> out, Vec3d a, Vec3d b, double jag, int depth) {
		if (depth == 0) {
			return;
		}
		Vec3d mid = a.add(b).multiply(0.5).add(randomDir().multiply(a.distanceTo(b) * jag));
		subdivide(out, a, mid, jag, depth - 1);
		out.add(mid);
		subdivide(out, mid, b, jag, depth - 1);
	}

	static void renderItem(Draw d, ItemRenderState state, ItemStack stack, Vec3d at, float yaw, float roll, float scale) {
		MinecraftClient mc = MinecraftClient.getInstance();
		mc.getItemModelManager().update(state, stack, ModelTransformationMode.NONE, mc.world, null, 0);
		MatrixStack ms = new MatrixStack();
		ms.translate(at.x - d.cam.x, at.y - d.cam.y, at.z - d.cam.z);
		ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
		ms.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(roll));
		ms.scale(scale, scale, scale);
		net.minecraft.client.render.VertexConsumerProvider.Immediate entities = mc.getBufferBuilders().getEntityVertexConsumers();
		state.render(ms, entities, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
		entities.draw();
	}
	public static final class Ritual extends FxEffect {
		static final int RISE = 20;
		static final int ASSEMBLE = dev.demora.Ritual.ASSEMBLE;
		final Vec3d top;
		final int duration;
		final ItemStack[] shards = {new ItemStack(DemoraSkySword.SHARD_BLADE), new ItemStack(DemoraSkySword.SHARD_GUARD),
				new ItemStack(DemoraSkySword.SHARD_HILT)};
		final ItemRenderState[] states = {new ItemRenderState(), new ItemRenderState(), new ItemRenderState()};
		final ItemStack sword = new ItemStack(DemoraSkySword.SKY_SWORD);
		final ItemRenderState swordState = new ItemRenderState();
		final double[] finalHeight = {3.2, 2.25, 1.35};
		java.util.List<java.util.List<Vec3d>> arcs = new java.util.ArrayList<>();
		public Ritual(FxPayload p) {
			super((int) p.f(0, 112));
			top = p.pos();
			duration = (int) p.f(0, 112);
			FxManager.flash(top, 0.15F, 16, 1, 0.9F, 0.6F);
		}
		Vec3d shardPos(int i, float t) {
			double base = Math.PI * 2 * i / 3;
			if (t < RISE) {
				float k = ease(t / RISE);
				double a = base + t * 0.05;
				return top.add(Math.cos(a) * 1.3 * k, 0.15 + k * (1.3 + i * 0.35), Math.sin(a) * 1.3 * k);
			}
			float k = easeInOut((t - RISE) / (ASSEMBLE - RISE));
			double tt = t - RISE;
			double a = base + RISE * 0.05 + tt * 0.07 + tt * tt * 0.0042;
			double radius = 1.3 * (1 - k);
			double h0 = 1.45 + i * 0.35;
			double h = h0 + (finalHeight[i] - h0) * k;
			return top.add(Math.cos(a) * radius, h, Math.sin(a) * radius);
		}
		Vec3d core() {
			return top.add(0, 2.25, 0);
		}
		@Override
		protected void tick() {
			Vec3d c = core();
			if (age < ASSEMBLE) {
				for (int i = 0; i < 3 + age / 10; i++) {
					Vec3d from = c.add(randomDir().multiply(r(3.0, 4.5)));
					PARTICLES.add(Particles.GLOW, from, 40).vel(randomDir().multiply(0.03)).attract(c, 0.035).drag(0.94F)
							.size(0.16F, 0.05F).color(1.0F, (float) r(0.8, 0.95), (float) r(0.45, 0.7));
				}
				if (age % 2 == 0) {
					double a = r(0, Math.PI * 2);
					PARTICLES.add(Particles.RUNES, top.add(Math.cos(a) * 1.6, 0.05, Math.sin(a) * 1.6), 28).rune()
							.vel(0, r(0.03, 0.07), 0).size(0.22F, 0.08F).color(1.0F, 0.82F, 0.4F);
				}
				for (int i = 0; i < 3; i++) {
					Vec3d s = shardPos(i, age);
					spark(s, randomDir().multiply(0.03), 0.6F, 0.85F, 1, 0.14F, 10);
				}
				if (age >= RISE && age % 2 == 0) {
					arcs = new java.util.ArrayList<>();
					for (int i = 0; i < 3; i++) {
						arcs.add(bolt(shardPos(i, age), shardPos((i + 1) % 3, age), 0.25, 3));
					}
					if (age > 45) {
						for (int i = 0; i < 3; i++) {
							arcs.add(bolt(shardPos(i, age), top.add(r(-0.4, 0.4), 0.05, r(-0.4, 0.4)), 0.3, 3));
						}
					}
				}
				if (age > 40 && age % 4 == 0) {
					FxManager.shake(top, 0.06F, 20);
				}
			}
			if (age == ASSEMBLE) {
				arcs = new java.util.ArrayList<>();
				FxManager.shake(top, 0.8F, 30);
				FxManager.flash(top, 0.6F, 28, 1.0F, 0.94F, 0.75F);
				for (int i = 0; i < 160; i++) {
					PARTICLES.add(Particles.GLOW, c, (int) r(20, 45)).vel(randomDir().multiply(r(0.15, 0.7))).drag(0.9F)
							.size(0.3F, 0.04F).color(1.0F, (float) r(0.78, 0.95), (float) r(0.4, 0.7));
				}
				for (int i = 0; i < 60; i++) {
					spark(c, randomDir().multiply(r(0.2, 0.8)), 1, 0.95F, 0.75F, 0.35F, (int) r(15, 30));
				}
				for (int i = 0; i < 30; i++) {
					PARTICLES.add(Particles.SHARD, c, (int) r(20, 35)).vel(randomDir().multiply(r(0.2, 0.6)))
							.gravity(0.02F).drag(0.94F).size(0.25F, 0.08F).color(0.7F, 0.88F, 1).spin((float) r(-0.5, 0.5));
				}
			}
			if (age > ASSEMBLE && age < duration - 4) {
				for (int i = 0; i < 2; i++) {
					mote(c.add(r(-0.3, 0.3), r(-1.2, 1.2), r(-0.3, 0.3)), new Vec3d(0, r(0.02, 0.06), 0), 1, 0.9F, 0.6F, 0.14F, 25);
				}
			}
			if (age == duration - 4) {
				FxManager.flash(top, 0.25F, 20, 1.0F, 0.95F, 0.8F);
				for (int i = 0; i < 50; i++) {
					mote(top.add(0, 0.8, 0), randomDir().multiply(r(0.05, 0.3)), 1, 0.9F, 0.6F, 0.25F, 25);
				}
			}
		}
		@Override
		public void render(Draw d) {
			float t = time(d);
			float fadeOut = 1 - FxManager.clamp01((t - (duration - 10)) / 10);
			float appear = FxManager.clamp01(t / 10);
			VertexConsumer circle = d.tex(MAGIC_CIRCLE);
			d.groundDisc(circle, top.add(0, 0.02, 0), 1.0 + 0.6 * appear, t * 0.03, 1.0F, 0.8F, 0.38F, appear * fadeOut);
			d.groundDisc(circle, top.add(0, -0.95, 0), 3.4 * appear, -t * 0.012, 1.0F, 0.85F, 0.5F, 0.8F * appear * fadeOut);
			d.groundRing(d.glow(), top.add(0, -0.93, 0), 3.4 * appear, 0.25, 1.0F, 0.8F, 0.4F, 0.6F * appear * fadeOut);
			if (t < ASSEMBLE) {
				float k = FxManager.clamp01((t - RISE) / (ASSEMBLE - RISE));
				for (int i = 0; i < 3; i++) {
					Vec3d s = shardPos(i, t);
					float spin = (1 - k) * t * 14 + i * 40;
					renderItem(d, states[i], shards[i], s, spin, 45, 1.35F);
					d.billboard(d.tex(Particles.GLOW), s, 0.8F, 0, 1.0F, 0.85F, 0.5F, 0.3F + 0.3F * k);
				}
				for (java.util.List<Vec3d> arc : arcs) {
					VertexConsumer vc = d.glow();
					float fl = (float) r(0.5, 1.0);
					d.ribbon(vc, arc, 0.12F, 1.0F, 0.8F, 0.4F, 0.35F * fl);
					d.ribbon(vc, arc, 0.03F, 1, 1, 1, 0.9F * fl);
				}
				if (t > 30) {
					float b = FxManager.clamp01((t - 30) / 40);
					d.beam(core(), core().add(0, 70, 0), 0.05F + 0.35F * b, 1.0F, 0.88F, 0.55F, b);
				}
			} else {
				float since = t - ASSEMBLE;
				float descend = easeInOut((t - (duration - 22)) / 18);
				Vec3d pos = core().add(0, -1.0 * descend + 0.08 * Math.sin(t * 0.15), 0);
				if (t < duration - 4) {
					float grow = ease(since / 6);
					renderItem(d, swordState, sword, pos, since * 3.5F, 0, 2.0F * grow);
					d.beam(pos.add(0, -0.9, 0), pos.add(0, 1.6, 0), 0.22F, 1.0F, 0.85F, 0.5F, 0.4F * fadeOut);
					d.billboard(d.tex(Particles.GLOW), pos.add(0, -0.15, 0), 1.3F, 0, 1.0F, 0.9F, 0.6F, 0.35F * fadeOut);
				}
				float strike = 1 - FxManager.clamp01(since / 25);
				if (strike > 0) {
					d.beam(core(), core().add(0, 120, 0), 1.8F * strike + 0.1F, 1.0F, 0.9F, 0.6F, strike);
					d.sphere(d.glow(), core(), 2.6 * ease(since / 10), 1, 0.9F, 0.65F, strike * 0.7F, 1.0F);
				}
				VertexConsumer glow = d.glow();
				for (int i = 0; i < 2; i++) {
					float w = FxManager.clamp01((since - i * 3) / 14);
					if (w > 0 && w < 1) {
						d.groundRing(glow, top.add(0, -0.9, 0), w * 7, 0.5 * (1 - w) + 0.1, 1, 0.88F, 0.55F, 1 - w);
					}
				}
			}
		}
	}
	private Effects() {
	}
}
