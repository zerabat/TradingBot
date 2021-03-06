package com.tradingbot.service.channel;

import com.google.gson.Gson;
import com.tradingbot.entity.order.OpenOrder;
import com.tradingbot.entity.order.response.OrderResponse;
import com.tradingbot.service.TradingServiceImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class OrdersServiceImpl implements MessageProcessingI {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrdersServiceImpl.class);

    final
    TradingServiceImpl tradingService;

    final Environment env;

    public OrdersServiceImpl(TradingServiceImpl tradingService, Environment env) {
        this.tradingService = tradingService;
        this.env = env;
    }

    @Override
    public List<WebSocketMessage<String>> processMessage(JSONObject jsonResponse) {

        try {
            JSONObject isError = jsonResponse.getJSONObject("result").getJSONObject("data");
            if (isError.getString("tag").equalsIgnoreCase("err")) {
                String error = isError.getJSONObject("value").getString("code");
                LOGGER.error("Error in orders channel, error code is: " + error);
                return null;
            }
            String type = jsonResponse.getJSONObject("result").getJSONObject("data").getJSONObject("value").getString("type");
            JSONObject ordersObject = jsonResponse.getJSONObject("result").getJSONObject("data").getJSONObject("value").getJSONObject("payload");
            ArrayList<OrderResponse> lastOrders = new ArrayList<>();
            Gson g = new Gson();
            if (type.equals("snapshot")) {
                JSONArray jsonArray = ordersObject.toJSONArray(ordersObject.names());
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        OrderResponse order = g.fromJson((jsonArray).getString(i), OrderResponse.class);
                        if (order.getInstrument() == Integer.parseInt(Objects.requireNonNull(env.getProperty("instrument.code")))) {
                            lastOrders.add(order);
                        }
                    }
                }
                // is update
            } else {
                OrderResponse order = g.fromJson(String.valueOf(ordersObject), OrderResponse.class);
                lastOrders.add(order);
            }

            for (OrderResponse order : lastOrders) {
                // this is in case an order was open and the bot went down
                // so the next time bot starts, it won't create any order until checking for open ones
                if (order.getSide().equals("SELL")) {
                    if (this.tradingService.getSellOrder() == null && (order.getStatus().equals("PARTIALLY_FILLED") || order.getStatus().equals("NEW"))) {
                        this.tradingService.setOrderId(this.tradingService.getOrderId() + 1);
                        OpenOrder oo = new OpenOrder(order, this.tradingService.getOrderId());
                        this.tradingService.setSellOrder(oo);
                    } else if (this.tradingService.getSellOrder() != null) {
                        if (this.tradingService.getSellOrder().getUpdateTime() <= order.getUpdateTime().getSeconds()) {
                            if (tradingService.getSellOrder().getExchangeOrderId() != null) {
                                this.tradingService.updateOrderStatus(order);
                            }
                        }
                    }
                }
                if (order.getSide().equals("BUY")) {
                    if (this.tradingService.getBuyOrder() == null && (order.getStatus().equals("PARTIALLY_FILLED") || order.getStatus().equals("NEW"))) {
                        this.tradingService.setOrderId(this.tradingService.getOrderId() + 1);
                        OpenOrder oo = new OpenOrder(order, this.tradingService.getOrderId());
                        this.tradingService.setBuyOrder(oo);
                    } else if (this.tradingService.getBuyOrder() != null) {
                        if (this.tradingService.getBuyOrder().getUpdateTime() <= order.getUpdateTime().getSeconds()) {
                            if (this.tradingService.getBuyOrder().getExchangeOrderId() != null) {
                                this.tradingService.updateOrderStatus(order);
                            }
                        }
                    }
                }
            }
            if (!this.tradingService.isOrderInitialized()) {
                this.tradingService.setOrderInitialized(true);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
