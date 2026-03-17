import { useEffect, useState } from 'react'
import { api, AiDecisionLog } from '../api/client'
import ErrorBanner from '../components/ErrorBanner'
import useBuddyId from '../hooks/useBuddyId'
import styles from './AiDecisionLogPage.module.css'

export default function AiDecisionLogPage() {
  const buddyId = useBuddyId()
  const [logs, setLogs] = useState<AiDecisionLog[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api.getAiDecisionLogs(buddyId)
      .then(setLogs)
      .catch((e) => setError(e.message))
  }, [buddyId])

  return (
    <div className={styles.log}>
      <h2>AI-Entscheidungen</h2>

      {error && <ErrorBanner message={error} onDismiss={() => setError(null)} />}

      {logs.length === 0 && !error && (
        <p className={styles.empty}>Noch keine AI-Entscheidungen protokolliert.</p>
      )}

      {logs.map((entry) => (
        <div key={entry.id} className={styles.entry}>
          <div className={styles.header}>
            <time>{new Date(entry.createdAt).toLocaleString('de-DE')}</time>
          </div>
          <div className={styles.section}>
            <span className={styles.label}>Kontext</span>
            <p>{entry.context}</p>
          </div>
          <div className={styles.section}>
            <span className={styles.label}>Entscheidung</span>
            <p>{entry.decision}</p>
          </div>
          {entry.reasoning && (
            <div className={styles.section}>
              <span className={styles.label}>Begr&uuml;ndung</span>
              <p>{entry.reasoning}</p>
            </div>
          )}
        </div>
      ))}
    </div>
  )
}
