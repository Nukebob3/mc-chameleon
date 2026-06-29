#version 330

#define MINECRAFT_LIGHT_POWER   (1)
#define MINECRAFT_AMBIENT_LIGHT (1)

layout(std140) uniform Lighting {
    vec3 Light0_Direction;
    vec3 Light1_Direction;
};


vec2 minecraft_compute_light(vec3 lightDir0, vec3 lightDir1, vec3 normal) {
    return vec2(1.0, 1.0);
}

vec4 minecraft_mix_light_separate(vec2 light, vec4 color) {

    float lightAccum = 1.0;
    return vec4(color.rgb * lightAccum, color.a);
}

vec4 minecraft_mix_light(vec3 lightDir0, vec3 lightDir1, vec3 normal, vec4 color) {
    vec2 light = minecraft_compute_light(lightDir0, lightDir1, normal);
    return minecraft_mix_light_separate(light, color);
}