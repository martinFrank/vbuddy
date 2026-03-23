import { useState, useCallback, useRef, useEffect } from 'react'
import { api, Buddy, BuddyBackground, Need, VBuddyTask } from '../api/client'
import ErrorBanner from '../components/ErrorBanner'
import useBuddyId from '../hooks/useBuddyId'
import usePoll from '../hooks/usePoll'
import { formatRemainingMinutes } from '../utils/time'
import styles from './BuddyDetailsPage.module.css'

const NEED_LABELS: Record<string, string> = {
  HUNGER: 'Hunger',
  BOREDOM: 'Langeweile',
  KNOWLEDGE: 'Wissensdurst',
  EXERCISE: 'Bewegungsdrang',
  SOCIAL: 'Soziale Interaktion',
}

export default function NeedsPage() {
  const buddyId = useBuddyId()
  const [buddy, setBuddy] = useState<Buddy | null>(null)
  const [needs, setNeeds] = useState<Need[]>([])
  const [currentTask, setCurrentTask] = useState<VBuddyTask | null>(null)
  const [background, setBackground] = useState<BuddyBackground | null>(null)
  const [backgroundOpen, setBackgroundOpen] = useState(false)
  const [scheduleOpen, setScheduleOpen] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const bgIntervalRef = useRef<ReturnType<typeof setInterval> | null>(null)

  const loadBackground = useCallback(() => {
    api.getBackground(buddyId).then(setBackground).catch(() => setBackground(null))
  }, [buddyId])

  usePoll(() => {
    api.getBuddy(buddyId).then(setBuddy).catch((e) => setError(e.message))
    api.getNeeds(buddyId).then(setNeeds).catch((e) => setError(e.message))
    api.getCurrentTask(buddyId).then(setCurrentTask).catch(() => setCurrentTask(null))
  }, 30000)

  useEffect(() => {
    loadBackground()
  }, [loadBackground])

  useEffect(() => {
    if (bgIntervalRef.current) clearInterval(bgIntervalRef.current)

    const isGenerating = background?.status === 'GENERATING' || background?.status === 'PENDING'
    const pollMs = isGenerating ? 5000 : 30000
    bgIntervalRef.current = setInterval(loadBackground, pollMs)
    return () => { if (bgIntervalRef.current) clearInterval(bgIntervalRef.current) }
  }, [background?.status, loadBackground])

  return (
    <div className={styles.needs}>
      {error && <ErrorBanner message={error} onDismiss={() => setError(null)} />}

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

      {background && background.status === 'COMPLETED' && background.weeklySchedule && (
        <div className={styles.background}>
          <div className={styles.backgroundHeader} onClick={() => setScheduleOpen(!scheduleOpen)}>
            <span>{scheduleOpen ? '\u25BC' : '\u25B6'} Wochenplan</span>
          </div>
          {scheduleOpen && <ScheduleView json={background.weeklySchedule} />}
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
            <p style={{ color: 'var(--color-danger)' }}>Generierung fehlgeschlagen.</p>
            <button className={styles.regenerateButton} onClick={() => {
              api.generateBackground(buddyId)
                .then(loadBackground)
                .catch((e) => setError(e.message))
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
            {currentTask.sourceUrl && (
              <a href={currentTask.sourceUrl} target="_blank" rel="noopener noreferrer">&#128279; Quelle</a>
            )}
            <span className={styles.taskRemaining}>
              {formatRemainingMinutes(currentTask.startTime, currentTask.durationMinutes)} min verbleibend
            </span>
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
                    background: percent > 70 ? 'var(--color-danger)' : percent > 40 ? 'var(--color-warning)' : 'var(--color-success)',
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

interface TimeBlock {
  start: string
  end: string
  activity: string
}

interface WeeklySchedule {
  weekday: TimeBlock[]
  weekend: TimeBlock[]
}

function ScheduleView({ json }: { json: string }) {
  let schedule: WeeklySchedule
  try {
    schedule = JSON.parse(json)
  } catch {
    return <p className={styles.backgroundContent}>Stundenplan konnte nicht geladen werden.</p>
  }

  const isWeekend = new Date().getDay() === 0 || new Date().getDay() === 6
  const currentHHMM = new Date().toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })

  const renderBlocks = (blocks: TimeBlock[]) => (
    <div className={styles.scheduleBlocks}>
      {blocks.map((block, i) => {
        const active = isWeekend === (blocks === schedule.weekend)
          && currentHHMM >= block.start && currentHHMM < block.end
        return (
          <div key={i} className={`${styles.scheduleBlock} ${active ? styles.scheduleBlockActive : ''}`}>
            <span className={styles.scheduleTime}>{block.start} – {block.end}</span>
            <span className={styles.scheduleActivity}>{block.activity}</span>
          </div>
        )
      })}
    </div>
  )

  return (
    <div className={styles.backgroundContent}>
      <h4 className={styles.scheduleHeading}>Wochentag (Mo–Fr)</h4>
      {renderBlocks(schedule.weekday)}
      <h4 className={styles.scheduleHeading}>Wochenende (Sa–So)</h4>
      {renderBlocks(schedule.weekend)}
    </div>
  )
}
