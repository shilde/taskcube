// --- Stats ---

export interface TaskSummary {
  taskId: string;
  title: string;
  totalSpentTimeMs: number;
}

export async function fetchStats(from: Date, to: Date): Promise<TaskSummary[]> {
  const params = new URLSearchParams({
    from: from.toISOString(),
    to: to.toISOString(),
  });
  const res = await fetch(`/stats?${params}`);
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

// --- Tasks ---

export interface Task {
  id: string;
  title: string;
  description: string | null;
  jiraId: string | null;
  spentTimeMs: number;
}

export async function fetchTasks(): Promise<Task[]> {
  const res = await fetch('/task/tasks');
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

export async function createTask(payload: {
  title: string;
  description: string | null;
  jiraId: string | null;
}): Promise<void> {
  const res = await fetch('/task/create-task', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
}

// --- Face config ---

export interface FaceConfig {
  faceId: number;
  taskId: string;
}

export async function fetchFaceConfig(): Promise<FaceConfig[]> {
  const res = await fetch('/face-config/faces');
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

export async function assignTask(faceId: number, taskId: string): Promise<void> {
  const res = await fetch(`/face-config/${faceId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ taskId }),
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
}

// --- Utils ---

export function formatDuration(ms: number): string {
  const totalSeconds = Math.floor(ms / 1000);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  if (hours > 0) return `${hours}h ${minutes}m`;
  return `${minutes}m`;
}
