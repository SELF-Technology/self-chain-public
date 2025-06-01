import { ReactNode } from 'react'
import { Tooltip as HeadlessTooltip } from '@headlessui/react'

interface TooltipProps {
  content: ReactNode
  children: ReactNode
}

export function Tooltip({ content, children }: TooltipProps) {
  return (
    <HeadlessTooltip>
      {({ open }) => (
        <>
          <HeadlessTooltip.Button as="div">{children}</HeadlessTooltip.Button>
          <HeadlessTooltip.Panel
            className={`
              absolute z-50 px-4 py-2 bg-surface rounded-lg shadow-lg
              text-sm text-white ${open ? 'opacity-100 visible' : 'opacity-0 invisible'}
              transition-opacity duration-200
            `}
          >
            {content}
          </HeadlessTooltip.Panel>
        </>
      )}
    </HeadlessTooltip>
  )
}
