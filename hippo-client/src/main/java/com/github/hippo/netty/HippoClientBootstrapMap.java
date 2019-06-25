package com.github.hippo.netty;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sl
 */
public final class HippoClientBootstrapMap {
    private HippoClientBootstrapMap() {
    }

    private static final Map<String, Map<String, HippoClientBootstrap>> BOOTSTRAPMAP =
            new ConcurrentHashMap<>();

    public synchronized static void put(String serviceName, String host, int port,
                                        HippoClientBootstrap bootstrap) {
        if (!BOOTSTRAPMAP.containsKey(serviceName)) {
            Map<String, HippoClientBootstrap> map = new ConcurrentHashMap<>();
            map.put(host + ":" + port, bootstrap);
            BOOTSTRAPMAP.putIfAbsent(serviceName, map);
        } else {
            BOOTSTRAPMAP.get(serviceName).putIfAbsent(host + ":" + port, bootstrap);
        }
    }


    public static Map<String, HippoClientBootstrap> get(String serviceName) {
        return BOOTSTRAPMAP.get(serviceName);
    }

    public static boolean containsKey(String serviceName) {
        return BOOTSTRAPMAP.containsKey(serviceName);
    }

    public static boolean containsSubKey(String serviceName, String hostAndPort) {
        Map<String, HippoClientBootstrap> map = BOOTSTRAPMAP.get(serviceName);
        if (map == null || map.isEmpty()) {
            return false;
        }
        return map.containsKey(hostAndPort);
    }

    public static void remove(String serviceName, String host, int port) {
        Map<String, HippoClientBootstrap> map = BOOTSTRAPMAP.get(serviceName);
        if (map != null) {
            map.remove(host + ":" + port);
        }
    }

    public static HippoClientBootstrap getBootstrap(String serviceName) {
        Map<String, HippoClientBootstrap> map = get(serviceName);
        if (map == null || map.isEmpty()) {
            return null;
        }
        //  round robin
        Optional<HippoClientBootstrap> findFirst = map.values().stream().sorted().findAny();
        if (findFirst.isPresent()) {
            HippoClientBootstrap hippoClientBootstrap = findFirst.get();
            hippoClientBootstrap.getInvokeTimes().incrementAndGet();
            return hippoClientBootstrap;
        } else {
            return null;
        }
    }

    public static void refreshInvokeTimes(String serviceName) {
        Map<String, HippoClientBootstrap> map = get(serviceName);
        if (map == null || map.isEmpty()) {
            return;
        }
        map.values().stream().forEach(HippoClientBootstrap::refreshInvokeTimes);
    }
}
