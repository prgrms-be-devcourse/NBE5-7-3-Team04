import { api } from "./api"
import { AxiosError } from "axios"

export interface Performance {
  id: number
  title: string
  category: string
  venue: string
  startDate: string
  endDate: string
  price: number
  fileUrl: string | null
  image: string
}

export interface PerformancePageResponse {
  content: Performance[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export async function getPerformances(page = 0, size = 10): Promise<PerformancePageResponse> {
  try {
    console.log('Fetching performances with params:', { page, size })
    const response = await api.get(`/users/performances?page=${page}&size=${size}`)
    console.log('API Response:', response.data)
    
    if (!response.data || !Array.isArray(response.data.content)) {
      console.error('Invalid response format:', response.data)
      throw new Error('Invalid response format from server')
    }
    
    return response.data
  } catch (error) {
    console.error('Error in getPerformances:', error)
    if (error instanceof AxiosError && error.response) {
      console.error('Server response:', {
        status: error.response.status,
        data: error.response.data
      })
    }
    throw error
  }
}

export async function getPerformanceDetail(id: string | number): Promise<Performance> {
  try {
    console.log('Fetching performance detail for id:', id)
    const response = await api.get(`/users/performances/${id}`)
    console.log('Performance detail response:', response.data)
    return response.data
  } catch (error) {
    console.error('Error in getPerformanceDetail:', error)
    if (error instanceof AxiosError && error.response) {
      console.error('Server response:', {
        status: error.response.status,
        data: error.response.data
      })
    }
    throw error
  }
}