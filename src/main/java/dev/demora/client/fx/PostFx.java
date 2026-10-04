package dev.demora.client.fx;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.demora.DemoraSkySword;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.util.Identifier;
public final class PostFx {
	private static final Identifier ID = DemoraSkySword.id("flash");
	private static boolean failed;
	public static void apply(MinecraftClient client, ObjectAllocator pool) {
		float amount = FxManager.flash();
		if (failed || client.world == null || amount < 0.01F) {
			return;
		}
		PostEffectProcessor fx;
		try {
			fx = client.getShaderLoader().loadPostEffect(ID, DefaultFramebufferSet.MAIN_ONLY);
		} catch (RuntimeException e) {
			failed = true;
			return;
		}
		if (fx == null) {
			return;
		}
		float[] c = FxManager.flashColor();
		fx.setUniforms("Flash", amount);
		fx.setUniforms("FlashR", c[0]);
		fx.setUniforms("FlashG", c[1]);
		fx.setUniforms("FlashB", c[2]);
		RenderSystem.disableBlend();
		RenderSystem.disableDepthTest();
		RenderSystem.resetTextureMatrix();
		fx.render(client.getFramebuffer(), pool);
		client.getFramebuffer().beginWrite(true);
	}

	private PostFx() {
	}
}
