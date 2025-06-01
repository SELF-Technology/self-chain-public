import { Line } from 'react-chartjs-2'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
)

const volumeData = {
  labels: [
    '12:00',
    '13:00',
    '14:00',
    '15:00',
    '16:00',
    '17:00',
    '18:00',
    '19:00',
  ],
  datasets: [
    {
      label: 'Volume',
      data: [65, 59, 80, 81, 56, 55, 40, 65],
      borderColor: '#4F46E5',
      backgroundColor: '#4F46E5',
      tension: 0.4,
    },
  ],
}

const volumeOptions = {
  responsive: true,
  plugins: {
    legend: {
      position: 'top' as const,
    },
    title: {
      display: false,
    },
  },
  scales: {
    y: {
      beginAtZero: true,
      grid: {
        color: '#4B5563',
      },
      ticks: {
        color: '#D1D5DB',
      },
    },
    x: {
      grid: {
        color: '#4B5563',
      },
      ticks: {
        color: '#D1D5DB',
      },
    },
  },
}

export function VolumeChart() {
  return <Line data={volumeData} options={volumeOptions} />
}
