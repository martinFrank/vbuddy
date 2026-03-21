import { useEffect, useRef, useState } from 'react'
import { api, ChatMessage } from '../api/client'
import ErrorBanner from '../components/ErrorBanner'
import useBuddyId from '../hooks/useBuddyId'
import styles from './ChatPage.module.css'

export default function ChatPage() {
  const buddyId = useBuddyId()
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [input, setInput] = useState('')
  const [sending, setSending] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const bottomRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    api.getChatHistory(buddyId)
      .then(setMessages)
      .catch((e) => setError(e.message))
  }, [buddyId])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const handleSend = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!input.trim()) return

    setError(null)
    const userMsg: ChatMessage = {
      id: Date.now(),
      role: 'USER',
      content: input,
      createdAt: new Date().toISOString(),
    }
    setMessages((prev) => [...prev, userMsg])
    const currentInput = input
    setInput('')
    setSending(true)

    try {
      const reply = await api.sendMessage(buddyId, currentInput)
      setMessages((prev) => [...prev, reply])
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Nachricht konnte nicht gesendet werden.')
    } finally {
      setSending(false)
    }
  }

  return (
    <div className={styles.chat}>
      <div className={styles.messages}>
        {error && <ErrorBanner message={error} onDismiss={() => setError(null)} />}
        {messages.map((msg) => (
          <div
            key={msg.id}
            className={`${styles.message} ${msg.role === 'USER' ? styles.user : styles.assistant}`}
          >
            <div className={styles.bubble}>{msg.content}</div>
          </div>
        ))}
        {sending && (
          <div className={`${styles.message} ${styles.assistant}`}>
            <div className={`${styles.bubble} ${styles.thinking}`}>Moment, ich denke nach...</div>
          </div>
        )}
        <div ref={bottomRef} />
      </div>
      <form onSubmit={handleSend} className={styles.inputArea}>
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Nachricht schreiben..."
          disabled={sending}
        />
        <button type="submit" disabled={sending || !input.trim()}>
          {sending ? '...' : 'Senden'}
        </button>
      </form>
    </div>
  )
}
