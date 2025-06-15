// src/components/__tests__/DialogueLog.test.tsx
import React from 'react'
import { render, screen } from '@testing-library/react'
import { DialogueLog } from '../DialogueLog'

describe('DialogueLog', () => {
    it('log 배열의 각 항목을 li로 렌더링한다', () => {
        const sampleLog = ['첫 번째 메시지', '두 번째 메시지']
        render(<DialogueLog log={sampleLog} />)

        // 제목
        expect(screen.getByText('기억의 기록')).toBeInTheDocument()

        // 리스트 아이템
        sampleLog.forEach((msg) => {
            expect(screen.getByText(msg)).toBeInTheDocument()
        })
    })
})
