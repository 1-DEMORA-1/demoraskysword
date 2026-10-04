package dev.demora.mixin.client;

import dev.demora.client.fx.FxManager;
import dev.demora.client.fx.PostFx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Pool;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	@Shadow
	@Final
	private Pool pool;
	@Shadow
	@Final
	private MinecraftClient client;
	@Inject(method = "tiltViewWhenHurt", at = @At("HEAD"))
	private void demora$shake(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
		float[] a = FxManager.shakeAngles(tickDelta);
		if (a[0] != 0 || a[1] != 0 || a[2] != 0) {
			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(a[2]));
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(a[1]));
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(a[0]));
		}
	}

	@Inject(method = "render", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/render/WorldRenderer;drawEntityOutlinesFramebuffer()V", shift = At.Shift.AFTER))
	private void demora$post(RenderTickCounter counter, boolean tick, CallbackInfo ci) {
		PostFx.apply(client, pool);
	}
}
