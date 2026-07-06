#version 330

layout(std140) uniform Projection {
    mat4 ProjMat;
};

layout(std140) uniform PhotonAltBG {
    vec4 uResTime;
    vec4 uMouse;
};

in vec3 Position;

out vec2 vUV;

void main() {
    vUV = Position.xy;
    vec2 pos = Position.xy * uResTime.xy;
    gl_Position = ProjMat * vec4(pos, 0.0, 1.0);
}
