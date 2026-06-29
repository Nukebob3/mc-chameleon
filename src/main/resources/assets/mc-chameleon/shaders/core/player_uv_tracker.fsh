#version 330

in vec2 texCoord0;
out vec4 fragColor;

void main() {
    fragColor = vec4(floor(texCoord0.x*64.0)/64.0, floor(texCoord0.y*64.0)/64.0, 0.0, 1.0);
}