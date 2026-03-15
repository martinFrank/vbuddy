import { Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/Layout'
import ChatPage from './pages/ChatPage'
import DailyPlanPage from './pages/DailyPlanPage'
import NeedsPage from './pages/NeedsPage'
import AiDecisionLogPage from './pages/AiDecisionLogPage'
import SetupPage from './pages/SetupPage'

export default function App() {
  return (
    <Routes>
      <Route path="/setup" element={<SetupPage />} />
      <Route path="/buddy/:buddyId" element={<Layout />}>
        <Route index element={<Navigate to="chat" replace />} />
        <Route path="chat" element={<ChatPage />} />
        <Route path="daily-plan" element={<DailyPlanPage />} />
        <Route path="needs" element={<NeedsPage />} />
        <Route path="ai-log" element={<AiDecisionLogPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/setup" replace />} />
    </Routes>
  )
}
