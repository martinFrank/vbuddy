import { useEffect, useState, useCallback, useRef } from 'react'
import { useParams } from 'react-router-dom'
import { api, Buddy, BuddyBackground, Need, VBuddyTask } from '../api/client'
import styles from './NeedsPage.module.css'

const NEED_LABELS: Record<string, string> = {
  HUNGER: 'Hunger',
  BOREDOM: 'Langeweile',
  KNOWLEDGE: 'Wissensdurst',
  EXERCISE: 'Bewegungsdrang',
  SOCIAL: 'Soziale Interaktion',
}

export default function NeedsPage() {
  const { buddyId } = useParams()
  const [buddy, setBuddy] = useState<Buddy | null>(null)
  const [needs, setNeeds] = useState<Need[]>([])
  const [currentTask, setCurrentTask] = useState<VBuddyTask | null>(null)
  const [background, setBackground] = useState<BuddyBackground | null>(null)
  const [backgroundOpen, setBackgroundOpen] = useState(false)
  const bgIntervalRef = useRef<ReturnType<typeof setInterval> | null>(null)

  const loadBackground = useCallback((id: number) => {
    api.getBackground(id).then(setBackground).catch(() => setBackground(null))
  }, [])

  useEffect(() => {
    if (!buddyId) return
    const id = Number(buddyId)

    const load = () => {
      api.getBuddy(id).then(setBuddy)
      api.getNeeds(id).then(setNeeds)
      api.getCurrentTask(id).then(setCurrentTask).catch(() => setCurrentTask(null))
    }

    load()
    loadBackground(id)
    const interval = setInterval(load, 30000)
    return () => clearInterval(interval)
  }, [buddyId, loadBackground])

  useEffect(() => {
    if (!buddyId) return
    const id = Number(buddyId)
    if (bgIntervalRef.current) clearInterval(bgIntervalRef.current)

    const isGenerating = background?.status === 'GENERATING' || background?.status === 'PENDING'
    const pollMs = isGenerating ? 5000 : 30000
    bgIntervalRef.current = setInterval(() => loadBackground(id), pollMs)
    return () => { if (bgIntervalRef.current) clearInterval(bgIntervalRef.current) }
  }, [buddyId, background?.status, loadBackground])

  const formatRemainingTime = (task: VBuddyTask) => {
    const end = new Date(new Date(task.startTime).getTime() + task.durationMinutes * 60000)
    const remaining = Math.max(0, Math.round((end.getTime() - Date.now()) / 60000))
    return `${remaining} min verbleibend`
  }

  return (
    <div className={styles.needs}>
      {buddy && (
        <div className={styles.location}>
          <span className={styles.locationIcon}>&#128205;</span>
          <span>{buddy.currentLocation}</span>
        </div>
      )}

      {background && background.status === 'COMPLETED' && background.narrativeText && (
        <div className={styles.background}>
          <div className={styles.backgroundHeader} onClick={() => setBackgroundOpen(!backgroundOpen)}>
            <span>{backgroundOpen ? '\u25BC' : '\u25B6'} Hintergrund</span>
          </div>
          {backgroundOpen && (
            <div className={styles.backgroundContent}>
              {background.narrativeText.split('\n').map((p, i) => p.trim() ? <p key={i}>{p}</p> : null)}
            </div>
          )}
        </div>
      )}

      {background && (background.status === 'GENERATING' || background.status === 'PENDING') && (
        <div className={styles.backgroundLoading}>Hintergrund wird generiert...</div>
      )}

      {background && background.status === 'FAILED' && (
        <div className={styles.background}>
          <div className={styles.backgroundHeader}>
            <span>Hintergrund</span>
          </div>
          <div className={styles.backgroundContent}>
            <p style={{ color: '#e74c3c' }}>Generierung fehlgeschlagen.</p>
            <button className={styles.regenerateButton} onClick={() => {
              if (buddyId) {
                api.generateBackground(Number(buddyId))
                loadBackground(Number(buddyId))
              }
            }}>Neu generieren</button>
          </div>
        </div>
      )}

      {currentTask && (
        <div className={styles.currentTask}>
          <div className={styles.taskHeader}>
            <span className={styles.taskPulse}>&#9679;</span>
            <span className={styles.taskLabel}>Aktuelle Aktivit&auml;t</span>
          </div>
          <h3>{currentTask.title}</h3>
          <p className={styles.taskDescription}>{currentTask.description}</p>
          <div className={styles.taskMeta}>
            <span>&#128205; {currentTask.location}</span>
            <span>&#9202; {currentTask.durationMinutes} min</span>
            <span className={styles.taskRemaining}>{formatRemainingTime(currentTask)}</span>
          </div>
        </div>
      )}

      {!currentTask && buddy && (
        <div className={styles.noTask}>Kein aktiver Task — VBuddy wartet auf die n&auml;chste Aktivit&auml;t.</div>
      )}

      <h2>Bed&uuml;rfnisse</h2>
      <div className={styles.grid}>
        {needs.map((need) => {
          const percent = (need.currentValue / need.maxValue) * 100
          return (
            <div key={need.id} className={styles.card}>
              <div className={styles.label}>
                {NEED_LABELS[need.needType] ?? need.needType}
              </div>
              <div className={styles.bar}>
                <div
                  className={styles.fill}
                  style={{
                    width: `${percent}%`,
                    background: percent > 70 ? '#e74c3c' : percent > 40 ? '#f39c12' : '#27ae60',
                  }}
                />
              </div>
              <div className={styles.value}>{Math.round(need.currentValue)} / {Math.round(need.maxValue)}</div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
