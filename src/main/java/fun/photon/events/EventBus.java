package fun.photon.events;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventBus {

    private final Map<Class<?>, List<MethodData>> registry = new ConcurrentHashMap<>();

    public void register(Object object) {
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventTarget.class)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1) {
                    method.setAccessible(true);
                    Class<?> eventClass = parameterTypes[0];
                    registry.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(new MethodData(object, method));
                }
            }
        }
    }

    public void unregister(Object object) {
        for (List<MethodData> list : registry.values()) {
            list.removeIf(data -> data.source.equals(object));
        }
    }

    public void post(Object event) {
        List<MethodData> methods = registry.get(event.getClass());
        if (methods != null) {
            for (MethodData data : methods) {
                try {
                    data.method.invoke(data.source, event);
                } catch (Exception e) {
                    System.err.println("[Photon] Error posting event " + event.getClass().getSimpleName() + " to " + data.source.getClass().getSimpleName());
                    e.printStackTrace();
                }
            }
        } else {

        }
    }

    private record MethodData(Object source, Method method) {}
}