import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Login from "./pages/Login";
import MainGame from "./pages/MainGame"; // 기존 게임 페이지

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<MainGame />} />
        <Route path="/login" element={<Login />} />
      </Routes>
    </Router>
  );
}

export default App;