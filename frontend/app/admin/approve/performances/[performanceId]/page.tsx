"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { ArrowLeft, CheckCircle, XCircle, MapPin, Calendar, Clock, Users, Ticket } from "lucide-react"
import Link from "next/link"
import Image from "next/image"
import { Separator } from "@/components/ui/separator"

type Props = {
  params: {
    performanceId: string
  }
}

export default async function PerformanceApprovePage({ params }: Props) {
  return (
    <div className="flex flex-col gap-6">
      <h1>공연 승인 페이지</h1>
      <p>공연 ID: {params.performanceId}</p>
    </div>
  )
}
