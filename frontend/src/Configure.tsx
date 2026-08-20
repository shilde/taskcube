import { useEffect, useRef, useState } from 'react';
import {
  fetchTasks, fetchFaceConfig, assignTask, createTask,
  type Task, type FaceConfig,
} from './api';
import './Configure.css';

const FACE_COUNT = 6;

export default function Configure() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [faceConfig, setFaceConfig] = useState<FaceConfig[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // new task form
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [jiraId, setJiraId] = useState('');
  const [creating, setCreating] = useState(false);
  const titleRef = useRef<HTMLInputElement>(null);

  const load = () => {
    setLoading(true);
    setError(null);
    Promise.all([fetchTasks(), fetchFaceConfig()])
      .then(([t, f]) => { setTasks(t); setFaceConfig(f); })
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const taskForFace = (faceId: number) =>
    faceConfig.find((f) => f.faceId === faceId)?.taskId ?? '';

  const handleAssign = async (faceId: number, taskId: string) => {
    if (!taskId) return;
    try {
      await assignTask(faceId, taskId);
      setFaceConfig((prev) => {
        const next = prev.filter((f) => f.faceId !== faceId);
        return [...next, { faceId, taskId }];
      });
    } catch {
      alert(`Failed to assign task to face ${faceId}`);
    }
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) return;
    setCreating(true);
    try {
      await createTask({
        title: title.trim(),
        description: description.trim() || null,
        jiraId: jiraId.trim() || null,
      });
      setTitle('');
      setDescription('');
      setJiraId('');
      titleRef.current?.focus();
      // reload tasks to get the new id
      const updated = await fetchTasks();
      setTasks(updated);
    } catch {
      alert('Failed to create task');
    } finally {
      setCreating(false);
    }
  };

  if (loading) return <p className="state-msg">Loading…</p>;
  if (error) return <p className="state-msg error">Failed to load: {error}</p>;

  return (
    <>
      <section className="config-section">
        <h2>Cube faces</h2>
        <div className="face-grid">
          {Array.from({ length: FACE_COUNT }, (_, i) => i + 1).map((faceId) => (
            <div key={faceId} className="face-card">
              <span className="face-badge">{faceId}</span>
              <select
                value={taskForFace(faceId)}
                onChange={(e) => handleAssign(faceId, e.target.value)}
              >
                <option value="">— unassigned —</option>
                {tasks.map((t) => (
                  <option key={t.id} value={t.id}>{t.title}</option>
                ))}
              </select>
            </div>
          ))}
        </div>
      </section>

      <section className="config-section">
        <h2>Tasks</h2>

        {tasks.length > 0 && (
          <table className="task-table">
            <thead>
              <tr>
                <th>Title</th>
                <th>Description</th>
                <th>Jira</th>
              </tr>
            </thead>
            <tbody>
              {tasks.map((t) => (
                <tr key={t.id}>
                  <td>{t.title}</td>
                  <td className="secondary">{t.description ?? '—'}</td>
                  <td className="secondary">{t.jiraId ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        <form className="create-form" onSubmit={handleCreate}>
          <h3>New task</h3>
          <div className="form-row">
            <input
              ref={titleRef}
              type="text"
              placeholder="Title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
            />
            <input
              type="text"
              placeholder="Description (optional)"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
            <input
              type="text"
              placeholder="Jira ID (optional)"
              value={jiraId}
              onChange={(e) => setJiraId(e.target.value)}
            />
            <button type="submit" disabled={creating || !title.trim()}>
              {creating ? 'Creating…' : 'Create'}
            </button>
          </div>
        </form>
      </section>
    </>
  );
}
