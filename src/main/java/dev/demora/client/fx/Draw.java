package dev.demora.client.fx;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.List;
public final class Draw {
	public final VertexConsumerProvider.Immediate buffers;
	public final Vec3d cam;
	public final Vector3f right;
	public final Vector3f up;
	public final float tickDelta;
	private final Matrix4f mat = new Matrix4f();
	public Draw(VertexConsumerProvider.Immediate buffers, Camera camera, float tickDelta) {
		this.buffers = buffers;
		this.cam = camera.getPos();
		Quaternionf rot = camera.getRotation();
		this.right = rot.transform(new Vector3f(1, 0, 0));
		this.up = rot.transform(new Vector3f(0, 1, 0));
		this.tickDelta = tickDelta;
	}

	public VertexConsumer glow() {
		return buffers.getBuffer(FxLayers.GLOW);
	}
	public VertexConsumer dark() {
		return buffers.getBuffer(FxLayers.DARK);
	}
	public VertexConsumer tex(Identifier id) {
		return buffers.getBuffer(FxLayers.glowTex(id));
	}
	public void v(VertexConsumer vc, double x, double y, double z, float r, float g, float b, float a) {
		vc.vertex(mat, (float) (x - cam.x), (float) (y - cam.y), (float) (z - cam.z)).color(r, g, b, a);
	}
	public void v(VertexConsumer vc, Vec3d p, float r, float g, float b, float a) {
		v(vc, p.x, p.y, p.z, r, g, b, a);
	}
	public void vt(VertexConsumer vc, double x, double y, double z, float u, float t, float r, float g, float b, float a) {
		vc.vertex(mat, (float) (x - cam.x), (float) (y - cam.y), (float) (z - cam.z)).texture(u, t).color(r, g, b, a);
	}
	public void billboard(VertexConsumer vc, Vec3d p, float size, float angle, float r, float g, float b, float a,
						  float u0, float v0, float u1, float v1) {
		float c = (float) Math.cos(angle) * size;
		float s = (float) Math.sin(angle) * size;
		float ax = right.x * c + up.x * s, ay = right.y * c + up.y * s, az = right.z * c + up.z * s;
		float bx = -right.x * s + up.x * c, by = -right.y * s + up.y * c, bz = -right.z * s + up.z * c;
		vt(vc, p.x - ax - bx, p.y - ay - by, p.z - az - bz, u0, v1, r, g, b, a);
		vt(vc, p.x + ax - bx, p.y + ay - by, p.z + az - bz, u1, v1, r, g, b, a);
		vt(vc, p.x + ax + bx, p.y + ay + by, p.z + az + bz, u1, v0, r, g, b, a);
		vt(vc, p.x - ax + bx, p.y - ay + by, p.z - az + bz, u0, v0, r, g, b, a);
	}
	public void billboard(VertexConsumer vc, Vec3d p, float size, float angle, float r, float g, float b, float a) {
		billboard(vc, p, size, angle, r, g, b, a, 0, 0, 1, 1);
	}

	public void disc(VertexConsumer vc, Vec3d c, Vec3d ax, Vec3d bx, double radius, double angle,
					 float r, float g, float b, float a) {
		double cs = Math.cos(angle), sn = Math.sin(angle);
		Vec3d a1 = ax.multiply(cs).add(bx.multiply(sn)).multiply(radius);
		Vec3d b1 = ax.multiply(-sn).add(bx.multiply(cs)).multiply(radius);
		Vec3d p0 = c.subtract(a1).subtract(b1), p1 = c.add(a1).subtract(b1), p2 = c.add(a1).add(b1), p3 = c.subtract(a1).add(b1);
		vt(vc, p0.x, p0.y, p0.z, 0, 1, r, g, b, a);
		vt(vc, p1.x, p1.y, p1.z, 1, 1, r, g, b, a);
		vt(vc, p2.x, p2.y, p2.z, 1, 0, r, g, b, a);
		vt(vc, p3.x, p3.y, p3.z, 0, 0, r, g, b, a);
	}
	public void groundDisc(VertexConsumer vc, Vec3d c, double radius, double angle, float r, float g, float b, float a) {
		disc(vc, c, new Vec3d(1, 0, 0), new Vec3d(0, 0, 1), radius, angle, r, g, b, a);
	}
	public void ring(VertexConsumer vc, Vec3d c, Vec3d ax, Vec3d bx, double radius, double width, int segments,
					 float r, float g, float b, float a) {
		double rin = Math.max(0, radius - width), rout = radius + width;
		for (int i = 0; i < segments; i++) {
			double t0 = Math.PI * 2 * i / segments, t1 = Math.PI * 2 * (i + 1) / segments;
			Vec3d d0 = ax.multiply(Math.cos(t0)).add(bx.multiply(Math.sin(t0)));
			Vec3d d1 = ax.multiply(Math.cos(t1)).add(bx.multiply(Math.sin(t1)));
			Vec3d i0 = c.add(d0.multiply(rin)), i1 = c.add(d1.multiply(rin));
			Vec3d m0 = c.add(d0.multiply(radius)), m1 = c.add(d1.multiply(radius));
			Vec3d o0 = c.add(d0.multiply(rout)), o1 = c.add(d1.multiply(rout));
			v(vc, i0, r, g, b, 0);
			v(vc, i1, r, g, b, 0);
			v(vc, m1, r, g, b, a);
			v(vc, m0, r, g, b, a);
			v(vc, m0, r, g, b, a);
			v(vc, m1, r, g, b, a);
			v(vc, o1, r, g, b, 0);
			v(vc, o0, r, g, b, 0);
		}
	}
	public void groundRing(VertexConsumer vc, Vec3d c, double radius, double width, float r, float g, float b, float a) {
		ring(vc, c, new Vec3d(1, 0, 0), new Vec3d(0, 0, 1), radius, width, 72, r, g, b, a);
	}
	public void faceRing(VertexConsumer vc, Vec3d c, double radius, double width, float r, float g, float b, float a) {
		ring(vc, c, new Vec3d(right.x, right.y, right.z), new Vec3d(up.x, up.y, up.z), radius, width, 72, r, g, b, a);
	}

	public void ribbon(VertexConsumer vc, List<Vec3d> pts, float width, float r, float g, float b, float a) {
		for (int i = 0; i + 1 < pts.size(); i++) {
			Vec3d p0 = pts.get(i), p1 = pts.get(i + 1);
			Vec3d dir = p1.subtract(p0);
			Vec3d mid = p0.add(p1).multiply(0.5);
			Vec3d side = dir.crossProduct(cam.subtract(mid));
			if (side.lengthSquared() < 1.0E-9) {
				continue;
			}
			side = side.normalize().multiply(width);
			v(vc, p0, r, g, b, a);
			v(vc, p1, r, g, b, a);
			v(vc, p1.add(side), r, g, b, 0);
			v(vc, p0.add(side), r, g, b, 0);
			v(vc, p0, r, g, b, a);
			v(vc, p1, r, g, b, a);
			v(vc, p1.subtract(side), r, g, b, 0);
			v(vc, p0.subtract(side), r, g, b, 0);
		}
	}
	public void beam(Vec3d from, Vec3d to, float width, float r, float g, float b, float a) {
		VertexConsumer vc = glow();
		List<Vec3d> pts = List.of(from, to);
		ribbon(vc, pts, width, r, g, b, a * 0.55F);
		ribbon(vc, pts, width * 0.3F, Math.min(1, r + 0.5F), Math.min(1, g + 0.5F), Math.min(1, b + 0.5F), a);
	}
	public void ev(VertexConsumer vc, double x, double y, double z, float u, float t, float r, float g, float b, float a,
				   int light, float nx, float ny, float nz) {
		vc.vertex(mat, (float) (x - cam.x), (float) (y - cam.y), (float) (z - cam.z)).color(r, g, b, a).texture(u, t)
				.overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(light).normal(nx, ny, nz);
	}
	private static final Vector3f SUN = new Vector3f(-0.45F, 0.75F, 0.5F).normalize();
	private static float shade(float nx, float ny, float nz, float bright) {
		float l = Math.max(0, nx * SUN.x + ny * SUN.y + nz * SUN.z);
		return Math.min(1.0F, (0.28F + 0.85F * l) * bright);
	}
	public void texSphere(VertexConsumer vc, Vec3d c, double radius, double rotY, float bright) {
		int lat = 24, lon = 48;
		for (int i = 0; i < lat; i++) {
			for (int j = 0; j < lon; j++) {
				double[][] q = {{i, j}, {i, j + 1}, {i + 1, j + 1}, {i + 1, j}};
				for (double[] k : q) {
					double theta = Math.PI * k[0] / lat;
					double phi = Math.PI * 2 * k[1] / lon + rotY;
					float nx = (float) (Math.sin(theta) * Math.cos(phi)), ny = (float) Math.cos(theta);
					float nz = (float) (Math.sin(theta) * Math.sin(phi));
					float sh = shade(nx, ny, nz, bright);
					vt(vc, c.x + nx * radius, c.y + ny * radius, c.z + nz * radius, (float) (k[1] / lon), (float) (k[0] / lat),
							sh, sh, Math.min(1.0F, sh * 1.04F), 1);
				}
			}
		}
	}
	public void texCube(VertexConsumer vc, Vec3d c, double half, Quaternionf rot, float u0, float v0, float u1, float v1,
						float bright) {
		float[][] faces = {{0, 0, 1}, {0, 0, -1}, {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}};
		float[][] corners = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
		for (float[] n : faces) {
			Vector3f nv = new Vector3f(n[0], n[1], n[2]);
			Vector3f a = Math.abs(n[1]) > 0.5F ? new Vector3f(1, 0, 0) : new Vector3f(0, 1, 0);
			Vector3f b = new Vector3f(nv).cross(a);
			Vector3f wn = rot.transform(new Vector3f(nv));
			float sh = shade(wn.x, wn.y, wn.z, bright);
			for (float[] k : corners) {
				Vector3f p = new Vector3f(nv).add(new Vector3f(a).mul(k[0])).add(new Vector3f(b).mul(k[1])).mul((float) half);
				rot.transform(p);
				vt(vc, c.x + p.x, c.y + p.y, c.z + p.z, k[0] < 0 ? u0 : u1, k[1] < 0 ? v0 : v1, sh, sh, sh, 1);
			}
		}
	}

	public void darkDisc(VertexConsumer vc, Vec3d c, double radius, float aCenter, float aEdge) {
		int seg = 48;
		for (int i = 0; i < seg; i++) {
			double t0 = Math.PI * 2 * i / seg, t1 = Math.PI * 2 * (i + 1) / seg;
			v(vc, c, 0, 0, 0, aCenter);
			v(vc, c, 0, 0, 0, aCenter);
			v(vc, c.x + Math.cos(t1) * radius, c.y, c.z + Math.sin(t1) * radius, 0, 0, 0, aEdge);
			v(vc, c.x + Math.cos(t0) * radius, c.y, c.z + Math.sin(t0) * radius, 0, 0, 0, aEdge);
		}
	}
	public void sphere(VertexConsumer vc, Vec3d c, double radius, float r, float g, float b, float a, float fresnel) {
		int lat = 18, lon = 32;
		for (int i = 0; i < lat; i++) {
			double t0 = Math.PI * i / lat, t1 = Math.PI * (i + 1) / lat;
			for (int j = 0; j < lon; j++) {
				double p0 = Math.PI * 2 * j / lon, p1 = Math.PI * 2 * (j + 1) / lon;
				sv(vc, c, radius, t0, p0, r, g, b, a, fresnel);
				sv(vc, c, radius, t0, p1, r, g, b, a, fresnel);
				sv(vc, c, radius, t1, p1, r, g, b, a, fresnel);
				sv(vc, c, radius, t1, p0, r, g, b, a, fresnel);
			}
		}
	}
	private void sv(VertexConsumer vc, Vec3d c, double radius, double theta, double phi,
					float r, float g, float b, float a, float fresnel) {
		Vec3d n = new Vec3d(Math.sin(theta) * Math.cos(phi), Math.cos(theta), Math.sin(theta) * Math.sin(phi));
		Vec3d p = c.add(n.multiply(radius));
		float alpha = a;
		if (fresnel > 0) {
			Vec3d view = cam.subtract(p);
			double len = view.length();
			double d = len < 1.0E-4 ? 1 : Math.abs(n.dotProduct(view) / len);
			alpha = (float) (a * ((1 - fresnel) + fresnel * Math.pow(1 - d, 2.5)));
		}
		v(vc, p, r, g, b, alpha);
	}
}
