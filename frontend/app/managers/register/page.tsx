"use client"

import type React from "react"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Calendar } from "@/components/ui/calendar"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { format } from "date-fns"
import { CalendarIcon, Loader2, Upload } from "lucide-react"
import { cn } from "@/lib/utils"
import { registerPerformance, uploadFile } from "@/src/api/api"
import { useAuth } from "@/src/auth/user"
import { useEffect } from "react"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"

export default function RegisterPerformancePage() {
  const [title, setTitle] = useState("")
  const [venue, setVenue] = useState("")
  const [price, setPrice] = useState("")
  const [totalSeats, setTotalSeats] = useState("")
  const [category, setCategory] = useState("")
  const [startDate, setStartDate] = useState<Date>()
  const [endDate, setEndDate] = useState<Date>()
  const [description, setDescription] = useState("")
  const [file, setFile] = useState<File | null>(null)
  const [filePreview, setFilePreview] = useState<string | null>(null)
  const [fileId, setFileId] = useState<number | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)
  const router = useRouter()
  const { requireRole } = useAuth()

  useEffect(() => {
    requireRole("MANAGER")
  }, [requireRole])

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const selectedFile = e.target.files[0]
      setFile(selectedFile)

      // 파일 미리보기 생성
      const reader = new FileReader()
      reader.onload = (event) => {
        setFilePreview(event.target?.result as string)
      }
      reader.readAsDataURL(selectedFile)
    }
  }

  const handleFileUpload = async () => {
    if (!file) {
      setError("포스터 이미지를 선택해주세요.")
      return null
    }

    try {
      const response = await uploadFile(file)
      setFileId(response.id)
      return response.id
    } catch (err) {
      console.error("파일 업로드 오류:", err)
      setError("파일 업로드 중 오류가 발생했습니다.")
      return null
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError(null)

    try {
      // 필수 필드 검증
      if (!title || !venue || !price || !totalSeats || !category || !startDate || !endDate || !description) {
        setError("모든 필드를 입력해주세요.")
        setLoading(false)
        return
      }

      // 가격과 좌석 수가 숫자인지 확인
      if (isNaN(Number(price)) || isNaN(Number(totalSeats))) {
        setError("가격과 좌석 수는 숫자여야 합니다.")
        setLoading(false)
        return
      }

      // 시작일이 종료일보다 이전인지 확인
      if (startDate > endDate) {
        setError("시작일은 종료일보다 이전이어야 합니다.")
        setLoading(false)
        return
      }

      // 파일 업로드
      const uploadedFileId = await handleFileUpload()
      if (!uploadedFileId) {
        setLoading(false)
        return
      }

      // 공연 등록
      const data = {
        title,
        venue,
        price: Number(price),
        totalSeats: Number(totalSeats),
        category,
        startDate: startDate.toISOString(),
        endDate: endDate.toISOString(),
        description,
        fileId: uploadedFileId,
      }

      const performanceId = await registerPerformance(data)
      setSuccess(true)

      // 성공 후 3초 후에 상세 페이지로 이동
      setTimeout(() => {
        router.push(`/managers/performances/${performanceId}`)
      }, 3000)
    } catch (err) {
      console.error("공연 등록 오류:", err)
      setError("공연 등록 중 오류가 발생했습니다.")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="container py-8">
      <div className="flex flex-col gap-6 max-w-3xl mx-auto">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">공연 등록</h1>
          <p className="text-muted-foreground mt-1">새로운 공연을 등록합니다.</p>
        </div>

        {success ? (
          <Alert className="bg-green-50 text-green-800 border-green-200">
            <AlertTitle>공연 등록 성공</AlertTitle>
            <AlertDescription>
              공연이 성공적으로 등록되었습니다. 잠시 후 공연 상세 페이지로 이동합니다.
            </AlertDescription>
          </Alert>
        ) : (
          <form onSubmit={handleSubmit}>
            <Card>
              <CardHeader>
                <CardTitle>공연 정보</CardTitle>
                <CardDescription>공연에 대한 기본 정보를 입력하세요.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label htmlFor="title">공연 제목</Label>
                    <Input
                      id="title"
                      placeholder="공연 제목을 입력하세요"
                      value={title}
                      onChange={(e) => setTitle(e.target.value)}
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="venue">공연 장소</Label>
                    <Input
                      id="venue"
                      placeholder="공연 장소를 입력하세요"
                      value={venue}
                      onChange={(e) => setVenue(e.target.value)}
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="price">티켓 가격</Label>
                    <Input
                      id="price"
                      type="number"
                      placeholder="티켓 가격을 입력하세요"
                      value={price}
                      onChange={(e) => setPrice(e.target.value)}
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="totalSeats">총 좌석 수</Label>
                    <Input
                      id="totalSeats"
                      type="number"
                      placeholder="총 좌석 수를 입력하세요"
                      value={totalSeats}
                      onChange={(e) => setTotalSeats(e.target.value)}
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="category">카테고리</Label>
                    <Select value={category} onValueChange={setCategory} required>
                      <SelectTrigger id="category">
                        <SelectValue placeholder="카테고리 선택" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="OPERA">오페라</SelectItem>
                        <SelectItem value="DANCING">무용</SelectItem>
                        <SelectItem value="SINGING">콘서트</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="space-y-2">
                    <Label>공연 기간</Label>
                    <div className="grid grid-cols-2 gap-2">
                      <Popover>
                        <PopoverTrigger asChild>
                          <Button
                            variant={"outline"}
                            className={cn(
                              "w-full justify-start text-left font-normal",
                              !startDate && "text-muted-foreground",
                            )}
                          >
                            <CalendarIcon className="mr-2 h-4 w-4" />
                            {startDate ? format(startDate, "PPP") : <span>시작일</span>}
                          </Button>
                        </PopoverTrigger>
                        <PopoverContent className="w-auto p-0">
                          <Calendar mode="single" selected={startDate} onSelect={setStartDate} initialFocus />
                        </PopoverContent>
                      </Popover>
                      <Popover>
                        <PopoverTrigger asChild>
                          <Button
                            variant={"outline"}
                            className={cn(
                              "w-full justify-start text-left font-normal",
                              !endDate && "text-muted-foreground",
                            )}
                          >
                            <CalendarIcon className="mr-2 h-4 w-4" />
                            {endDate ? format(endDate, "PPP") : <span>종료일</span>}
                          </Button>
                        </PopoverTrigger>
                        <PopoverContent className="w-auto p-0">
                          <Calendar mode="single" selected={endDate} onSelect={setEndDate} initialFocus />
                        </PopoverContent>
                      </Popover>
                    </div>
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="description">공연 설명</Label>
                  <Textarea
                    id="description"
                    placeholder="공연에 대한 상세 설명을 입력하세요"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    rows={5}
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="poster">공연 포스터</Label>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <div className="flex items-center justify-center w-full">
                        <label
                          htmlFor="poster"
                          className="flex flex-col items-center justify-center w-full h-64 border-2 border-dashed rounded-lg cursor-pointer bg-gray-50 hover:bg-gray-100"
                        >
                          <div className="flex flex-col items-center justify-center pt-5 pb-6">
                            <Upload className="w-8 h-8 mb-4 text-gray-500" />
                            <p className="mb-2 text-sm text-gray-500">
                              <span className="font-semibold">클릭하여 파일 선택</span> 또는 드래그 앤 드롭
                            </p>
                            <p className="text-xs text-gray-500">PNG, JPG, JPEG (최대 5MB)</p>
                          </div>
                          <Input
                            id="poster"
                            type="file"
                            accept="image/*"
                            className="hidden"
                            onChange={handleFileChange}
                            required
                          />
                        </label>
                      </div>
                    </div>
                    <div>
                      {filePreview ? (
                        <div className="relative h-64 w-full overflow-hidden rounded-lg border">
                          <img
                            src={filePreview || "/placeholder.svg"}
                            alt="포스터 미리보기"
                            className="h-full w-full object-cover"
                          />
                        </div>
                      ) : (
                        <div className="flex items-center justify-center h-64 w-full rounded-lg border bg-gray-50">
                          <p className="text-sm text-gray-500">포스터 미리보기</p>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </CardContent>
              <CardFooter className="flex justify-between">
                <Button variant="outline" type="button" onClick={() => router.back()}>
                  취소
                </Button>
                <Button type="submit" disabled={loading}>
                  {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                  공연 등록
                </Button>
              </CardFooter>
            </Card>

            {error && <div className="mt-4 p-4 bg-red-50 text-red-800 rounded-lg">{error}</div>}
          </form>
        )}
      </div>
    </div>
  )
}
