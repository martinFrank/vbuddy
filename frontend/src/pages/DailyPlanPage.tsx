import { useState } from 'react'
import { api, VBuddyTask } from '../api/client'
import ErrorBanner from '../components/ErrorBanner'
import useBuddyId from '../hooks/useBuddyId'
import usePoll from '../hooks/usePoll'
import { formatTime, formatEndTime } from '../utils/time'
import styles from './DailyPlanPage.module.css'

const STATUS_LABELS: Record<string, string> = {
  PLANNED: 'Geplant',
  IN_PROGRESS: 'Aktiv',
  COMPLETED: 'Erledigt',
  SKIPPED: 'Übersprungen',
  ABORTED: 'Abgebrochen',
}

export default function DailyPlanPage() {
  const buddyId = useBuddyId()
  const [tasks, setTasks] = useState<VBuddyTask[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  usePoll(() => {
    api.getTimeline(buddyId)
      .then(setTasks)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, 30000)

  if (loading) return <p>Laden...</p>

  return (
    <div className={styles.plan}>
      <h2>Tagesplan</h2>

      {error && <ErrorBanner message={error} onDismiss={() => setError(null)} />}

      {tasks.length === 0 && !error && (
        <p className={styles.empty}>Keine Aktivit&auml;ten im aktuellen Zeitfenster.</p>
      )}

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
                {task.sourceUrl && (
                  <a href={task.sourceUrl} target="_blank" rel="noopener noreferrer">&#128279; Quelle</a>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
