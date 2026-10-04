package dev.demora.client.fx;

import dev.demora.mixin.client.RenderLayerInvoker;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import java.util.HashMap;
import java.util.Map;
public abstract class FxLayers extends RenderLayer {
	private static final Map<Identifier, RenderLayer> TEXTURED = new HashMap<>();
	public static final RenderLayer GLOW = RenderLayerInvoker.demora$of("demora_glow",
			VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS, 1 << 16,
			MultiPhaseParameters.builder()
					.program(POSITION_COLOR_PROGRAM)
					.transparency(LIGHTNING_TRANSPARENCY)
					.writeMaskState(COLOR_MASK)
					.cull(DISABLE_CULLING)
					.depthTest(LEQUAL_DEPTH_TEST)
					.build(false));
	public static final RenderLayer DARK = RenderLayerInvoker.demora$of("demora_dark",
			VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS, 1 << 14,
			MultiPhaseParameters.builder()
					.program(POSITION_COLOR_PROGRAM)
					.transparency(TRANSLUCENT_TRANSPARENCY)
					.writeMaskState(ALL_MASK)
					.cull(DISABLE_CULLING)
					.depthTest(LEQUAL_DEPTH_TEST)
					.build(false));
	private static final Map<Identifier, RenderLayer> SOLID = new HashMap<>();
	public static RenderLayer texSolid(Identifier texture) {
		return SOLID.computeIfAbsent(texture, t -> RenderLayerInvoker.demora$of("demora_tex_solid",
				VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS, 1 << 16,
				MultiPhaseParameters.builder()
						.program(POSITION_TEXTURE_COLOR_PROGRAM)
						.texture(new Texture(t, TriState.FALSE, false))
						.transparency(TRANSLUCENT_TRANSPARENCY)
						.writeMaskState(ALL_MASK)
						.cull(DISABLE_CULLING)
						.depthTest(LEQUAL_DEPTH_TEST)
						.build(false)));
	}
	public static RenderLayer glowTex(Identifier texture) {
		return TEXTURED.computeIfAbsent(texture, t -> RenderLayerInvoker.demora$of("demora_glow_tex",
				VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS, 1 << 16,
				MultiPhaseParameters.builder()
						.program(POSITION_TEXTURE_COLOR_PROGRAM)
						.texture(new Texture(t, TriState.TRUE, false))
						.transparency(LIGHTNING_TRANSPARENCY)
						.writeMaskState(COLOR_MASK)
						.cull(DISABLE_CULLING)
						.depthTest(LEQUAL_DEPTH_TEST)
						.build(false)));
	}
	private FxLayers() {
		super(null, null, null, 0, false, false, null, null);
	}
}
