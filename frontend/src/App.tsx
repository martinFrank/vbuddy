import { Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/Layout'
import ChatPage from './pages/ChatPage'
import DailyPlanPage from './pages/DailyPlanPage'
import BuddyDetailsPage from './pages/BuddyDetailsPage'
import AiDecisionLogPage from './pages/AiDecisionLogPage'
import SetupPage from './pages/SetupPage'
import LoginPage from './pages/LoginPage'
import AdminPage from './pages/AdminPage'
import ProtectedRoute from './components/ProtectedRoute'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route path="/setup" element={<SetupPage />} />
        <Route path="/admin" element={<AdminPage />} />
        <Route path="/buddy/:buddyId" element={<Layout />}>
          <Route index element={<Navigate to="chat" replace />} />
          <Route path="chat" element={<ChatPage />} />
          <Route path="daily-plan" element={<DailyPlanPage />} />
          <Route path="details" element={<BuddyDetailsPage />} />
          <Route path="ai-log" element={<AiDecisionLogPage />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}
