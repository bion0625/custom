import {BrowserRouter, Route, Routes} from "react-router-dom";
import AuthProvider from "./provider/AuthProvider.tsx";
import {lazy, Suspense} from "react";
import LoadingScreen from "./component/LoadingScreen.tsx";
import ErrorBoundary from "./component/ErrorBoundary.tsx";
import PrivateRoute from "./component/PrivateRoute.tsx";

function App() {
    const Login = lazy(() => import("./pages/Login.tsx"))
    const Register = lazy(() => import("./pages/Register.tsx"))
    const PublicGame = lazy(() => import("./pages/PublicGame.tsx"))
    const PrivateGame = lazy(() => import("./pages/PrivateGame.tsx"))
    const PublicAdmin = lazy(() => import("./pages/PublicAdmin.tsx"))
    const PublicAdminGraph = lazy(() => import("./pages/PublicAdminGraph.tsx"))
    const PrivateAdminGraph = lazy(() => import("./pages/PrivateAdminGraph.tsx"))
    const PrivateAdmin = lazy(() => import("./pages/PrivateAdmin.tsx"))
    const MyStories = lazy(() => import("./pages/MyStories.tsx"))

  return (
      <AuthProvider>
          <BrowserRouter>
              <ErrorBoundary>
                  <Suspense fallback={<LoadingScreen/>}>
                      <Routes>
                          <Route path={"/"} element={<PublicGame/>}/>
                          <Route path={"/login"} element={<Login/>}/>
                          <Route path={"/register"} element={<Register/>}/>
                          <Route path={"/game/:memberId/:storyId"} element={<PrivateRoute><PrivateGame/></PrivateRoute>}/>
                        <Route path={"/admin/public"} element={<PublicAdmin/>}/>
                        <Route path={"/admin/public/graph"} element={<PublicAdminGraph/>}/>
                        <Route path={"/my/stories"} element={<MyStories/>}/>
                        <Route path={"/my"} element={<PrivateAdmin/>}/>
                        <Route path={"/my/graph"} element={<PrivateAdminGraph/>}/>
                      </Routes>
                  </Suspense>
              </ErrorBoundary>
          </BrowserRouter>
      </AuthProvider>
  )
}

export default App
