package fun.photon.utils.render;

import com.mojang.blaze3d.shaders.ShaderType;
import net.minecraft.resources.Identifier;

public final class PhotonShaders {

    private PhotonShaders() {}

    private static final String ROUNDED_VSH = """
            #version 330

            layout(std140) uniform Projection {
                mat4 ProjMat;
            };

            layout(std140) uniform PhotonShape {
                vec4 uRect;
                vec4 uRadius;
                vec4 uColor;
                vec4 uOutline;
                vec4 uParams;
            };

            in vec3 Position;

            out vec2 vLocal;

            void main() {
                vLocal = Position.xy * uRect.zw;
                vec2 pos = uRect.xy + vLocal;
                gl_Position = ProjMat * vec4(pos, 0.0, 1.0);
            }
            """;

    private static final String ROUNDED_FSH = """
            #version 330

            layout(std140) uniform PhotonShape {
                vec4 uRect;
                vec4 uRadius;
                vec4 uColor;
                vec4 uOutline;
                vec4 uParams;
            };

            in vec2 vLocal;

            out vec4 fragColor;

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

                float outerAlpha = 1.0 - smoothstep(0.0, softness, dist);

                vec4 col;
                if (thickness > 0.0) {
                    float ring = smoothstep(-thickness, -thickness + softness, dist);
                    col = vec4(uOutline.rgb, uOutline.a * ring * outerAlpha);
                } else {
                    col = vec4(uColor.rgb, uColor.a * outerAlpha);
                }

                if (col.a <= 0.0) discard;
                fragColor = col;
            }
            """;

    private static final String TEXT_VSH = """
            #version 330

            layout(std140) uniform Projection {
                mat4 ProjMat;
            };

            in vec3 Position;
            in vec2 UV0;
            in vec4 Color;

            out vec2 texCoord0;
            out vec4 vertexColor;

            void main() {
                gl_Position = ProjMat * vec4(Position, 1.0);
                texCoord0 = UV0;
                vertexColor = Color;
            }
            """;

    private static final String TEXT_FSH = """
            #version 330

            uniform sampler2D Sampler0;

            in vec2 texCoord0;
            in vec4 vertexColor;

            out vec4 fragColor;

            void main() {
                float a = texture(Sampler0, texCoord0).a;
                vec4 color = vec4(vertexColor.rgb, vertexColor.a * a);
                if (color.a <= 0.0) discard;
                fragColor = color;
            }
            """;

    private static final String IMAGE_VSH = """
            #version 330

            layout(std140) uniform Projection {
                mat4 ProjMat;
            };

            in vec3 Position;
            in vec2 UV0;
            in vec4 Color;

            out vec2 texCoord0;
            out vec4 vertexColor;

            void main() {
                gl_Position = ProjMat * vec4(Position, 1.0);
                texCoord0 = UV0;
                vertexColor = Color;
            }
            """;

    private static final String IMAGE_FSH = """
            #version 330

            uniform sampler2D Sampler0;

            in vec2 texCoord0;
            in vec4 vertexColor;

            out vec4 fragColor;

            void main() {
                vec4 color = texture(Sampler0, texCoord0) * vertexColor;
                if (color.a <= 0.0) discard;
                fragColor = color;
            }
            """;

    private static final String BG_VSH = """
            #version 330

            layout(std140) uniform Projection {
                mat4 ProjMat;
            };

            layout(std140) uniform PhotonBG {
                vec4 uResTime;
                vec4 uAccent;
            };

            in vec3 Position;

            out vec2 vUV;

            void main() {
                vUV = Position.xy;
                vec2 pos = Position.xy * uResTime.xy;
                gl_Position = ProjMat * vec4(pos, 0.0, 1.0);
            }
            """;

    private static final String BG_FSH = """
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

                vec2 q = vec2(fbm(p + vec2(0.0, t * 0.08)),
                              fbm(p + vec2(5.2, -t * 0.06)));
                vec2 r = vec2(fbm(p + 3.0 * q + vec2(1.7, 9.2) + t * 0.10),
                              fbm(p + 3.0 * q + vec2(8.3, 2.8) - t * 0.07));
                float f = fbm(p + 4.0 * r);

                vec3 col = palette(f + t * 0.03);
                col = mix(col, vec3(0.02, 0.03, 0.07), smoothstep(0.55, 1.1, length(q)));
                col *= 0.55 + 0.85 * f;
                col = mix(col, acc, 0.16);

                float streak = sin((p.x + p.y) * 3.0 + f * 8.0 - t * 0.8);
                col += acc * smoothstep(0.85, 1.0, streak) * 0.6;

                float glow = smoothstep(1.0, 0.0, length((uv - vec2(0.5, 0.42)) * vec2(aspect, 1.0)));
                col += acc * glow * 0.5;

                vec2 vc = uv - 0.5;
                col *= clamp(1.0 - dot(vc, vc) * 1.1, 0.0, 1.0);

                col = pow(col, vec3(0.92));
                fragColor = vec4(col, 1.0);
            }
            """;

    private static final String ALT_BG_VSH = """
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
            """;

    private static final String ALT_BG_FSH = """
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

                vec2 q = vec2(fbm(p + md * 0.5 + vec2(0.0, t * 0.05)),
                              fbm(p - md * 0.5 + vec2(5.2, -t * 0.04)));
                vec2 r = vec2(fbm(p + 3.0 * q + vec2(1.7, 9.2) + t * 0.06),
                              fbm(p + 3.0 * q + vec2(8.3, 2.8) - t * 0.05));
                float f = fbm(p + 4.0 * r + md * 0.35);

                vec3 col = vec3(0.012, 0.016, 0.024);
                col += vec3(0.05, 0.08, 0.15) * f * f;

                float streak = sin((p.x + p.y) * 3.0 + f * 8.0 - t * 0.6);
                col += vec3(0.06, 0.12, 0.24) * smoothstep(0.9, 1.0, streak) * 0.5;

                float gd = length((uv - mo) * vec2(aspect, 1.0));
                col += vec3(0.10, 0.20, 0.38) * smoothstep(0.55, 0.0, gd) * (0.55 + 0.5 * f);

                vec2 vc = uv - 0.5;
                col *= clamp(1.0 - dot(vc, vc) * 1.25, 0.0, 1.0);

                col = pow(col, vec3(0.95));
                fragColor = vec4(col, uMouse.z);
            }
            """;

    private static final String CHAMS_VSH = """
            #version 330

            layout(std140) uniform Projection {
                mat4 ProjMat;
            };

            layout(std140) uniform DynamicTransforms {
                mat4 ModelViewMat;
                vec4 ColorModulator;
                vec3 ModelOffset;
                mat4 TextureMat;
            };

            in vec3 Position;
            in vec4 Color;
            in vec3 Normal;

            out vec4 vColor;
            out vec3 vNormal;
            out vec3 vView;
            out vec3 vPos;

            void main() {
                vec4 mv = ModelViewMat * vec4(Position, 1.0);
                gl_Position = ProjMat * mv;
                vColor = Color * ColorModulator;
                vNormal = normalize(mat3(ModelViewMat) * Normal);
                vView = normalize(-mv.xyz);
                vPos = mv.xyz;
            }
            """;

    private static final String CHAMS_FSH_BODY = """
            layout(std140) uniform Globals {
                ivec3 GlobCamInt;
                vec3 GlobCamFract;
                vec2 GlobScreen;
                float GlobGlint;
                float GameTime;
                int GlobBlur;
                int GlobFlag;
            };

            in vec4 vColor;
            in vec3 vNormal;
            in vec3 vView;
            in vec3 vPos;

            out vec4 fragColor;

            vec3 hueShift(vec3 c, float h) {
                const vec3 k = vec3(0.57735);
                float ch = cos(h);
                return c * ch + cross(k, c) * sin(h) + k * dot(k, c) * (1.0 - ch);
            }

            void main() {
                vec3 N = normalize(vNormal);
                vec3 V = normalize(vView);
                float ndv = clamp(dot(N, V), 0.0, 1.0);
                float fres = pow(1.0 - ndv, 3.0);
                float rim = pow(1.0 - ndv, 1.6);
                float t = GameTime * 5200.0;
                vec3 col = vColor.rgb;
                float a = vColor.a;

            #if STYLE == 1
                // Френель — мягкое дышащее свечение по краям
                float br = 0.85 + 0.15 * sin(t * 0.7);
                col = mix(col * 0.6, col * 1.7 * br + vec3(0.08), rim);
                a *= mix(0.4, 1.0, rim);
            #elif STYLE == 2
                // Сплошной — плоская читаемая заливка с ярким кантом
                col = col * (0.82 + 0.18 * ndv) + col * pow(rim, 2.0) * 0.7 + vec3(0.02);
            #elif STYLE == 3
                // Контур — чистая яркая обводка
                float edge = smoothstep(0.45, 0.95, rim);
                col = mix(col * 0.22, mix(col, vec3(1.0), 0.45) * 1.9, edge);
                a *= mix(0.06, 1.0, edge);
            #elif STYLE == 4
                // Неон — пульсирующее ядро + сочный край
                float pulse = 0.78 + 0.22 * sin(t * 0.9);
                col = col * 1.15 + col * pow(rim, 1.1) * (2.6 * pulse) + vec3(0.04);
                a *= mix(0.7, 1.0, rim);
            #elif STYLE == 5
                // Свет — мягкая направленная подсветка
                float d = max(dot(N, normalize(vec3(0.35, 0.85, 0.4))), 0.0);
                float wrap = d * 0.5 + 0.5;
                col *= 0.45 + 0.85 * wrap * wrap;
                col += col * rim * 0.4;
            #elif STYLE == 6
                // Тун — ступенчатое затенение
                float d = max(dot(N, normalize(vec3(0.3, 0.95, 0.45))), 0.0);
                float band = floor(d * 3.0 + 0.5) / 3.0;
                col *= 0.4 + 0.75 * band;
                col = mix(col, mix(col, vec3(1.0), 0.5), smoothstep(0.6, 0.95, rim));
            #elif STYLE == 7
                // Блик — глянцевый отблеск
                vec3 L = normalize(vec3(0.45, 0.8, 0.55));
                vec3 H = normalize(L + V);
                float spec = pow(max(dot(N, H), 0.0), 48.0);
                col = col * (0.5 + 0.5 * ndv) + vec3(spec) * 0.9 + col * rim * 0.5;
            #elif STYLE == 8
                // Рентген — полупрозрачный объём с бегущей волной
                float scan = 0.5 + 0.5 * sin(vPos.y * 6.0 - t * 2.2);
                col = mix(col * 0.5, col * 1.5, rim);
                col += col * scan * 0.25;
                a *= mix(0.18, 0.6, rim) + scan * 0.08;
            #elif STYLE == 9
                // Заливка — чистый ровный цвет
                col = col;
            #elif STYLE == 10
                // Радуга — текучие переливы
                float ph = vPos.y * 0.5 - vPos.x * 0.25 + t * 0.8 + rim * 2.5;
                col = hueShift(col, ph);
                col += col * pow(rim, 1.4) * 1.3;
                a *= mix(0.55, 1.0, rim);
            #elif STYLE == 11
                // Иней — морозный белый край
                vec3 frost = mix(col, vec3(0.85, 0.95, 1.0), 0.6);
                col = mix(col * 0.7, frost * 1.6, pow(rim, 1.3));
                col += vec3(0.6, 0.8, 1.0) * pow(rim, 5.0) * 0.5;
                a *= mix(0.4, 1.0, rim);
            #endif

                fragColor = vec4(col, a);
                if (fragColor.a <= 0.002) discard;
            }
            """;

    private static String chamsFsh(int style) {
        return "#version 330\n#define STYLE " + style + "\n" + CHAMS_FSH_BODY;
    }

    private static final String CHAMS_ITEM_VSH = """
            #version 330

            layout(std140) uniform Projection {
                mat4 ProjMat;
            };

            layout(std140) uniform DynamicTransforms {
                mat4 ModelViewMat;
                vec4 ColorModulator;
                vec3 ModelOffset;
                mat4 TextureMat;
            };

            in vec3 Position;
            in vec4 Color;
            in vec2 UV0;
            in vec3 Normal;

            out vec4 vColor;
            out vec3 vNormal;
            out vec3 vView;
            out vec3 vPos;
            out vec2 vUV;

            void main() {
                vec4 mv = ModelViewMat * vec4(Position, 1.0);
                gl_Position = ProjMat * mv;
                vColor = Color * ColorModulator;
                vNormal = normalize(mat3(ModelViewMat) * Normal);
                vView = normalize(-mv.xyz);
                vPos = mv.xyz;
                vUV = UV0;
            }
            """;

    private static final String CHAMS_ITEM_FSH_BODY = """
            layout(std140) uniform Globals {
                ivec3 GlobCamInt;
                vec3 GlobCamFract;
                vec2 GlobScreen;
                float GlobGlint;
                float GameTime;
                int GlobBlur;
                int GlobFlag;
            };

            uniform sampler2D Sampler0;

            in vec4 vColor;
            in vec3 vNormal;
            in vec3 vView;
            in vec3 vPos;
            in vec2 vUV;

            out vec4 fragColor;

            vec3 hueShift(vec3 c, float h) {
                const vec3 k = vec3(0.57735);
                float ch = cos(h);
                return c * ch + cross(k, c) * sin(h) + k * dot(k, c) * (1.0 - ch);
            }

            void main() {
                float texA = texture(Sampler0, vUV).a;
                if (texA < 0.1) discard;

                vec3 N = normalize(vNormal);
                vec3 V = normalize(vView);
                float ndv = clamp(dot(N, V), 0.0, 1.0);
                float fres = pow(1.0 - ndv, 3.0);
                float rim = pow(1.0 - ndv, 1.6);
                float t = GameTime * 5200.0;
                vec3 col = vColor.rgb;
                float a = vColor.a;

            #if STYLE == 1
                float br = 0.85 + 0.15 * sin(t * 0.7);
                col = mix(col * 0.6, col * 1.7 * br + vec3(0.08), rim);
                a *= mix(0.4, 1.0, rim);
            #elif STYLE == 2
                col = col * (0.82 + 0.18 * ndv) + col * pow(rim, 2.0) * 0.7 + vec3(0.02);
            #elif STYLE == 3
                float edge = smoothstep(0.45, 0.95, rim);
                col = mix(col * 0.22, mix(col, vec3(1.0), 0.45) * 1.9, edge);
                a *= mix(0.06, 1.0, edge);
            #elif STYLE == 4
                float pulse = 0.78 + 0.22 * sin(t * 0.9);
                col = col * 1.15 + col * pow(rim, 1.1) * (2.6 * pulse) + vec3(0.04);
                a *= mix(0.7, 1.0, rim);
            #elif STYLE == 5
                float d = max(dot(N, normalize(vec3(0.35, 0.85, 0.4))), 0.0);
                float wrap = d * 0.5 + 0.5;
                col *= 0.45 + 0.85 * wrap * wrap;
                col += col * rim * 0.4;
            #elif STYLE == 6
                float d = max(dot(N, normalize(vec3(0.3, 0.95, 0.45))), 0.0);
                float band = floor(d * 3.0 + 0.5) / 3.0;
                col *= 0.4 + 0.75 * band;
                col = mix(col, mix(col, vec3(1.0), 0.5), smoothstep(0.6, 0.95, rim));
            #elif STYLE == 7
                vec3 L = normalize(vec3(0.45, 0.8, 0.55));
                vec3 H = normalize(L + V);
                float spec = pow(max(dot(N, H), 0.0), 48.0);
                col = col * (0.5 + 0.5 * ndv) + vec3(spec) * 0.9 + col * rim * 0.5;
            #elif STYLE == 8
                float scan = 0.5 + 0.5 * sin(vPos.y * 6.0 - t * 2.2);
                col = mix(col * 0.5, col * 1.5, rim);
                col += col * scan * 0.25;
                a *= mix(0.18, 0.6, rim) + scan * 0.08;
            #elif STYLE == 9
                col = col;
            #elif STYLE == 10
                float ph = vPos.y * 0.5 - vPos.x * 0.25 + t * 0.8 + rim * 2.5;
                col = hueShift(col, ph);
                col += col * pow(rim, 1.4) * 1.3;
                a *= mix(0.55, 1.0, rim);
            #elif STYLE == 11
                vec3 frost = mix(col, vec3(0.85, 0.95, 1.0), 0.6);
                col = mix(col * 0.7, frost * 1.6, pow(rim, 1.3));
                col += vec3(0.6, 0.8, 1.0) * pow(rim, 5.0) * 0.5;
                a *= mix(0.4, 1.0, rim);
            #endif

                a *= texA;
                fragColor = vec4(col, a);
                if (fragColor.a <= 0.002) discard;
            }
            """;

    private static String chamsItemFsh(int style) {
        return "#version 330\n#define STYLE " + style + "\n" + CHAMS_ITEM_FSH_BODY;
    }

    private static final String BLOCK_VSH = """
            #version 330

            layout(std140) uniform Projection {
                mat4 ProjMat;
            };

            layout(std140) uniform DynamicTransforms {
                mat4 ModelViewMat;
                vec4 ColorModulator;
                vec3 ModelOffset;
                mat4 TextureMat;
            };

            in vec3 Position;
            in vec2 UV0;
            in vec4 Color;

            out vec2 vUV;
            out vec4 vColor;

            void main() {
                gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
                vUV = UV0;
                vColor = Color * ColorModulator;
            }
            """;

    private static final String BLOCK_FSH_BODY = """
            layout(std140) uniform Globals {
                ivec3 GlobCamInt;
                vec3 GlobCamFract;
                vec2 GlobScreen;
                float GlobGlint;
                float GameTime;
                int GlobBlur;
                int GlobFlag;
            };

            in vec2 vUV;
            in vec4 vColor;

            out vec4 fragColor;

            vec3 hueShift(vec3 c, float h) {
                const vec3 k = vec3(0.57735);
                float ch = cos(h);
                return c * ch + cross(k, c) * sin(h) + k * dot(k, c) * (1.0 - ch);
            }

            void main() {
                vec3 col = vColor.rgb;
                float a = vColor.a;
                float t = GameTime * 1200.0;
                float e = min(min(vUV.x, 1.0 - vUV.x), min(vUV.y, 1.0 - vUV.y));

            #if STYLE == 1
                // Заливка — ровная заливка с мягким кантом
                float edge = 1.0 - smoothstep(0.0, 0.08, e);
                col += col * edge * 0.6;
                a *= 0.82 + 0.18 * edge;
            #elif STYLE == 2
                // Грани — свечение по рёбрам, прозрачный центр
                float edge = 1.0 - smoothstep(0.0, 0.14, e);
                col = mix(col * 0.5, col * 1.8 + vec3(0.1), edge);
                a *= mix(0.08, 1.0, edge);
            #elif STYLE == 3
                // Пульс — дышащая яркость
                float pulse = 0.5 + 0.5 * sin(t * 0.5);
                col *= 0.7 + 0.7 * pulse;
                float edge = 1.0 - smoothstep(0.0, 0.10, e);
                a *= (0.45 + 0.55 * pulse) * (0.7 + 0.3 * edge);
            #elif STYLE == 4
                // Поток — бегущая радужная волна
                float scan = fract(vUV.y * 2.0 - t * 0.25);
                float band = smoothstep(0.0, 0.08, scan) * (1.0 - smoothstep(0.18, 0.30, scan));
                col = hueShift(col, vUV.y * 1.5 + t * 0.2);
                col += col * band * 1.6;
                a *= 0.30 + 0.7 * band;
            #endif

                if (a <= 0.003) discard;
                fragColor = vec4(col, a);
            }
            """;

    private static String blockFsh(int style) {
        return "#version 330\n#define STYLE " + style + "\n" + BLOCK_FSH_BODY;
    }

    public static String get(Identifier id, ShaderType type) {
        String s = id.toString();
        if (s.equals("minecraft:photon/photon_chams_item")) {
            return type == ShaderType.VERTEX ? CHAMS_ITEM_VSH : null;
        }
        if (s.startsWith("minecraft:photon/photon_chams_item_")) {
            if (type == ShaderType.VERTEX) return CHAMS_ITEM_VSH;
            try {
                int style = Integer.parseInt(s.substring("minecraft:photon/photon_chams_item_".length()));
                return chamsItemFsh(style);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (s.equals("minecraft:photon/photon_block")) {
            return type == ShaderType.VERTEX ? BLOCK_VSH : null;
        }
        if (s.startsWith("minecraft:photon/photon_block_")) {
            if (type == ShaderType.VERTEX) return BLOCK_VSH;
            try {
                int style = Integer.parseInt(s.substring("minecraft:photon/photon_block_".length()));
                return blockFsh(style);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (s.equals("minecraft:photon/photon_chams")) {
            return type == ShaderType.VERTEX ? CHAMS_VSH : null;
        }
        if (s.startsWith("minecraft:photon/photon_chams_")) {
            if (type == ShaderType.VERTEX) return CHAMS_VSH;
            try {
                int style = Integer.parseInt(s.substring("minecraft:photon/photon_chams_".length()));
                return chamsFsh(style);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        switch (id.toString()) {
            case "minecraft:photon/photon_rounded":
                return type == ShaderType.VERTEX ? ROUNDED_VSH : ROUNDED_FSH;
            case "minecraft:photon/photon_text":
                return type == ShaderType.VERTEX ? TEXT_VSH : TEXT_FSH;
            case "minecraft:photon/photon_image":
                return type == ShaderType.VERTEX ? IMAGE_VSH : IMAGE_FSH;
            case "minecraft:photon/photon_bg":
                return type == ShaderType.VERTEX ? BG_VSH : BG_FSH;
            case "minecraft:photon/photon_alt_bg":
                return type == ShaderType.VERTEX ? ALT_BG_VSH : ALT_BG_FSH;
            default:
                return null;
        }
    }
}
