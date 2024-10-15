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

public class SMAAlgoLogic implements AlgoLogic{
    private static final long BUY_THRESHOLD = 90L;
    private static final long SELL_THRESHOLD = 110L;
    private static final int SMA_PERIOD = 5; // Time period for the Simple Moving Average

    private Queue<Long> priceWindow = new LinkedList<>();
    private long sma;

    @Override
    public Action evaluate(SimpleAlgoState state) {
        // Get the best bid level (index 0)
        if (state.getBidLevels() > 0) { 
            BidLevel bestBid = state.getBidAt(0); 
            long currentPrice = bestBid.getPrice();

        // Add current price to the window
        priceWindow.add(currentPrice);

        // Maintain the fixed period for SMA
        if (priceWindow.size() > SMA_PERIOD) {
            priceWindow.poll(); // Remove the oldest price
        }

        
        sma = calculateSMA();

        // Trading logic based on SMA
        if (sma <= BUY_THRESHOLD) {
            return new CreateChildOrder(Side.BUY, currentPrice, 50L);
        } else if (sma >= SELL_THRESHOLD) {
            // Cancel all child orders
            for (ChildOrder order : state.getActiveChildOrders()) {
                return new CancelChildOrder(order); 
            }
        }
    }
        return new NoAction();
    
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
