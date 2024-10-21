package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import messages.marketdata.Source;
import messages.marketdata.BookUpdateEncoder;
import messages.marketdata.InstrumentStatus;
import messages.marketdata.Venue;
import messages.marketdata.MessageHeaderEncoder;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Test;

import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;


public class MyAlgoBackTest extends AbstractAlgoBackTest {
    private static final Logger logger = LoggerFactory.getLogger(MyAlgoBackTest.class);

    @Override
    public AlgoLogic createAlgoLogic() {
        return new MyAlgoLogic();
    }

    @Test
    public void testBuyCondition() throws Exception {
        // Test case where the best bid price is below the BUY_THRESHOLD, should create a buy order

        send(createMarketTick(90L, 100L));

        // Get the state of the order book after the market tick
        var state = container.getState();

        // Check that a buy order was created
        assertEquals(1, state.getChildOrders().size());
        assertEquals(90L, state.getChildOrders().get(0).getPrice()); 
        assertEquals(50L, state.getChildOrders().get(0).getQuantity()); 

        long filledQuantity = state.getChildOrders().stream()
                                   .map(ChildOrder::getFilledQuantity)
                                   .reduce(Long::sum)
                                   .orElse(0L);

         // No fill should occur immediately as the market price hasn’t reached the buy order price
         assertEquals(0L, filledQuantity);
    }
    private long calculateProfitOrLoss(long buyPrice, long sellPrice, long quantity) {
        return (sellPrice - buyPrice) * quantity;
    }

    @Test
    public void testSellCondition() throws Exception {
 // Send market data where ask price is above the SELL_THRESHOLD.......
         long newBidPrice = 110L;  
         long newAskPrice = 130L;  
         send(createMarketTick(newBidPrice, newAskPrice)); 
         var state = container.getState();
         assertEquals(0, state.getChildOrders().size());

         // Create a buy order to be canceled
         long buyPrice = 93L;  
         long askPrice = 113L; 
         send(createMarketTick(buyPrice, askPrice)); 
         state = container.getState();
         assertEquals(1, state.getChildOrders().size()); 
        logger.info("State after creating buy order: " + state);

    // Calculate expected profit/loss
        long profitOrLoss = calculateProfitOrLoss(buyPrice, newAskPrice, 50L);
        logger.info(profitOrLoss > 0 ? "Expected Profit: " + profitOrLoss : "Expected Loss: " + (-profitOrLoss));

          // Calculate and assert the total filled quantity
         long filledQuantity = state.getChildOrders().stream()
          .map(ChildOrder::getFilledQuantity)
          .reduce(Long::sum)
          .orElse(0L);

        // No fill should occur as the order was canceled
         assertEquals(0L, filledQuantity);

          // Check the active child orders count
        assertEquals(1, state.getActiveChildOrders().size());
    }

    @Test
    public void testNoAction() throws Exception {
        // Test case where no conditions are met, should return NoAction..........
        send(createMarketTick(110L, 115L)); 
        var state = container.getState();
        assertEquals(0, state.getChildOrders().size());

        long filledQuantity = state.getChildOrders().stream()
                                   .map(ChildOrder::getFilledQuantity)
                                   .reduce(Long::sum)
                                   .orElse(0L);

        assertEquals(0L, filledQuantity);
    }

    @Test
    public void testDispatchThroughSequencer() throws Exception {
        // Test the sequence of actions through multiple market ticks

// First tick: Above SELL_THRESHOLD, should cancel the buy order.............
       long newBidPrice = 115L; 
       long newAskPrice = 130L; 
       send(createMarketTick(newBidPrice, newAskPrice));
        assertEquals(0, container.getState().getActiveChildOrders().size());

        // Second tick: Below BUY_THRESHOLD, should create a buy order
        long buyPrice = 95L;  
        long askPrice = 110L;  
        send(createMarketTick(buyPrice, askPrice)); 
        assertEquals(1, container.getState().getChildOrders().size());

       

   // Check profit or loss after cancellation
   long profitOrLoss = calculateProfitOrLoss(buyPrice, newAskPrice, 50L);
   logger.info(profitOrLoss > 0 ? "Expected Profit: " + profitOrLoss : "Expected Loss: " + (-profitOrLoss));


        // Third tick: No action should be taken (prices within thresholds)
        send(createMarketTick(105L, 115L)); // bidPrice=105L, askPrice=115L
        assertEquals(1, container.getState().getActiveChildOrders().size());

        // Fourth tick: Still no action should be taken (prices within thresholds)
        send(createMarketTick(100L, 120L)); 
        assertEquals(1, container.getState().getActiveChildOrders().size());

        logger.info("Final state after dispatch: " + container.getState());
    }

    @Test
    public void testNegativePrices() throws Exception {
        send(createMarketTick(-10L, 20L));
        var state = container.getState();
        assertEquals(0, state.getChildOrders().size());
    }


    private UnsafeBuffer createMarketTick(long bidPrice, long askPrice) {
        return createSampleMarketData(bidPrice, askPrice);
    }

    private UnsafeBuffer createSampleMarketData(long bidPrice, long askPrice) {
        // This method constructs a market data tick with the given bid and ask prices
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        BookUpdateEncoder encoder = new BookUpdateEncoder();
        MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();

        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);
        headerEncoder.schemaId(BookUpdateEncoder.SCHEMA_ID);
        headerEncoder.version(BookUpdateEncoder.SCHEMA_VERSION);

        encoder.venue(Venue.XLON);
        encoder.instrumentId(123L);
        encoder.source(Source.STREAM);

        encoder.bidBookCount(1)
                .next().price(bidPrice).size(100L);
            
        encoder.askBookCount(1)
                .next().price(askPrice).size(200L);
                

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);

        return directBuffer;
    }
}
