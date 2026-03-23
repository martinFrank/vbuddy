const BASE_URL = '/vbuddy/api';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    ...options,
  });
  if (response.status === 401 && !path.startsWith('/auth/')) {
    window.location.href = '/vbuddy/login';
    throw new Error('Nicht eingeloggt');
  }
  if (!response.ok) {
    let detail = '';
    try {
      const body = await response.json();
      detail = body.message || '';
    } catch {
      // ignore parse errors
    }
    throw new Error(detail || `Serverfehler (${response.status})`);
  }
  return response.json();
}

async function requestVoid(path: string, options?: RequestInit): Promise<void> {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    ...options,
  });
  if (response.status === 401 && !path.startsWith('/auth/')) {
    window.location.href = '/vbuddy/login';
    throw new Error('Nicht eingeloggt');
  }
  if (!response.ok) {
    let detail = '';
    try {
      const body = await response.json();
      detail = body.message || '';
    } catch {
      // ignore parse errors
    }
    throw new Error(detail || `Serverfehler (${response.status})`);
  }
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
  sourceUrl: string | null;
  createdAt: string;
}

export interface AiDecisionLog {
  id: number;
  context: string;
  decision: string;
  reasoning: string | null;
  createdAt: string;
}

export interface BuddyBackground {
  id: number;
  structuredData: string | null;
  weeklySchedule: string | null;
  narrativeText: string | null;
  status: 'PENDING' | 'GENERATING' | 'COMPLETED' | 'FAILED';
  createdAt: string;
  updatedAt: string;
}

export interface AuthUser {
  username: string;
  role: string;
}

export interface AppUserResponse {
  id: number;
  username: string;
  role: string;
  createdAt: string;
}

export const api = {
  authLogin: (username: string, password: string) =>
    request<AuthUser>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    }),
  authLogout: () =>
    requestVoid('/auth/logout', { method: 'POST' }),
  authMe: () =>
    request<AuthUser>('/auth/me'),
  getUsers: () =>
    request<AppUserResponse[]>('/users'),
  createUser: (username: string, password: string, role: string) =>
    request<AppUserResponse>('/users', {
      method: 'POST',
      body: JSON.stringify({ username, password, role }),
    }),
  deleteUser: (id: number) =>
    requestVoid(`/users/${id}`, { method: 'DELETE' }),
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
  getTimeline: (buddyId: number) =>
    request<VBuddyTask[]>(`/buddies/${buddyId}/tasks/timeline`),
  getBackground: (buddyId: number) =>
    request<BuddyBackground>(`/buddies/${buddyId}/background`),
  generateBackground: (buddyId: number) =>
    request<void>(`/buddies/${buddyId}/background/generate`, { method: 'POST' }),
};
