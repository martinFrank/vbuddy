import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { api, AiDecisionLog } from '../api/client'
import styles from './AiDecisionLogPage.module.css'

export default function AiDecisionLogPage() {
  const { buddyId } = useParams()
  const [logs, setLogs] = useState<AiDecisionLog[]>([])

  useEffect(() => {
    if (buddyId) {
      api.getAiDecisionLogs(Number(buddyId)).then(setLogs)
    }
  }, [buddyId])

  if (logs.length === 0) {
    return <p className={styles.empty}>Noch keine AI-Entscheidungen protokolliert.</p>
  }

  return (
    <div className={styles.log}>
      <h2>AI-Entscheidungen</h2>
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
