const BASE_URL = '/vbuddy/api';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (!response.ok) {
    throw new Error(`API error: ${response.status}`);
  }
  return response.json();
}

export interface Buddy {
  id: number;
  name: string;
  personality: string;
  currentLocation: string;
  createdAt: string;
}

export interface Need {
  id: number;
  needType: string;
  currentValue: number;
  maxValue: number;
  decayRatePerHour: number;
  updatedAt: string;
}

export interface ChatMessage {
  id: number;
  role: 'USER' | 'ASSISTANT';
  content: string;
  createdAt: string;
}

export interface DailyPlan {
  id: number;
  planDate: string;
  createdAt: string;
  activities: Activity[];
}

export interface Activity {
  id: number;
  title: string;
  description: string;
  startTime: string;
  endTime: string;
  status: string;
}

export interface VBuddyTask {
  id: number;
  title: string;
  description: string;
  location: string;
  startTime: string;
  durationMinutes: number;
  status: string;
  createdAt: string;
}

export interface AiDecisionLog {
  id: number;
  context: string;
  decision: string;
  reasoning: string | null;
  createdAt: string;
}

export const api = {
  getBuddies: () => request<Buddy[]>('/buddies'),
  createBuddy: (name: string, personality: string) =>
    request<Buddy>('/buddies', {
      method: 'POST',
      body: JSON.stringify({ name, personality }),
    }),
  getBuddy: (id: number) => request<Buddy>(`/buddies/${id}`),
  getNeeds: (buddyId: number) => request<Need[]>(`/buddies/${buddyId}/needs`),
  getChatHistory: (buddyId: number) =>
    request<ChatMessage[]>(`/buddies/${buddyId}/chat`),
  sendMessage: (buddyId: number, content: string) =>
    request<ChatMessage>(`/buddies/${buddyId}/chat`, {
      method: 'POST',
      body: JSON.stringify({ content }),
    }),
  getDailyPlans: (buddyId: number) =>
    request<DailyPlan[]>(`/buddies/${buddyId}/daily-plans`),
  getTodayPlan: (buddyId: number) =>
    request<DailyPlan>(`/buddies/${buddyId}/daily-plans/today`),
  getAiDecisionLogs: (buddyId: number) =>
    request<AiDecisionLog[]>(`/buddies/${buddyId}/ai-decision-log`),
  getCurrentTask: (buddyId: number) =>
    request<VBuddyTask>(`/buddies/${buddyId}/tasks/current`),
};
