package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.Action;
import codingblackfemales.action.NoAction;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.BidLevel;
import messages.order.Side;

import java.util.LinkedList;
import java.util.Queue;

public class SMAAlgoLogic implements AlgoLogic {
    private static final long BUY_THRESHOLD = 90L;
    private static final long SELL_THRESHOLD = 110L;
    private static final int SMA_PERIOD = 5; // Time period for the Simple Moving Average

    private Queue<Long> priceWindow = new LinkedList<>();
    private long sma;

    @Override
    public Action evaluate(SimpleAlgoState state) {
        // Ensure we have at least one bid level
        if (state.getBidLevels() > 0) {
            BidLevel bestBid = state.getBidAt(0);
            long currentPrice = bestBid.getPrice();

            // Add the current price to the window
            priceWindow.add(currentPrice);

            // Maintain the fixed period for SMA
            if (priceWindow.size() > SMA_PERIOD) {
                priceWindow.poll(); // Remove the oldest price
            }

            // Calculate the SMA
            sma = calculateSMA();

            // Trading logic based on SMA
            if (sma <= BUY_THRESHOLD) {
                return new CreateChildOrder(Side.BUY, currentPrice, 50L);
            } else if (sma >= SELL_THRESHOLD) {
                // Cancel the first active child order (for simplicity in this example)
                if (!state.getActiveChildOrders().isEmpty()) {
                    ChildOrder orderToCancel = state.getActiveChildOrders().get(0);
                    return new CancelChildOrder(orderToCancel);
                }
            }
        }

        return new NoAction(); // Default return if no conditions are met
    }

    private long calculateSMA() {
        if (priceWindow.isEmpty()) return 0;

        long sum = 0L;
        for (long price : priceWindow) {
            sum += price;
        }
        return sum / priceWindow.size();
    }
}
