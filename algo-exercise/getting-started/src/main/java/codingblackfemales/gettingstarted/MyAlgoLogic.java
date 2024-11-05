package codingblackfemales.gettingstarted;

import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.Action;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.util.Util;
import messages.order.Side;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

    private Map<Long, Long> buyPrices = new HashMap<>();

    
    @Override
    public Action evaluate(SimpleAlgoState state) {
        var orderBookAsString = Util.orderBookToString(state);
        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);

        BidLevel bestBid = state.getBidAt(0);
        AskLevel bestAsk = state.getAskAt(0);

        if (bestBid == null || bestAsk == null || isInvalidPrice(bestBid.price) || isInvalidPrice(bestAsk.price)) {
            logger.warn("Invalid or incomplete market data received.");
            return NoAction.NoAction;
        }

        long bestBidPrice = bestBid.price;
        long bestAskPrice = bestAsk.price;
        
        var activeOrders = state.getActiveChildOrders();

        if (shouldBuy(bestBidPrice, activeOrders)) {
            return createBuyOrder(bestBidPrice);
        } else if (shouldSell(bestAskPrice, activeOrders)) {
            return processSellOrder(bestAskPrice, activeOrders);
        }

        return NoAction.NoAction;
    }

    private boolean shouldBuy(long bestBidPrice, List<ChildOrder>activeOrders) {
        return bestBidPrice < MyAlgoLogicConfig.BUY_THRESHOLD && 
               activeOrders.stream().noneMatch(order -> order.getPrice() == bestBidPrice && order.getSide() == Side.BUY);
    }

    private Action createBuyOrder(long bestBidPrice) {
        logger.info("Creating buy order at price: " + bestBidPrice);
        buyPrices.put(bestBidPrice, bestBidPrice);
        return new CreateChildOrder(Side.BUY, MyAlgoLogicConfig.ORDER_QUANTITY, bestBidPrice);
    }

    private boolean shouldSell(long bestAskPrice, List<ChildOrder>activeOrders) {
        return bestAskPrice > MyAlgoLogicConfig.SELL_THRESHOLD &&
               activeOrders.stream().anyMatch(order -> order.getSide() == Side.BUY && order.getPrice() <= bestAskPrice);
    }

    private Action processSellOrder(long bestAskPrice, List<ChildOrder> activeOrders) {
        var orderToCancel = activeOrders.stream()
                .filter(order -> order.getSide() == Side.BUY && order.getPrice() <= bestAskPrice)
                .findFirst()
                .orElse(null);

        if (orderToCancel != null) {
            long buyPrice = buyPrices.getOrDefault(orderToCancel.getOrderId(), 0L);
            long profitOrLoss = handleProfitOrLoss(buyPrice, bestAskPrice);
            return bookProfitOrLoss(profitOrLoss, bestAskPrice, orderToCancel);
        }
        return NoAction.NoAction;
    }

    private long handleProfitOrLoss(long buyPrice, long bestAskPrice) {
        return (bestAskPrice - buyPrice) * MyAlgoLogicConfig.ORDER_QUANTITY;
    }

    private Action bookProfitOrLoss(long profitOrLoss, long bestAskPrice, ChildOrder orderToCancel) {
        if (profitOrLoss > 0) {
            logger.info("Selling at price: " + bestAskPrice + ", Profit: " + profitOrLoss);
            buyPrices.remove(orderToCancel.getPrice());
            return new CancelChildOrder(orderToCancel);
        } else if (profitOrLoss == 0) {
            logger.info("Order at price: " + orderToCancel.getPrice() + " has no profit. No action taken.");
            return NoAction.NoAction;
        } else {
            long loss = -profitOrLoss;
            logger.info("Cancelling order at price: " + orderToCancel.getPrice() + ", Loss: " + loss);
            buyPrices.remove(orderToCancel.getOrderId());
            return new CancelChildOrder(orderToCancel);
        }
    }

    private boolean isInvalidPrice(long price) {
        return price <= 0;
    }
}