package com.todoapp.controller.api;

import com.todoapp.util.SseBroadcaster;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * GET /api/events  SSE 订阅端点（text/event-stream）
 * 客户端用 EventSource 连接，任务增删改时收到 task-changed 事件。
 */
public class SseServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/event-stream;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setHeader("Connection", "keep-alive");

        AsyncContext ctx = req.startAsync();
        ctx.setTimeout(10 * 60 * 1000L); // 10 分钟超时，客户端 EventSource 会自动重连
        SseBroadcaster.register(ctx);

        ctx.addListener(new javax.servlet.AsyncListener() {
            @Override public void onComplete(javax.servlet.AsyncEvent event) { SseBroadcaster.unregister(ctx); }
            @Override public void onTimeout(javax.servlet.AsyncEvent event) { SseBroadcaster.unregister(ctx); try { ctx.complete(); } catch (Exception ignored) {} }
            @Override public void onError(javax.servlet.AsyncEvent event) { SseBroadcaster.unregister(ctx); try { ctx.complete(); } catch (Exception ignored) {} }
            @Override public void onStartAsync(javax.servlet.AsyncEvent event) {}
        });

        // 连接建立后立刻发一条欢迎事件，便于前端确认连通
        resp.getWriter().write("event: connected\ndata: {\"ok\":true}\n\n");
        resp.getWriter().flush();
    }
}
