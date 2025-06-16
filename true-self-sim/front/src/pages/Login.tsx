import {useNavigate} from "react-router-dom";

const Login: React.FC = () => {
    const navigate = useNavigate();
    return (
        <div className="flex flex-col items-center justify-center h-screen">
            <div className="w-80 p-6 bg-white rounded shadow">
                <h2 className="text-xl font-bold mb-4">로그인</h2>
                <form onSubmit={() => alert('로그인')}>
                    <input className="w-full mb-2 p-2 border rounded" placeholder="아이디"/>
                    <input className="w-full mb-2 p-2 border rounded" placeholder="비밀번호"/>
                    <button className="w-full p-2 bg-blue-500 text-white rounded">로그인</button>
                    <button className="w-full mt-2 p-2 text-sm text-blue-500" type="button" onClick={() => navigate("/register")}>계정이 없으신가요? 회원가입</button>
                </form>
            </div>
        </div>
    )
}

export default Login;