package codingblackfemales.gettingstarted;

import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.Action;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.util.Util;
import messages.order.Side;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

    //Threshold for buying shares, algo will consider buying when the price is at or below this value 
    private static final long BUY_THRESHOLD = 100L;
    //Threshold for selling, if reached or exceeded algo will consider selling
    private static final long SELL_THRESHOLD = 120L;
    //Quantity to buy or sell when an action is triggered
    private static final long ORDER_QUANTITY = 50L;

    // Map to store the buy price against the orderId
    private Map<Long, Long> buyPrices = new HashMap<>();

    @Override
    public Action evaluate(SimpleAlgoState state) {

        var orderBookAsString = Util.orderBookToString(state);

        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);
        

        // Fetch the best bid (highest price buyers are willing to pay)
        BidLevel bestBid = state.getBidAt(0);
        long bestBidPrice = bestBid.price;

        // Fetch the best ask (lowest price sellers are willing to accept)
        AskLevel bestAsk = state.getAskAt(0);
        long bestAskPrice = bestAsk.price;

        logger.info("Best Bid Price: " + bestBidPrice);
    logger.info("Best Ask Price: " + bestAskPrice);
        // Retrieve active child orders
        var activeOrders = state.getActiveChildOrders();
        logger.info("Active Orders: " + activeOrders);

        //Algo checks if the current best bid price is below a certain threshold, if this condition is met and there is no active buy order at that price, 
        //algo creates a new child order to buy a specified quantity of shares at the best bid price.
        if (bestBidPrice < BUY_THRESHOLD) {
        if (activeOrders.stream().noneMatch(order -> order.getPrice() == bestBidPrice && order.getSide() == Side.BUY)) {
         //creates a new child order with the specified side(buy), quantity and price. A new order is only created if there is no existing order at that price.
         //this prevents duplicate orders. 
         logger.info("Creating buy order at price: " + bestBidPrice);
         
         //track the buy price.
         buyPrices.put(bestBidPrice, bestBidPrice);  
        return new CreateChildOrder(Side.BUY, ORDER_QUANTITY, bestBidPrice);
    } 
     }

     //Algo checks if current best ask price is above a certain threshold, if this condition is met, it looks for any existing buy orders that could be sold at this price.
     //When such an order is found, algo cancels the existing buy order to be able to place a sell order or avoid holding unto it during unfavourable market conditions
     if (bestAskPrice > SELL_THRESHOLD) {
        var orderToCancel = activeOrders.stream()
            .filter(order -> order.getSide() == Side.BUY && order.getPrice() <= bestAskPrice)
            .findFirst()
            .orElse(null);
    
            //if order to cancel is not null, then an order has been identified for cancellation which could be due to unfavourable market conditions
        if (orderToCancel != null) {
            
            //to calculate profit or loss
            long buyPrice = buyPrices.getOrDefault(orderToCancel.getOrderId(), 0L);
            long profit = (bestAskPrice - buyPrice) * ORDER_QUANTITY;
            //Makes sure algo doesn't attempt to sell without having a corresponding buy order 

           // Handle profit scenarios
        if (profit > 0) {
            logger.info("Cancelling order at price: " + orderToCancel.getPrice() + ", Profit: " + profit);
            buyPrices.remove(buyPrice); // remove buy price if order is canceled

            return new CancelChildOrder(orderToCancel);

        } else if (profit == 0) {
            logger.info("Order at price: " + orderToCancel.getPrice() + " has no profit. No action taken.");
            return NoAction.NoAction; 
        } else { // profit < 0
            long loss = -profit;
            logger.info("Cancelling order at price: " + orderToCancel.getPrice() + ", Loss: " + loss);
            buyPrices.remove(buyPrice); // remove buy price if order is canceled
            
            return new CancelChildOrder(orderToCancel);
        }
    }
    }

    //Default if no actions are met.
        return NoAction.NoAction;
}
}

