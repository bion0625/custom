import {BrowserRouter, Route, Routes} from "react-router-dom";
import AuthProvider from "./provider/AuthProvider.tsx";
import {lazy, Suspense} from "react";
import LoadingScreen from "./pages/LoadingScreen.tsx";

function App() {
    const Login = lazy(() => import("./pages/Login.tsx"))
    const Register = lazy(() => import("./pages/Register.tsx"))
    const PublicGame = lazy(() => import("./pages/PublicGame.tsx"))
    const PublicAdmin = lazy(() => import("./pages/PublicAdmin.tsx"))

  return (
      <AuthProvider>
          <BrowserRouter>
              <Suspense fallback={<LoadingScreen/>}>
                  <Routes>
                      <Route path={"/"} element={<PublicGame/>}/>
                      <Route path={"/login"} element={<Login/>}/>
                      <Route path={"/register"} element={<Register/>}/>
                      <Route path={"/admin/public"} element={<PublicAdmin/>}/>
                  </Routes>
              </Suspense>
          </BrowserRouter>
      </AuthProvider>
  )
}

export default App
