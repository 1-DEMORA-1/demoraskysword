package dev.demora.net;

import dev.demora.DemoraSkySword;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.Vec3d;
public record FxPayload(int type, Vec3d pos, Vec3d vec, float[] f, int[] ids) implements CustomPayload {
	public static final Id<FxPayload> ID = new Id<>(DemoraSkySword.id("fx"));
	public static final PacketCodec<RegistryByteBuf, FxPayload> CODEC = CustomPayload.codecOf(FxPayload::write, FxPayload::read);
	private static FxPayload read(RegistryByteBuf buf) {
		int type = buf.readVarInt();
		Vec3d pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		Vec3d vec = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		float[] f = new float[buf.readVarInt()];
		for (int i = 0; i < f.length; i++) {
			f[i] = buf.readFloat();
		}
		int[] ids = new int[buf.readVarInt()];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = buf.readVarInt();
		}
		return new FxPayload(type, pos, vec, f, ids);
	}
	private void write(RegistryByteBuf buf) {
		buf.writeVarInt(type);
		buf.writeDouble(pos.x).writeDouble(pos.y).writeDouble(pos.z);
		buf.writeDouble(vec.x).writeDouble(vec.y).writeDouble(vec.z);
		buf.writeVarInt(f.length);
		for (float v : f) {
			buf.writeFloat(v);
		}
		buf.writeVarInt(ids.length);
		for (int id : ids) {
			buf.writeVarInt(id);
		}
	}

	public float f(int i, float def) {
		return i < f.length ? f[i] : def;
	}
	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
