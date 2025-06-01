import { Card } from '@/components/ui/Card'
import { TokenPrice } from '@/components/ui/TokenPrice'

export function TokenPrices() {
  const tokens = [
    { symbol: 'SELF', price: '1.23', change: '0.05', changeType: 'positive' },
    { symbol: 'USDC', price: '1.00', change: '0.00', changeType: 'neutral' },
    { symbol: 'ETH', price: '1,234.56', change: '-0.12', changeType: 'negative' },
  ]

  return (
    <Card className="bg-surface">
      <h2 className="text-xl font-bold mb-4">Token Prices</h2>
      <div className="space-y-4">
        {tokens.map((token) => (
          <TokenPrice
            key={token.symbol}
            symbol={token.symbol}
            price={token.price}
            change={token.change}
            changeType={token.changeType}
          />
        ))}
      </div>
    </Card>
  )
}
