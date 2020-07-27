package com.tradingbot.service.channel;

import com.tradingbot.entity.orderbook.OrderBookItem;
import com.tradingbot.service.TradingServiceImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketMessage;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderBookServiceImpl implements MessageProcessingI {


    private static final Logger LOGGER = LoggerFactory.getLogger(OrderBookServiceImpl.class);

    final TradingServiceImpl tradingService;

    public OrderBookServiceImpl(TradingServiceImpl tradingService) {
        this.tradingService = tradingService;
    }

    @Override
    public List<WebSocketMessage<String>> processMessage(JSONObject jsonResponse) {

        try {
            jsonResponse = jsonResponse.getJSONObject("result").getJSONObject("data").getJSONObject("value");
            if (jsonResponse.has("1")){
                jsonResponse = jsonResponse.getJSONObject("1");
                int id = jsonResponse.getInt("instrumentId");
                if (id != 1){
                    LOGGER.error("wrong instrument Id:" +id );
                }else{
                    List<OrderBookItem> asksList = this.buildBookOrderItem(jsonResponse.getJSONArray("asks"));
                    List<OrderBookItem> bidsList = this.buildBookOrderItem(jsonResponse.getJSONArray("bids"));
                    if (!bidsList.isEmpty() && !asksList.isEmpty()){
                        boolean n = this.tradingService.checkForConditions(asksList,bidsList);
                    }
                }
            }

        } catch (JSONException ex){
            LOGGER.error(ex.getMessage());
        }

        return null;
    }

    private List<OrderBookItem> buildBookOrderItem(JSONArray orderItems){
        List<OrderBookItem> itemsList = new ArrayList<>();
        if (orderItems != null) {
            for (int i=0;i<orderItems.length();i++){
                try {
                    JSONObject jsO = orderItems.getJSONObject(i);
                    itemsList.add(new OrderBookItem(jsO));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return itemsList;
    }

}
