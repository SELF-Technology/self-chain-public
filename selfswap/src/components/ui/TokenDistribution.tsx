import { Doughnut } from 'react-chartjs-2'
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
} from 'chart.js'

ChartJS.register(ArcElement, Tooltip, Legend)

const distributionData = {
  labels: ['SELF', 'USDC', 'ETH', 'Other'],
  datasets: [
    {
      data: [300, 50, 100, 40],
      backgroundColor: [
        '#4F46E5',
        '#3B82F6',
        '#10B981',
        '#F59E0B',
      ],
      hoverOffset: 4,
    },
  ],
}

const distributionOptions = {
  responsive: true,
  plugins: {
    legend: {
      position: 'top' as const,
    },
    title: {
      display: false,
    },
  },
}

export function TokenDistribution() {
  return <Doughnut data={distributionData} options={distributionOptions} />
}
