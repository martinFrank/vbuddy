import { NavLink, Outlet, useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import styles from './Layout.module.css'

export default function Layout() {
  const { buddyId } = useParams()
  const base = `/buddy/${buddyId}`
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  return (
    <div className={styles.layout}>
      <nav className={styles.nav}>
        <h1 className={styles.logo}>VBuddy</h1>
        <NavLink to={`${base}/chat`} className={({ isActive }) => isActive ? styles.active : ''}>
          Chat
        </NavLink>
        <NavLink to={`${base}/daily-plan`} className={({ isActive }) => isActive ? styles.active : ''}>
          Tagesplan
        </NavLink>
        <NavLink to={`${base}/details`} className={({ isActive }) => isActive ? styles.active : ''}>
          Details
        </NavLink>
        <NavLink to={`${base}/ai-log`} className={({ isActive }) => isActive ? styles.active : ''}>
          AI-Entscheidungen
        </NavLink>
        <div className={styles.spacer} />
        {user?.role === 'ADMIN' && (
          <NavLink to="/admin" className={({ isActive }) => isActive ? styles.active : ''}>
            Benutzerverwaltung
          </NavLink>
        )}
        <button className={styles.logoutButton} onClick={handleLogout}>
          Abmelden ({user?.username})
        </button>
      </nav>
      <main className={styles.main}>
        <Outlet />
      </main>
    </div>
  )
}
