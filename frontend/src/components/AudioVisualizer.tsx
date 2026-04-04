import React from 'react';

interface AudioVisualizerProps {
  volume: number;
}

export const AudioVisualizer: React.FC<AudioVisualizerProps> = ({ volume }) => {
  const bars = 15;
  const activeBars = Math.floor((volume / 100) * bars);

  return (
    <div className="flex items-center gap-1.5 h-8">
      {Array.from({ length: bars }).map((_, i) => (
        <div
          key={i}
          className={`w-1.5 rounded-full transition-all duration-150 ${
            i < activeBars 
              ? 'bg-primary shadow-[0_0_8px_rgba(var(--primary),0.4)]' 
              : 'bg-slate-200'
          }`}
          style={{
            height: i < activeBars ? '100%' : '30%'
          }}
        />
      ))}
    </div>
  );
};