import React from 'react'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import Login from '../Login'
import { AuthContext } from '../../contexts/AuthContext'
import { api } from '../../api'
import { MemoryRouter } from 'react-router-dom'

const mockNavigate = jest.fn()

jest.mock('react-router-dom', () => ({
    ...jest.requireActual('react-router-dom'),
    useNavigate: () => mockNavigate,
}))
jest.mock('../../api', () => ({ api: { post: jest.fn() } }))

describe('Login Page', () => {
    beforeEach(() => jest.clearAllMocks())

    it('로그인 성공 시 동작', async () => {
        (api.post as jest.Mock).mockResolvedValue({ data: { access_token: 'TOK' } })
        const refreshUser = jest.fn()

        render(
            <AuthContext.Provider value={{ user: null, loading: false, refreshUser, logout: jest.fn() }}>
                <MemoryRouter>
                    <Login />
                </MemoryRouter>
            </AuthContext.Provider>
        )

        fireEvent.change(screen.getByPlaceholderText(/아이디/), { target: { value: 'u' } })
        fireEvent.change(screen.getByPlaceholderText(/비밀번호/), { target: { value: 'p' } })
        fireEvent.click(screen.getByRole('button', { name: /^로그인$/i }))

        // ① localStorage 저장 확인 (비동기)
        await waitFor(() =>
            expect(window.localStorage.getItem('access_token')).toBe('TOK')
        )

        // ② refreshUser 호출은 즉시 동기 호출이므로 waitFor 밖으로
        expect(refreshUser).toHaveBeenCalled()

        // ③ useNavigate 호출도 동기 호출이므로 밖으로
        expect(mockNavigate).toHaveBeenCalledWith('/game')
    })
})
