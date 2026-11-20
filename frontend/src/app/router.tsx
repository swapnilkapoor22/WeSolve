import { BrowserRouter, Route, Routes } from "react-router-dom";
import { SignInPage } from "../features/auth/SignInPage";

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<SignInPage />} />
      </Routes>
    </BrowserRouter>
  );
}
