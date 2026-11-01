package com.todoapp.util;

import javax.servlet.AsyncContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * SSE 广播器：维护所有订阅 /api/events 的异步连接，
 * 任务发生增删改时向各客户端推送事件，并定时发送心跳保活。
 */
public class SseBroadcaster {

    private static final Set<AsyncContext> CLIENTS =
            Collections.newSetFromMap(new ConcurrentHashMap<AsyncContext, Boolean>());

    private static volatile ScheduledExecutorService heartbeat;

    public static void register(AsyncContext ctx) {
        CLIENTS.add(ctx);
        startHeartbeat();
    }

    public static void unregister(AsyncContext ctx) {
        CLIENTS.remove(ctx);
    }

    /** 广播一个命名事件，data 为 JSON 字符串 */
    public static void broadcast(String event, String data) {
        String payload = "event: " + event + "\ndata: " + data + "\n\n";
        for (AsyncContext ctx : CLIENTS) {
            if (!write(ctx, payload)) {
                CLIENTS.remove(ctx);
            }
        }
    }

    private static boolean write(AsyncContext ctx, String payload) {
        try {
            PrintWriter w = ctx.getResponse().getWriter();
            w.write(payload);
            w.flush();
            return !w.checkError();
        } catch (Exception e) {
            try { ctx.complete(); } catch (Exception ignored) {}
            return false;
        }
    }

    private static synchronized void startHeartbeat() {
        if (heartbeat != null) return;
        heartbeat = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "sse-heartbeat");
            t.setDaemon(true);
            return t;
        });
        heartbeat.scheduleAtFixedRate(() -> {
            for (AsyncContext ctx : CLIENTS) {
                if (!write(ctx, ": ping\n\n")) {
                    CLIENTS.remove(ctx);
                }
            }
        }, 25, 25, TimeUnit.SECONDS);
    }
}
