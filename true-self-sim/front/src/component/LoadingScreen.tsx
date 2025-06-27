import React, { useMemo } from 'react';

const backgrounds = [
    'loading-background1.png',
    'loading-background2.png',
    'loading-background3.png',
    'loading-background4.png',
];

const LoadingScreen: React.FC = () => {
    // 컴포넌트 마운트 시 한 번만 랜덤 선택하려면 useMemo 사용
    const bgImage = useMemo(() => {
        const idx = Math.floor(Math.random() * backgrounds.length);
        return backgrounds[idx];
    }, []);

    return (
        <div className="fixed inset-0 flex items-center justify-center overflow-hidden">
            {/* 랜덤 배경 이미지 (50% 투명) */}
            <img
                src={`/background/${bgImage}`}
                className="absolute inset-0 w-full h-full object-cover opacity-50"
                alt="loading background"
            />

            {/* 로딩 스피너나 텍스트 같은 중앙 컨텐츠 */}
            <div className="relative z-10">
                <div className="loader">Loading...</div>
            </div>
        </div>
    );
};

export default LoadingScreen;
