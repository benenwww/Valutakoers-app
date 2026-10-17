import React, { useState, useRef } from 'react';
import { Upload, FileCheck, AlertCircle } from 'lucide-react';
import { DepositSession } from '../types';

interface JSONImportDropzoneProps {
  onImport: (session: DepositSession) => void;
}

export const JSONImportDropzone: React.FC<JSONImportDropzoneProps> = ({ onImport }) => {
  const [dragActive, setDragActive] = useState(false);
  const [feedback, setFeedback] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const processFile = (file: File) => {
    if (file.type !== 'application/json' && !file.name.endsWith('.json')) {
      setFeedback({ type: 'error', message: 'Only standard JSON export files are allowed.' });
      return;
    }

    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const text = e.target?.result as string;
        const parsed = JSON.parse(text);

        // Simple schema checks
        if (parsed && typeof parsed === 'object' && parsed.items && typeof parsed.items === 'object') {
          onImport({
            items: parsed.items,
            updatedAt: parsed.updatedAt || new Date().toISOString(),
            storeName: parsed.storeName || '',
          });
          setFeedback({
            type: 'success',
            message: `Successfully imported ${Object.keys(parsed.items).length} category counts!`,
          });
          // Automatically clear success feedback after 4 seconds
          setTimeout(() => setFeedback(null), 4000);
        } else {
          setFeedback({ type: 'error', message: 'File is valid JSON but does not contain valid count tracking records.' });
        }
      } catch (err) {
        setFeedback({ type: 'error', message: 'Could not parse JSON. The file might be corrupted.' });
      }
    };
    reader.readAsText(file);
  };

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      processFile(e.dataTransfer.files[0]);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    e.preventDefault();
    if (e.target.files && e.target.files[0]) {
      processFile(e.target.files[0]);
    }
  };

  const onButtonClick = () => {
    fileInputRef.current?.click();
  };

  return (
    <div 
      className="bg-slate-50 border border-slate-200 rounded-xl p-5"
      id="json-dropzone-container"
    >
      <h3 className="text-sm font-semibold text-slate-700 font-display mb-1">
        Resume Previous Session
      </h3>
      <p className="text-xs text-slate-500 mb-4">
        Restore your item counts by dropping your previously exported JSON file below.
      </p>

      {/* Interactive area */}
      <div
        id="drag-drop-zone"
        onDragEnter={handleDrag}
        onDragOver={handleDrag}
        onDragLeave={handleDrag}
        onDrop={handleDrop}
        onClick={onButtonClick}
        className={`relative border-2 border-dashed rounded-xl p-6 text-center cursor-pointer transition-all ${
          dragActive 
            ? 'border-emerald-500 bg-emerald-50/40 scale-[0.99]' 
            : 'border-slate-200 hover:border-slate-300 hover:bg-slate-100/50'
        }`}
      >
        <input
          ref={fileInputRef}
          type="file"
          accept=".json"
          onChange={handleChange}
          className="hidden"
          id="json-file-picker"
        />

        <div className="flex flex-col items-center justify-center gap-1.5">
          <Upload className={`w-7 h-7 text-slate-400 group-hover:text-slate-500 transition-colors ${
            dragActive && 'text-emerald-500 scale-110'
          }`} />
          <p className="text-xs font-semibold text-slate-600">
            {dragActive ? 'Drop your session file here' : 'Drop your JSON file here, or click to browse'}
          </p>
          <p className="text-[10px] text-slate-400 font-mono">
            Support: *.json tracking exports
          </p>
        </div>
      </div>

      {/* Dynamic Feedback Display */}
      {feedback && (
        <div 
          id={`import-feedback-${feedback.type}`} 
          className={`mt-4 p-3 rounded-lg flex items-start gap-2.5 border text-xs leading-relaxed animate-fade-in ${
            feedback.type === 'success'
              ? 'bg-emerald-50 border-emerald-100 text-emerald-800'
              : 'bg-rose-50 border-rose-100 text-rose-800'
          }`}
        >
          {feedback.type === 'success' ? (
            <FileCheck className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
          ) : (
            <AlertCircle className="w-4 h-4 text-rose-600 shrink-0 mt-0.5" />
          )}
          <span className="font-medium">{feedback.message}</span>
        </div>
      )}
    </div>
  );
};
