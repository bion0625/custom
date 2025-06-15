// src/components/__tests__/Retrospective.test.tsx
import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import { Retrospective } from '../Retrospective'

describe('Retrospective', () => {
    const sampleLog = ['로그1', '로그2']

    it('log를 순서대로 렌더링하고, 버튼 클릭 시 onRestart를 호출한다', () => {
        const onRestart = jest.fn()
        render(<Retrospective log={sampleLog} onRestart={onRestart} />)

        // 여정 제목
        expect(screen.getByText('당신의 여정')).toBeInTheDocument()

        // 로그 리스트
        sampleLog.forEach((entry) => {
            expect(screen.getByText(entry)).toBeInTheDocument()
        })

        // 버튼 동작
        const btn = screen.getByRole('button', { name: /처음부터 다시 시작/i })
        fireEvent.click(btn)
        expect(onRestart).toHaveBeenCalled()
    })
})
