// src/components/__tests__/NavBar.test.tsx
import React from 'react'
import {render, screen} from '@testing-library/react'
import {MemoryRouter} from 'react-router-dom'
import NavBar from '../NavBar'
import {AuthContext} from '../../contexts/AuthContext'

describe('NavBar', () => {
    const logoutMock = jest.fn()

    it('미로그인 시 로그인 링크만 렌더링', () => {
        render(
            <AuthContext.Provider value={{ user: null, loading: false, refreshUser: jest.fn(), logout: logoutMock }}>
                <MemoryRouter>
                    <NavBar />
                </MemoryRouter>
            </AuthContext.Provider>
        )
        expect(screen.getByRole('link', { name: /로그인/i })).toBeInTheDocument()
    })

    // … 나머지 테스트 …
})
