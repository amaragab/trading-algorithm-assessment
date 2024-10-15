import { MarketDepthRow } from './MarketDepthFeature';
import { FaArrowUp, FaArrowDown } from 'react-icons/fa'; // FontAwesome icons for arrows



interface MarketDepthPanelProps {
    data: MarketDepthRow[];
  }
  

const getWidthBasedOnQuantity = (quantity: number) => {
  const maxQuantityScale = 4000;
  const minWidth = 40;
  const calculatedWidth = (quantity / maxQuantityScale) * 100; // Percentage width

 
  return Math.max(minWidth, calculatedWidth); 
};

  export const MarketDepthPanel = (props: MarketDepthPanelProps) => {
    const { data } = props;  
  
    return (
      <table className="MarketDepthPanel">
        <thead>
          <tr>
          <th></th> {/* New column header for row numbering */}
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
      <td>{index}</td> 
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