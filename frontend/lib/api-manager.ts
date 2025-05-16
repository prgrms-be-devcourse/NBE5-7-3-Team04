// API functions for Performance Manager
import { fetchAPI, getToken } from "./api"

// Base URL for API
const API_BASE_URL = "http://43.201.79.165:8080/api/v1"

// Performance Manager API functions

// Get all performances for the manager
export async function getManagerPerformances(page = 0, size = 10) {
  return fetchAPI(`/managers/performances?page=${page}&size=${size}`)
}

// Search performances for the manager
export async function searchManagerPerformances(params: {
  title?: string
  venue?: string
  start?: string
  end?: string
  status?: string
  page?: number
  size?: number
}) {
  const queryParams = new URLSearchParams()

  if (params.title) queryParams.append("title", params.title)
  if (params.venue) queryParams.append("venue", params.venue)
  if (params.start) queryParams.append("start", params.start)
  if (params.end) queryParams.append("end", params.end)
  if (params.status) queryParams.append("status", params.status)
  queryParams.append("page", String(params.page || 0))
  queryParams.append("size", String(params.size || 10))

  return fetchAPI(`/managers/performances/search?${queryParams.toString()}`)
}

// Get performance details for the manager
export async function getManagerPerformanceDetails(performanceId: number | string) {
  return fetchAPI(`/managers/performances/${performanceId}`)
}

// Register a new performance
export async function registerPerformance(data: {
  title: string
  venue: string
  price: number
  totalSeats: number
  category: string
  startDate: string
  endDate: string
  description: string
  fileId?: number
}) {
  return fetchAPI("/managers/register", {
    method: "POST",
    body: JSON.stringify(data),
  })
}

// Register a performance schedule
export async function registerPerformanceSchedule(
  performanceId: number | string,
  data: {
    startTime: string
    endTime: string
  },
) {
  return fetchAPI(`/managers/performances/${performanceId}/register`, {
    method: "POST",
    body: JSON.stringify(data),
  })
}

// Update a performance
export async function updatePerformance(
  performanceId: number | string,
  data: {
    fileId: number
    description: string
  },
) {
  return fetchAPI(`/managers/performance/${performanceId}`, {
    method: "PATCH",
    body: JSON.stringify(data),
  })
}

// Cancel a performance
export async function cancelPerformance(performanceId: number | string) {
  return fetchAPI(`/managers/performances/${performanceId}/cancel`, {
    method: "PATCH",
  })
}

// Cancel a performance schedule
export async function cancelPerformanceSchedule(performanceId: number | string, scheduleId: number | string) {
  return fetchAPI(`/managers/performances/${performanceId}/schedules/${scheduleId}`, {
    method: "PATCH",
  })
}

// Get all settlements for the manager
export async function getManagerSettlements(page = 0, size = 10) {
  return fetchAPI(`/managers/settlements/me?page=${page}&size=${size}`)
}

// Create a settlement request
export async function createSettlement(data: {
  performanceId: number
  account: string
  bank: string
}) {
  return fetchAPI("/managers/settlements/register", {
    method: "POST",
    body: JSON.stringify(data),
  })
}

// Upload a file
export async function uploadFile(file: File) {
  const formData = new FormData()
  formData.append("file", file)

  const token = getToken()
  const headers = {
    Authorization: token ? `Bearer ${token}` : "",
  }

  try {
    const response = await fetch(`${API_BASE_URL}/files`, {
      method: "POST",
      headers,
      body: formData,
    })

    if (!response.ok) {
      const error = await response.json().catch(() => ({}))
      throw new Error(error.message || `API 요청 실패: ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    console.error("파일 업로드 오류:", error)
    throw error
  }
}
