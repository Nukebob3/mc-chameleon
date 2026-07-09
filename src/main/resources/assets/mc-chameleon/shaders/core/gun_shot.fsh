#version 330

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

vec3 getBeamColour(float progress) {
    vec3 pink   = vec3(1.0, 0.4, 0.7);
    vec3 yellow = vec3(1.0, 1.0, 0.2);
    vec3 green  = vec3(0.2, 1.0, 0.4);
    vec3 blue   = vec3(0.2, 0.5, 1.0);

    float p = progress * 4.0;

    if (p < 1.0) {
        return mix(pink, green, p);
    } else if (p < 2.0) {
        return mix(green, yellow, p - 1.0);
    } else if (p < 3.0) {
        return mix(yellow, blue, p - 2.0);
    } else {
        return mix(blue, pink, p - 3.0);
    }
}

void main() {
    float age = vertexColor.r;

    float positionAlongBeam = mod(texCoord0.y, 1.0);
    vec4 colour = vec4(getBeamColour(positionAlongBeam), 1.0);

    if (age<0.5) {
        colour = colour*(2*age)+vec4(1)*(1-2*age);
    } else {
        colour.a = 2-2*age;
    }

    fragColor = colour;
}