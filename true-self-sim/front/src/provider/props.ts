import type {ReactNode} from "react";

export type ErrorBoundaryProps = {
  children: ReactNode;
};

export type ErrorBoundaryState = {
    hasError: boolean;
    errorMessage?: string;
};