import {useMutation} from "@tanstack/react-query";
import {deletePublicScene} from "../api/adminPublic.ts";

const useDeletePublicScene = () => {
    return useMutation({
        mutationFn: (id: number) => deletePublicScene(id)
    })
}

export default useDeletePublicScene;