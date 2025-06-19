import {useMutation} from "@tanstack/react-query";
import {deletePublicScene} from "../api.ts";

const useDeletePublicScene = () => {
    return useMutation({
        mutationFn: (id: number) => deletePublicScene(id)
    })
}

export default useDeletePublicScene;