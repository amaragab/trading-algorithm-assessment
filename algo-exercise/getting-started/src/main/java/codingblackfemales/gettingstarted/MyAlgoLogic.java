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

     
    private static final long BUY_THRESHOLD = 100L;
    private static final long SELL_THRESHOLD = 120L;
    private static final long ORDER_QUANTITY = 50L;

    
    private Map<Long, Long> buyPrices = new HashMap<>();

    @Override
    public Action evaluate(SimpleAlgoState state) {

        var orderBookAsString = Util.orderBookToString(state);
        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);
        
        BidLevel bestBid = state.getBidAt(0);
        if (bestBid == null) {
            logger.info("No best bid available.");
            return NoAction.NoAction;
        }
        long bestBidPrice = bestBid.price;

        AskLevel bestAsk = state.getAskAt(0);
        if (bestAsk == null) {
            logger.info("No best ask available.");
            return NoAction.NoAction;
        }
        long bestAskPrice = bestAsk.price;

        // Validate prices
        if (isInvalidPrice(bestBidPrice) || isInvalidPrice(bestAskPrice)) {
            logger.warn("Invalid market data received: Bid Price: " + bestBidPrice + ", Ask Price: " + bestAskPrice);
            return NoAction.NoAction; // Reject invalid data
        }


        logger.info("Best Bid Price: " + bestBidPrice);
    logger.info("Best Ask Price: " + bestAskPrice);
        
        var activeOrders = state.getActiveChildOrders();
        logger.info("Active Orders: " + activeOrders);

        if (bestBidPrice < BUY_THRESHOLD) {
        if (activeOrders.stream().noneMatch(order -> order.getPrice() == bestBidPrice && order.getSide() == Side.BUY)) { 
         logger.info("Creating buy order at price: " + bestBidPrice);
         
         buyPrices.put(bestBidPrice, bestBidPrice);  
        return new CreateChildOrder(Side.BUY, ORDER_QUANTITY, bestBidPrice);
    } 
     }

     if (bestAskPrice > SELL_THRESHOLD) {
        var orderToCancel = activeOrders.stream()
            .filter(order -> order.getSide() == Side.BUY && order.getPrice() <= bestAskPrice)
            .findFirst()
            .orElse(null);
    
        if (orderToCancel != null) {
            //Makes sure algo doesn't attempt to sell without having a corresponding buy order
            long buyPrice = buyPrices.getOrDefault(orderToCancel.getOrderId(), 0L);

            long profit = (bestAskPrice - buyPrice) * ORDER_QUANTITY;
            
           // Handle profit scenarios
        if (profit > 0) {
            logger.info("Selling at price: " + bestAskPrice + ", Profit: " + profit);
            buyPrices.remove(buyPrice); // remove buy price if order is canceled
            return new CancelChildOrder(orderToCancel);

        } else if (profit == 0) {
            logger.info("Order at price: " + orderToCancel.getPrice() + " has no profit. No action taken.");
            return NoAction.NoAction; 

        } else { // profit < 0
            long loss = -profit;
            logger.info("Cancelling order at price: " + orderToCancel.getPrice() + ", Loss: " + loss);
            buyPrices.remove(buyPrice); 
            return new CancelChildOrder(orderToCancel);
        }
    }
    }

    //Default if no actions are met.
        return NoAction.NoAction;
}
private boolean isInvalidPrice(long price) {
    return price <= 0; // Add any other invalid conditions here
}
}

