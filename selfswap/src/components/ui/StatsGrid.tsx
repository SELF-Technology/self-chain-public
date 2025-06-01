import { ReactNode } from 'react'

interface StatProps {
  label: string
  value: string
  change: string
}

interface StatsGridProps {
  stats: StatProps[]
}

export function StatsGrid({ stats }: StatsGridProps) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      {stats.map((stat) => (
        <div
          key={stat.label}
          className="bg-surface rounded-lg p-6 hover:bg-gray-700 transition-colors duration-200"
        >
          <div className="flex justify-between items-center">
            <span className="text-sm text-gray-400">{stat.label}</span>
            <span
              className={`text-sm ${
                stat.change.startsWith('+') ? 'text-green-400' : 'text-red-400'
              }`}
            >
              {stat.change}
            </span>
          </div>
          <div className="mt-2">
            <span className="text-2xl font-bold">{stat.value}</span>
          </div>
        </div>
      ))}
    </div>
  )
}
