import { Card, CardContent, CardFooter } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Calendar, MapPin, Clock } from "lucide-react"
import Image from "next/image"
import Link from "next/link"

interface PerformanceCardProps {
  performance: {
    id: string
    title: string
    date: string
    time: string
    location: string
    price: number
    image: string
    category: string
    status: "예매가능" | "매진임박" | "매진" | "종료"
  }
}

export function PerformanceCard({ performance }: PerformanceCardProps) {
  const statusVariant =
    performance.status === "예매가능"
      ? "success"
      : performance.status === "매진임박"
        ? "secondary"
        : performance.status === "매진"
          ? "destructive"
          : "outline"

  return (
    <Card className="overflow-hidden transition-all hover:shadow-md">
      <div className="relative aspect-[4/3] w-full overflow-hidden">
        <Image
          src={performance.image || "/placeholder.svg?height=300&width=400"}
          alt={performance.title}
          fill
          className="object-cover transition-transform hover:scale-105"
        />
        <div className="absolute right-2 top-2">
          <Badge variant={statusVariant}>{performance.status}</Badge>
        </div>
      </div>
      <CardContent className="p-4">
        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <Badge variant="outline" className="px-2 py-0 text-xs">
              {performance.category}
            </Badge>
          </div>
          <h3 className="line-clamp-1 text-base font-bold">{performance.title}</h3>
          <div className="flex flex-col gap-1 text-xs text-muted-foreground">
            <div className="flex items-center gap-1">
              <Clock className="h-3 w-3" />
              <span>{performance.time}</span>
            </div>
            <div className="flex items-center gap-1">
              <MapPin className="h-3 w-3" />
              <span className="line-clamp-1">{performance.location}</span>
            </div>
          </div>
        </div>
      </CardContent>
      <CardFooter className="p-4 pt-0">
        <Button asChild className="w-full">
          <Link href={`/performances/${performance.id}`}>상세보기</Link>
        </Button>
      </CardFooter>
    </Card>
  )
}
