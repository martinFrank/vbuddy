import styles from './ErrorBanner.module.css'

interface Props {
  message: string
  onDismiss?: () => void
}

export default function ErrorBanner({ message, onDismiss }: Props) {
  return (
    <div className={styles.banner}>
      <span className={styles.message}>{message}</span>
      {onDismiss && (
        <button className={styles.dismiss} onClick={onDismiss}>&times;</button>
      )}
    </div>
  )
}
