#version 150

uniform sampler2D InSampler;
uniform float Flash;
uniform float FlashR;
uniform float FlashG;
uniform float FlashB;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec3 col = texture(InSampler, texCoord).rgb;
    fragColor = vec4(mix(col, vec3(FlashR, FlashG, FlashB), Flash), 1.0);
}
