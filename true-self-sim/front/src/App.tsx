import {BrowserRouter, Route, Routes} from "react-router-dom";
import Login from "./pages/Login.tsx";
import Register from "./pages/Register.tsx";
import PublicGame from "./pages/PublicGame.tsx";
import PublicAdmin from "./pages/PublicAdmin.tsx";
import AuthProvider from "./provider/AuthProvider.tsx";

function App() {

  return (
      <AuthProvider>
          <BrowserRouter>
              <Routes>
                  <Route path={"/"} element={<PublicGame/>}/>
                  <Route path={"/login"} element={<Login/>}/>
                  <Route path={"/register"} element={<Register/>}/>
                  <Route path={"/admin/public"} element={<PublicAdmin/>}/>
              </Routes>
          </BrowserRouter>
      </AuthProvider>
  )
}

export default App
