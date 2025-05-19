'use client';

import { useState } from 'react';
import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import { useQuery } from '@tanstack/react-query';
import { getManagerPerformancesV1, ManagerPerformance, ManagerPerformancePageResponse } from '@/src/api/api-manager';
import dayjs from 'dayjs';
import './calendar-custom.css';

export default function SchedulesPage() {
  const [page] = useState(0);
  const [size] = useState(100);

  const { data, isFetching } = useQuery<ManagerPerformancePageResponse, Error>({
    queryKey: ['manager-performances', page, size],
    queryFn: () => getManagerPerformancesV1(page, size),
  });

  const performances: ManagerPerformance[] = (data?.content || []).filter(
    (performance) => !['REJECTED', 'CANCELLED'].includes(performance.status)
  );

  // 모든 공연을 이벤트로 변환
  const events = performances.map((performance) => {
    let backgroundColor = '#ede7f6'; // COMPLETED: 연한 보라색
    let borderColor = '#ede7f6';
    let textColor = '#7a3fd8';
    if (performance.status === 'CONFIRMED') {
      backgroundColor = '#7a3fd8'; // 보라색
      borderColor = '#7a3fd8';
      textColor = '#fff';
    } else if (performance.status === 'PENDING') {
      backgroundColor = '#e0e0e0'; // 회색
      borderColor = '#e0e0e0';
      textColor = '#757575';
    }
    return {
      title: performance.title,
      start: performance.startDate,
      end: performance.endDate
        ? dayjs(performance.endDate).add(1, 'day').format('YYYY-MM-DD')
        : undefined,
      allDay: true,
      backgroundColor,
      borderColor,
      textColor,
      display: 'block',
    };
  });

  return (
    <div className="p-6">
      <div className="mb-8">
        <h1 className="text-2xl font-bold mb-2">공연 일정</h1>
        <p className="text-gray-600" style={{marginTop: '10px'}}>등록된 공연의 일정을 확인합니다.</p>
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
