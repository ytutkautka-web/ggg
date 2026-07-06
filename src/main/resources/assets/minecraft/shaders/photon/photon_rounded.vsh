#version 330

layout(std140) uniform Projection {
    mat4 ProjMat;
};

layout(std140) uniform PhotonShape {
    vec4 uRect;      // x, y, w, h  (реальные пиксели)
    vec4 uRadius;    // радиусы углов
    vec4 uColor;     // заливка rgba
    vec4 uOutline;   // обводка rgba
    vec4 uParams;    // x=thickness, y=softness
};

in vec3 Position;

out vec2 vLocal;

void main() {
    vLocal = Position.xy * uRect.zw;
    vec2 pos = uRect.xy + vLocal;
    gl_Position = ProjMat * vec4(pos, 0.0, 1.0);
}
