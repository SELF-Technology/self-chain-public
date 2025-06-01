import { ReactNode } from 'react'

interface CardProps {
  children: ReactNode
  className?: string
}

export function Card({ children, className = '' }: CardProps) {
  return (
    <div className={`bg-surface rounded-lg p-6 shadow-lg ${className}`}>
      {children}
    </div>
  )
}
