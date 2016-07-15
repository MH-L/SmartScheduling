package Server;

import Algorithm.RoutePlanningService;
import Core.PerformanceMonitor;
import Test.ConversionTest;
import Util.*;
import com.sun.javafx.perf.PerformanceTracker;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Minghao Liu
 */
public class BalancingHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("Request coming in!");
//        if (!httpExchange.getRequestMethod().equalsIgnoreCase("POST")) {
//            ServerUtil.page404(httpExchange);
//            return;
//        }
        if (PerformanceMonitor.getPerformanceStatus()) {
            String returnMessage = "{\"message\": \"Load Balancing in Progress.\"}";
            httpExchange.sendResponseHeaders(200, returnMessage.getBytes().length);
            httpExchange.getResponseBody().write(returnMessage.getBytes());
            httpExchange.getResponseBody().close();
            return;
        }

        String requestParams = httpExchange.getRequestURI().getQuery();
        Map<String, String> requestMapping = queryToMap(requestParams);
        int minTour = Integer.parseInt(requestMapping.get("minTour"));
        int maxTour = Integer.parseInt(requestMapping.get("maxTour"));
        String dropoffPointsJson = requestMapping.get("points");
        JSONObject allObj = new JSONObject(dropoffPointsJson);
        JSONArray ja = allObj.getJSONArray("all");
        List<SimplifiedTMSOrder> allOrders = new ArrayList<>();
        for (int i = 0; i < ja.length(); i++) {
            JSONObject singleOrder = ja.getJSONObject(i);
            try {
                SimplifiedTMSOrder currentOrder = new SimplifiedTMSOrder(new TMSOrderLabel(
                        singleOrder.getString("orderNumber"), singleOrder.getString("addr")),
                        new LatLon(Double.parseDouble(singleOrder.getString("latitude")),
                                Double.parseDouble(singleOrder.getString("longitude"))));
                allOrders.add(currentOrder);
            } catch (JSONException ee) {
                System.out.println("Malformed order!!!");
                continue;
            }
        }

        ConversionTest.testCoordinateConversion(allOrders);
        FileUtil.exportDistanceMatrix(allOrders);
        RoutePlanningService rp = new RoutePlanningService(allOrders);
        TMSRoutePlan optimal = rp.getOptimalPlan(minTour, maxTour);
        JSONObject returningObject = new JSONObject();
        JSONArray planBreaksArr = new JSONArray();
        JSONArray planPointsArr = new JSONArray();
        for (int ii = 0; ii < optimal.getBreaks().size(); ii++) {
            planBreaksArr.put(optimal.getBreaks().get(ii));
        }

        for (int jj = 0; jj < optimal.getPoints().size(); jj++) {
            SimplifiedTMSOrder curOrder = optimal.getPoints().get(jj);
            JSONObject singleOrderObj = new JSONObject();
            singleOrderObj.put("latitude", curOrder.getLatlon().getLat());
            singleOrderObj.put("longitude", curOrder.getLatlon().getLon());
            singleOrderObj.put("orderAddr", curOrder.getOrderLabel().getOrderAddr());
            singleOrderObj.put("orderCode", curOrder.getOrderLabel().getOrderNumber());
            planPointsArr.put(singleOrderObj);
        }
        returningObject.put("planBreaks", planBreaksArr);
        returningObject.put("dropoffPoints", planPointsArr);

        String returningString = returningObject.toString(4);
        httpExchange.sendResponseHeaders(200, returningString.getBytes("UTF-8").length);
        httpExchange.getResponseBody().write(returningString.getBytes());
        httpExchange.getResponseBody().close();
        System.out.println("Load Balancing Server Running!");
    }

    public static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length > 1) {
                result.put(pair[0], pair[1]);
            } else {
                result.put(pair[0], "");
            }
        }
        return result;
    }
}