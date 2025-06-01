import { createSlice, PayloadAction } from '@reduxjs/toolkit'

interface WalletState {
  isConnected: boolean
  address: string | null
  chainId: string | null
  balance: string | null
  loading: boolean
  error: string | null
}

const initialState: WalletState = {
  isConnected: false,
  address: null,
  chainId: null,
  balance: null,
  loading: false,
  error: null,
}

const walletSlice = createSlice({
  name: 'wallet',
  initialState,
  reducers: {
    connectWallet: (state, action: PayloadAction<string>) => {
      state.address = action.payload
      state.isConnected = true
    },
    disconnectWallet: (state) => {
      state.address = null
      state.isConnected = false
      state.balance = null
    },
    updateChainId: (state, action: PayloadAction<string>) => {
      state.chainId = action.payload
    },
    updateBalance: (state, action: PayloadAction<string>) => {
      state.balance = action.payload
    },
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.loading = action.payload
    },
    setError: (state, action: PayloadAction<string | null>) => {
      state.error = action.payload
    },
  },
})

export const {
  connectWallet,
  disconnectWallet,
  updateChainId,
  updateBalance,
  setLoading,
  setError,
} = walletSlice.actions

export default walletSlice.reducer
