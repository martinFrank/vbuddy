import { NavLink, Outlet, useParams } from 'react-router-dom'
import styles from './Layout.module.css'

export default function Layout() {
  const { buddyId } = useParams()
  const base = `/buddy/${buddyId}`

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
        <NavLink to={`${base}/needs`} className={({ isActive }) => isActive ? styles.active : ''}>
          Bed&uuml;rfnisse
        </NavLink>
        <NavLink to={`${base}/ai-log`} className={({ isActive }) => isActive ? styles.active : ''}>
          AI-Entscheidungen
        </NavLink>
      </nav>
      <main className={styles.main}>
        <Outlet />
      </main>
    </div>
  )
}
