import { api } from "./api";
import { BookmarkedPerformance } from "@/src/types/bookmark";

export async function getMyBookmarks(): Promise<BookmarkedPerformance[]> {
  const res = await api.get("/bookmark");
  return res.data.content ?? res.data;
}