import { PriceCell } from './PriceCell';
import { MarketDepthRow } from '../type';
import { FaArrowUp, FaArrowDown } from 'react-icons/fa'; // FontAwesome icons for arrows


interface MarketDepthPanelProps {
    data: MarketDepthRow[];
  }
  
// Function to determine color based on price
const getWidthBasedOnQuantity = (quantity: number) => {
  const maxQuantity = 4000; // Define a maximum value for scaling
  const minWidth = 40; // Set a minimum width in percentage
  const calculatedWidth = (quantity / maxQuantity) * 100; // Percentage width

  // Ensure minimum width and limit the maximum width to 100%
  return Math.max(minWidth, calculatedWidth); 
};

  export const MarketDepthPanel = (props: MarketDepthPanelProps) => {
    const { data } = props;  // Destructure the data prop
  
    return (
      <table className="MarketDepthPanel">
        <thead>
          <tr>
          <th>#</th> {/* New column header for row numbering */}
            <th colSpan={2}>Bid</th>
            <th colSpan={2}>Ask</th>
          </tr>
          <tr>
          <th></th> {/* Empty cell for row numbers */}
            <th>Quantity</th>
            <th>Price</th>
            <th>Price</th>
            <th>Quantity</th>
          </tr>
        </thead>
        <tbody>
          {data.map((row, index) => (
            <tr key={index}>
              {/* Row numbering */}
      <td>{index}</td> {/* Add this to display the row number */}
              {/* Bid Side */}
              <td>
                <div
                  style={{
                    width: `${getWidthBasedOnQuantity(row.bidQuantity)}%`,
                    backgroundColor: 'blue',
                    color: 'white',
                    textAlign: 'right',
                    paddingRight: '3px',
                  }}
                >
                  {row.bidQuantity}
                </div>
              </td>
              <td>
                <span style={{ marginRight: '5px' }}>
                  {row.bid > row.offer ? <FaArrowUp /> : <FaArrowDown />}
                </span>
                {row.bid}
              </td>
  
              {/* Ask Side */}
              <td>
                {row.offer}
                <span style={{ marginLeft: '5px' }}>
                  {row.offer > row.bid ? <FaArrowUp /> : <FaArrowDown />}
                </span>
              </td>
              <td>
                <div
                  style={{
                    width: `${getWidthBasedOnQuantity(row.offerQuantity)}%`,
                    backgroundColor: 'red',
                    color: 'white',
                    textAlign: 'right',
                    paddingRight: '5px',
                  }}
                >
                  {row.offerQuantity}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    );
  };