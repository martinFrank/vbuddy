import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { api, VBuddyTask } from '../api/client'
import styles from './DailyPlanPage.module.css'

const STATUS_LABELS: Record<string, string> = {
  PLANNED: 'Geplant',
  IN_PROGRESS: 'Aktiv',
  COMPLETED: 'Erledigt',
  SKIPPED: 'Übersprungen',
  ABORTED: 'Abgebrochen',
}

function formatTime(iso: string) {
  return new Date(iso).toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })
}

function formatEndTime(iso: string, durationMinutes: number) {
  const end = new Date(new Date(iso).getTime() + durationMinutes * 60000)
  return end.toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })
}

export default function DailyPlanPage() {
  const { buddyId } = useParams()
  const [tasks, setTasks] = useState<VBuddyTask[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!buddyId) return
    const id = Number(buddyId)

    const load = () => {
      api.getTimeline(id).then(setTasks).finally(() => setLoading(false))
    }

    load()
    const interval = setInterval(load, 30000)
    return () => clearInterval(interval)
  }, [buddyId])

  if (loading) return <p>Laden...</p>

  if (tasks.length === 0) {
    return <p className={styles.empty}>Keine Aktivit&auml;ten im aktuellen Zeitfenster.</p>
  }

  return (
    <div className={styles.plan}>
      <h2>Tagesplan</h2>
      <div className={styles.timeline}>
        {tasks.map((task) => (
          <div
            key={task.id}
            className={`${styles.task} ${styles[task.status.toLowerCase()]}`}
          >
            <div className={styles.timeColumn}>
              <div className={styles.time}>{formatTime(task.startTime)}</div>
              <div className={styles.timeSeparator}>|</div>
              <div className={styles.timeEnd}>{formatEndTime(task.startTime, task.durationMinutes)}</div>
            </div>
            <div className={styles.indicator}>
              <div className={styles.dot} />
              <div className={styles.line} />
            </div>
            <div className={styles.content}>
              <div className={styles.titleRow}>
                <h3>{task.title}</h3>
                <span className={`${styles.statusBadge} ${styles[`badge_${task.status.toLowerCase()}`]}`}>
                  {STATUS_LABELS[task.status] ?? task.status}
                </span>
              </div>
              <p className={styles.description}>{task.description}</p>
              <div className={styles.meta}>
                <span>&#128205; {task.location}</span>
                <span>&#9202; {task.durationMinutes} min</span>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
