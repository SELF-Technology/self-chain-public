import { ReactNode } from 'react'

interface TokenPairProps {
  base: string
  quote: string
  price: string
  volume: string
  liquidity: string
}

export function TokenPair({ base, quote, price, volume, liquidity }: TokenPairProps) {
  return (
    <div className="flex items-center justify-between">
      <div className="flex items-center space-x-4">
        <div className="flex items-center space-x-2">
          <span className="text-xl font-bold">{base}</span>
          <span className="text-gray-400">/</span>
          <span className="text-xl font-bold">{quote}</span>
        </div>
        <span className="text-2xl font-bold">${price}</span>
      </div>
      <div className="flex items-center space-x-4">
        <div className="flex items-center space-x-2">
          <span className="text-sm text-gray-400">Volume:</span>
          <span className="text-sm">${volume}</span>
        </div>
        <div className="flex items-center space-x-2">
          <span className="text-sm text-gray-400">Liquidity:</span>
          <span className="text-sm">${liquidity}</span>
        </div>
      </div>
    </div>
  )
}
