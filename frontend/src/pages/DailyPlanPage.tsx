import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { api, DailyPlan } from '../api/client'
import styles from './DailyPlanPage.module.css'

export default function DailyPlanPage() {
  const { buddyId } = useParams()
  const [plan, setPlan] = useState<DailyPlan | null>(null)
  const [notFound, setNotFound] = useState(false)

  useEffect(() => {
    if (buddyId) {
      api.getTodayPlan(Number(buddyId))
        .then(setPlan)
        .catch(() => setNotFound(true))
    }
  }, [buddyId])

  if (notFound) {
    return <p className={styles.empty}>Kein Tagesplan für heute vorhanden.</p>
  }

  if (!plan) {
    return <p>Laden...</p>
  }

  return (
    <div className={styles.plan}>
      <h2>Tagesplan — {new Date(plan.planDate).toLocaleDateString('de-DE')}</h2>
      <div className={styles.timeline}>
        {plan.activities.map((activity) => (
          <div key={activity.id} className={styles.activity}>
            <div className={styles.time}>
              {activity.startTime} – {activity.endTime}
            </div>
            <div className={styles.content}>
              <h3>{activity.title}</h3>
              {activity.description && <p>{activity.description}</p>}
              <span className={`${styles.status} ${styles[activity.status.toLowerCase()]}`}>
                {activity.status}
              </span>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
