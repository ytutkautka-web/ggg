#version 330

layout(std140) uniform PhotonBG {
    vec4 uResTime;
    vec4 uAccent;
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

vec3 palette(float t) {
    vec3 a = vec3(0.10, 0.12, 0.22);
    vec3 b = vec3(0.35, 0.30, 0.55);
    vec3 c = vec3(1.0, 1.0, 1.0);
    vec3 d = vec3(0.30, 0.55, 0.85);
    return a + b * cos(6.28318 * (c * t + d));
}

void main() {
    float t = uResTime.z;
    float aspect = uResTime.x / max(uResTime.y, 1.0);
    vec3 acc = uAccent.rgb;
    vec2 uv = vUV;
    vec2 p = (uv - 0.5) * vec2(aspect, 1.0) * 2.4;

    // domain warp
    vec2 q = vec2(fbm(p + vec2(0.0, t * 0.08)),
                  fbm(p + vec2(5.2, -t * 0.06)));
    vec2 r = vec2(fbm(p + 3.0 * q + vec2(1.7, 9.2) + t * 0.10),
                  fbm(p + 3.0 * q + vec2(8.3, 2.8) - t * 0.07));
    float f = fbm(p + 4.0 * r);

    vec3 col = palette(f + t * 0.03);
    col = mix(col, vec3(0.02, 0.03, 0.07), smoothstep(0.55, 1.1, length(q)));
    col *= 0.55 + 0.85 * f;
    col = mix(col, acc, 0.16);

    // neon streaks
    float streak = sin((p.x + p.y) * 3.0 + f * 8.0 - t * 0.8);
    col += acc * smoothstep(0.85, 1.0, streak) * 0.6;

    // central glow
    float glow = smoothstep(1.0, 0.0, length((uv - vec2(0.5, 0.42)) * vec2(aspect, 1.0)));
    col += acc * glow * 0.5;

    // vignette
    vec2 vc = uv - 0.5;
    col *= clamp(1.0 - dot(vc, vc) * 1.1, 0.0, 1.0);

    col = pow(col, vec3(0.92));
    fragColor = vec4(col, 1.0);
}
