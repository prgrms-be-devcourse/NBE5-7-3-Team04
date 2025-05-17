'use client'

import { Banner } from './banner'

const banners = [
  {
    id: 1,
    image: '/banners/배너1.gif',
    alt: '봄맞이 특별 할인'
  },
  {
    id: 2,
    image: '/banners/배너2.gif',
    alt: '신규 회원 혜택'
  }
]

export function Hero() {
  return (
    <section className="relative w-full py-10">
      {/* 그라데이션 배경 */}
      <div
        className="absolute left-[-200px] bottom-[-150px] w-[600px] h-[500px] rounded-full opacity-60 blur-3xl pointer-events-none z-0"
        style={{ background: 'radial-gradient(circle,rgb(215, 210, 232),rgb(164, 138, 210),rgb(109, 49, 199))' }}
      />
      <div
        className="absolute right-[-150px] top-[-110px] w-[600px] h-[500px] rounded-full opacity-40 blur-3xl pointer-events-none z-0"
        style={{ background: 'radial-gradient(circle,rgb(213, 190, 229),rgb(200, 158, 216),rgb(134, 53, 168))' }}
      />

      {/* 상단 텍스트 */}
      <div className="flex flex-col items-center gap-3 text-center mb-6">
        <h1 className="text-4xl font-bold sm:text-5xl md:text-6xl bg-gradient-to-r from-purple-600 via-indigo-500 to-purple-600 bg-clip-text text-transparent">TICKET4U</h1>
        <p className="text-lg sm:text-xl text-slate-600">당신의 특별한 순간을 위한<br />티켓 예매 서비스</p>
      </div>

      {/* 배너 캐러셀 */}
      <div className="max-w-7xl mx-auto">
        <Banner banners={banners} />
      </div>
    </section>
  )
} 