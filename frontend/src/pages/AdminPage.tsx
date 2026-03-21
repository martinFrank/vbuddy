import { useState, useEffect, FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { api, AppUserResponse } from '../api/client'
import { useAuth } from '../hooks/useAuth'
import styles from './AdminPage.module.css'

export default function AdminPage() {
  const [users, setUsers] = useState<AppUserResponse[]>([])
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [role, setRole] = useState('USER')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const { user } = useAuth()
  const navigate = useNavigate()

  useEffect(() => {
    if (user && user.role !== 'ADMIN') {
      navigate('/setup')
    }
  }, [user, navigate])

  useEffect(() => {
    loadUsers()
  }, [])

  async function loadUsers() {
    try {
      setUsers(await api.getUsers())
    } catch {
      setError('Benutzer konnten nicht geladen werden.')
    }
  }

  async function handleCreate(e: FormEvent) {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await api.createUser(username, password, role)
      setUsername('')
      setPassword('')
      setRole('USER')
      await loadUsers()
    } catch (err: any) {
      setError(err.message || 'Fehler beim Anlegen des Benutzers.')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDelete(id: number) {
    try {
      await api.deleteUser(id)
      await loadUsers()
    } catch (err: any) {
      setError(err.message || 'Fehler beim Löschen des Benutzers.')
    }
  }

  return (
    <div className={styles.container}>
      <h1>Benutzerverwaltung</h1>
      <button className={styles.backButton} onClick={() => navigate('/setup')}>
        Zurück
      </button>

      <section className={styles.section}>
        <h2>Neuen Benutzer anlegen</h2>
        <form className={styles.form} onSubmit={handleCreate}>
          <input
            type="text"
            placeholder="Benutzername"
            value={username}
            onChange={e => setUsername(e.target.value)}
            required
          />
          <input
            type="password"
            placeholder="Passwort"
            value={password}
            onChange={e => setPassword(e.target.value)}
            required
          />
          <select value={role} onChange={e => setRole(e.target.value)}>
            <option value="USER">USER</option>
            <option value="ADMIN">ADMIN</option>
          </select>
          {error && <p className={styles.error}>{error}</p>}
          <button type="submit" disabled={submitting}>
            {submitting ? 'Erstelle...' : 'Benutzer erstellen'}
          </button>
        </form>
      </section>

      <section className={styles.section}>
        <h2>Benutzer</h2>
        <div className={styles.userList}>
          {users.map(u => (
            <div key={u.id} className={styles.userCard}>
              <div>
                <strong>{u.username}</strong>
                <span className={styles.role}>{u.role}</span>
              </div>
              <button
                className={styles.deleteButton}
                onClick={() => handleDelete(u.id)}
              >
                Löschen
              </button>
            </div>
          ))}
        </div>
      </section>
    </div>
  )
}
