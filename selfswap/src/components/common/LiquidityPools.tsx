import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { LiquidityPool } from '@/components/ui/LiquidityPool'

export function LiquidityPools() {
  const pools = [
    {
      token0: 'SELF',
      token1: 'USDC',
      totalLiquidity: '567,890',
      apy: '12.34%',
      yourLiquidity: '12,345',
      rewards: '0.12',
    },
    {
      token0: 'ETH',
      token1: 'SELF',
      totalLiquidity: '456,789',
      apy: '8.56%',
      yourLiquidity: '8,910',
      rewards: '0.08',
    },
  ]

  return (
    <Card className="bg-surface">
      <h2 className="text-xl font-bold mb-4">Liquidity Pools</h2>
      <div className="space-y-4">
        {pools.map((pool) => (
          <LiquidityPool
            key={`${pool.token0}-${pool.token1}`}
            token0={pool.token0}
            token1={pool.token1}
            totalLiquidity={pool.totalLiquidity}
            apy={pool.apy}
            yourLiquidity={pool.yourLiquidity}
            rewards={pool.rewards}
          />
        ))}
      </div>
      <div className="mt-6">
        <Button className="w-full bg-green-500 hover:bg-green-600">
          Create Pool
        </Button>
      </div>
    </Card>
  )
}
