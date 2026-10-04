package dev.demora.net;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
public final class Fx {
	public static final int SLASH = 1;
	public static final int CHARGE = 2;
	public static final int STRIKE = 3;
	public static final int SPARK = 4;
	public static final int RITUAL = 5;
	public static void send(ServerWorld world, int type, Vec3d pos, Vec3d vec, float[] f, int[] ids, double range) {
		FxPayload payload = new FxPayload(type, pos, vec, f, ids);
		for (ServerPlayerEntity p : PlayerLookup.around(world, pos, range)) {
			ServerPlayNetworking.send(p, payload);
		}
	}

	private Fx() {
	}
}
