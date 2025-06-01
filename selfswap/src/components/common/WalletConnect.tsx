import { useWallet } from '@/components/providers/WalletProvider'
import { Button } from '@/components/ui/Button'
import { Tooltip } from '@/components/ui/Tooltip'
import { CopyIcon, WalletIcon } from '@heroicons/react/24/outline'

export function WalletConnect() {
  const { connect, disconnect, isConnected, address, balance } = useWallet()

  if (!isConnected) {
    return (
      <Button onClick={connect} className="bg-primary hover:bg-blue-600">
        Connect Wallet
      </Button>
    )
  }

  return (
    <div className="flex items-center space-x-4">
      <div className="flex items-center space-x-2">
        <span className="text-sm text-gray-300">Balance:</span>
        <span className="text-sm font-medium">{balance || '0.00'} SELF</span>
      </div>
      <Tooltip content="Copy address">
        <button
          onClick={() => {
            navigator.clipboard.writeText(address || '')
          }}
          className="p-2 rounded-lg hover:bg-gray-700 transition-colors"
          title="Copy address"
        >
          <CopyIcon className="w-5 h-5 text-gray-400" />
        </button>
      </Tooltip>
      <Button onClick={disconnect} className="bg-red-500 hover:bg-red-600">
        Disconnect
      </Button>
    </div>
  )
}
