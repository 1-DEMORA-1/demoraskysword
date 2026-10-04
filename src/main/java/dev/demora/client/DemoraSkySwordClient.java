package dev.demora.client;

import dev.demora.client.fx.FxManager;
import dev.demora.net.FxPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
public class DemoraSkySwordClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(FxPayload.ID, (payload, context) -> FxManager.handle(payload));
		ClientTickEvents.END_CLIENT_TICK.register(client -> FxManager.tick());
		WorldRenderEvents.AFTER_TRANSLUCENT.register(ctx -> FxManager.render(ctx.camera(), ctx.tickCounter().getTickDelta(false)));
	}
}
