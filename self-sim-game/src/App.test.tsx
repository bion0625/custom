// src/App.test.tsx
import React from 'react'
import { render, screen } from '@testing-library/react'
import App from './App'

test('인증되지 않은 사용자는 /login 페이지로 리다이렉트된다', () => {
    render(<App />)
    expect(screen.getByRole('heading', { name: /로그인/i })).toBeInTheDocument()
})
