Market Depth Trading Algorithm Project

The goal of this project is to develop a trading algorithm that uses market depth data to automate the buying and selling of financial instruments.The main objective is to develop a strong and effective trading strategy that minimizes risks and optimizes earnings.
By analyzing bid and ask prices, along with their respective quantities, the algorithm makes informed decisions about when to enter and exit trades. This approach addresses common challenges in trading, such as timing the market and executing orders quickly, allowing traders to capitalize on favorable price movements without constant manual oversight.
Incorporating various logic components, the project aims to track performance through profit and loss calculations and facilitate efficient trading strategies by visualizing market depth data, ensuring a comprehensive overview of trading activities. This allows users to evaluate the effectiveness of the algorithm and refine their strategies over time.

CLASSES

MYAlgoLogic

Overview
MyAlgoLogic implements the AlgoLogic interface and encapsulates the algorithm's decision-making logic for trading operations. It evaluates market conditions and decides when to create or cancel child orders based on predefined thresholds for buying and selling.

Key Features
* Buy and Sell Thresholds:
    * BUY_THRESHOLD: The algorithm initiates a buy order when the best bid price is below this threshold (set at 100). 
    * SELL_THRESHOLD: The algorithm considers selling when the best ask price exceeds this threshold (set at 120). 
* Order Quantity: The algorithm operates with a fixed quantity for each trade (set at 50 shares).
* Order Management:
    * Buy Orders:
        * The algorithm checks if the current best bid price is below the BUY_THRESHOLD.
        * If no active buy orders exist at that price, it creates a new buy order.
        * It tracks the buy prices using a map to facilitate profit/loss calculations when selling.
    * Sell Orders:
        * The algorithm assesses whether the best ask price is above the SELL_THRESHOLD.
        * It searches for active buy orders that could be canceled to facilitate a sell order.
* Profit and Loss Calculation:
    * Before canceling an existing buy order, the algorithm calculates the potential profit or loss based on the difference between the best ask price and the recorded buy price.
    * Actions are taken based on the profit/loss:
        * If profit > 0: Cancels the buy order and logs the profit.
        * If profit == 0: Logs that no action is taken.
        * If profit < 0: Cancels the buy order and logs the loss.
        
Methods
* evaluate(SimpleAlgoState state): This method analyzes the current market state to determine if the algorithm should create or cancel orders. It logs the relevant details and returns the appropriate action (create or cancel an order, or no action).

SMAAlgoLogic

Overview
SMAAlgoLogic introduces a trading algorithm based on the Simple Moving Average (SMA) indicator. This logic aims to smooth out price data over a specific period to identify potential buy and sell signals. The class evaluates price movements against defined thresholds to execute trades automatically. Although this logic was not fully implemented into the test classes, it represents a planned extension to enhance the existing algorithm.

Key Features
* Simple Moving Average (SMA):
Utilizes a moving average over a set time period (SMA_PERIOD = 5) to smooth out short-term fluctuations in market prices.
If the average price over the period is below the BUY_THRESHOLD (90L), a buy order is created.
If the average price is above the SELL_THRESHOLD (110L), all active buy orders are canceled.

Methods
* evaluate(SimpleAlgoState state):
This method evaluates the current state of the market and makes trading decisions based on the SMA value. It checks the best bid price, adds it to the priceWindow, and calculates the SMA. Depending on whether the SMA meets the thresholds, the method returns either a CreateChildOrder, CancelChildOrder, or NoAction.

* calculateSMA():
This utility method calculates the SMA by averaging the prices in the priceWindow. The method maintains a fixed period, ensuring that only recent prices are considered.


MyAlgoTest

Overview
MyAlgoTest is a JUnit test class that validates the behavior of the MyAlgoLogic trading algorithm. It tests various scenarios to ensure the algorithm correctly creates and cancels child orders based on market conditions. The class utilizes mocking to simulate market data and algorithm states for effective unit testing. It includes tests for trade execution, order management, and profit/loss calculations.

Key Features
* Profit and Loss Calculation:
    * Contains methods to calculate and log expected profit or loss based on the buy and sell prices and the order quantity.

Methods
* createAlgoLogic(): Instantiates the MyAlgoLogic class, setting up the environment for testing.
* testCreateBuyOrder(): Tests the creation of a buy order when conditions are met (bid price below BUY_THRESHOLD).
* testCancelBuyOrder(): Tests the cancellation of a buy order when the ask price exceeds the SELL_THRESHOLD and logs the expected profit or loss.
* testNoAction(): Validates that no actions are taken when market conditions do not meet the thresholds.
* testDispatchThroughSequencer(): Tests the sequential processing of market data ticks to ensure proper order management and profit/loss logging.
* Utility Methods:
    * mockState(long bestBidPrice, long bestAskPrice): Mocks the market state with specified bid and ask prices.
    * mockOrder(Side side, long price, long filledQuantity): Creates a mock ChildOrder for testing purposes.
    * createSampleMarketData(long bidPrice, long askPrice): Constructs a mock market data tick with specified bid and ask prices.
Market Condition Simulations:
        * createTick1(): Simulates a market condition where the bid price is below the BUY_THRESHOLD.
        * createTick2(): Simulates a condition where the ask price is above the SELL_THRESHOLD.
        * createTick3(): Simulates a condition where prices are within thresholds (no action should be taken).
        * createTick4(): Simulates minimal price movement, ensuring no action is triggered.


MyAlgoBackTest

Overview
MyAlgoBackTest is a JUnit test class that facilitates backtesting the MyAlgoLogic trading algorithm. It evaluates the algorithm's performance against simulated market data, ensuring it correctly creates and cancels orders based on predefined trading conditions. This class emphasizes the validation of the trading logic's robustness and accuracy, logging expected profits and losses throughout the testing process.

Key Features
* Order Management:
    * Tests the algorithm's ability to create and cancel orders under varying market conditions, ensuring it reacts appropriately to price movements.
* Profit and Loss Calculation:
    * Contains methods to calculate and log expected profit or loss based on the best bid and ask prices, as well as the order quantity.

Methods
* createAlgoLogic():
    * Instantiates the MyAlgoLogic class, establishing the environment necessary for backtesting.
* testBuyCondition():
    * Validates the creation of a buy order when the best bid price falls below the BUY_THRESHOLD.
    * Ensures that the order book reflects this action accurately.
* testSellCondition():
    * Tests the cancellation of a buy order when the best ask price rises above the SELL_THRESHOLD.
    * Logs the expected profit or loss resulting from the cancellation.
* testNoAction():
    * Checks that no orders are created or canceled when market conditions are within thresholds, confirming that the algorithm does not execute unnecessary actions.
* testDispatchThroughSequencer():
    * Evaluates the sequential processing of multiple market ticks to ensure correct order management and accurate profit/loss logging based on changing market conditions.
Utility Methods
* createMarketTick(long bidPrice, long askPrice):
    * Constructs a market data tick with specified bid and ask prices for use in tests.
* createSampleMarketData(long bidPrice, long askPrice):
    * Generates a mock market data tick with the given bid and ask prices, providing a controlled environment for testing the algorithm


Market Depth Feature
This component is responsible for displaying the market depth table, which presents the current bid and ask prices along with their respective quantities.

Imports
* React: The React library is imported to enable the creation of functional components and manage the component's state.
* MarketDepthPanel: This component renders the market depth table.
* MarketDepthPanel.css: This stylesheet contains the styles for the MarketDepthPanel.

MarketDepthPanel.css

The MarketDepthPanel.css file contains the styles for the MarketDepthPanel component, which presents the market depth table.


MarketDepthPanel.tsx 

This file defines the MarketDepthPanel component, which displays a table of market depth information, including bid and ask prices and quantities.

Imports
* react-icons/fa: Imports FaArrowUp and FaArrowDown for arrow indicators next to bid and ask prices.

Functions
* getWidthBasedOnQuantity(quantity: number): A utility function that calculates the width of the colored bars in the table based on the quantity.


LOGIC EXPLANATION

The trading algorithm application is designed to analyze market data, execute trades based on predefined thresholds, manage orders effectively, and provide an interactive user interface (UI) for monitoring and controlling trading activities. Below is a description of the main logic and flow of the application, including both the backend logic and the UI components:

Market Data Processing:
* The application begins by receiving market data ticks, which represent real-time bid and ask prices. These ticks are processed through the MyAlgoLogic class, where they are evaluated against the predefined buy and sell thresholds (BUY_THRESHOLD and SELL_THRESHOLD).

Order Creation and Management:
* When the bid price falls below the BUY_THRESHOLD, the algorithm triggers the creation of a buy order. This process is handled by methods within the MyAlgoLogic class to create a ChildOrder.
* The MyAlgoBackTest class is responsible for testing the logic of order creation. It simulates market conditions and validates that the orders are created and managed correctly.

Order Cancellation:
* If the best ask price exceeds the SELL_THRESHOLD, any existing buy orders are canceled. The algorithm ensures that the orders in the order book are updated accordingly. This functionality is also encapsulated in the MyAlgoLogic class and tested within the MyAlgoBackTest class.
* The logic checks the current market state and assesses whether to cancel orders based on incoming market data, providing real-time responsiveness.

Profit and Loss Calculation:
* The algorithm includes functionality to calculate potential profit or loss from executed trades. This is accomplished through utility methods that compute the difference between the selling price and the buying price, multiplied by the order quantity.
* The MyAlgoTest class validates the correctness of profit and loss calculations through various test cases that simulate different market scenarios.

User Interface (UI):
* The application features a UI built using React, designed to mirror the local website. The UI provides a user-friendly way to view the current state of the market, including bid and ask prices, active orders, and profit/loss status.
* A table component in the UI displays real-time data, allowing users to monitor trading activities visually. The UI responds to user actions, such as clicking buttons to trigger trading operations, and reflects the current market conditions dynamically.

Interaction Between Classes:
* The core logic is encapsulated in the MyAlgoLogic class, which implements the AlgoLogic interface.
* The MyAlgoBackTest and MyAlgoTest classes serve as testing frameworks to ensure the main algorithm behaves as expected under various conditions. They utilize mocking to simulate market data and verify the logic's correctness, allowing for thorough validation before deployment.

Sequential Processing:
* The application processes market data in a sequenced manner, where each tick can trigger multiple actions (creating or canceling orders) based on the current state. The use of methods like testDispatchThroughSequencer() in MyAlgoBackTest demonstrates how the algorithm reacts to a series of market ticks and maintains order integrity.

The trading algorithm functions effectively thanks to the interaction between the UI elements and the backend logic, which allows it to adjust dynamically to market conditions and maintain precise order management and profit/loss tracking. The UI enhances user interaction, providing a seamless experience for monitoring and controlling trades.



