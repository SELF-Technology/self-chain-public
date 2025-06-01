import { ReactNode } from 'react'

interface TokenPriceProps {
  symbol: string
  price: string
  change: string
  changeType: 'positive' | 'negative' | 'neutral'
}

export function TokenPrice({ symbol, price, change, changeType }: TokenPriceProps) {
  const changeClass = changeType === 'positive' ? 'text-success' : changeType === 'negative' ? 'text-error' : 'text-gray-400'

  return (
    <div className="flex items-center justify-between">
      <div>
        <h3 className="text-xl font-bold">{symbol}</h3>
        <p className="text-2xl font-bold">${price}</p>
      </div>
      <div className={`flex items-center ${changeClass}`}>
        <span className="text-sm">{changeType === 'neutral' ? '' : changeType === 'positive' ? '+' : '-'}</span>
        <span className="text-sm">{Math.abs(parseFloat(change)).toFixed(2)}%</span>
      </div>
    </div>
  )
}
