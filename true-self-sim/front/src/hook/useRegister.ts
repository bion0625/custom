import {useMutation} from "@tanstack/react-query";
import type {RegisterRequest} from "../types.ts";
import {register} from "../api/member.ts";

const useRegister = () => {
    return useMutation({
        mutationFn: (data: RegisterRequest) => register(data),
    })
}

export default useRegister;