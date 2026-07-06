#version 330

layout(std140) uniform PhotonShape {
    vec4 uRect;      // x, y, w, h  (реальные пиксели)
    vec4 uRadius;    // радиусы углов (tl, tr, br, bl)
    vec4 uColor;     // заливка rgba
    vec4 uOutline;   // обводка rgba
    vec4 uParams;    // x=thickness, y=softness
};

in vec2 vLocal;

out vec4 fragColor;

// SDF прямоугольника с разными радиусами углов
float sdRoundBox(vec2 p, vec2 b, vec4 r) {
    r.xy = (p.x > 0.0) ? r.xy : r.zw;
    r.x  = (p.y > 0.0) ? r.x  : r.y;
    vec2 q = abs(p) - b + r.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;
}

void main() {
    vec2 size = uRect.zw;
    vec2 p = vLocal - size * 0.5;

    float dist = sdRoundBox(p, size * 0.5, uRadius);

    float softness  = max(uParams.y, 0.0001);
    float thickness = uParams.x;

    // внешняя граница фигуры со сглаживанием
    float outerAlpha = 1.0 - smoothstep(0.0, softness, dist);

    vec4 col;
    if (thickness > 0.0) {
        // только обводка: кольцо строго ВНУТРИ фигуры (не вылезает за скругление)
        float ring = smoothstep(-thickness, -thickness + softness, dist);
        col = vec4(uOutline.rgb, uOutline.a * ring * outerAlpha);
    } else {
        col = vec4(uColor.rgb, uColor.a * outerAlpha);
    }

    if (col.a <= 0.0) discard;
    fragColor = col;
}
