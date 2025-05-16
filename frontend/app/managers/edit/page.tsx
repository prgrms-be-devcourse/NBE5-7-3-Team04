"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Textarea } from "@/components/ui/textarea"
import { Loader2, Upload } from "lucide-react"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { getManagerPerformanceDetails, updatePerformance, uploadFile } from "@/lib/api-manager"
import { useAuth } from "@/lib/auth"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"

export default function EditPerformancePage() {
  const searchParams = useSearchParams()
  const performanceId = searchParams.get("id")
  const router = useRouter()
  const { requireRole } = useAuth()

  const [performance, setPerformance] = useState<any>(null)
  const [description, setDescription] = useState("")
  const [file, setFile] = useState<File | null>(null)
  const [filePreview, setFilePreview] = useState<string | null>(null)
  const [fileId, setFileId] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)

  useEffect(() => {
    requireRole("MANAGER")

    if (!performanceId) {
      router.push("/managers/performances")
      return
    }

    const fetchPerformance = async () => {
      try {
        setLoading(true)
        setError(null)
        const data = await getManagerPerformanceDetails(performanceId)
        setPerformance(data)
        setDescription(data.description || "")
        setFilePreview(data.fileUrl || null)
      } catch (err) {
        console.error("공연 상세 정보 가져오기 오류:", err)
        setError("공연 정보를 불러오는 중 오류가 발생했습니다.")
      } finally {
        setLoading(false)
      }
    }

    fetchPerformance()
  }, [performanceId, router, requireRole])

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
    setSubmitting(true)
    setError(null)

    try {
      // 필수 필드 검증
      if (!description) {
        setError("공연 설명을 입력해주세요.")
        setSubmitting(false)
        return
      }

      // 파일 업로드 (선택된 경우에만)
      let uploadedFileId = null
      if (file) {
        uploadedFileId = await handleFileUpload()
        if (!uploadedFileId && file) {
          setSubmitting(false)
          return
        }
      }

      // 공연 수정
      const data = {
        description,
        fileId: uploadedFileId || performance.fileId,
      }

      await updatePerformance(performanceId!, data)
      setSuccess(true)

      // 성공 후 3초 후에 상세 페이지로 이동
      setTimeout(() => {
        router.push(`/managers/performances/${performanceId}`)
      }, 3000)
    } catch (err) {
      console.error("공연 수정 오류:", err)
      setError("공연 수정 중 오류가 발생했습니다.")
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return (
      <div className="container py-8 flex items-center justify-center min-h-[50vh]">
        <div className="flex flex-col items-center">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
          <p className="mt-4 text-muted-foreground">공연 정보를 불러오는 중...</p>
        </div>
      </div>
    )
  }

  if (error && !performance) {
    return (
      <div className="container py-8 flex items-center justify-center min-h-[50vh]">
        <div className="flex flex-col items-center">
          <p className="text-destructive font-medium">{error}</p>
          <Button variant="outline" className="mt-4" asChild>
            <a href="/managers/performances">목록으로 돌아가기</a>
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div className="container py-8">
      <div className="flex flex-col gap-6 max-w-3xl mx-auto">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">공연 수정</h1>
          <p className="text-muted-foreground mt-1">공연 정보를 수정합니다.</p>
        </div>

        {success ? (
          <Alert className="bg-green-50 text-green-800 border-green-200">
            <AlertTitle>공연 수정 성공</AlertTitle>
            <AlertDescription>
              공연이 성공적으로 수정되었습니다. 잠시 후 공연 상세 페이지로 이동합니다.
            </AlertDescription>
          </Alert>
        ) : (
          <form onSubmit={handleSubmit}>
            <Card>
              <CardHeader>
                <CardTitle>공연 정보 수정</CardTitle>
                <CardDescription>공연 설명과 포스터를 수정할 수 있습니다.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="space-y-2">
                  <Label htmlFor="title">공연 제목</Label>
                  <Input id="title" value={performance?.title || ""} disabled />
                  <p className="text-xs text-muted-foreground">공연 제목은 수정할 수 없습니다.</p>
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
                <Button type="submit" disabled={submitting}>
                  {submitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                  공연 수정
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
