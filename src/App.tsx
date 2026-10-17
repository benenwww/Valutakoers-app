import React, { useState, useEffect } from 'react';
import { DepositItem, DepositSession } from './types';
import { ItemCard } from './components/ItemCard';
import { JSONImportDropzone } from './components/JSONImportDropzone';
import { generateDepositReceiptPDF } from './utils/pdfGenerator';
import { 
  Leaf, 
  Trash2, 
  Download, 
  Store, 
  RefreshCw, 
  Sparkles, 
  AlertCircle,
  HelpCircle,
  Info,
  Calendar,
  Clock
} from 'lucide-react';

const INITIAL_CATEGORIES = [
  {
    id: 'cans',
    name: 'Drink Cans',
    value: 0.15,
    count: 0,
    description: 'Soda, energy drinks, and beer aluminum/steel cans',
    unitText: 'Recyclable Metal Cans (€0.15)',
  },
  {
    id: 'smallPet',
    name: 'Small PET',
    value: 0.15,
    count: 0,
    description: 'Water, soda, or juice plastic bottles up to 1.0 Liters',
    unitText: 'Lightweight Polyethylene PET (€0.15)',
  },
  {
    id: 'bigPet',
    name: 'Large PET',
    value: 0.25,
    count: 0,
    description: 'Large family-size soda or water bottles over 1.0 Liters',
    unitText: 'Heavy-duty Polyethylene PET (€0.25)',
  },
  {
    id: 'glass',
    name: 'Glass Bottles',
    value: 0.10,
    count: 0,
    description: 'Classic glass beer, juice, water, or specialty glass containers',
    unitText: 'Reusable Amber / Flint Glass (€0.10)',
  },
];

export default function App() {
  const [items, setItems] = useState<DepositItem[]>(INITIAL_CATEGORIES);
  const [storeName, setStoreName] = useState<string>('');
  const [showAlert, setShowAlert] = useState<boolean>(false);
  const [alertType, setAlertType] = useState<'success' | 'reset' | null>(null);
  const [sessionStartTime] = useState<string>(() => {
    return new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  });
  const [currentDateTime, setCurrentDateTime] = useState<string>('');

  // Update real-time formatted date and time
  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      const options: Intl.DateTimeFormatOptions = { 
        month: 'short', 
        day: '2-digit', 
        year: 'numeric' 
      };
      const datePart = now.toLocaleDateString('en-US', options);
      const timePart = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
      setCurrentDateTime(`${datePart} • ${timePart}`);
    };
    
    updateTime();
    const interval = setInterval(updateTime, 1000);
    return () => clearInterval(interval);
  }, []);

  // Load from local storage on mount
  useEffect(() => {
    const savedCounts = localStorage.getItem('deposit_calculated_counts');
    const savedStore = localStorage.getItem('deposit_calculated_store');
    
    if (savedCounts) {
      try {
        const parsedCounts = JSON.parse(savedCounts) as Record<string, number>;
        setItems(prev => prev.map(item => ({
          ...item,
          count: typeof parsedCounts[item.id] === 'number' ? parsedCounts[item.id] : 0
        })));
      } catch (e) {
        console.error("Failed to load saved deposit counts", e);
      }
    }

    if (savedStore) {
      setStoreName(savedStore);
    }
  }, []);

  // Sync to local storage when items or storeName changes
  const saveToLocalStorage = (currentItems: DepositItem[], currentStore: string) => {
    const countsRecord = currentItems.reduce((acc, item) => {
      acc[item.id] = item.count;
      return acc;
    }, {} as Record<string, number>);
    
    localStorage.setItem('deposit_calculated_counts', JSON.stringify(countsRecord));
    localStorage.setItem('deposit_calculated_store', currentStore);
  };

  const handleCountChange = (id: string, newCount: number) => {
    const updated = items.map(item => {
      if (item.id === id) {
        return { ...item, count: newCount };
      }
      return item;
    });
    setItems(updated);
    saveToLocalStorage(updated, storeName);
  };

  const handleStoreNameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    setStoreName(val);
    saveToLocalStorage(items, val);
  };

  // Import JSON File State Re-population
  const handleImportSession = (session: DepositSession) => {
    const updated = items.map(item => ({
      ...item,
      count: typeof session.items[item.id] === 'number' ? session.items[item.id] : 0,
    }));
    setItems(updated);
    if (session.storeName !== undefined) {
      setStoreName(session.storeName);
    }
    saveToLocalStorage(updated, session.storeName || '');
  };

  // Reset Session Counts
  const handleResetSession = () => {
    const updated = items.map(item => ({ ...item, count: 0 }));
    setItems(updated);
    setStoreName('');
    localStorage.removeItem('deposit_calculated_counts');
    localStorage.removeItem('deposit_calculated_store');
    
    setAlertType('reset');
    setShowAlert(true);
    setTimeout(() => setShowAlert(false), 3000);
  };

  // Checkout Handler: Downloads both the PDF Receipt and the JSON Session State
  const handleCheckoutAndDownload = () => {
    const hasItems = items.some(i => i.count > 0);
    if (!hasItems) {
      alert("Please add at least one bottle or can to your tracking list before checking out!");
      return;
    }

    // A. Generate and trigger download of PDF
    const grandTotal = items.reduce((sum, item) => sum + (item.value * item.count), 0);
    generateDepositReceiptPDF(items, grandTotal, storeName);

    // B. Generate and trigger download of JSON state
    const dataToExport = {
      items: items.reduce((acc, item) => {
        acc[item.id] = item.count;
        return acc;
      }, {} as Record<string, number>),
      updatedAt: new Date().toISOString(),
      storeName: storeName,
    };

    const jsonString = JSON.stringify(dataToExport, null, 2);
    const blob = new Blob([jsonString], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    
    const downloadAnchor = document.createElement('a');
    downloadAnchor.href = url;
    downloadAnchor.download = `Deposit_Return_Session_${new Date().toISOString().slice(0, 10)}.json`;
    document.body.appendChild(downloadAnchor);
    downloadAnchor.click();
    
    // Clean up
    document.body.removeChild(downloadAnchor);
    URL.revokeObjectURL(url);

    // Show visual confirmation alert
    setAlertType('success');
    setShowAlert(true);
    setTimeout(() => setShowAlert(false), 6000);
  };

  // Load a quick demo load-out for testing/previewing
  const handleLoadDemo = () => {
    const demo = items.map(item => {
      switch (item.id) {
        case 'cans': return { ...item, count: 24 };
        case 'smallPet': return { ...item, count: 12 };
        case 'bigPet': return { ...item, count: 8 };
        case 'glass': return { ...item, count: 5 };
        default: return item;
      }
    });
    setItems(demo);
    setStoreName('Rewe Center Market');
    saveToLocalStorage(demo, 'Rewe Center Market');
  };

  // Calculations for dashboard
  const totalRefund = items.reduce((sum, item) => sum + (item.count * item.value), 0);
  const totalItemCount = items.reduce((sum, item) => sum + item.count, 0);

  // Environmental impact values
  const weightRecycledGrams = (
    (items.find(i => i.id === 'cans')?.count || 0) * 14 + // 14g alum
    (items.find(i => i.id === 'smallPet')?.count || 0) * 22 + // 22g plastic
    (items.find(i => i.id === 'bigPet')?.count || 0) * 45 + // 45g thick plastic
    (items.find(i => i.id === 'glass')?.count || 0) * 250 // 250g glass beer bottle
  );
  
  // CO2 reduction (average 150g CO2 per bottle/can recycled)
  const co2OffsetKg = (totalItemCount * 0.15);

  // Storage progression capacity ratio (e.g. out of 100 units limit target)
  const capacityLimit = 100;
  const capacityPct = Math.min(100, Math.round((totalItemCount / capacityLimit) * 100));

  return (
    <div id="deposit-root" className="min-h-screen flex flex-col bg-slate-50 font-sans text-slate-900 selection:bg-emerald-500 selection:text-white">
      
      {/* Visual Accent Top Bar */}
      <div className="h-1 bg-gradient-to-r from-blue-500 via-teal-500 to-emerald-500 w-full" />

      {/* Header Section styled exactly like High Density template */}
      <header id="app-header" className="flex flex-col sm:flex-row items-center justify-between px-8 py-6 bg-white border-b border-slate-200 gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight flex items-center gap-2 font-display">
            <span className="p-2 bg-emerald-500 rounded-lg text-white block">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
            </span>
            RecycleCalc <span className="text-slate-400 font-normal text-lg">v1.0</span>
          </h1>
        </div>
        
        <div className="flex flex-wrap items-center justify-center sm:justify-end gap-6 w-full sm:w-auto">
          <div className="text-center sm:text-right">
            <div className="text-xs font-semibold text-slate-400 uppercase tracking-widest">Current Session</div>
            <div className="text-sm font-semibold text-slate-700 mt-0.5 font-mono">{currentDateTime || 'Loading session calendar...'}</div>
          </div>
          <div className="hidden md:block h-10 w-px bg-slate-200"></div>
          <div className="flex gap-2">
            <button 
              id="btn-header-demo"
              onClick={handleLoadDemo}
              className="px-4 py-2 text-sm font-semibold border border-emerald-200 text-emerald-700 hover:bg-emerald-50 rounded-md transition-colors flex items-center gap-1 cursor-pointer"
            >
              <Sparkles className="w-3.5 h-3.5" />
              Demo Bags
            </button>
            <button 
              id="btn-header-reset"
              onClick={handleResetSession}
              disabled={totalItemCount === 0 && !storeName}
              className={`px-4 py-2 text-sm font-medium rounded-md transition-colors border ${
                totalItemCount > 0 || storeName
                  ? 'bg-slate-50 hover:bg-slate-100 text-slate-700 border-slate-200 cursor-pointer'
                  : 'bg-slate-50/50 text-slate-300 border-slate-100 cursor-not-allowed'
              }`}
            >
              Reset
            </button>
          </div>
        </div>
      </header>

      {/* Main Dashboard Content */}
      <main id="app-layout" className="flex-1 grid grid-cols-1 lg:grid-cols-12 gap-8 p-6 lg:p-8">
        
        {/* Alerts Center */}
        {showAlert && (
          <div 
            id="toast-notification"
            className={`col-span-1 lg:col-span-12 p-4 rounded-xl border flex items-start gap-3 shadow-sm animate-fade-in ${
              alertType === 'success'
                ? 'bg-emerald-50 border-emerald-200 text-emerald-800'
                : 'bg-slate-50 border-slate-200 text-slate-700'
            }`}
          >
            <div className="p-1 rounded-lg shrink-0 mt-0.5 bg-white border border-emerald-100 text-emerald-600">
              <Sparkles className="w-4 h-4" />
            </div>
            <div>
              <h4 className="font-bold text-sm">
                {alertType === 'success' ? 'Voucher Generated Successfully!' : 'Voucher Resetted'}
              </h4>
              <p className="text-xs mt-0.5 leading-relaxed text-slate-600">
                {alertType === 'success' 
                  ? 'Your deposit return checklist state has been downloaded as Pfand_Refund_Receipt_*.pdf and Deposit_Return_Session_*.json backup.'
                  : 'State cleared completely.'}
              </p>
            </div>
          </div>
        )}

        {/* Item Grid (8 columns on desktop) */}
        <div className="col-span-1 lg:col-span-8 flex flex-col gap-6">
          
          {/* Target Return Store Picker */}
          <div className="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm">
            <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-4">
              <div className="flex-1">
                <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-1.5">
                  Target Store or Return Station
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-3 flex items-center pointer-events-none text-slate-400">
                    <Store className="w-4 h-4" />
                  </div>
                  <input
                    id="input-store-name"
                    type="text"
                    placeholder="e.g. Rewe, Edeka, Lidl, Aldi..."
                    value={storeName}
                    onChange={handleStoreNameChange}
                    className="w-full pl-10 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800 placeholder-slate-400 font-medium focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500/10 focus:border-emerald-500 transition-all font-display"
                  />
                </div>
              </div>
              <div className="shrink-0 text-left sm:text-right flex flex-col justify-center">
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Calculations Standard</span>
                <span className="text-xs text-slate-500 mt-1">Automatic deposit rates matched</span>
              </div>
            </div>
          </div>

          {/* Cards Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6" id="item-grid-container">
            {items.map(item => (
              <ItemCard
                key={item.id}
                item={item}
                onCountChange={handleCountChange}
              />
            ))}
          </div>

          {/* Tips Banner */}
          <div className="bg-amber-50/50 border border-amber-100 rounded-2xl p-4 flex gap-3 text-xs text-amber-800 leading-relaxed">
            <Info className="w-4.5 h-4.5 text-amber-600 shrink-0 mt-0.5" />
            <div>
              <strong className="font-semibold text-amber-950">Sorting Tip:</strong> Glass bottles and cans should be separated into different cardboard boxes or bags. This simplifies the feed cycle into reverse vending machines and speeds up checkout.
            </div>
          </div>

        </div>

        {/* Summary Sidebar (4 columns on desktop) */}
        <div className="col-span-1 lg:col-span-4 flex flex-col gap-6">
          
          {/* Main Slate-900 Refund Card */}
          <div className="bg-slate-900 rounded-2xl p-8 text-white flex flex-col shadow-xl relative overflow-hidden" id="checkout-summary-card">
            
            {/* Background design glow elements */}
            <div className="absolute top-0 right-0 w-32 h-32 bg-emerald-500/10 rounded-full blur-2xl pointer-events-none" />
            <div className="absolute bottom-0 left-0 w-24 h-24 bg-teal-500/10 rounded-full blur-xl pointer-events-none" />

            <div className="text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">
              Total Reimbursement
            </div>
            
            {/* Giant Currency view */}
            <div className="text-5xl sm:text-6xl lg:text-7xl font-mono font-bold tracking-tight text-white mt-1 leading-none">
              €{totalRefund.toFixed(2)}
            </div>

            {/* Refund Trend line */}
            <div className="mt-4 text-emerald-400 flex items-center gap-2 text-xs font-medium">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
              </svg>
              <span>Voucher simulation active</span>
            </div>

            {/* Items storage limits details */}
            <div className="mt-10 space-y-4 pt-6 border-t border-slate-800">
              <div className="flex justify-between items-center text-sm">
                <span className="text-slate-400">Total Items Counted</span>
                <span className="font-mono font-bold text-slate-100">{totalItemCount}</span>
              </div>
              
              {/* Target capacity progress bar */}
              <div className="w-full bg-slate-800 h-2 rounded-full overflow-hidden">
                <div 
                  className="bg-emerald-400 h-full transition-all duration-300"
                  style={{ width: `${capacityPct}%` }}
                />
              </div>

              <div className="flex justify-between items-center text-xs">
                <span className="text-slate-500">Suggested Batch Bag Capacity</span>
                <span className="text-slate-300 font-medium font-mono">{totalItemCount} / {capacityLimit} bottles</span>
              </div>
            </div>

            {/* Interactive Downloads block */}
            <div className="mt-8 space-y-3 pt-6 border-t border-slate-800">
              <button
                id="btn-checkout"
                onClick={handleCheckoutAndDownload}
                disabled={totalItemCount === 0}
                className={`w-full py-4 rounded-xl font-bold flex items-center justify-center gap-2.5 tracking-wide transition-all uppercase text-sm ${
                  totalItemCount > 0
                    ? 'bg-emerald-500 hover:bg-emerald-600 text-slate-950 shadow-lg shadow-emerald-500/20 active:scale-[0.98] cursor-pointer'
                    : 'bg-slate-800 text-slate-500 border border-slate-800 cursor-not-allowed'
                }`}
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                CHECKOUT NOW
              </button>

              <div className="grid grid-cols-2 gap-3">
                <button
                  id="btn-export-pdf"
                  onClick={handleCheckoutAndDownload}
                  disabled={totalItemCount === 0}
                  className={`py-3 text-xs font-bold rounded-xl flex flex-col items-center justify-center gap-1 border transition-colors ${
                    totalItemCount > 0
                      ? 'bg-slate-800 hover:bg-slate-700 text-slate-200 border-slate-700 cursor-pointer'
                      : 'bg-slate-850 text-slate-600 border-slate-850 cursor-not-allowed'
                  }`}
                  title="Generate Voucher PDF"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-blue-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
                  </svg>
                  VIRTUAL PDF
                </button>
                <button
                  id="btn-export-json"
                  onClick={handleCheckoutAndDownload}
                  disabled={totalItemCount === 0}
                  className={`py-3 text-xs font-bold rounded-xl flex flex-col items-center justify-center gap-1 border transition-colors ${
                    totalItemCount > 0
                      ? 'bg-slate-800 hover:bg-slate-700 text-slate-200 border-slate-700 cursor-pointer'
                      : 'bg-slate-850 text-slate-600 border-slate-850 cursor-not-allowed'
                  }`}
                  title="Download session backup JSON"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-orange-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                  </svg>
                  SAVE CONFIG
                </button>
              </div>
            </div>

          </div>

          {/* Quick Facts Sustainability Box */}
          <div className="bg-white border border-slate-200 rounded-2xl p-6" id="environmental-impact-card">
            <h3 className="text-xs font-bold text-slate-400 uppercase tracking-widest mb-4">Sustainability Impact</h3>
            <div className="flex items-center gap-4">
              <div className="p-3 bg-blue-50 rounded-full shrink-0">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
                </svg>
              </div>
              <div>
                <div className="text-lg font-bold text-slate-850 font-mono">
                  ~{co2OffsetKg.toFixed(2)}kg CO2
                </div>
                <div className="text-xs text-slate-500">Emissions prevented this session</div>
              </div>
            </div>
            
            {/* Mini Legend & Distribution bar if count > 0 */}
            {totalItemCount > 0 && (
              <div className="pt-4 mt-4 border-t border-slate-100">
                <div className="flex h-1.5 w-full rounded-full overflow-hidden bg-slate-100">
                  {items.map((item, idx) => {
                    const count = item.count;
                    if (count === 0) return null;
                    const pctStr = `${Math.max(4, Math.round((count / totalItemCount) * 100))}%`;
                    const bgColors = ['bg-blue-500', 'bg-teal-500', 'bg-emerald-500', 'bg-amber-600'];
                    return (
                      <div
                        key={item.id}
                        style={{ width: pctStr }}
                        className={`${bgColors[idx]} h-full`}
                      />
                    );
                  })}
                </div>
                <div className="flex flex-wrap gap-2.5 mt-2.5 text-[10px] text-slate-500">
                  {items.map((item, idx) => {
                    const dots = ['bg-blue-500', 'bg-teal-500', 'bg-emerald-500', 'bg-amber-600'];
                    return (
                      <div key={item.id} className="flex items-center gap-1">
                        <span className={`w-1.5 h-1.5 rounded-full ${dots[idx]}`} />
                        <span>{item.name}: {item.count}</span>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}
          </div>

          {/* Session restore area */}
          <JSONImportDropzone onImport={handleImportSession} />

        </div>

      </main>

      {/* Footer / Status Bar styled exactly like High Density template */}
      <footer id="app-footer" className="px-8 py-4 bg-white border-t border-slate-200 flex flex-col md:flex-row items-center justify-between gap-4">
        <div className="flex flex-wrap gap-x-8 gap-y-2 justify-center">
          <div className="flex items-center gap-2">
            <div className="w-2 h-2 rounded-full bg-emerald-500"></div>
            <span className="text-xs font-medium text-slate-500">Sync Active: local backup offline-ready</span>
          </div>
          <div className="flex items-center gap-2">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span className="text-xs font-medium text-slate-500">Session init time: {sessionStartTime}</span>
          </div>
          <button
            id="btn-trigger-crash-test"
            onClick={() => {
              throw new Error("Handmatige test error getriggerd in de gebruikersinterface (App.tsx:544)");
            }}
            className="text-[10px] font-bold font-mono tracking-wider px-2 py-1 rounded bg-red-50 text-red-600 border border-red-200 hover:bg-red-100 transition-colors cursor-pointer"
            title="Simuleer een gecrashte app-toestand om logbestand aanmaak te testen"
          >
            Simuleer Crash Error
          </button>
        </div>
        <div className="text-[10px] font-bold text-slate-300 uppercase tracking-wider italic">
          ECO-TRACKER DEPOSIT SUITE 2026
        </div>
      </footer>
    </div>
  );
}
