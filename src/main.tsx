import {StrictMode} from 'react';
import {createRoot} from 'react-dom/client';
import App from './App';
import { ErrorBoundary } from './components/ErrorBoundary';
import { logCrashToApi } from './utils/crashLogger';
import './index.css';

// Log global unhandled window exceptions and syntax runtime crashes
window.addEventListener('error', (event) => {
  // Gracefully filter out standard service worker or telemetry fetch errors to prevent loops
  if (event.message?.includes('fetch') || event.error?.message?.includes('fetch')) return;
  
  const source = `Global uncaught browser crash at:\nURL: ${event.filename || 'unknown'}\nLine: ${event.lineno || '?'}:${event.colno || '?'}\nStack: ${event.error?.stack || 'No explicit stack'}`;
  logCrashToApi(event.error || event.message, source);
});

// Log any unhandled async Promise rejections
window.addEventListener('unhandledrejection', (event) => {
  const reasonStr = event.reason instanceof Error 
    ? event.reason.stack 
    : (typeof event.reason === 'object' ? JSON.stringify(event.reason) : String(event.reason));
  
  const source = `Unhandled Promise Rejection:\n${reasonStr || 'Promise rejected without description'}`;
  logCrashToApi(event.reason || 'Unhandled Promise Rejection', source);
});

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ErrorBoundary>
      <App />
    </ErrorBoundary>
  </StrictMode>,
);
