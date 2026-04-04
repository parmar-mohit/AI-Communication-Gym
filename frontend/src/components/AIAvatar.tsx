import React, { useEffect, useState } from 'react';
import { Bot } from 'lucide-react';

interface AIAvatarProps {
  isSpeaking: boolean;
}

export const AIAvatar: React.FC<AIAvatarProps> = ({ isSpeaking }) => {
  const [randomHeights, setRandomHeights] = useState<number[]>([]);

  useEffect(() => {
    // Generate stable random heights on client mount to avoid hydration mismatch
    setRandomHeights([...Array(8)].map(() => 20 + Math.random() * 80));
  }, []);

  return (
    <div className="relative w-full h-full bg-white flex flex-col items-center justify-center rounded-[2rem] overflow-hidden border border-slate-100 shadow-xl group">
      <div className={`absolute inset-0 bg-primary/[0.03] transition-opacity duration-1000 ${isSpeaking ? 'opacity-100' : 'opacity-0'}`} />
      
      <div className="relative z-10 flex flex-col items-center space-y-8 p-10">
        <div className={`relative p-10 rounded-[2.5rem] bg-slate-50 border-2 transition-all duration-700 ${isSpeaking ? 'border-primary/20 scale-110 shadow-[0_20px_40px_rgba(0,0,0,0.05)]' : 'border-slate-100'}`}>
          <Bot className={`w-16 h-16 transition-colors duration-700 ${isSpeaking ? 'text-primary' : 'text-slate-300'}`} />
          {isSpeaking && (
            <div className="absolute -top-1 -right-1 w-5 h-5 bg-emerald-500 border-4 border-white rounded-full animate-pulse" />
          )}
        </div>
        
        <div className="text-center space-y-2">
          <p className={`font-bold text-xs tracking-[0.2em] uppercase transition-colors duration-700 ${isSpeaking ? 'text-primary' : 'text-slate-400'}`}>
            {isSpeaking ? 'Coach Speaking' : 'Coach Listening'}
          </p>
          <p className="text-slate-900 font-headline font-bold text-xl">Dr. Communication AI</p>
        </div>
      </div>

      {isSpeaking && randomHeights.length > 0 && (
        <div className="absolute bottom-12 left-1/2 -translate-x-1/2 flex gap-1.5 items-end h-12">
          {randomHeights.map((height, i) => (
            <div
              key={i}
              className="w-1.5 bg-primary/20 rounded-full animate-bounce"
              style={{ 
                height: `${height}%`,
                animationDelay: `${i * 0.1}s`,
                animationDuration: '0.8s'
              }}
            />
          ))}
        </div>
      )}
    </div>
  );
};
