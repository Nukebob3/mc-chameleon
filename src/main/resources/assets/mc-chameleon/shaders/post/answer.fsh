#version 330

layout(std140) uniform Globals {
    ivec3 CameraBlockPos;
    vec3 CameraOffset;
    vec2 ScreenSize;
    float GlintAlpha;
    float GameTime;
    int MenuBlurRadius;
    int UseRgss;
};

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

void main(){
    float pulse =0.8*sin(GameTime * 10000.0)+0.2;
    if  (pulse<0) pulse = 0;

    vec4 color = texture(InSampler, texCoord);
    color.a *= pulse;
    fragColor = color;
}