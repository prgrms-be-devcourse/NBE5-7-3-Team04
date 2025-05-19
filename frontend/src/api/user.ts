import { getToken } from "@/src/auth/user"

const API_BASE = process.env.NEXT_PUBLIC_API_URL

export const canRequestManagerRole = async () => {
  const token = getToken()
  if (!token) {
    window.location.href = '/login'
    throw new Error('Unauthorized')
  }

  const response = await fetch(`${API_BASE}/users/manager-status`, {
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    }
  })
  
  if (response.status === 401) {
    window.location.href = '/login'
    throw new Error('Unauthorized')
  }
  
  return response.json()
}

export const submitManagerRequest = async () => {
  const token = getToken()
  if (!token) {
    window.location.href = '/login'
    throw new Error('Unauthorized')
  }

  const response = await fetch(`${API_BASE}/users/manager-request`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    }
  })
  
  if (response.status === 401) {
    window.location.href = '/login'
    throw new Error('Unauthorized')
  }

  if (!response.ok) {
    throw new Error('신청 중 오류가 발생했습니다.')
  }

  // 204 No Content 응답이므로 JSON 파싱하지 않음
  return null
}