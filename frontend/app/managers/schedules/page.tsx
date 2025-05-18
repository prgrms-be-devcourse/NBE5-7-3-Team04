'use client';

import { useEffect, useState, useCallback } from 'react';
import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import { useQuery } from '@tanstack/react-query';
import { searchManagerPerformances, ManagerPerformance, ManagerPerformancePageResponse } from '@/src/api/api-manager';
import dayjs from 'dayjs';
import './calendar-custom.css';

export default function SchedulesPage() {
  const [page] = useState(0);
  const [size] = useState(100);

  // 공연관리자용 공연 검색 API 사용
  const { data, isFetching } = useQuery<ManagerPerformancePageResponse, Error>({
    queryKey: ['manager-performances', page, size],
    queryFn: () => searchManagerPerformances({ page, size }),
  });

  const performances: ManagerPerformance[] = data?.content || [];

  // 모든 공연을 이벤트로 변환
  const events = performances.map((performance) => ({
    title: performance.title,
    start: performance.startDate,
    end: performance.endDate
      ? dayjs(performance.endDate).add(1, 'day').format('YYYY-MM-DD')
      : undefined,
    allDay: true,
    backgroundColor: '#ede7f6',
    borderColor: '#ede7f6',
    textColor: '#7a3fd8',
    display: 'block',
  }));

  return (
    <div className="p-6">
      <div className="mb-8">
        <h1 className="text-2xl font-bold mb-2">공연 일정</h1>
        <p className="text-gray-600">등록된 공연의 일정을 확인합니다.</p>
      </div>
      <div className="bg-white rounded-lg shadow p-6">
        <FullCalendar
          plugins={[dayGridPlugin, interactionPlugin]}
          initialView="dayGridMonth"
          events={events}
          headerToolbar={{
            left: 'prev,next today',
            center: 'title',
            right: '',
          }}
          height="auto"
          locale="ko"
        />
        {isFetching && <div className="text-center text-xs text-gray-400 mt-2">공연 정보를 불러오는 중...</div>}
      </div>
    </div>
  );
}
