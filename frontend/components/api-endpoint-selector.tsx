"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from "@/components/ui/command"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Check, ChevronsUpDown } from "lucide-react"
import { cn } from "@/lib/utils"

// ApiEndpoint 인터페이스에 id 필드를 추가합니다.
interface ApiEndpoint {
  id: string
  value: string
  label: string
  method: string
  description?: string
}

interface ApiEndpointSelectorProps {
  endpoints: ApiEndpoint[]
  value: string
  onSelect: (value: string, method: string) => void
}

export function ApiEndpointSelector({ endpoints, value, onSelect }: ApiEndpointSelectorProps) {
  const [open, setOpen] = useState(false)

  const selectedEndpoint = endpoints.find((endpoint) => endpoint.value === value)

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button variant="outline" role="combobox" aria-expanded={open} className="w-full justify-between">
          {selectedEndpoint ? selectedEndpoint.label : "엔드포인트 선택"}
          <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-[400px] p-0">
        <Command>
          <CommandInput placeholder="엔드포인트 검색..." />
          <CommandList>
            <CommandEmpty>일치하는 엔드포인트가 없습니다.</CommandEmpty>
            <CommandGroup className="max-h-[300px] overflow-auto">
              {endpoints.map((endpoint) => (
                <CommandItem
                  key={endpoint.id}
                  value={endpoint.value}
                  onSelect={() => {
                    onSelect(endpoint.value, endpoint.method)
                    setOpen(false)
                  }}
                >
                  <Check className={cn("mr-2 h-4 w-4", value === endpoint.value ? "opacity-100" : "opacity-0")} />
                  <div className="flex flex-col">
                    <span>{endpoint.label}</span>
                    <span className="text-xs text-muted-foreground">
                      {endpoint.method} {endpoint.value}
                    </span>
                    {endpoint.description && (
                      <span className="text-xs text-muted-foreground mt-1">{endpoint.description}</span>
                    )}
                  </div>
                </CommandItem>
              ))}
            </CommandGroup>
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  )
}
