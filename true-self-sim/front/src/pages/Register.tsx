import {useNavigate} from "react-router-dom";
import {useState} from "react";
import useRegister from "../hook/useRegister.ts";

const Register: React.FC = () => {
    const navigate = useNavigate();

    const [id, setId] = useState("");
    const [password, setPassword] = useState("");
    const [passwordCheck, setPasswordCheck] = useState("");
    const [name, setName] = useState("");
    const [phoneNumber, setPhoneNumber] = useState("");
    const [email, setEmail] = useState("");

    const [error, setError] = useState("");

    const passwordValid = (pwd: string, pwdCheck: string) => {
        if (pwd !== "" && pwdCheck !== "" && pwd !== pwdCheck) setError("비밀번호와 비밀번호 확인이 다릅니다.")
        else setError("")
    }

    const {mutate: register, isPending} = useRegister();

    const handleRegister = (e: React.FormEvent) => {
        e.preventDefault();
        register(
            {id, password, name, phoneNumber, email},
            {
                onSuccess: () => {
                    alert('회원가입 성공, 로그인 페이지로 이동합니다.');
                    navigate("/login");
                },
                onError: () => {
                    setError("회원가입 실패: 이미 존재하는 아이디일 수 있어요.")
                }
            },
        );
    }
    return (
        <div className="flex flex-col items-center justify-center h-screen">
            <div className="w-80 p-6 bg-white rounded shadow">
                <h2 className="text-xl font-bold mb-4">회원가입</h2>
                <form onSubmit={handleRegister}>
                    <input className="w-full mb-2 p-2 border rounded" placeholder="아이디" onChange={(e) => setId(e.target.value)}/>
                    <input className="w-full mb-2 p-2 border rounded" placeholder="비밀번호"
                           type="password"
                           onChange={(e) => {
                               setPassword(() => e.target.value);
                               passwordValid(e.target.value, passwordCheck);
                           }}/>
                    <input className="w-full mb-2 p-2 border rounded" placeholder="비밀번호 확인"
                           type="password"
                           onChange={(e) => {
                               setPasswordCheck(() => e.target.value);
                               passwordValid(password, e.target.value);
                           }}/>
                    <input className="w-full mb-2 p-2 border rounded" placeholder="이름" onChange={(e) => setName(e.target.value)}/>
                    <input className="w-full mb-2 p-2 border rounded" placeholder="전화번호" onChange={(e) => setPhoneNumber(e.target.value)}/>
                    <input className="w-full mb-2 p-2 border rounded" placeholder="이메일" onChange={(e) => setEmail(e.target.value)}/>
                    {error && <p className="text-red-500">{error}</p>}
                    <button className="w-full p-2 bg-green-500 text-white rounded" type="submit">
                        {isPending ? "회원가입중 ..." : "회원가입"}
                    </button>
                    <button className="w-full mt-2 p-2 text-sm text-blue-500" type="button" onClick={() => navigate("/login")}>이미 계정이 있으신가요? 로그인</button>
                </form>
            </div>
        </div>
    )
}

export default Register;