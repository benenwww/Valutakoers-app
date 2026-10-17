import React, { useRef, useState, useEffect } from 'react';
import { DepositItem } from '../types';
import { Plus, Minus } from 'lucide-react';

interface ItemCardProps {
  item: DepositItem;
  onCountChange: (id: string, value: number) => void;
}

// Custom Premium Bottle / Can SVG illustrations with gorgeous CSS gradients
const BottleIllustration: React.FC<{ id: string }> = ({ id }) => {
  switch (id) {
    case 'cans':
      return (
        <svg className="w-10 h-12 filter drop-shadow-sm" viewBox="0 0 60 100" fill="none" xmlns="http://www.w3.org/2500/svg">
          <defs>
            <linearGradient id="canGrad" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stopColor="#94a3b8" />
              <stop offset="30%" stopColor="#cbd5e1" />
              <stop offset="70%" stopColor="#f1f5f9" />
              <stop offset="100%" stopColor="#64748b" />
            </linearGradient>
            <linearGradient id="redAccent" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stopColor="#3b82f6" />
              <stop offset="100%" stopColor="#1d4ed8" />
            </linearGradient>
          </defs>
          <ellipse cx="30" cy="12" rx="20" ry="5" fill="#e2e8f0" stroke="#94a3b8" strokeWidth="1.5" />
          <path d="M10,12 C10,12 10,16 10,88 C10,93 14,94 30,94 C46,94 50,93 50,88 L50,12 Z" fill="url(#canGrad)" stroke="#475569" strokeWidth="1" />
          <path d="M10,35 C20,38 40,30 50,35 L50,65 C40,60 20,68 10,65 Z" fill="url(#redAccent)" opacity="0.85" />
          <line x1="10" y1="45" x2="50" y2="45" stroke="#ffffff" strokeWidth="1.5" strokeDasharray="3 3" opacity="0.6" />
          <circle cx="30" cy="11" r="3" stroke="#475569" strokeWidth="1.5" fill="none" />
          <path d="M30,11 L25,12" stroke="#475569" strokeWidth="1.5" />
          <ellipse cx="30" cy="88" rx="20" ry="5" fill="none" stroke="#475569" strokeWidth="1" />
          <ellipse cx="30" cy="94" rx="14" ry="3" fill="#cbd5e1" />
        </svg>
      );
    case 'smallPet':
      return (
        <svg className="w-10 h-12 filter drop-shadow-sm" viewBox="0 0 60 100" fill="none" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <linearGradient id="smallPetGrad" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stopColor="#2dd4bf" />
              <stop offset="40%" stopColor="#f0fdfa" />
              <stop offset="70%" stopColor="#99f6e4" />
              <stop offset="100%" stopColor="#0d9488" />
            </linearGradient>
            <linearGradient id="waterLevel" x1="0%" y1="100%" x2="0%" y2="0%">
              <stop offset="0%" stopColor="#0f766e" />
              <stop offset="100%" stopColor="#2dd4bf" />
            </linearGradient>
          </defs>
          <rect x="23" y="2" width="14" height="7" rx="1" fill="#0d9488" stroke="#115e59" strokeWidth="1" />
          <path d="M24,9 L36,9 L38,20 L22,20 Z" fill="url(#smallPetGrad)" stroke="#0d9488" strokeWidth="0.75" />
          <path d="M22,20 C18,24 14,30 14,40 L14,84 C14,89 18,91 30,91 C42,91 46,89 46,84 L46,40 C46,30 42,24 38,20 Z" fill="url(#smallPetGrad)" stroke="#0d9488" strokeWidth="1" />
          <path d="M14.5,48 C14.5,48 20,46 30,48 C40,50 45.5,48 45.5,48 L45.5,84 C45.5,88 41,89.5 30,89.5 C19,89.5 14.5,88 14.5,84 Z" fill="url(#waterLevel)" opacity="0.45" />
          <rect x="14" y="52" width="32" height="12" fill="#0d9488" opacity="0.9" />
          <line x1="16" y1="58" x2="44" y2="58" stroke="#ffffff" strokeWidth="1" opacity="0.6" />
        </svg>
      );
    case 'bigPet':
      return (
        <svg className="w-10 h-12 filter drop-shadow-sm" viewBox="0 0 60 100" fill="none" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <linearGradient id="bigPetGrad" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stopColor="#4ade80" />
              <stop offset="45%" stopColor="#f0fdf4" />
              <stop offset="75%" stopColor="#bbf7d0" />
              <stop offset="100%" stopColor="#22c55e" />
            </linearGradient>
            <linearGradient id="colaLevel" x1="0%" y1="100%" x2="0%" y2="0%">
              <stop offset="0%" stopColor="#14532d" />
              <stop offset="100%" stopColor="#16a34a" />
            </linearGradient>
          </defs>
          <rect x="22" y="1" width="16" height="8" rx="1.5" fill="#15803d" stroke="#166534" strokeWidth="1" />
          <path d="M23,9 L37,9 L39,22 L21,22 Z" fill="url(#bigPetGrad)" stroke="#16a34a" strokeWidth="0.75" />
          <path d="M21,22 C15,26 11,32 11,42 L11,88 C11,94 16,95 30,95 C44,95 49,94 49,88 L49,42 C49,32 45,26 39,22 Z" fill="url(#bigPetGrad)" stroke="#15803d" strokeWidth="1" />
          <path d="M11.5,40 C11.5,40 18,37 30,40 C42,43 48.5,40 48.5,40 L48.5,88 C48.5,93.5 43,94 30,94 C17,94 11.5,93.5 11.5,88 Z" fill="url(#colaLevel)" opacity="0.65" />
          <rect x="11" y="47" width="38" height="16" fill="#15803d" opacity="0.95" />
          <rect x="15" y="51" width="30" height="8" fill="#facc15" rx="1" />
        </svg>
      );
    case 'glass':
      return (
        <svg className="w-10 h-12 filter drop-shadow-sm" viewBox="0 0 60 100" fill="none" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <linearGradient id="glassGrad" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stopColor="#b45309" />
              <stop offset="40%" stopColor="#fef3c7" />
              <stop offset="70%" stopColor="#d97706" />
              <stop offset="100%" stopColor="#78350f" />
            </linearGradient>
            <linearGradient id="liquidBeer" x1="0%" y1="100%" x2="0%" y2="0%">
              <stop offset="0%" stopColor="#7c2d12" />
              <stop offset="100%" stopColor="#ea580c" />
            </linearGradient>
          </defs>
          <path d="M21,1 L39,1 L38,6 L22,6 Z" fill="#94a3b8" stroke="#64748b" strokeWidth="1" />
          <path d="M23,6 L37,6 L35,38 L25,38 Z" fill="url(#glassGrad)" stroke="#78350f" strokeWidth="1" />
          <path d="M25,38 C21,42 15,48 15,55 L15,88 C15,93 18,94 30,94 C42,94 45,93 45,88 L45,55 C45,48 39,42 35,38 Z" fill="url(#glassGrad)" stroke="#78350f" strokeWidth="1" />
          <path d="M18,65 C18,58 20,55 22,55" stroke="#ffffff" strokeWidth="1.5" strokeLinecap="round" opacity="0.4" />
          <circle cx="30" cy="70" r="4" fill="#d97706" />
        </svg>
      );
    default:
      return null;
  }
};

export const ItemCard: React.FC<ItemCardProps> = ({
  item,
  onCountChange,
}) => {
  const [inputValue, setInputValue] = useState<string>(item.count.toString());
  const [isFocused, setIsFocused] = useState<boolean>(false);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (!isFocused) {
      setInputValue(item.count.toString());
    }
  }, [item.count, isFocused]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const text = e.target.value.replace(/[^0-9]/g, '');
    setInputValue(text);
  };

  const handleBlur = () => {
    setIsFocused(false);
    const parsed = parseInt(inputValue, 10);
    const confirmedCount = isNaN(parsed) || parsed < 0 ? 0 : parsed;
    onCountChange(item.id, confirmedCount);
    setInputValue(confirmedCount.toString());
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      inputRef.current?.blur();
    }
  };

  const triggerIncrementBy = (val: number) => {
    const nextCount = item.count + val;
    onCountChange(item.id, nextCount >= 0 ? nextCount : 0);
  };

  // Extract category config based on High Density theme details
  const getCategoryDetails = () => {
    switch (item.id) {
      case 'cans':
        return {
          badgeText: 'Aluminium',
          badgeClass: 'bg-blue-100 text-blue-700 border border-blue-200',
          btnClass: 'bg-blue-600 hover:bg-blue-700 active:bg-blue-800 shadow-lg shadow-blue-100',
          focusBorder: 'focus:border-blue-500 focus:ring-blue-500/10',
        };
      case 'smallPet':
        return {
          badgeText: 'Plastic',
          badgeClass: 'bg-teal-100 text-teal-700 border border-teal-200',
          btnClass: 'bg-teal-600 hover:bg-teal-700 active:bg-teal-800 shadow-lg shadow-teal-100',
          focusBorder: 'focus:border-teal-500 focus:ring-teal-500/10',
        };
      case 'bigPet':
        return {
          badgeText: 'Plastic',
          badgeClass: 'bg-emerald-100 text-emerald-700 border border-emerald-200',
          btnClass: 'bg-emerald-600 hover:bg-emerald-700 active:bg-emerald-800 shadow-lg shadow-emerald-100',
          focusBorder: 'focus:border-emerald-500 focus:ring-emerald-500/10',
        };
      case 'glass':
        return {
          badgeText: 'Reusable',
          badgeClass: 'bg-amber-100 text-amber-700 border border-amber-200',
          btnClass: 'bg-amber-600 hover:bg-amber-700 active:bg-amber-800 shadow-lg shadow-amber-100',
          focusBorder: 'focus:border-amber-500 focus:ring-amber-500/10',
        };
      default:
        return {
          badgeText: 'Recyclable',
          badgeClass: 'bg-slate-100 text-slate-700 border border-slate-200',
          btnClass: 'bg-slate-700 hover:bg-slate-800 active:bg-slate-900 shadow-lg shadow-slate-100',
          focusBorder: 'focus:border-slate-500 focus:ring-slate-500/10',
        };
    }
  };

  const config = getCategoryDetails();
  const subtotalValue = item.value * item.count;
  const displayCount = item.count.toString().padStart(2, '0');

  return (
    <div 
      id={`item-card-${item.id}`} 
      className={`bg-white rounded-2xl p-6 border border-slate-200 shadow-sm flex flex-col justify-between transition-all duration-200 ${
        item.count > 0 ? 'ring-1 ring-slate-400/20 shadow-md scale-[1.005]' : 'hover:shadow-md'
      }`}
    >
      {/* Upper row */}
      <div className="flex justify-between items-start gap-4">
        <div className="flex-1">
          <span className={`inline-block px-2.5 py-0.5 text-[10px] font-bold uppercase rounded-md tracking-wider mb-2 ${config.badgeClass}`}>
            {config.badgeText}
          </span>
          <div className="flex items-center gap-2">
            <h2 className="text-xl font-bold font-display tracking-tight text-slate-900">{item.name}</h2>
            <div className="shrink-0 opacity-80" title="Item Category Illustration">
              <BottleIllustration id={item.id} />
            </div>
          </div>
          <p className="text-xs text-slate-500 italic mt-1 leading-relaxed">{item.description}</p>
        </div>
        
        {/* Rate block */}
        <div className="text-right shrink-0">
          <span className="text-xl font-mono font-bold text-slate-800">€{item.value.toFixed(2)}</span>
          <div className="text-[10px] text-slate-400 uppercase tracking-widest font-semibold mt-0.5">Per Unit</div>
        </div>
      </div>

      {/* Control structure - Giant Count combined with premium touch controls */}
      <div className="flex items-center justify-between mt-6 pt-5 border-t border-slate-100">
        
        {/* Count display with manual inline input */}
        <div className="flex items-center gap-3">
          <div 
            className={`text-5xl font-mono font-extrabold tracking-tight transition-colors duration-200 ${
              item.count > 0 ? 'text-slate-900' : 'text-slate-200'
            }`}
          >
            {displayCount}
          </div>
          
          {/* Quick micro controls for step adjustment */}
          <div className="flex flex-col gap-1">
            <button
              id={`btn-plus-step-${item.id}`}
              onClick={() => triggerIncrementBy(1)}
              className="p-1.5 rounded-md border border-slate-200 hover:bg-slate-50 text-slate-600 transition-colors"
              title="Add 1"
            >
              <Plus className="w-3 h-3 stroke-[2.5]" />
            </button>
            <button
              id={`btn-minus-step-${item.id}`}
              onClick={() => triggerIncrementBy(-1)}
              disabled={item.count <= 0}
              className={`p-1.5 rounded-md border transition-colors ${
                item.count > 0 
                  ? 'border-slate-200 hover:bg-slate-50 text-slate-600' 
                  : 'border-slate-100 text-slate-300 cursor-not-allowed'
              }`}
              title="Subtract 1"
            >
              <Minus className="w-3 h-3 stroke-[2.5]" />
            </button>
          </div>
        </div>

        {/* Inputs & Presets group */}
        <div className="flex flex-col items-end gap-2.5">
          {/* Main numeric text manual override */}
          <div className="flex items-center gap-1.5">
            <span className="text-[10px] uppercase font-bold text-slate-400 font-mono">Count:</span>
            <input
              ref={inputRef}
              id={`input-raw-count-${item.id}`}
              type="text"
              inputMode="numeric"
              pattern="[0-9]*"
              value={inputValue}
              onChange={handleInputChange}
              onFocus={() => setIsFocused(true)}
              onBlur={handleBlur}
              onKeyDown={handleKeyDown}
              className={`w-12 text-center font-mono font-bold text-xs text-slate-800 bg-slate-50 border border-slate-200 rounded-md py-1 focus:bg-white focus:outline-none focus:ring-2 transition-all ${config.focusBorder}`}
            />
          </div>

          {/* Quick block loaders (+5, +10) */}
          <div className="flex items-center gap-1">
            <button
              id={`btn-add5-${item.id}`}
              onClick={() => triggerIncrementBy(5)}
              className="text-[10px] font-mono font-bold text-slate-500 bg-slate-100 hover:bg-slate-200/80 active:bg-slate-200 px-2 py-1 rounded transition-colors"
              title="Add 5 items"
            >
              +5
            </button>
            <button
              id={`btn-add10-${item.id}`}
              onClick={() => triggerIncrementBy(10)}
              className="text-[10px] font-mono font-bold text-slate-500 bg-slate-100 hover:bg-slate-200/80 active:bg-slate-200 px-2 py-1 rounded transition-colors"
              title="Add 10 items"
            >
              +10
            </button>
          </div>
        </div>

      </div>

      {/* Subtotal line */}
      <div className="flex justify-between items-center mt-3 text-[11px] font-mono text-slate-400 font-medium">
        <span>Subtotal refund</span>
        <span className={item.count > 0 ? 'text-slate-800 font-bold' : ''}>
          €{subtotalValue.toFixed(2)}
        </span>
      </div>

    </div>
  );
};
