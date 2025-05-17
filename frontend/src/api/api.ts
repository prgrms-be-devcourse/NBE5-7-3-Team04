import axios, { InternalAxiosRequestConfig, AxiosResponse, AxiosError } from 'axios'

// API 기본 URL
export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL
export const CLOUDFRONT_URL = process.env.NEXT_PUBLIC_CLOUDFRONT_URL

// 로그인 관련 API URL
const AUTH_API_URL = "http://localhost:8080"

// 개발 환경에서 API 요청 실패 시 사용할 모의 데이터
const MOCK_DATA_ENABLED = false

// Axios 인스턴스 생성
export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10초 타임아웃 설정
})

// 요청 인터셉터
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 응답 인터셉터
api.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error: AxiosError) => {
    if (error.response) {
      // 서버가 응답을 반환한 경우
      console.error('Response error:', {
        status: error.response.status,
        data: error.response.data,
        headers: error.response.headers
      })
      
      if (error.response.status === 401) {
        // 토큰이 만료되었거나 유효하지 않은 경우
        localStorage.removeItem('token')
        window.location.href = '/login'
      }
    } else if (error.request) {
      // 요청은 보냈지만 응답을 받지 못한 경우
      console.error('No response received:', error.request)
    } else {
      // 요청 설정 중에 에러가 발생한 경우
      console.error('Request setup error:', error.message)
    }
    return Promise.reject(error)
  }
)

// API 함수들
export async function getPerformances(page = 0, size = 10) {
  const response = await api.get(`/users/performances?page=${page}&size=${size}`)
  return response.data
}

export async function searchPerformances(params: {
  title?: string
  venue?: string
  start?: string
  end?: string
  category?: string
  page?: number
  size?: number
  sort?: string[]
}) {
  const queryParams = new URLSearchParams()
  if (params.title) queryParams.append('title', params.title)
  if (params.venue) queryParams.append('venue', params.venue)
  if (params.start) queryParams.append('start', params.start)
  if (params.end) queryParams.append('end', params.end)
  if (params.category) queryParams.append('category', params.category)
  if (params.page !== undefined) queryParams.append('page', params.page.toString())
  if (params.size !== undefined) queryParams.append('size', params.size.toString())
  if (params.sort) params.sort.forEach(sort => queryParams.append('sort', sort))

  const response = await api.get(`/users/search?${queryParams.toString()}`)
  return response.data
}

export async function getPerformanceDetail(performanceId: number | string) {
  const response = await api.get(`/users/performances/${performanceId}`)
  return response.data
}

export async function createReservation(data: { scheduleId: number; quantity: number }) {
  const response = await api.post('/reservations', data)
  return response.data
}

export async function cancelReservation(reservationId: number | string) {
  const response = await api.post(`/reservations/${reservationId}/cancel`)
  return response.data
}

export async function getUserReservations(page = 0, size = 10) {
  const response = await api.get(`/users/reservations?page=${page}&size=${size}`)
  return response.data
}

export async function getReservationDetail(reservationId: number | string) {
  const response = await api.get(`/users/reservations/${reservationId}`)
  return response.data
}

export async function updateRefundBankInfo(data: {
  refundId: number
  account: string
  bank: string
  depositorName: string
}) {
  const response = await api.put(`/users/refunds/${data.refundId}/bank-info`, {
    account: data.account,
    bank: data.bank,
    depositorName: data.depositorName
  })
  return response.data
}

export async function addBookmark(performanceId: number | string) {
  const response = await api.post(`/users/bookmarks/${performanceId}`)
  return response.data
}

export async function removeBookmark(performanceId: number | string) {
  const response = await api.delete(`/users/bookmarks/${performanceId}`)
  return response.data
}

export async function createReview(data: {
  performanceId: number
  scheduledId: number
  comments: string
  rating?: number
}) {
  const response = await api.post(`/users/performances/${data.performanceId}/reviews`, {
    scheduledId: data.scheduledId,
    comments: data.comments,
    rating: data.rating
  })
  return response.data
}

export async function getReviews(performanceId: number | string, page = 0, size = 5) {
  const response = await api.get(`/users/performances/${performanceId}/reviews?page=${page}&size=${size}`)
  return response.data
}

export async function getUserInfo() {
  const response = await api.get('/users/me')
  return response.data
}

export async function submitManagerRequest() {
  const response = await api.post('/users/manager-request')
  return response.data
}

export async function userOnboarding(data: { phoneNumber: string; email: string }) {
  const response = await api.post('/users/onboarding', data)
  return response.data
}

export const getSocialLoginUrl = (provider: string) => {
  return `${AUTH_API_URL}/oauth2/authorization/${provider}`
} 