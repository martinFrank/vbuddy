import { Component, ReactNode } from 'react'

interface Props {
  children: ReactNode
}

interface State {
  hasError: boolean
  error: Error | null
}

export default class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false, error: null }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error }
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{
          padding: 'var(--space-2xl)',
          margin: 'var(--space-2xl) auto',
          maxWidth: 500,
          background: 'var(--color-surface)',
          border: '1px solid var(--color-danger)',
          borderLeft: '4px solid var(--color-danger)',
          borderRadius: 'var(--radius-md)',
        }}>
          <h2 style={{ color: 'var(--color-danger)', marginBottom: 'var(--space-sm)' }}>Etwas ist schiefgelaufen</h2>
          <p style={{ color: 'var(--color-text-secondary)', marginBottom: 'var(--space-lg)' }}>
            {this.state.error?.message || 'Ein unerwarteter Fehler ist aufgetreten.'}
          </p>
          <button
            onClick={() => this.setState({ hasError: false, error: null })}
            style={{
              padding: 'var(--space-sm) var(--space-lg)',
              background: 'var(--color-primary)',
              color: 'white',
              border: 'none',
              borderRadius: 'var(--radius-sm)',
              cursor: 'pointer',
            }}
          >
            Erneut versuchen
          </button>
        </div>
      )
    }

    return this.props.children
  }
}
