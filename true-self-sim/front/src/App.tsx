import {BrowserRouter, Route, Routes} from "react-router-dom";
import AuthProvider from "./provider/AuthProvider.tsx";
import {lazy, Suspense} from "react";
import LoadingScreen from "./pages/LoadingScreen.tsx";
import ErrorBoundary from "./pages/ErrorBoundary.tsx";
import PrivateRoute from "./component/PrivateRoute.tsx";

function App() {
    const Login = lazy(() => import("./pages/Login.tsx"))
    const Register = lazy(() => import("./pages/Register.tsx"))
    const PublicGame = lazy(() => import("./pages/PublicGame.tsx"))
    const PrivateGame = lazy(() => import("./pages/PrivateGame.tsx"))
    const PublicAdmin = lazy(() => import("./pages/PublicAdmin.tsx"))
    const PublicAdminGraph = lazy(() => import("./pages/PublicAdminGraph.tsx"))

  return (
      <AuthProvider>
          <BrowserRouter>
              <ErrorBoundary>
                  <Suspense fallback={<LoadingScreen/>}>
                      <Routes>
                          <Route path={"/"} element={<PublicGame/>}/>
                          <Route path={"/login"} element={<Login/>}/>
                          <Route path={"/register"} element={<Register/>}/>
                          <Route path={"/game"} element={<PrivateRoute><PrivateGame/></PrivateRoute>}/>
                          <Route path={"/admin/public"} element={<PublicAdmin/>}/>
                          <Route path={"/admin/public/graph"} element={<PublicAdminGraph/>}/>
                      </Routes>
                  </Suspense>
              </ErrorBoundary>
          </BrowserRouter>
      </AuthProvider>
  )
}

export default App
