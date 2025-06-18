import {BrowserRouter, Route, Routes} from "react-router-dom";
import Login from "./pages/Login.tsx";
import Register from "./pages/Register.tsx";
import PublicGame from "./pages/PublicGame.tsx";

function App() {

  return (
      <BrowserRouter>
          <Routes>
              <Route path={"/"} element={<PublicGame/>}/>
              <Route path={"/login"} element={<Login/>}/>
              <Route path={"/register"} element={<Register/>}/>
          </Routes>
      </BrowserRouter>
  )
}

export default App
