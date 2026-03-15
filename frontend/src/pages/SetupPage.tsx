import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api, Buddy } from '../api/client'
import styles from './SetupPage.module.css'

export default function SetupPage() {
  const navigate = useNavigate()
  const [buddies, setBuddies] = useState<Buddy[]>([])
  const [name, setName] = useState('')
  const [personality, setPersonality] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.getBuddies().then(setBuddies).finally(() => setLoading(false))
  }, [])

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault()
    const buddy = await api.createBuddy(name, personality)
    navigate(`/buddy/${buddy.id}/chat`)
  }

  if (loading) return <div className={styles.container}>Laden...</div>

  return (
    <div className={styles.container}>
      <h1>VBuddy</h1>
      <p>Erstelle deinen virtuellen Freund oder wähle einen bestehenden aus.</p>

      {buddies.length > 0 && (
        <div className={styles.section}>
          <h2>Bestehende Buddies</h2>
          <div className={styles.buddyList}>
            {buddies.map((b) => (
              <button
                key={b.id}
                className={styles.buddyCard}
                onClick={() => navigate(`/buddy/${b.id}/chat`)}
              >
                <strong>{b.name}</strong>
                <span>{b.personality.substring(0, 80)}...</span>
              </button>
            ))}
          </div>
        </div>
      )}

      <div className={styles.section}>
        <h2>Neuen Buddy erstellen</h2>
        <form onSubmit={handleCreate} className={styles.form}>
          <input
            type="text"
            placeholder="Name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
          <textarea
            placeholder="Persönlichkeit beschreiben..."
            value={personality}
            onChange={(e) => setPersonality(e.target.value)}
            rows={4}
            required
          />
          <button type="submit">Erstellen</button>
        </form>
      </div>
    </div>
  )
}
