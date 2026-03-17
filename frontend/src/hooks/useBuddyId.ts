import { useParams } from 'react-router-dom'

export default function useBuddyId(): number {
  const { buddyId } = useParams<{ buddyId: string }>()
  return Number(buddyId)
}
