export function formatTime(iso: string): string {
  return new Date(iso).toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })
}

export function formatEndTime(iso: string, durationMinutes: number): string {
  const end = new Date(new Date(iso).getTime() + durationMinutes * 60000)
  return end.toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })
}

export function formatRemainingMinutes(iso: string, durationMinutes: number): number {
  const end = new Date(new Date(iso).getTime() + durationMinutes * 60000)
  return Math.max(0, Math.round((end.getTime() - Date.now()) / 60000))
}
