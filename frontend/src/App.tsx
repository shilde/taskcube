import { useState } from 'react';
import Analytics from './Analytics';
import Configure from './Configure';
import './App.css';

type Tab = 'analytics' | 'configure';

export default function App() {
  const [tab, setTab] = useState<Tab>('analytics');

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1>TaskCube</h1>
        <nav className="tab-nav">
          <button className={tab === 'analytics' ? 'active' : ''} onClick={() => setTab('analytics')}>
            Analytics
          </button>
          <button className={tab === 'configure' ? 'active' : ''} onClick={() => setTab('configure')}>
            Configure
          </button>
        </nav>
      </header>

      <main>
        {tab === 'analytics' && <Analytics />}
        {tab === 'configure' && <Configure />}
      </main>
    </div>
  );
}
