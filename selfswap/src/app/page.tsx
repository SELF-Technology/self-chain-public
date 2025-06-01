import Link from 'next/link'
import { WalletConnect } from '@/components/WalletConnect'
import { TokenPrices } from '@/components/TokenPrices'
import { TradingPairs } from '@/components/TradingPairs'
import { LiquidityPools } from '@/components/LiquidityPools'
import { Analytics } from '@/components/Analytics'

export default function Home() {
  return (
    <main className="min-h-screen bg-background">
      <nav className="bg-surface border-b border-gray-700">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center">
              <Link href="/" className="flex items-center space-x-2">
                <img
                  src="https://docs.self.app/img/SELFwhitelogo.png"
                  alt="SELF Swap"
                  className="h-8 w-auto"
                />
                <span className="text-xl font-bold text-white">SELF Swap</span>
              </Link>
            </div>
            <WalletConnect />
          </div>
        </div>
      </nav>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          <TokenPrices />
          <TradingPairs />
          <LiquidityPools />
        </div>
      </div>

      <Analytics />
    </main>
  )
}
