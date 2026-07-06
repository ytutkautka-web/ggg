package fun.photon.utils.render.svg;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class SvgRasterizer {

    private SvgRasterizer() {}

    public static BufferedImage rasterize(String resourcePath, int width, int height) {
        try (InputStream stream = SvgRasterizer.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                System.out.println("[Photon] svg not found on classpath: " + resourcePath);
                return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(stream);

            Element svgElement = doc.getDocumentElement();
            float viewBoxW = 16, viewBoxH = 16;

            String viewBox = svgElement.getAttribute("viewBox");
            if (!viewBox.isEmpty()) {
                String[] parts = viewBox.trim().split("\\s+");
                if (parts.length == 4) {
                    viewBoxW = Float.parseFloat(parts[2]);
                    viewBoxH = Float.parseFloat(parts[3]);
                }
            }

            float scaleX = width / viewBoxW;
            float scaleY = height / viewBoxH;

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.scale(scaleX, scaleY);

            renderNode(doc.getDocumentElement(), g);

            g.dispose();
            return image;

        } catch (Exception e) {
            e.printStackTrace();
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }
    }

    private static void renderNode(Node node, Graphics2D g) {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return;
        }

        Element element = (Element) node;
        String tagName = element.getTagName().toLowerCase();

        AffineTransform savedTransform = g.getTransform();
        applyTransform(element, g);
        try {
            renderElement(element, tagName, g);
        } finally {
            g.setTransform(savedTransform);
        }
    }

    private static void renderElement(Element element, String tagName, Graphics2D g) {
        Shape shape = null;

        switch (tagName) {
            case "path": {
                String d = element.getAttribute("d");
                if (!d.isEmpty()) {
                    shape = parseSvgPath(d, element);
                }
                break;
            }
            case "circle": {
                shape = parseCircle(element);
                break;
            }
            case "ellipse": {
                shape = parseEllipse(element);
                break;
            }
            case "rect": {
                shape = parseRect(element);
                break;
            }
            case "line": {
                shape = parseLine(element);
                break;
            }
            case "polygon": {
                shape = parsePolygon(element, true);
                break;
            }
            case "polyline": {
                shape = parsePolygon(element, false);
                break;
            }
            case "svg":
            case "g": {
                NodeList children = element.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    renderNode(children.item(i), g);
                }
                return;
            }
            case "defs":
            case "clipPath":
            case "mask":
            case "symbol":
            case "pattern":
            case "lineargradient":
            case "radialgradient":
            case "filter":
            case "title":
            case "desc":
            case "metadata": {
                return;
            }
            default: {
                NodeList children = element.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    renderNode(children.item(i), g);
                }
                return;
            }
        }

        if (shape != null) {
            renderShape(shape, element, g);
        }
    }

    private static final Pattern TRANSFORM = Pattern.compile("(\\w+)\\s*\\(([^)]*)\\)");

    private static void applyTransform(Element element, Graphics2D g) {
        String t = element.getAttribute("transform");
        if (t == null || t.isEmpty()) return;

        Matcher m = TRANSFORM.matcher(t);
        while (m.find()) {
            String name = m.group(1);
            String[] raw = m.group(2).trim().split("[\\s,]+");
            double[] v = new double[raw.length];
            for (int i = 0; i < raw.length; i++) {
                try {
                    v[i] = Double.parseDouble(raw[i]);
                } catch (NumberFormatException e) {
                    v[i] = 0;
                }
            }
            switch (name) {
                case "translate":
                    g.translate(v[0], v.length > 1 ? v[1] : 0);
                    break;
                case "scale":
                    g.scale(v[0], v.length > 1 ? v[1] : v[0]);
                    break;
                case "rotate":
                    if (v.length >= 3) {
                        g.rotate(Math.toRadians(v[0]), v[1], v[2]);
                    } else {
                        g.rotate(Math.toRadians(v[0]));
                    }
                    break;
                case "skewX":
                    g.shear(Math.tan(Math.toRadians(v[0])), 0);
                    break;
                case "skewY":
                    g.shear(0, Math.tan(Math.toRadians(v[0])));
                    break;
                case "matrix":
                    if (v.length == 6) {
                        g.transform(new AffineTransform(v[0], v[1], v[2], v[3], v[4], v[5]));
                    }
                    break;
            }
        }
    }

    private static Shape parseCircle(Element element) {
        float cx = getFloatAttr(element, "cx", 0);
        float cy = getFloatAttr(element, "cy", 0);
        float r = getFloatAttr(element, "r", 0);

        if (r <= 0) return null;

        return new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2);
    }

    private static Shape parseEllipse(Element element) {
        float cx = getFloatAttr(element, "cx", 0);
        float cy = getFloatAttr(element, "cy", 0);
        float rx = getFloatAttr(element, "rx", 0);
        float ry = getFloatAttr(element, "ry", 0);

        if (rx <= 0 || ry <= 0) return null;

        return new Ellipse2D.Float(cx - rx, cy - ry, rx * 2, ry * 2);
    }

    private static Shape parseRect(Element element) {
        float x = getFloatAttr(element, "x", 0);
        float y = getFloatAttr(element, "y", 0);
        float w = getFloatAttr(element, "width", 0);
        float h = getFloatAttr(element, "height", 0);
        float rx = getFloatAttr(element, "rx", 0);
        float ry = getFloatAttr(element, "ry", 0);

        if (w <= 0 || h <= 0) return null;

        if (rx > 0 || ry > 0) {
            if (rx == 0) rx = ry;
            if (ry == 0) ry = rx;
            rx = Math.min(rx, w / 2);
            ry = Math.min(ry, h / 2);
            return new java.awt.geom.RoundRectangle2D.Float(x, y, w, h, rx * 2, ry * 2);
        }

        return new Rectangle2D.Float(x, y, w, h);
    }

    private static Shape parseLine(Element element) {
        float x1 = getFloatAttr(element, "x1", 0);
        float y1 = getFloatAttr(element, "y1", 0);
        float x2 = getFloatAttr(element, "x2", 0);
        float y2 = getFloatAttr(element, "y2", 0);

        return new Line2D.Float(x1, y1, x2, y2);
    }

    private static Shape parsePolygon(Element element, boolean close) {
        String points = element.getAttribute("points");
        if (points.isEmpty()) return null;

        GeneralPath path = new GeneralPath();
        String[] parts = points.trim().split("[\\s,]+");

        boolean first = true;
        for (int i = 0; i + 1 < parts.length; i += 2) {
            float x = Float.parseFloat(parts[i]);
            float y = Float.parseFloat(parts[i + 1]);

            if (first) {
                path.moveTo(x, y);
                first = false;
            } else {
                path.lineTo(x, y);
            }
        }

        if (close) {
            path.closePath();
        }

        return path;
    }

    private static float getFloatAttr(Element element, String name, float defaultValue) {
        String value = element.getAttribute(name);
        if (value.isEmpty()) return defaultValue;
        try {
            value = value.replaceAll("[^\\d.\\-]", "");
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static void renderShape(Shape shape, Element element, Graphics2D g) {
        String fillAttr = element.getAttribute("fill");
        String strokeAttr = element.getAttribute("stroke");
        String strokeWidthAttr = element.getAttribute("stroke-width");
        String strokeLinecapAttr = element.getAttribute("stroke-linecap");
        String strokeLinejoinAttr = element.getAttribute("stroke-linejoin");

        String style = element.getAttribute("style");
        String fillOpacityAttr = element.getAttribute("fill-opacity");
        String strokeOpacityAttr = element.getAttribute("stroke-opacity");
        String opacityAttr = element.getAttribute("opacity");
        if (!style.isEmpty()) {
            if (fillAttr.isEmpty()) fillAttr = getStyleProperty(style, "fill");
            if (strokeAttr.isEmpty()) strokeAttr = getStyleProperty(style, "stroke");
            if (strokeWidthAttr.isEmpty()) strokeWidthAttr = getStyleProperty(style, "stroke-width");
            if (strokeLinecapAttr.isEmpty()) strokeLinecapAttr = getStyleProperty(style, "stroke-linecap");
            if (strokeLinejoinAttr.isEmpty()) strokeLinejoinAttr = getStyleProperty(style, "stroke-linejoin");
            if (fillOpacityAttr.isEmpty()) fillOpacityAttr = getStyleProperty(style, "fill-opacity");
            if (strokeOpacityAttr.isEmpty()) strokeOpacityAttr = getStyleProperty(style, "stroke-opacity");
            if (opacityAttr.isEmpty()) opacityAttr = getStyleProperty(style, "opacity");
        }

        boolean hasFill = !fillAttr.isEmpty() && !fillAttr.equals("none");
        boolean hasStroke = !strokeAttr.isEmpty() && !strokeAttr.equals("none");

        float globalOpacity = parseOpacity(opacityAttr, 1f);

        if (hasFill && !(shape instanceof Line2D)) {
            g.setColor(paint(fillAttr, parseOpacity(fillOpacityAttr, 1f) * globalOpacity));
            g.fill(shape);
        }

        if (hasStroke) {
            float strokeWidth = 1.0f;
            if (!strokeWidthAttr.isEmpty()) {
                try {
                    strokeWidth = Float.parseFloat(strokeWidthAttr.replaceAll("[^\\d.]", ""));
                } catch (NumberFormatException ignored) {}
            }

            int cap = BasicStroke.CAP_BUTT;
            if ("round".equals(strokeLinecapAttr)) cap = BasicStroke.CAP_ROUND;
            else if ("square".equals(strokeLinecapAttr)) cap = BasicStroke.CAP_SQUARE;

            int join = BasicStroke.JOIN_MITER;
            if ("round".equals(strokeLinejoinAttr)) join = BasicStroke.JOIN_ROUND;
            else if ("bevel".equals(strokeLinejoinAttr)) join = BasicStroke.JOIN_BEVEL;

            g.setColor(paint(strokeAttr, parseOpacity(strokeOpacityAttr, 1f) * globalOpacity));
            g.setStroke(new BasicStroke(strokeWidth, cap, join));
            g.draw(shape);
        }

        if (!hasFill && !hasStroke && !(shape instanceof Line2D)) {
            g.setColor(paint("", globalOpacity));
            g.fill(shape);
        }
    }

    private static Color paint(String colorStr, float opacity) {
        float lum = 1f;
        float colorAlpha = 1f;
        float[] c = parseColor(colorStr);
        if (c != null) {
            lum = 0.299f * c[0] + 0.587f * c[1] + 0.114f * c[2];
            colorAlpha = c[3];
        }
        int a = Math.round(lum * colorAlpha * opacity * 255f);
        if (a < 0) a = 0;
        if (a > 255) a = 255;
        return new Color(255, 255, 255, a);
    }

    private static float[] parseColor(String s) {
        if (s == null) return null;
        s = s.trim().toLowerCase();
        if (s.isEmpty() || s.equals("none") || s.equals("currentcolor") || s.equals("transparent")) {
            return null;
        }
        try {
            if (s.startsWith("#")) {
                String hex = s.substring(1);
                if (hex.length() == 3 || hex.length() == 4) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < hex.length(); i++) {
                        sb.append(hex.charAt(i)).append(hex.charAt(i));
                    }
                    hex = sb.toString();
                }
                if (hex.length() == 6 || hex.length() == 8) {
                    int r = Integer.parseInt(hex.substring(0, 2), 16);
                    int g = Integer.parseInt(hex.substring(2, 4), 16);
                    int b = Integer.parseInt(hex.substring(4, 6), 16);
                    int a = hex.length() == 8 ? Integer.parseInt(hex.substring(6, 8), 16) : 255;
                    return new float[]{r / 255f, g / 255f, b / 255f, a / 255f};
                }
            }
            if (s.startsWith("rgb")) {
                String inner = s.substring(s.indexOf('(') + 1, s.indexOf(')'));
                String[] p = inner.split("[\\s,]+");
                float r = parseChannel(p[0]);
                float g = parseChannel(p[1]);
                float b = parseChannel(p[2]);
                float a = p.length > 3 ? Float.parseFloat(p[3]) : 1f;
                return new float[]{r, g, b, a};
            }
        } catch (Exception ignored) {
        }
        switch (s) {
            case "white": return new float[]{1, 1, 1, 1};
            case "black": return new float[]{0, 0, 0, 1};
            case "red": return new float[]{1, 0, 0, 1};
            case "gray":
            case "grey": return new float[]{0.5f, 0.5f, 0.5f, 1};
            case "silver": return new float[]{0.75f, 0.75f, 0.75f, 1};
            default: return null;
        }
    }

    private static float parseChannel(String v) {
        v = v.trim();
        if (v.endsWith("%")) {
            return Float.parseFloat(v.substring(0, v.length() - 1)) / 100f;
        }
        return Float.parseFloat(v) / 255f;
    }

    private static float parseOpacity(String v, float def) {
        if (v == null || v.trim().isEmpty()) return def;
        try {
            v = v.trim();
            if (v.endsWith("%")) {
                return Float.parseFloat(v.substring(0, v.length() - 1)) / 100f;
            }
            return Float.parseFloat(v);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static String getStyleProperty(String style, String property) {
        String[] parts = style.split(";");
        for (String part : parts) {
            String[] kv = part.split(":");
            if (kv.length == 2 && kv[0].trim().equals(property)) {
                return kv[1].trim();
            }
        }
        return "";
    }

    private static GeneralPath parseSvgPath(String d, Element element) {
        GeneralPath path = new GeneralPath();

        String fillRule = element.getAttribute("fill-rule");
        if ("evenodd".equals(fillRule)) {
            path.setWindingRule(GeneralPath.WIND_EVEN_ODD);
        } else {
            path.setWindingRule(GeneralPath.WIND_NON_ZERO);
        }

        List<Token> tokens = tokenize(d);

        float curX = 0, curY = 0;
        float startX = 0, startY = 0;
        float lastCpX = 0, lastCpY = 0;
        char lastCmd = 0;
        char prevCmd = 0;

        int i = 0;
        while (i < tokens.size()) {
            Token token = tokens.get(i);

            if (token.isCommand) {
                lastCmd = token.command;
                i++;

                if (lastCmd == 'Z' || lastCmd == 'z') {
                    path.closePath();
                    curX = startX;
                    curY = startY;
                    lastCpX = curX;
                    lastCpY = curY;
                    prevCmd = lastCmd;
                    continue;
                }
            }

            if (i >= tokens.size() || tokens.get(i).isCommand) {
                continue;
            }

            switch (lastCmd) {
                case 'M': {
                    float x = tokens.get(i).value; i++;
                    float y = tokens.get(i).value; i++;
                    path.moveTo(x, y);
                    curX = x; curY = y;
                    startX = x; startY = y;
                    lastCpX = curX; lastCpY = curY;
                    lastCmd = 'L';
                    break;
                }
                case 'm': {
                    float dx = tokens.get(i).value; i++;
                    float dy = tokens.get(i).value; i++;
                    curX += dx; curY += dy;
                    path.moveTo(curX, curY);
                    startX = curX; startY = curY;
                    lastCpX = curX; lastCpY = curY;
                    lastCmd = 'l';
                    break;
                }
                case 'L': {
                    float x = tokens.get(i).value; i++;
                    float y = tokens.get(i).value; i++;
                    path.lineTo(x, y);
                    curX = x; curY = y;
                    lastCpX = curX; lastCpY = curY;
                    break;
                }
                case 'l': {
                    float dx = tokens.get(i).value; i++;
                    float dy = tokens.get(i).value; i++;
                    curX += dx; curY += dy;
                    path.lineTo(curX, curY);
                    lastCpX = curX; lastCpY = curY;
                    break;
                }
                case 'H': {
                    float x = tokens.get(i).value; i++;
                    path.lineTo(x, curY);
                    curX = x;
                    lastCpX = curX; lastCpY = curY;
                    break;
                }
                case 'h': {
                    float dx = tokens.get(i).value; i++;
                    curX += dx;
                    path.lineTo(curX, curY);
                    lastCpX = curX; lastCpY = curY;
                    break;
                }
                case 'V': {
                    float y = tokens.get(i).value; i++;
                    path.lineTo(curX, y);
                    curY = y;
                    lastCpX = curX; lastCpY = curY;
                    break;
                }
                case 'v': {
                    float dy = tokens.get(i).value; i++;
                    curY += dy;
                    path.lineTo(curX, curY);
                    lastCpX = curX; lastCpY = curY;
                    break;
                }
                case 'C': {
                    float x1 = tokens.get(i).value; i++;
                    float y1 = tokens.get(i).value; i++;
                    float x2 = tokens.get(i).value; i++;
                    float y2 = tokens.get(i).value; i++;
                    float x = tokens.get(i).value; i++;
                    float y = tokens.get(i).value; i++;
                    path.curveTo(x1, y1, x2, y2, x, y);
                    lastCpX = x2; lastCpY = y2;
                    curX = x; curY = y;
                    break;
                }
                case 'c': {
                    float dx1 = tokens.get(i).value; i++;
                    float dy1 = tokens.get(i).value; i++;
                    float dx2 = tokens.get(i).value; i++;
                    float dy2 = tokens.get(i).value; i++;
                    float dx = tokens.get(i).value; i++;
                    float dy = tokens.get(i).value; i++;
                    float x1 = curX + dx1;
                    float y1 = curY + dy1;
                    float x2 = curX + dx2;
                    float y2 = curY + dy2;
                    float x = curX + dx;
                    float y = curY + dy;
                    path.curveTo(x1, y1, x2, y2, x, y);
                    lastCpX = x2; lastCpY = y2;
                    curX = x; curY = y;
                    break;
                }
                case 'S': {
                    float cpX, cpY;
                    if (isCubicCommand(prevCmd)) {
                        cpX = 2 * curX - lastCpX;
                        cpY = 2 * curY - lastCpY;
                    } else {
                        cpX = curX;
                        cpY = curY;
                    }
                    float x2 = tokens.get(i).value; i++;
                    float y2 = tokens.get(i).value; i++;
                    float x = tokens.get(i).value; i++;
                    float y = tokens.get(i).value; i++;
                    path.curveTo(cpX, cpY, x2, y2, x, y);
                    lastCpX = x2; lastCpY = y2;
                    curX = x; curY = y;
                    break;
                }
                case 's': {
                    float cpX, cpY;
                    if (isCubicCommand(prevCmd)) {
                        cpX = 2 * curX - lastCpX;
                        cpY = 2 * curY - lastCpY;
                    } else {
                        cpX = curX;
                        cpY = curY;
                    }
                    float dx2 = tokens.get(i).value; i++;
                    float dy2 = tokens.get(i).value; i++;
                    float dx = tokens.get(i).value; i++;
                    float dy = tokens.get(i).value; i++;
                    float x2 = curX + dx2;
                    float y2 = curY + dy2;
                    float x = curX + dx;
                    float y = curY + dy;
                    path.curveTo(cpX, cpY, x2, y2, x, y);
                    lastCpX = x2; lastCpY = y2;
                    curX = x; curY = y;
                    break;
                }
                case 'Q': {
                    float x1 = tokens.get(i).value; i++;
                    float y1 = tokens.get(i).value; i++;
                    float x = tokens.get(i).value; i++;
                    float y = tokens.get(i).value; i++;
                    path.quadTo(x1, y1, x, y);
                    lastCpX = x1; lastCpY = y1;
                    curX = x; curY = y;
                    break;
                }
                case 'q': {
                    float dx1 = tokens.get(i).value; i++;
                    float dy1 = tokens.get(i).value; i++;
                    float dx = tokens.get(i).value; i++;
                    float dy = tokens.get(i).value; i++;
                    float x1 = curX + dx1;
                    float y1 = curY + dy1;
                    float x = curX + dx;
                    float y = curY + dy;
                    path.quadTo(x1, y1, x, y);
                    lastCpX = x1; lastCpY = y1;
                    curX = x; curY = y;
                    break;
                }
                case 'T': {
                    float cpX, cpY;
                    if (isQuadCommand(prevCmd)) {
                        cpX = 2 * curX - lastCpX;
                        cpY = 2 * curY - lastCpY;
                    } else {
                        cpX = curX;
                        cpY = curY;
                    }
                    float x = tokens.get(i).value; i++;
                    float y = tokens.get(i).value; i++;
                    path.quadTo(cpX, cpY, x, y);
                    lastCpX = cpX; lastCpY = cpY;
                    curX = x; curY = y;
                    break;
                }
                case 't': {
                    float cpX, cpY;
                    if (isQuadCommand(prevCmd)) {
                        cpX = 2 * curX - lastCpX;
                        cpY = 2 * curY - lastCpY;
                    } else {
                        cpX = curX;
                        cpY = curY;
                    }
                    float dx = tokens.get(i).value; i++;
                    float dy = tokens.get(i).value; i++;
                    float x = curX + dx;
                    float y = curY + dy;
                    path.quadTo(cpX, cpY, x, y);
                    lastCpX = cpX; lastCpY = cpY;
                    curX = x; curY = y;
                    break;
                }
                case 'A': {
                    float rx = tokens.get(i).value; i++;
                    float ry = tokens.get(i).value; i++;
                    float rotation = tokens.get(i).value; i++;
                    float largeArc = tokens.get(i).value; i++;
                    float sweep = tokens.get(i).value; i++;
                    float x = tokens.get(i).value; i++;
                    float y = tokens.get(i).value; i++;
                    arcTo(path, curX, curY, x, y, rx, ry, rotation, largeArc != 0, sweep != 0);
                    curX = x; curY = y;
                    lastCpX = curX; lastCpY = curY;
                    break;
                }
                case 'a': {
                    float rx = tokens.get(i).value; i++;
                    float ry = tokens.get(i).value; i++;
                    float rotation = tokens.get(i).value; i++;
                    float largeArc = tokens.get(i).value; i++;
                    float sweep = tokens.get(i).value; i++;
                    float dx = tokens.get(i).value; i++;
                    float dy = tokens.get(i).value; i++;
                    float x = curX + dx;
                    float y = curY + dy;
                    arcTo(path, curX, curY, x, y, rx, ry, rotation, largeArc != 0, sweep != 0);
                    curX = x; curY = y;
                    lastCpX = curX; lastCpY = curY;
                    break;
                }
                default: {
                    i++;
                    break;
                }
            }

            prevCmd = lastCmd;
        }

        return path;
    }

    private static boolean isCubicCommand(char cmd) {
        return cmd == 'C' || cmd == 'c' || cmd == 'S' || cmd == 's';
    }

    private static boolean isQuadCommand(char cmd) {
        return cmd == 'Q' || cmd == 'q' || cmd == 'T' || cmd == 't';
    }

    private static void arcTo(GeneralPath path, float x1, float y1, float x2, float y2,
                              float rx, float ry, float rotation, boolean largeArc, boolean sweep) {
        if (rx == 0 || ry == 0) {
            path.lineTo(x2, y2);
            return;
        }

        rx = Math.abs(rx);
        ry = Math.abs(ry);

        double cosAngle = Math.cos(Math.toRadians(rotation));
        double sinAngle = Math.sin(Math.toRadians(rotation));

        double dx2 = (x1 - x2) / 2.0;
        double dy2 = (y1 - y2) / 2.0;

        double x1p = cosAngle * dx2 + sinAngle * dy2;
        double y1p = -sinAngle * dx2 + cosAngle * dy2;

        double rxSq = (double) rx * rx;
        double rySq = (double) ry * ry;
        double x1pSq = x1p * x1p;
        double y1pSq = y1p * y1p;

        double lambda = x1pSq / rxSq + y1pSq / rySq;
        if (lambda > 1) {
            double lambdaSqrt = Math.sqrt(lambda);
            rx = (float) (lambdaSqrt * rx);
            ry = (float) (lambdaSqrt * ry);
            rxSq = (double) rx * rx;
            rySq = (double) ry * ry;
        }

        double num = Math.max(rxSq * rySq - rxSq * y1pSq - rySq * x1pSq, 0);
        double den = rxSq * y1pSq + rySq * x1pSq;
        double sq = (den == 0) ? 0 : Math.sqrt(num / den);
        if (largeArc == sweep) sq = -sq;

        double cxp = sq * rx * y1p / ry;
        double cyp = -sq * ry * x1p / rx;

        double cx = cosAngle * cxp - sinAngle * cyp + (x1 + x2) / 2.0;
        double cy = sinAngle * cxp + cosAngle * cyp + (y1 + y2) / 2.0;

        double theta1 = angle(1, 0, (x1p - cxp) / rx, (y1p - cyp) / ry);
        double dTheta = angle((x1p - cxp) / rx, (y1p - cyp) / ry, (-x1p - cxp) / rx, (-y1p - cyp) / ry);

        if (!sweep && dTheta > 0) dTheta -= 2 * Math.PI;
        else if (sweep && dTheta < 0) dTheta += 2 * Math.PI;

        int segments = (int) Math.ceil(Math.abs(dTheta) / (Math.PI / 4));
        if (segments == 0) segments = 1;

        double step = dTheta / segments;
        for (int seg = 0; seg < segments; seg++) {
            double t1 = theta1 + seg * step;
            double t2 = theta1 + (seg + 1) * step;

            double alpha = Math.sin(step) * (Math.sqrt(4 + 3 * Math.pow(Math.tan(step / 2), 2)) - 1) / 3.0;

            double cos1 = Math.cos(t1);
            double sin1 = Math.sin(t1);
            double cos2 = Math.cos(t2);
            double sin2 = Math.sin(t2);

            double q1x = cx + cosAngle * rx * cos1 - sinAngle * ry * sin1;
            double q1y = cy + sinAngle * rx * cos1 + cosAngle * ry * sin1;
            double q2x = cx + cosAngle * rx * cos2 - sinAngle * ry * sin2;
            double q2y = cy + sinAngle * rx * cos2 + cosAngle * ry * sin2;

            double dp1x = cosAngle * (-rx * sin1) - sinAngle * (ry * cos1);
            double dp1y = sinAngle * (-rx * sin1) + cosAngle * (ry * cos1);
            double dp2x = cosAngle * (-rx * sin2) - sinAngle * (ry * cos2);
            double dp2y = sinAngle * (-rx * sin2) + cosAngle * (ry * cos2);

            double cp1x = q1x + alpha * dp1x;
            double cp1y = q1y + alpha * dp1y;
            double cp2x = q2x - alpha * dp2x;
            double cp2y = q2y - alpha * dp2y;

            path.curveTo((float) cp1x, (float) cp1y, (float) cp2x, (float) cp2y, (float) q2x, (float) q2y);
        }
    }

    private static double angle(double ux, double uy, double vx, double vy) {
        double n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
        if (n == 0) return 0;
        double c = (ux * vx + uy * vy) / n;
        c = Math.max(-1, Math.min(1, c));
        double a = Math.acos(c);
        if (ux * vy - uy * vx < 0) a = -a;
        return a;
    }

    private static List<Token> tokenize(String d) {
        List<Token> tokens = new ArrayList<>();
        int len = d.length();
        int i = 0;

        while (i < len) {
            char c = d.charAt(i);

            if (Character.isWhitespace(c) || c == ',') {
                i++;
                continue;
            }

            if (Character.isLetter(c)) {
                tokens.add(new Token(c));
                i++;
                continue;
            }

            int start = i;
            if (c == '-' || c == '+') i++;
            boolean hasDot = false;
            boolean hasE = false;
            while (i < len) {
                c = d.charAt(i);
                if (c == '.' && !hasDot && !hasE) {
                    hasDot = true;
                    i++;
                } else if ((c == 'e' || c == 'E') && !hasE) {
                    hasE = true;
                    i++;
                    if (i < len && (d.charAt(i) == '+' || d.charAt(i) == '-')) i++;
                } else if (Character.isDigit(c)) {
                    i++;
                } else {
                    break;
                }
            }

            if (i > start) {
                String numStr = d.substring(start, i);
                try {
                    tokens.add(new Token(Float.parseFloat(numStr)));
                } catch (NumberFormatException e) {

                }
            } else {
                i++;
            }
        }

        return tokens;
    }

    private static final class Token {
        boolean isCommand;
        char command;
        float value;

        Token(char command) {
            this.isCommand = true;
            this.command = command;
        }

        Token(float value) {
            this.isCommand = false;
            this.value = value;
        }
    }
}
