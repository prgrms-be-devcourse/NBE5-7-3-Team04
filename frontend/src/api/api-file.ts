import { api } from "./api";

/**
 * 파일 업로드 (S3)
 * POST /api/v1/files
 * @param file 첨부 파일
 * @returns { id: number, key: string }
 */
export async function uploadFileToS3(file: File): Promise<{ id: number; key: string }> {
  const formData = new FormData();
  formData.append("file", file);
  const res = await api.post("/files", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return res.data;
} 