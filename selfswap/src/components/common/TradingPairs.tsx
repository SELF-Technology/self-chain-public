import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { TokenPair } from '@/components/ui/TokenPair'

export function TradingPairs() {
  const pairs = [
    {
      base: 'SELF',
      quote: 'USDC',
      price: '1.23',
      volume: '123,456',
      liquidity: '567,890',
    },
    {
      base: 'ETH',
      quote: 'SELF',
      price: '0.85',
      volume: '987,654',
      liquidity: '456,789',
    },
  ]

  return (
    <Card className="bg-surface">
      <h2 className="text-xl font-bold mb-4">Trading Pairs</h2>
      <div className="space-y-4">
        {pairs.map((pair) => (
          <TokenPair
            key={`${pair.base}-${pair.quote}`}
            base={pair.base}
            quote={pair.quote}
            price={pair.price}
            volume={pair.volume}
            liquidity={pair.liquidity}
          />
        ))}
      </div>
      <div className="mt-6">
        <Button className="w-full bg-primary hover:bg-blue-600">
          Add Liquidity
        </Button>
      </div>
    </Card>
  )
}
