export interface Review {
    id: number;
    performanceId: number;
    scheduleId: number;
    userId: number;
    comments: string;
    createdAt: string;
}

export interface ReviewPageResponse {
    content: Review[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
} 