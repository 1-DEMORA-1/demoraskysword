package dev.demora.client.fx;

import dev.demora.DemoraSkySword;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
public final class Particles {
	public static final Identifier GLOW = DemoraSkySword.id("textures/fx/glow.png");
	public static final Identifier STAR = DemoraSkySword.id("textures/fx/star.png");
	public static final Identifier SHARD = DemoraSkySword.id("textures/fx/shard.png");
	public static final Identifier RUNES = DemoraSkySword.id("textures/fx/runes.png");
	public static final Identifier SMOKE = DemoraSkySword.id("textures/fx/smoke.png");
	private static final Identifier[] TEXTURES = {SMOKE, GLOW, STAR, SHARD, RUNES};
	private static final int MAX = 8000;
	public static final class P {
		double x, y, z, px, py, pz, vx, vy, vz;
		float drag = 0.92F, gravity, s0, s1, r, g, b, a = 1, rot, spin;
		int age, life;
		int frame = -1;
		Identifier tex;
		Vec3d attractor;
		double pull;
		boolean frozen;
		public P vel(double x, double y, double z) {
			vx = x;
			vy = y;
			vz = z;
			return this;
		}
		public P vel(Vec3d v) {
			return vel(v.x, v.y, v.z);
		}
		public P drag(float d) {
			drag = d;
			return this;
		}
		public P gravity(float gr) {
			gravity = gr;
			return this;
		}
		public P size(float from, float to) {
			s0 = from;
			s1 = to;
			return this;
		}

		public P color(float r, float g, float b) {
			this.r = r;
			this.g = g;
			this.b = b;
			return this;
		}
		public P alpha(float a) {
			this.a = a;
			return this;
		}
		public P spin(float s) {
			spin = s;
			rot = ThreadLocalRandom.current().nextFloat() * 6.283F;
			return this;
		}












		public P rune() {
			frame = ThreadLocalRandom.current().nextInt(16);
			return this;
		}
		public P attract(Vec3d target, double strength) {
			attractor = target;
			pull = strength;
			return this;
		}
		public P frozen() {
			frozen = true;
			return this;
		}
	}
	private final List<P> list = new ArrayList<>();
	public P add(Identifier tex, Vec3d pos, int life) {
		P p = new P();
		p.tex = tex;
		p.x = p.px = pos.x;
		p.y = p.py = pos.y;
		p.z = p.pz = pos.z;
		p.life = Math.max(1, life);
		p.s0 = p.s1 = 0.2F;
		p.r = p.g = p.b = 1;
		if (list.size() < MAX) {
			list.add(p);
		}
		return p;
	}
	public void clear() {
		list.clear();
	}
	public void tick(boolean timeFrozen) {
		Iterator<P> it = list.iterator();
		while (it.hasNext()) {
			P p = it.next();
			p.px = p.x;
			p.py = p.y;
			p.pz = p.z;
			if (p.frozen && timeFrozen) {
				continue;
			}
			if (p.attractor != null) {
				double dx = p.attractor.x - p.x, dy = p.attractor.y - p.y, dz = p.attractor.z - p.z;
				double d = Math.sqrt(dx * dx + dy * dy + dz * dz) + 1.0E-4;
				double k = p.pull / Math.max(0.6, d);
				p.vx += dx / d * k;
				p.vy += dy / d * k;
				p.vz += dz / d * k;
				if (d < 0.8) {
					p.age = p.life;
				}
			}
			p.vx *= p.drag;
			p.vy = p.vy * p.drag - p.gravity;
			p.vz *= p.drag;
			p.x += p.vx;
			p.y += p.vy;
			p.z += p.vz;
			p.rot += p.spin;
			if (++p.age >= p.life) {
				it.remove();
			}
		}
	}
	public void render(Draw d) {
		for (Identifier tex : TEXTURES) {
			VertexConsumer vc = null;
			for (P p : list) {
				if (p.tex != tex) {
					continue;
				}
				if (vc == null) {
					vc = d.tex(tex);
				}
				float t = Math.min(1, (p.age + (p.frozen ? 0 : d.tickDelta)) / p.life);
				float fade = t < 0.12F ? t / 0.12F : t > 0.55F ? 1 - (t - 0.55F) / 0.45F : 1;
				float size = MathHelper.lerp(t, p.s0, p.s1);
				float td = p.frozen ? 0 : d.tickDelta;
				Vec3d pos = new Vec3d(MathHelper.lerp(td, p.px, p.x), MathHelper.lerp(td, p.py, p.y), MathHelper.lerp(td, p.pz, p.z));
				float a = p.a * fade;
				if (p.frame >= 0) {
					float u0 = (p.frame % 4) / 4F, v0 = (p.frame / 4) / 4F;
					d.billboard(vc, pos, size, p.rot, p.r, p.g, p.b, a, u0, v0, u0 + 0.25F, v0 + 0.25F);
				} else {
					d.billboard(vc, pos, size, p.rot + p.spin * td, p.r, p.g, p.b, a);
				}
			}
		}
	}
}
