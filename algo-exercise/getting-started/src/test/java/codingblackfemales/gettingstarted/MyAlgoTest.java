package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.action.CreateChildOrder;
import messages.marketdata.Source;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.NoAction;
import messages.marketdata.BookUpdateEncoder;
import messages.marketdata.InstrumentStatus;
import messages.marketdata.Venue;
import messages.order.Side;
import messages.marketdata.MessageHeaderEncoder;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class MyAlgoTest extends AbstractAlgoTest {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoBackTest.class);

    @Override
    public AlgoLogic createAlgoLogic() {
        //this adds your algo logic to the container classes
        return new MyAlgoLogic();
    }

    @Test
    public void testCreateBuyOrder() throws Exception {
        // Test case where the best bid price is below the BUY_THRESHOLD, should create a buy order

        long bestBidPrice = 95L;  // Below the BUY_THRESHOLD of 100L
        long bestAskPrice = 105L;

        //creates a mock instance of SimpleAlgoState with specified bid and ask prices.
        SimpleAlgoState state = mockState(bestBidPrice, bestAskPrice);
        //sends a sample market data message to algo.
        send(createSampleMarketData(bestBidPrice, bestAskPrice));

        var action = createAlgoLogic().evaluate(state);

        assertTrue(action instanceof CreateChildOrder);
        CreateChildOrder createAction = (CreateChildOrder) action;
       String actionString = createAction.toString();
        assertTrue(actionString.contains("side=BUY"));
        assertTrue(actionString.contains("quantity=50"));
        assertTrue(actionString.contains("price=" + bestBidPrice));
    }

    //private long calculateProfitOrLoss(long buyPrice, long sellPrice, long quantity) {
      //  return (sellPrice - buyPrice) * quantity;
    //}

    private long calculateProfitOrLoss(long buyPrice, long sellPrice, long quantity) {
        return (sellPrice - buyPrice) * quantity;
    }

    private void logProfitOrLoss(long profitOrLoss) {
        if (profitOrLoss > 0) {
            logger.info("Expected Profit: " + profitOrLoss);
        } else {
            logger.info("Expected Loss: " + (-profitOrLoss));
        }
    }
    
    @Test
    public void testCancelBuyOrder() throws Exception {
        // Test case where the best ask price is above the SELL_THRESHOLD, should cancel a buy order

        long bestBidPrice = 115L; 
        long bestAskPrice = 125L; // Above SELL_THRESHOLD........
       
//Mock buy order that should get cancelled
        ChildOrder childOrder = mockOrder(Side.BUY, 100L, 110L);
        SimpleAlgoState state = mockState(bestBidPrice, bestAskPrice, Side.BUY, 100L, 110L);

        //send market data with updated ask price 
        send(createSampleMarketData(bestBidPrice, bestAskPrice));

        //evaluate the algo logic
        var action = createAlgoLogic().evaluate(state);

        //assert that the action taken is to cancel the buy order
        assertTrue(action instanceof CancelChildOrder);
        CancelChildOrder cancelAction = (CancelChildOrder) action;

        // Calculate expected profit: (bestAskPrice - buyPrice) * quantity
        long expectedProfitOrLoss = calculateProfitOrLoss(100L, bestAskPrice, 50L);
        logProfitOrLoss(expectedProfitOrLoss);

        

        assertNotNull(cancelAction.toString());
    }

    @Test
    public void testNoAction() throws Exception {
        // Test case where no conditions are met, should return NoAction

        long bestBidPrice = 110L; // Above BUY_THRESHOLD
        long bestAskPrice = 115L; // Below SELL_THRESHOLD

        SimpleAlgoState state = mockState(bestBidPrice, bestAskPrice);
        send(createSampleMarketData(bestBidPrice, bestAskPrice));

        var action = createAlgoLogic().evaluate(state);

        assertTrue(action instanceof NoAction);
    }

    @Test
    public void testDispatchThroughSequencer() throws Exception {
       // Test case to check if orders are created and canceled correctly through sequencer

        // First tick: Below BUY_THRESHOLD, should create a buy order
        send(createTick1());
        assertEquals(1, container.getState().getChildOrders().size());

        // Second tick: Above SELL_THRESHOLD, should cancel the buy order
        send(createTick2());
        assertEquals(0, container.getState().getActiveChildOrders().size());

        // Log profit or loss based on ask price and buy price......
        long buyPrice = 95L;
        long sellPrice = 125L;
        long profitOrLoss = calculateProfitOrLoss(buyPrice, sellPrice, 50L);
        logProfitOrLoss(profitOrLoss);


        // Third tick: No action should be taken (prices within thresholds)
        send(createTick3());
        assertEquals(1, container.getState().getChildOrders().size());

        // Fourth tick: Still no action should be taken (prices within thresholds)
        send(createTick4());
        assertEquals(0, container.getState().getActiveChildOrders().size());
    }

    private SimpleAlgoState mockState(long bestBidPrice, long bestAskPrice) {
        // Mock state with no active orders
        return mockState(bestBidPrice, bestAskPrice, null, 0L, 0L);
    }

    private SimpleAlgoState mockState(long bestBidPrice, long bestAskPrice, Side side, long orderPrice, long filledPrice) {
        SimpleAlgoState state = mock(SimpleAlgoState.class);

        BidLevel bidLevel = new BidLevel();
        bidLevel.setPrice(bestBidPrice);
        bidLevel.setQuantity(100L);

        AskLevel askLevel = new AskLevel();
        askLevel.setPrice(bestAskPrice);
        askLevel.setQuantity(200L);

        when(state.getBidAt(0)).thenReturn(bidLevel);
        when(state.getAskAt(0)).thenReturn(askLevel);

        if (side != null) {
            var childOrder = mockOrder(side, orderPrice, filledPrice);
            when(state.getActiveChildOrders()).thenReturn(List.of(childOrder));
        } else {
            when(state.getActiveChildOrders()).thenReturn(List.of());
        }

        return state;
    }

    private ChildOrder mockOrder(Side side, long price, long filledQuantity) {
        ChildOrder order = mock(ChildOrder.class);
        when(order.getSide()).thenReturn(side);
        when(order.getPrice()).thenReturn(price);
        when(order.getFilledQuantity()).thenReturn(filledQuantity);
        when(order.getOrderId()).thenReturn(123L);
        return order;
    }

    private UnsafeBuffer createSampleMarketData(long bidPrice, long askPrice) {
        // This method constructs a market data tick with the given bid and ask prices
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        BookUpdateEncoder encoder = new BookUpdateEncoder();
       //messages.marketdata.MessageHeaderEncoder headerEncoder = new messages.marketdata.MessageHeaderEncoder();

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

    private UnsafeBuffer createTick1() {
        // Simulates a market condition where the bid price is below the BUY_THRESHOLD
        return createSampleMarketData(95L, 110L);  // bidPrice=95L, askPrice=110L
    }

    private UnsafeBuffer createTick2() {
        // Simulates a market condition where the ask price is above the SELL_THRESHOLD
        return createSampleMarketData(115L, 125L);  // bidPrice=115L, askPrice=125L
    }

    private UnsafeBuffer createTick3() {
        // Simulates a market condition where no thresholds are crossed
        return createSampleMarketData(105L, 115L);  // bidPrice=105L, askPrice=115L
    }

    private UnsafeBuffer createTick4() {
        // Simulates a market condition with minimal price movement (no action should be taken)
        return createSampleMarketData(100L, 120L);  // bidPrice=100L, askPrice=120L
    }
}
