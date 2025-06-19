import {useMutation} from "@tanstack/react-query";
import type {LoginRequest} from "../types.ts";
import {login} from "../api/auth.ts";

const useLogin = () => {
    return useMutation({
        mutationFn: (data: LoginRequest) => login(data),
    })
}

export default useLogin;