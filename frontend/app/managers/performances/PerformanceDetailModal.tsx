"use client"
import { useEffect, useState } from "react"
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { getManagerPerformanceDetailV1, updateManagerPerformance, cancelPerformance, cancelPerformanceSchedule, registerPerformanceSchedule } from "@/src/api/api-manager"
import { Loader2, AlertCircle } from "lucide-react"
import { formatKSTDateTime } from "@/src/utils/date"
import { Button } from "@/components/ui/button"
import { uploadFileToS3 } from "@/src/api/api-file"
import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import DatePicker, { registerLocale } from "react-datepicker"
import "react-datepicker/dist/react-datepicker.css"
import { ko } from "date-fns/locale/ko"
import { getPerformanceImageUrl } from "@/lib/utils"

export function PerformanceDetailModal({ open, onOpenChange, performanceId }: { open: boolean, onOpenChange: (v: boolean) => void, performanceId: string | null }) {
  const [performance, setPerformance] = useState<any>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [editOpen, setEditOpen] = useState(false);
  const [editDescription, setEditDescription] = useState("");
  const [editImage, setEditImage] = useState<File | null>(null);
  const [editImagePreview, setEditImagePreview] = useState<string | null>(null);
  const [editLoading, setEditLoading] = useState(false);
  const [cancelLoading, setCancelLoading] = useState(false);
  const [scheduleModalOpen, setScheduleModalOpen] = useState(false);
  const [newSchedules, setNewSchedules] = useState([{ startTime: "", endTime: "" }]);
  const [scheduleLoading, setScheduleLoading] = useState(false);
  const [editFileId, setEditFileId] = useState<number | null>(null);

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

  useEffect(() => {
    registerLocale("ko", ko);
  }, []);

  // 수정 모달이 열릴 때마다 현재 공연 이미지 미리보기를 세팅하고 fileId도 세팅
  useEffect(() => {
    if (editOpen && performance) {
      if (performance.fileId !== undefined && performance.fileId !== null) {
        setEditFileId(performance.fileId);
      } else {
        setEditFileId(null);
      }
      setEditImagePreview(performance.fileUrl ? getPerformanceImageUrl(performance.fileUrl) : null);
      setEditImage(null);
    }
  }, [editOpen, performance]);

  // 날짜 포맷 함수 (공통 util 사용)
  const formatDateTime = formatKSTDateTime;

  // 이미지 파일 첨부 핸들러
  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    setEditImage(file || null);
    setEditFileId(null); // 새 파일 첨부 시 기존 fileId 무효화
    if (file) {
      const reader = new FileReader();
      reader.onload = (ev) => setEditImagePreview(ev.target?.result as string);
      reader.readAsDataURL(file);
    } else {
      setEditImagePreview(performance?.fileUrl ? getPerformanceImageUrl(performance.fileUrl) : null);
    }
  };

  // 수정 완료 버튼 클릭 시
  const handleEditSubmit = async () => {
    setEditLoading(true);
    try {
      let fileId = editFileId;
      console.log('handleEditSubmit - editFileId:', editFileId, typeof editFileId);
      if (editImage) {
        const uploadRes = await uploadFileToS3(editImage);
        if (uploadRes && uploadRes.id) {
          fileId = uploadRes.id;
        } else {
          throw new Error('S3 업로드 응답에서 파일 ID를 찾을 수 없습니다.');
        }
      }
      console.log('performance.fileId:', performance?.fileId, typeof performance?.fileId);
      console.log('editFileId:', editFileId, typeof editFileId);
      console.log('최종 fileId:', fileId, typeof fileId);
      const updateData = {
        description: editDescription,
        fileId: fileId !== null && fileId !== undefined ? String(fileId) : undefined,
      };
      console.log('공연 수정 API 요청 데이터:', updateData);
      
      // 공연 정보 수정 API 호출
      const response = await updateManagerPerformance(performanceId!, updateData);
      console.log('공연 수정 API 응답:', response);
      
      if (response.success) {
        alert("공연 정보가 성공적으로 수정되었습니다.");
        // 모달을 닫지 않고 상세정보만 새로고침
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

  // 단일 스케줄 취소
  const handleCancelSchedule = async (scheduleId: number) => {
    if (!performanceId) return;
    if (!window.confirm('정말 이 스케줄을 취소하시겠습니까?')) return;
    setCancelLoading(true);
    try {
      await cancelPerformanceSchedule(performanceId, scheduleId);
      const updated = await getManagerPerformanceDetailV1(performanceId);
      setPerformance(updated);
    } catch (e) {
      alert("스케줄 취소에 실패했습니다.");
    } finally {
      setCancelLoading(false);
    }
  };

  // 전체 공연 취소
  const handleCancelAll = async () => {
    if (!performanceId) return;
    if (!window.confirm('정말 이 공연의 모든 스케줄을 일괄 취소하시겠습니까?')) return;
    setCancelLoading(true);
    try {
      await cancelPerformance(performanceId);
      const updated = await getManagerPerformanceDetailV1(performanceId);
      setPerformance(updated);
    } catch (e) {
      alert("공연 전체 취소에 실패했습니다.");
    } finally {
      setCancelLoading(false);
    }
  };

  // CONFIRMED 상태만 취소 가능
  const canCancelSchedule = performance?.status === 'CONFIRMED';

  const addNewScheduleRow = () => setNewSchedules([...newSchedules, { startTime: "", endTime: "" }]);
  const removeNewScheduleRow = (idx: number) => setNewSchedules(newSchedules.filter((_, i) => i !== idx));
  const updateNewSchedule = (idx: number, field: string, value: string) => {
    setNewSchedules(newSchedules.map((row, i) => i === idx ? { ...row, [field]: value } : row));
  };

  const handleAddSchedules = async () => {
    if (!performanceId) return;
    if (newSchedules.length === 0 || newSchedules.some(sch => !sch.startTime || !sch.endTime)) {
      alert("모든 회차(스케줄)의 시작/종료 시간을 입력해주세요.");
      return;
    }
    setScheduleLoading(true);
    try {
      for (const sch of newSchedules) {
        await registerPerformanceSchedule(performanceId, {
          startTime: new Date(sch.startTime),
          endTime: new Date(sch.endTime),
        });
      }
      setScheduleModalOpen(false);
      setNewSchedules([{ startTime: "", endTime: "" }]);
      // 상세정보 새로고침
      const updated = await getManagerPerformanceDetailV1(performanceId);
      setPerformance(updated);
      alert("스케줄이 성공적으로 추가되었습니다.");
    } catch (e) {
      alert("스케줄 추가 중 오류가 발생했습니다.");
    } finally {
      setScheduleLoading(false);
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
              {performance && performance.fileUrl ? (
                <img
                  src={getPerformanceImageUrl(performance.fileUrl)}
                  alt={performance.title}
                  className="w-32 h-auto rounded border"
                  onError={e => { e.currentTarget.src = "/placeholder.svg?height=300&width=400" }}
                />
              ) : (
                <div className="w-32 h-40 flex items-center justify-center bg-gray-100 text-gray-400 border rounded">
                  이미지 없음
                </div>
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
                        <th className="border px-3 py-1 min-w-[80px] whitespace-nowrap">회차ID</th>
                        <th className="border px-3 py-1 min-w-[120px] whitespace-nowrap">시작 시간</th>
                        <th className="border px-3 py-1 min-w-[120px] whitespace-nowrap">종료 시간</th>
                        <th className="border px-3 py-1 min-w-[90px] whitespace-nowrap">잔여 좌석</th>
                        <th className="border px-3 py-1 min-w-[90px] whitespace-nowrap">취소 여부</th>
                        <th className="border px-3 py-1 min-w-[80px] whitespace-nowrap">취소</th>
                      </tr>
                    </thead>
                    <tbody>
                      {performance.schedules.map((sch: any) => (
                        <tr key={sch.id}>
                          <td className="border px-3 py-1 text-center min-w-[80px]">{sch.id}</td>
                          <td className="border px-3 py-1 text-center min-w-[120px]">{formatDateTime(sch.startTime)}</td>
                          <td className="border px-3 py-1 text-center min-w-[120px]">{formatDateTime(sch.endTime)}</td>
                          <td className="border px-3 py-1 text-center min-w-[90px]">{sch.remainingSeats}</td>
                          <td className="border px-3 py-1 text-center min-w-[90px]">{sch.isCanceled ? '취소됨' : '정상'}</td>
                          <td className="border px-3 py-1 text-center min-w-[80px]">
                            {sch.isCanceled ? (
                              <span className="text-gray-400">-</span>
                            ) : canCancelSchedule ? (
                              <Button size="sm" variant="destructive" disabled={cancelLoading} className="text-xs px-2 py-1 h-7 min-w-0" onClick={() => handleCancelSchedule(sch.id)}>
                                취소
                              </Button>
                            ) : (
                              <span className="text-gray-300">-</span>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  <div className="flex justify-end mt-3">
                    {canCancelSchedule && (
                      <Button variant="destructive" disabled={cancelLoading} onClick={handleCancelAll} className="text-xs px-3 py-2 h-8 min-w-0">
                        전체 스케줄 일괄 취소
                      </Button>
                    )}
                  </div>
                </div>
              ) : (
                <div className="mt-2 text-xs text-gray-400">회차(스케줄) 정보가 없습니다.</div>
              )}
            </div>
          </div>
        ) : null}
        {/* 스케줄 추가 버튼 */}
        <div className="flex justify-end mb-2 gap-2">
          <Button size="sm" variant="secondary" className="mr-2" onClick={() => setScheduleModalOpen(true)} disabled={performance?.status !== 'CONFIRMED' && performance?.status !== 'PENDING'}>스케줄 추가</Button>
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
                <img
                  src={editImagePreview || (performance && performance.fileUrl ? getPerformanceImageUrl(performance.fileUrl) : "/placeholder.svg?height=300&width=400")}
                  alt="미리보기"
                  className="w-full h-auto rounded border mb-2"
                  onError={e => { e.currentTarget.src = "/placeholder.svg?height=300&width=400" }}
                />
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

        {/* 스케줄 추가 모달 */}
        <Dialog open={scheduleModalOpen} onOpenChange={setScheduleModalOpen}>
          <DialogContent className="max-w-2xl">
            <DialogHeader>
              <DialogTitle>스케줄(회차) 추가</DialogTitle>
              <div className="mt-2 text-sm text-muted-foreground">
                공연 기간: {performance && performance.startDate ? formatDateTime(performance.startDate) : "-"} ~ {performance && performance.endDate ? formatDateTime(performance.endDate) : "-"}
              </div>
              <div className="mt-1 text-xs text-primary">
                * 종료 시각은 시작 시각보다 늦어야 합니다.
              </div>
            </DialogHeader>
            <div className="mb-4">
              {newSchedules.map((sch, idx) => (
                <div key={idx} className="flex gap-2 items-center mb-2 flex-wrap md:flex-nowrap">
                  <Label className="min-w-[36px]">시작</Label>
                  <DatePicker
                    selected={sch.startTime ? new Date(sch.startTime) : null}
                    onChange={date => updateNewSchedule(idx, "startTime", date ? date.toISOString() : "")}
                    showTimeSelect
                    timeFormat="HH:mm"
                    timeIntervals={10}
                    dateFormat="yyyy년 MM월 dd일 (eee) HH:mm"
                    placeholderText="시작 시각 선택"
                    className="min-w-[240px] flex-grow max-w-[280px] border rounded px-2 py-1"
                    locale="ko"
                  />
                  <Label className="min-w-[36px]">종료</Label>
                  <DatePicker
                    selected={sch.endTime ? new Date(sch.endTime) : null}
                    onChange={date => updateNewSchedule(idx, "endTime", date ? date.toISOString() : "")}
                    showTimeSelect
                    timeFormat="HH:mm"
                    timeIntervals={10}
                    dateFormat="yyyy년 MM월 dd일 (eee) HH:mm"
                    placeholderText="종료 시각 선택"
                    className="min-w-[240px] flex-grow max-w-[280px] border rounded px-2 py-1"
                    locale="ko"
                  />
                  {newSchedules.length > 1 && (
                    <Button type="button" variant="outline" size="icon" onClick={() => removeNewScheduleRow(idx)}>
                      X
                    </Button>
                  )}
                </div>
              ))}
              <Button type="button" variant="secondary" onClick={addNewScheduleRow}>+ 회차 추가</Button>
            </div>
            <div className="flex justify-end">
              <Button onClick={handleAddSchedules} disabled={scheduleLoading}>
                {scheduleLoading ? "등록 중..." : "등록"}
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      </DialogContent>
    </Dialog>
  )
} 