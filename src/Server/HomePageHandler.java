package Server;

import Core.PerformanceMonitor;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * @author: Minghao Liu
 */
public class HomePageHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        if (PerformanceMonitor.getPerformanceStatus()) {
            String returnMessage = "{\"message\": \"Load Balancing in Progress.\"}";
            httpExchange.sendResponseHeaders(200, returnMessage.getBytes().length);
            httpExchange.getResponseBody().write(returnMessage.getBytes());
            httpExchange.getResponseBody().close();
            return;
        }
        String htmlBody = "<html><head><title>Welcome to Load Balancing Server</title></head>" +
                "<body><h1>Load Balancing Server -- Smart Scheduling</h1>" +
                "<p>Please use route \"/loadBalancing\" to schedule trucks.</p></body></html>";
        httpExchange.sendResponseHeaders(200, htmlBody.getBytes().length);
        httpExchange.getResponseBody().write(htmlBody.getBytes());
        httpExchange.getResponseBody().close();
    }
}
