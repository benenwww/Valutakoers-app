import React, { ErrorInfo, ReactNode } from 'react';
import { logCrashToApi } from '../utils/crashLogger';
import { AlertTriangle, RefreshCw, Home } from 'lucide-react';

interface Props {
  children?: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
  errorInfo: ErrorInfo | null;
}

export class ErrorBoundary extends React.Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null,
    errorInfo: null
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error, errorInfo: null };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    this.setState({
      error,
      errorInfo
    });
    
    // Construct rich code source info for client side crashes
    const codeSource = `React Component Tree Rendering \nComponent Stack Info:\n${errorInfo.componentStack || 'No component stack'}`;
    logCrashToApi(error, codeSource);
  }

  private handleReset = () => {
    localStorage.clear();
    window.location.reload();
  };

  public render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen bg-slate-900 text-slate-100 flex items-center justify-center p-6 font-sans">
          <div className="bg-slate-800/60 backdrop-blur-md max-w-xl w-full border border-slate-700/80 rounded-2xl p-8 shadow-2xl relative overflow-hidden animate-fade-in">
            <div className="absolute top-0 right-0 w-32 h-32 bg-red-500/10 rounded-full blur-2xl pointer-events-none" />
            
            <div className="flex items-center gap-3.5 mb-6">
              <div className="p-3 bg-red-500/20 text-red-400 rounded-xl border border-red-500/20 shrink-0">
                <AlertTriangle className="w-8 h-8" />
              </div>
              <div>
                <h1 className="text-2xl font-bold font-display tracking-tight text-white">Systeem Fout Gedetecteerd</h1>
                <p className="text-xs text-red-400/80 font-mono mt-0.5">Crash Log automatisch opgeslagen</p>
              </div>
            </div>

            <div className="space-y-4 mb-8">
              <div className="p-4 bg-slate-950/70 border border-slate-800 rounded-xl">
                <h2 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Foutmelding:</h2>
                <p className="text-sm font-mono text-red-300 break-words leading-relaxed select-all">
                  {this.state.error?.toString() || 'Onbekende React Rendering Fout'}
                </p>
              </div>

              <div className="p-4 bg-slate-950/70 border border-slate-800 rounded-xl">
                <h2 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Welke deel van de code heeft dit veroorzaakt:</h2>
                <div className="text-[11px] font-mono text-slate-400 max-h-32 overflow-y-auto leading-relaxed whitespace-pre-wrap select-all">
                  {this.state.errorInfo?.componentStack?.trim() || 'App.tsx rendering flow'}
                </div>
              </div>
            </div>

            <p className="text-xs text-slate-500 mb-6 leading-relaxed">
              Er is automatisch een crash-logbestand (.txt-formaat) weggeschreven onder de directory <code className="bg-slate-900 border border-slate-850 px-1.5 py-0.5 rounded text-red-400 font-mono">/logs/Debug logs/</code>. U kunt proberen de app te herstarten of te herstellen door uw lokale staten (cache) te resetten.
            </p>

            <div className="flex flex-col sm:flex-row gap-3">
              <button
                id="btn-crash-reload"
                onClick={() => window.location.reload()}
                className="flex-1 py-3 bg-white hover:bg-slate-50 text-slate-900 font-bold rounded-xl flex items-center justify-center gap-2 transition-all shadow-md active:scale-[0.98] cursor-pointer"
              >
                <RefreshCw className="w-4 h-4" />
                Opnieuw Laden
              </button>
              <button
                id="btn-crash-reset-cache"
                onClick={this.handleReset}
                className="flex-1 py-3 bg-slate-800 hover:bg-slate-700/80 text-slate-300 border border-slate-700 font-semibold rounded-xl flex items-center justify-center gap-2 transition-all active:scale-[0.98] cursor-pointer"
                title="Wist localStorage caches en laadt app schoon in"
              >
                <Home className="w-4 h-4" />
                Wis App-Cache
              </button>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
