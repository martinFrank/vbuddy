import { useEffect, useRef, useState } from 'react'
import { useParams } from 'react-router-dom'
import { api, ChatMessage } from '../api/client'
import styles from './ChatPage.module.css'

export default function ChatPage() {
  const { buddyId } = useParams()
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [input, setInput] = useState('')
  const [sending, setSending] = useState(false)
  const bottomRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (buddyId) {
      api.getChatHistory(Number(buddyId)).then(setMessages)
    }
  }, [buddyId])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const handleSend = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!input.trim() || !buddyId) return

    const userMsg: ChatMessage = {
      id: Date.now(),
      role: 'USER',
      content: input,
      createdAt: new Date().toISOString(),
    }
    setMessages((prev) => [...prev, userMsg])
    setInput('')
    setSending(true)

    try {
      const reply = await api.sendMessage(Number(buddyId), input)
      setMessages((prev) => [...prev, reply])
    } finally {
      setSending(false)
    }
  }

  return (
    <div className={styles.chat}>
      <div className={styles.messages}>
        {messages.map((msg) => (
          <div
            key={msg.id}
            className={`${styles.message} ${msg.role === 'USER' ? styles.user : styles.assistant}`}
          >
            <div className={styles.bubble}>{msg.content}</div>
          </div>
        ))}
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
          Senden
        </button>
      </form>
    </div>
  )
}
