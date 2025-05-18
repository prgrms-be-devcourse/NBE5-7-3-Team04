"use client"
import { useEffect, useState } from "react"
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { getManagerPerformanceDetailV1, updateManagerPerformance } from "@/src/api/api-manager"
import { Loader2, AlertCircle } from "lucide-react"
import { format, parseISO } from "date-fns"
import { Button } from "@/components/ui/button"
import { uploadFileToS3 } from "@/src/api/api-file"

export function PerformanceDetailModal({ open, onOpenChange, performanceId }: { open: boolean, onOpenChange: (v: boolean) => void, performanceId: string | null }) {
  const [performance, setPerformance] = useState<any>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [editOpen, setEditOpen] = useState(false);
  const [editDescription, setEditDescription] = useState("");
  const [editImage, setEditImage] = useState<File | null>(null);
  const [editImagePreview, setEditImagePreview] = useState<string | null>(null);
  const [editLoading, setEditLoading] = useState(false);

  useEffect(() => {
    if (!performanceId) return
    setLoading(true)
    setError(null)
    getManagerPerformanceDetailV1(performanceId)
      .then((data) => {
        console.log('상세정보 응답:', data);
        setPerformance(data);
      })
      .catch(() => setError("공연 정보를 불러오지 못했습니다."))
      .finally(() => setLoading(false))
  }, [performanceId])

  useEffect(() => {
    if (performance) {
      setEditDescription(performance.description || "");
      setEditImagePreview(performance.fileUrl || null);
    }
  }, [performance]);

  // 날짜 포맷 함수
  const formatDateTime = (dateString: string) => {
    try {
      return format(parseISO(dateString), "yyyy년 MM월 dd일 HH시 mm분")
    } catch {
      return dateString
    }
  }

  // 이미지 파일 첨부 핸들러
  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    setEditImage(file || null);
    if (file) {
      const reader = new FileReader();
      reader.onload = (ev) => setEditImagePreview(ev.target?.result as string);
      reader.readAsDataURL(file);
    } else {
      setEditImagePreview(performance?.fileUrl || null);
    }
  };

  // 수정 완료 버튼 클릭 시
  const handleEditSubmit = async () => {
    setEditLoading(true);
    try {
      let fileId = performance?.fileUrl; // 기존 파일 ID
      console.log('기존 fileId:', fileId);
      
      // 새 이미지가 있는 경우 S3에 업로드
      if (editImage) {
        console.log('새 이미지 업로드 시작:', editImage);
        const uploadRes = await uploadFileToS3(editImage);
        console.log('S3 업로드 결과:', uploadRes);
        
        // S3 응답에서 파일 ID 추출
        if (uploadRes && uploadRes.id) {
          fileId = String(uploadRes.id);
          console.log('S3에서 받은 파일 ID:', fileId);
          console.log('파일 ID 타입:', typeof fileId);
        } else {
          throw new Error('S3 업로드 응답에서 파일 ID를 찾을 수 없습니다.');
        }
      }
      
      // 공연 수정 API 요청 데이터 준비
      const updateData = {
        description: editDescription,
        fileId: fileId, // fileUrl을 fileId로 변경
      };
      console.log('공연 수정 API 요청 데이터:', updateData);
      console.log('fileId 타입:', typeof updateData.fileId);
      
      // 공연 정보 수정 API 호출
      const response = await updateManagerPerformance(performanceId!, updateData);
      console.log('공연 수정 API 응답:', response);
      
      if (response.success) {
        alert("공연 정보가 성공적으로 수정되었습니다.");
        // 성공 시 모달 닫기
        setEditOpen(false);
        onOpenChange(false);
        // 상세 정보 새로고침
        const updatedData = await getManagerPerformanceDetailV1(performanceId!);
        setPerformance(updatedData);
      }
    } catch (e) {
      console.error('공연 수정 중 오류 발생:', e);
      alert("수정에 실패했습니다.");
    } finally {
      setEditLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-3xl">
        <DialogHeader>
          <DialogTitle>공연 상세 정보</DialogTitle>
        </DialogHeader>
        {loading ? (
          <div className="flex flex-col items-center py-8">
            <Loader2 className="h-8 w-8 animate-spin text-primary" />
            <p className="mt-4 text-muted-foreground">불러오는 중...</p>
          </div>
        ) : error ? (
          <div className="flex flex-col items-center py-8">
            <AlertCircle className="h-8 w-8 text-destructive" />
            <p className="mt-4 text-destructive font-medium">{error}</p>
          </div>
        ) : performance ? (
          <div className="flex gap-6 items-start">
            {/* 왼쪽: 이미지 */}
            <div className="flex-shrink-0">
              {performance.fileUrl ? (
                <img src={performance.fileUrl} alt={performance.title} className="w-32 h-auto rounded border" />
              ) : (
                <div className="w-32 h-40 flex items-center justify-center bg-gray-100 text-gray-400 border rounded">이미지 없음</div>
              )}
            </div>
            {/* 오른쪽: 정보 */}
            <div className="flex-1">
              <h2 className="text-2xl font-bold mb-1">{performance.title}</h2>
              <div className="text-sm text-muted-foreground mb-2">{performance.venue}</div>
              <div className="flex flex-col gap-1 text-sm mb-4">
                
                <div>
                  <span className="font-medium">공연 종류: </span>{
                    performance.category === 'OPERA' ? '오페라'
                    : performance.category === 'DANCING' ? '춤 공연'
                    : performance.category === 'SINGING' ? '노래'
                    : performance.category ?? <span className="text-red-500">(정보 없음)</span>
                  }
                </div>
                <div>
                  <span className="font-medium">공연ID:</span> {performance.id}
                </div>
                <div>
                  <span className="font-medium">공연 기간:</span> {formatDateTime(performance.startDate)} ~ {formatDateTime(performance.endDate)}
                </div>
                <div>
                  <span className="font-medium">상태:</span> {performance.status === 'PENDING' ? '대기중'
                    : performance.status === 'CONFIRMED' ? '승인됨'
                    : performance.status === 'REJECTED' ? '거절됨'
                    : performance.status === 'CANCELLED' ? '취소됨'
                    : performance.status === 'COMPLETED' ? '완료됨'
                    : performance.status}
                </div>
                <div>
                  <span className="font-medium">총 좌석 수:</span> {performance.totalSeats}석
                </div>
                {performance.price && (
                  <div>
                    <span className="font-medium">가격:</span> {performance.price.toLocaleString()}원
                  </div>
                )}
                {performance.description && (
                  <div>
                    <span className="font-medium">공연 설명:</span> {performance.description}
                  </div>
                )}
                {performance.createdAt && (
                  <div>
                    <span className="font-medium">등록일:</span> {formatDateTime(performance.createdAt)}
                  </div>
                )}
                {performance.updatedAt && (
                  <div>
                    <span className="font-medium">수정일:</span> {formatDateTime(performance.updatedAt)}
                  </div>
                )}
              </div>
              {/* 스케줄 정보 */}
              {Array.isArray(performance.schedules) && performance.schedules.length > 0 ? (
                <div className="mt-2">
                  <div className="font-semibold mb-1">회차(스케줄) 정보</div>
                  <table className="w-full text-xs border">
                    <thead>
                      <tr className="bg-gray-50">
                        <th className="border px-2 py-1">회차ID</th>
                        <th className="border px-2 py-1">시작 시간</th>
                        <th className="border px-2 py-1">종료 시간</th>
                        <th className="border px-2 py-1">잔여 좌석</th>
                        <th className="border px-2 py-1">취소 여부</th>
                      </tr>
                    </thead>
                    <tbody>
                      {performance.schedules.map((sch: any) => (
                        <tr key={sch.id}>
                          <td className="border px-2 py-1 text-center">{sch.id}</td>
                          <td className="border px-2 py-1 text-center">{formatDateTime(sch.startTime)}</td>
                          <td className="border px-2 py-1 text-center">{formatDateTime(sch.endTime)}</td>
                          <td className="border px-2 py-1 text-center">{sch.remainingSeats}</td>
                          <td className="border px-2 py-1 text-center">{sch.isCanceled ? '취소됨' : '정상'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <div className="mt-2 text-xs text-gray-400">회차(스케줄) 정보가 없습니다.</div>
              )}
            </div>
          </div>
        ) : null}
        {/* 수정 버튼 추가 */}
        <div className="flex justify-end mb-2">
          <Button size="sm" variant="outline" onClick={() => setEditOpen(true)}>수정</Button>
        </div>

        {/* 수정 모달 */}
        <Dialog open={editOpen} onOpenChange={setEditOpen}>
          <DialogContent className="max-w-2xl">
            <DialogHeader>
              <DialogTitle>공연 정보 수정</DialogTitle>
            </DialogHeader>
            <div className="flex gap-6 items-start">
              {/* 이미지 첨부 및 미리보기 */}
              <div className="flex-shrink-0 w-2/5 min-w-[100px]">
                {editImagePreview ? (
                  <img src={editImagePreview} alt="미리보기" className="w-full h-auto rounded border mb-2" />
                ) : (
                  <div className="w-full aspect-[2/3] flex items-center justify-center bg-gray-100 text-gray-400 border rounded mb-2">이미지 없음</div>
                )}
                <input type="file" accept="image/*" onChange={handleImageChange} className="w-full mt-1" />
              </div>
              {/* 설명 입력 */}
              <div className="flex-1 w-3/5 min-w-0">
                <label className="block text-sm font-medium mb-1">설명</label>
                <textarea
                  className="w-full max-w-full border rounded p-2 text-sm resize-none"
                  style={{ boxSizing: 'border-box' }}
                  rows={6}
                  value={editDescription}
                  onChange={e => setEditDescription(e.target.value)}
                />
                <Button className="mt-2 w-full" onClick={handleEditSubmit} disabled={editLoading}>
                  {editLoading ? "수정 중..." : "수정 완료"}
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      </DialogContent>
    </Dialog>
  )
} 