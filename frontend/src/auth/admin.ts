import { create } from 'zustand'

interface AdminAuthState {
  isAuthenticated: boolean
  isLoading: boolean
  setIsAuthenticated: (isAuthenticated: boolean) => void
}

export const useAdminAuth = create<AdminAuthState>((set) => ({
  isAuthenticated: false,
  isLoading: true,
  setIsAuthenticated: (isAuthenticated) => set({ isAuthenticated, isLoading: false }),
}))

export const adminLogout = () => {
  useAdminAuth.getState().setIsAuthenticated(false)
  // 추가적인 로그아웃 로직이 필요한 경우 여기에 구현
} 