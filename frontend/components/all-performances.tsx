'use client'

import { useState } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { Card, CardContent } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Performance } from '@/src/api/performance'
import { CLOUDFRONT_URL } from '@/src/api/api'

interface AllPerformancesProps {
  performances: Performance[]
}

export function AllPerformances({ performances }: AllPerformancesProps) {
  const router = useRouter()
  const [activeTab, setActiveTab] = useState('all')

  const filteredPerformances = performances
    .filter((performance) => {
      if (activeTab === 'all') return true
      if (activeTab === 'concert') return performance.category === 'SINGING'
      if (activeTab === 'dance') return performance.category === 'DANCING'
      if (activeTab === 'opera') return performance.category === 'OPERA'
      return false
    })
    .map((performance) => ({
      ...performance,
      image: performance.fileUrl
        ? `${CLOUDFRONT_URL}/${performance.fileUrl}`
        : "/placeholder.svg?height=400&width=800",
    }))

  if (!performances.length) return null

  return (
    <section className="container py-8 px-4 md:px-6">
      <div className="flex flex-col gap-6">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-bold tracking-tight">공연 목록</h2>
          <Button variant="outline" asChild>
            <Link href="/performances">모든 공연 보기</Link>
          </Button>
        </div>

        <Tabs defaultValue="all" className="w-full" onValueChange={setActiveTab}>
          <TabsList className="mb-6">
            <TabsTrigger value="all">전체</TabsTrigger>
            <TabsTrigger value="concert">콘서트</TabsTrigger>
            <TabsTrigger value="dance">무용</TabsTrigger>
            <TabsTrigger value="opera">오페라</TabsTrigger>
          </TabsList>
          <TabsContent value={activeTab} className="mt-0">
            <div className="grid grid-cols-4 gap-6">
              {filteredPerformances.slice(0, 4).map((performance) => (
                <div key={performance.id} className="cursor-pointer" onClick={() => router.push(`/performances/${performance.id}`)}>
                  <Card className="overflow-hidden hover:shadow-lg transition-all duration-300 hover:-translate-y-1">
                    <div className="aspect-[2/3] relative overflow-hidden">
                      <img
                        src={performance.image}
                        alt={performance.title}
                        className="object-cover w-full h-full transition-transform duration-300 hover:scale-105"
                      />
                      <div className="absolute top-3 left-3">
                        <Badge variant="secondary" className="bg-purple-100 text-purple-700 border-purple-200">
                          {performance.category === "SINGING" ? "콘서트" : 
                           performance.category === "DANCING" ? "무용" : "오페라"}
                        </Badge>
                      </div>
                    </div>
                    <CardContent className="p-4">
                      <div className="space-y-2">
                        <h3 className="text-lg font-semibold line-clamp-2 bg-gradient-to-r from-gray-900 to-gray-700 bg-clip-text text-transparent hover:from-purple-600 hover:to-indigo-600 transition-all duration-300">
                          {performance.title}
                        </h3>
                        <div className="flex items-center gap-2 text-sm text-muted-foreground/80">
                          <span className="font-medium">{performance.venue}</span>
                          <span className="text-purple-400">•</span>
                          <span>{new Date(performance.startDate).toLocaleDateString()}</span>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                </div>
              ))}
            </div>
          </TabsContent>
        </Tabs>
      </div>
    </section>
  )
} 