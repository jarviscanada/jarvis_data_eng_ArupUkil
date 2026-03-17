import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import Dashboard from "./page/Dashboard/Dashboard";
import QuotePage from "./page/QuotePage/QuotePage";
import TraderDetails from "./page/TraderDetails/TraderDetails";

export default function Router() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/quotes" element={<QuotePage />} />
        <Route path="/trader/:traderId" element={<TraderDetails />} />
        <Route path="/traders/:traderId" element={<TraderDetails />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
