import {useNavigate} from "react-router-dom";

const Register: React.FC = () => {
    const navigate = useNavigate();
    return (
        <div className="flex flex-col items-center justify-center h-screen">
            <div className="w-80 p-6 bg-white rounded shadow">
                <h2 className="text-xl font-bold mb-4">회원가입</h2>
                <form onSubmit={() => alert('회원가입')}>
                    <input className="w-full mb-2 p-2 border rounded" placeholder="아이디"/>
                    <input className="w-full mb-2 p-2 border rounded" placeholder="비밀번호"/>
                    <input className="w-full mb-2 p-2 border rounded" placeholder="비밀번호 확인"/>
                    <input className="w-full mb-2 p-2 border rounded" placeholder="이름"/>
                    <input className="w-full mb-2 p-2 border rounded" placeholder="전화번호"/>
                    <input className="w-full mb-2 p-2 border rounded" placeholder="이메일"/>
                    <button className="w-full p-2 bg-green-500 text-white rounded" type="submit">회원가입</button>
                    <button className="w-full mt-2 p-2 text-sm text-blue-500" type="button" onClick={() => navigate("/login")}>이미 계정이 있으신가요? 로그인</button>
                </form>
            </div>
        </div>
    )
}

export default Register;