package fun.photon.utils;

import java.util.HashMap;
import java.util.Map;

public final class KeyUtil {

    public static final int NONE = 0;
    public static final String NONE_NAME = "NONE";

    public static final int MOUSE_BASE = 1000;

    private static final Map<Integer, String> CODE_TO_NAME = new HashMap<>();
    private static final Map<String, Integer> NAME_TO_CODE = new HashMap<>();

    private KeyUtil() {}

    private static void put(int code, String name) {
        CODE_TO_NAME.put(code, name);
        NAME_TO_CODE.put(name, code);
    }

    static {

        for (int c = 65; c <= 90; c++) {
            put(c, String.valueOf((char) c));
        }

        for (int c = 48; c <= 57; c++) {
            put(c, String.valueOf((char) c));
        }

        for (int i = 1; i <= 25; i++) {
            put(289 + i, "F" + i);
        }

        for (int i = 0; i <= 9; i++) {
            put(320 + i, "NUM" + i);
        }

        put(32, "SPACE");
        put(39, "APOSTROPHE");
        put(44, "COMMA");
        put(45, "MINUS");
        put(46, "PERIOD");
        put(47, "SLASH");
        put(59, "SEMICOLON");
        put(61, "EQUAL");
        put(91, "LBRACKET");
        put(92, "BACKSLASH");
        put(93, "RBRACKET");
        put(96, "GRAVE");
        put(256, "ESCAPE");
        put(257, "ENTER");
        put(258, "TAB");
        put(259, "BACKSPACE");
        put(260, "INSERT");
        put(261, "DELETE");
        put(262, "RIGHT");
        put(263, "LEFT");
        put(264, "DOWN");
        put(265, "UP");
        put(266, "PAGEUP");
        put(267, "PAGEDOWN");
        put(268, "HOME");
        put(269, "END");
        put(280, "CAPSLOCK");
        put(281, "SCROLLLOCK");
        put(282, "NUMLOCK");
        put(283, "PRINTSCREEN");
        put(284, "PAUSE");
        put(330, "NUMDECIMAL");
        put(331, "NUMDIVIDE");
        put(332, "NUMMULTIPLY");
        put(333, "NUMSUBTRACT");
        put(334, "NUMADD");
        put(335, "NUMENTER");
        put(336, "NUMEQUAL");
        put(340, "LSHIFT");
        put(341, "LCONTROL");
        put(342, "LALT");
        put(343, "LSUPER");
        put(344, "RSHIFT");
        put(345, "RCONTROL");
        put(346, "RALT");
        put(347, "RSUPER");
        put(348, "MENU");

        for (int b = 0; b < 8; b++) {
            put(MOUSE_BASE + b, "MOUSE" + (b + 1));
        }
    }

    public static boolean isMouse(int code) {
        return code >= MOUSE_BASE && code < MOUSE_BASE + 8;
    }

    public static int mouseCode(int glfwButton) {
        return MOUSE_BASE + glfwButton;
    }

    public static String getName(int code) {
        if (code <= 0) return NONE_NAME;
        String name = CODE_TO_NAME.get(code);
        return name != null ? name : "KEY_" + code;
    }

    public static int getCode(String name) {
        if (name == null) return NONE;
        String n = name.trim().toUpperCase();
        if (n.isEmpty() || n.equals(NONE_NAME)) return NONE;
        Integer code = NAME_TO_CODE.get(n);
        if (code != null) return code;
        if (n.startsWith("KEY_")) {
            try {
                return Integer.parseInt(n.substring(4));
            } catch (NumberFormatException ignored) {
            }
        }
        return NONE;
    }
}
