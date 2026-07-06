#version 330

layout(std140) uniform PhotonAltBG {
    vec4 uResTime;
    vec4 uMouse;
};

in vec2 vUV;

out vec4 fragColor;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    mat2 m = mat2(1.6, 1.2, -1.2, 1.6);
    for (int i = 0; i < 6; i++) {
        v += a * noise(p);
        p = m * p;
        a *= 0.5;
    }
    return v;
}

void main() {
    float t = uResTime.z;
    float aspect = uResTime.x / max(uResTime.y, 1.0);
    vec2 uv = vUV;
    vec2 mo = uMouse.xy;

    vec2 p = (uv - 0.5) * vec2(aspect, 1.0) * 2.6;
    vec2 md = (mo - 0.5) * vec2(aspect, 1.0);

    // domain warp, слегка ведомый мышью
    vec2 q = vec2(fbm(p + md * 0.5 + vec2(0.0, t * 0.05)),
                  fbm(p - md * 0.5 + vec2(5.2, -t * 0.04)));
    vec2 r = vec2(fbm(p + 3.0 * q + vec2(1.7, 9.2) + t * 0.06),
                  fbm(p + 3.0 * q + vec2(8.3, 2.8) - t * 0.05));
    float f = fbm(p + 4.0 * r + md * 0.35);

    // чёрная база + тёмно-синие волокна
    vec3 col = vec3(0.012, 0.016, 0.024);
    col += vec3(0.05, 0.08, 0.15) * f * f;

    // тонкие неоновые штрихи
    float streak = sin((p.x + p.y) * 3.0 + f * 8.0 - t * 0.6);
    col += vec3(0.06, 0.12, 0.24) * smoothstep(0.9, 1.0, streak) * 0.5;

    // свечение, следящее за курсором
    float gd = length((uv - mo) * vec2(aspect, 1.0));
    col += vec3(0.10, 0.20, 0.38) * smoothstep(0.55, 0.0, gd) * (0.55 + 0.5 * f);

    // виньетка
    vec2 vc = uv - 0.5;
    col *= clamp(1.0 - dot(vc, vc) * 1.25, 0.0, 1.0);

    col = pow(col, vec3(0.95));
    fragColor = vec4(col, uMouse.z);
}
