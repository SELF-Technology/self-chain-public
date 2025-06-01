import { Card } from '@/components/ui/Card'
import { StatsGrid } from '@/components/ui/StatsGrid'
import { VolumeChart } from '@/components/ui/VolumeChart'
import { TokenDistribution } from '@/components/ui/TokenDistribution'

export function Analytics() {
  const stats = [
    {
      label: 'Total Volume',
      value: '$1,234,567,890',
      change: '+12.34%',
    },
    {
      label: 'Total Liquidity',
      value: '$987,654,321',
      change: '+8.56%',
    },
    {
      label: 'Active Pairs',
      value: '1,234',
      change: '+5.67%',
    },
    {
      label: 'Active Users',
      value: '8,910',
      change: '+3.45%',
    },
  ]

  return (
    <Card className="bg-surface mt-8">
      <h2 className="text-xl font-bold mb-4">Analytics</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatsGrid stats={stats} />
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        <div className="bg-surface rounded-lg p-6">
          <h3 className="text-lg font-bold mb-4">24h Volume</h3>
          <VolumeChart />
        </div>
        <div className="bg-surface rounded-lg p-6">
          <h3 className="text-lg font-bold mb-4">Token Distribution</h3>
          <TokenDistribution />
        </div>
      </div>
    </Card>
  )
}
