import React from 'react';

interface PriceCellProps {
  price: number;
}

export const PriceCell: React.FC<PriceCellProps> = ({ price }) => {
  return (
    <td>
      {price}
    </td>
  );
};