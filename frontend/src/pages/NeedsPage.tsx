import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { api, Buddy, Need } from '../api/client'
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

  useEffect(() => {
    if (buddyId) {
      const id = Number(buddyId)
      api.getBuddy(id).then(setBuddy)
      api.getNeeds(id).then(setNeeds)
    }
  }, [buddyId])

  return (
    <div className={styles.needs}>
      {buddy && (
        <div className={styles.location}>
          <span className={styles.locationIcon}>&#128205;</span>
          <span>{buddy.currentLocation}</span>
        </div>
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
