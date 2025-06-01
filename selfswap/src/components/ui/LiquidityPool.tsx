import { ReactNode } from 'react'

interface LiquidityPoolProps {
  token0: string
  token1: string
  totalLiquidity: string
  apy: string
  yourLiquidity: string
  rewards: string
}

export function LiquidityPool({
  token0,
  token1,
  totalLiquidity,
  apy,
  yourLiquidity,
  rewards,
}: LiquidityPoolProps) {
  return (
    <div className="flex items-center justify-between">
      <div className="flex items-center space-x-4">
        <div className="flex items-center space-x-2">
          <span className="text-xl font-bold">{token0}</span>
          <span className="text-gray-400">/</span>
          <span className="text-xl font-bold">{token1}</span>
        </div>
        <span className="text-2xl font-bold">${totalLiquidity}</span>
      </div>
      <div className="flex items-center space-x-4">
        <div className="flex items-center space-x-2">
          <span className="text-sm text-gray-400">APY:</span>
          <span className="text-sm text-green-400">{apy}</span>
        </div>
        <div className="flex items-center space-x-2">
          <span className="text-sm text-gray-400">Your Liquidity:</span>
          <span className="text-sm">${yourLiquidity}</span>
        </div>
        <div className="flex items-center space-x-2">
          <span className="text-sm text-gray-400">Rewards:</span>
          <span className="text-sm">{rewards}%</span>
        </div>
      </div>
    </div>
  )
}
