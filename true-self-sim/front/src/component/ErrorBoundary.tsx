import React from "react";
import type { ErrorBoundaryProps, ErrorBoundaryState } from "../provider/props.ts";

const backgrounds = [
    "loading-background1.png",
    "loading-background2.png",
    "loading-background3.png",
    "loading-background4.png",
];

class ErrorBoundary extends React.Component<ErrorBoundaryProps, ErrorBoundaryState> {
    private bgImage: string;

    constructor(props: ErrorBoundaryProps) {
        super(props);
        this.state = { hasError: false };
        const idx = Math.floor(Math.random() * backgrounds.length);
        this.bgImage = backgrounds[idx];
    }

    static getDerivedStateFromError(_: Error): ErrorBoundaryState {
        return { hasError: true };
    }

    componentDidCatch(error: any, errorInfo: React.ErrorInfo) {
        console.error("ErrorBoundary caught an error:", error, errorInfo);

        const message =
            error?.response?.data?.message ||
            error?.message ||
            "예기치 못한 오류가 발생했습니다.";

        this.setState({ errorMessage: message });
    }

    render() {
        if (this.state.hasError) {
            return (
                <div className="fixed inset-0 flex items-center justify-center overflow-hidden text-red-800">
                    <img
                        src={`/background/${this.bgImage}`}
                        className="absolute inset-0 w-full h-full object-cover opacity-50"
                        alt="error background"
                    />
                    <div className="relative z-10 text-center max-w-xl px-6">
                        <h1 className="text-2xl font-bold mb-4">Something went wrong.</h1>
                        <p>{this.state.errorMessage || "Please try refreshing the page."}</p>
                    </div>
                </div>
            );
        }

        return this.props.children;
    }
}

export default ErrorBoundary;
