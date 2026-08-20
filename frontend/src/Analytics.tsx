import { useEffect, useState } from 'react';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell,
} from 'recharts';
import { fetchStats, formatDuration, type TaskSummary } from './api';

type Range = 'today' | 'yesterday' | 'last3days' | 'week';

const RANGES: { key: Range; label: string }[] = [
  { key: 'today', label: 'Today' },
  { key: 'yesterday', label: 'Yesterday' },
  { key: 'last3days', label: 'Last 3 days' },
  { key: 'week', label: 'This week' },
];

const BAR_COLORS = ['#6366f1', '#8b5cf6', '#a855f7', '#c084fc', '#d8b4fe', '#ede9fe'];

function getRangeDates(range: Range): { from: Date; to: Date } {
  const now = new Date();
  const startOfDay = (d: Date) => new Date(d.getFullYear(), d.getMonth(), d.getDate());

  switch (range) {
    case 'today':
      return { from: startOfDay(now), to: now };
    case 'yesterday': {
      const yesterday = new Date(now);
      yesterday.setDate(yesterday.getDate() - 1);
      return { from: startOfDay(yesterday), to: startOfDay(now) };
    }
    case 'last3days': {
      const threeDaysAgo = new Date(now);
      threeDaysAgo.setDate(threeDaysAgo.getDate() - 3);
      return { from: startOfDay(threeDaysAgo), to: now };
    }
    case 'week': {
      const d = new Date(now);
      const day = d.getDay();
      const diff = day === 0 ? -6 : 1 - day;
      d.setDate(d.getDate() + diff);
      return { from: startOfDay(d), to: now };
    }
  }
}

function CustomTooltip({ active, payload }: { active?: boolean; payload?: { value: number }[] }) {
  if (!active || !payload?.length) return null;
  return <div className="chart-tooltip">{formatDuration(payload[0].value)}</div>;
}

export default function Analytics() {
  const [range, setRange] = useState<Range>('today');
  const [data, setData] = useState<TaskSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    const { from, to } = getRangeDates(range);
    fetchStats(from, to)
      .then(setData)
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }, [range]);

  const chartData = data.map((d) => ({ name: d.title, ms: d.totalSpentTimeMs }));
  const totalMs = data.reduce((sum, d) => sum + d.totalSpentTimeMs, 0);

  return (
    <>
      <nav className="range-selector">
        {RANGES.map(({ key, label }) => (
          <button
            key={key}
            className={range === key ? 'active' : ''}
            onClick={() => setRange(key)}
          >
            {label}
          </button>
        ))}
      </nav>

      {loading && <p className="state-msg">Loading…</p>}
      {error && <p className="state-msg error">Failed to load: {error}</p>}
      {!loading && !error && data.length === 0 && (
        <p className="state-msg">No sessions in this period.</p>
      )}

      {!loading && !error && data.length > 0 && (
        <>
          <p className="total">Total: <strong>{formatDuration(totalMs)}</strong></p>

          <div className="chart-wrapper">
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={chartData} margin={{ top: 8, right: 16, left: 0, bottom: 48 }}>
                <XAxis dataKey="name" angle={-35} textAnchor="end" tick={{ fontSize: 13 }} interval={0} />
                <YAxis tickFormatter={(ms: number) => formatDuration(ms)} tick={{ fontSize: 12 }} width={56} />
                <Tooltip content={<CustomTooltip />} cursor={{ fill: 'var(--accent-bg)' }} />
                <Bar dataKey="ms" radius={[4, 4, 0, 0]}>
                  {chartData.map((_, i) => (
                    <Cell key={i} fill={BAR_COLORS[i % BAR_COLORS.length]} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>

          <table className="task-table">
            <thead>
              <tr><th>Task</th><th>Time spent</th></tr>
            </thead>
            <tbody>
              {data.map((d) => (
                <tr key={d.taskId}>
                  <td>{d.title}</td>
                  <td className="duration">{formatDuration(d.totalSpentTimeMs)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </>
  );
}
