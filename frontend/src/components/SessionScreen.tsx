
"use client";

import React, { useState, useEffect, useRef } from 'react';
import { APP_CONFIG } from '@/constants/config';
import { Button } from '@/components/ui/button';
import { StopCircle, Activity } from 'lucide-react';
import { AudioVisualizer } from '@/components/AudioVisualizer';
import { SessionTranscript } from '@/components/SessionTranscript';
import { AIAvatar } from '@/components/AIAvatar';

interface SessionScreenProps {
  stream: MediaStream | null;
  volume: number;
  messages: { speaker: 'USER' | 'ASSISTANT'; text: string }[];
  isAiSpeaking: boolean;
  onEnd: () => void;
  onTimerExpired: () => void;
}

export const SessionScreen: React.FC<SessionScreenProps> = ({
  stream,
  volume,
  messages,
  isAiSpeaking,
  onEnd,
  onTimerExpired
}) => {
  const [timeLeft, setTimeLeft] = useState(APP_CONFIG.sessionDurationSeconds);
  const videoRef = useRef<HTMLVideoElement>(null);

  useEffect(() => {
    const persistedEndTime = localStorage.getItem('session_end_time');
    let endTime: number;
    
    if (persistedEndTime) {
      endTime = parseInt(persistedEndTime);
      const remaining = Math.max(0, Math.floor((endTime - Date.now()) / 1000));
      if (remaining <= 0) {
        onTimerExpired();
        return;
      }
      setTimeLeft(remaining);
    } else {
      endTime = Date.now() + (APP_CONFIG.sessionDurationSeconds * 1000);
      localStorage.setItem('session_end_time', endTime.toString());
      setTimeLeft(APP_CONFIG.sessionDurationSeconds);
    }

    const timer = setInterval(() => {
      const remaining = Math.max(0, Math.floor((endTime - Date.now()) / 1000));
      setTimeLeft(remaining);
      if (remaining <= 0) {
        clearInterval(timer);
        localStorage.removeItem('session_end_time');
        onTimerExpired();
      }
    }, 1000);

    return () => clearInterval(timer);
  }, [onTimerExpired]);

  useEffect(() => {
    if (stream && videoRef.current) {
      videoRef.current.srcObject = stream;
    }
  }, [stream]);

  const formatTime = (seconds: number) => {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  return (
    <div className="flex-1 flex flex-col overflow-hidden">
      <header className="h-16 border-b bg-white flex items-center px-8 justify-between z-10 shrink-0">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-primary rounded-xl flex items-center justify-center text-white shadow-lg shadow-primary/20">
            <Activity className="w-6 h-6" />
          </div>
          <h1 className="font-headline font-bold text-xl tracking-tight text-primary">
            {APP_CONFIG.appName}
          </h1>
        </div>
        
        <div className="flex items-center gap-6">
          <div className={`flex items-center gap-3 px-4 py-2 rounded-full font-mono font-bold text-lg shadow-inner ${timeLeft < 30 ? 'bg-destructive/10 text-destructive animate-pulse' : 'bg-muted text-primary'}`}>
            <div className="w-2 h-2 rounded-full bg-current" />
            {formatTime(timeLeft)}
          </div>
        </div>
      </header>

      <div className="flex-1 flex overflow-hidden">
        <div className="flex-1 flex flex-col min-w-0 bg-slate-50">
          <div className="flex-1 p-6 flex flex-col gap-6 overflow-hidden">
            <div className="flex-1 grid grid-cols-1 lg:grid-cols-4 gap-6 min-h-0">
              <div className="lg:col-span-3 relative rounded-2xl overflow-hidden border-2 border-slate-200 bg-slate-900 shadow-xl">
                 <video 
                    ref={videoRef}
                    autoPlay 
                    muted 
                    playsInline 
                    className="w-full h-full object-cover video-mirror"
                  />
                  <div className="absolute top-4 left-4 px-3 py-1 bg-black/50 backdrop-blur-md rounded-full border border-white/20">
                    <p className="text-white text-[10px] font-bold uppercase tracking-widest flex items-center gap-2">
                      <span className="w-1.5 h-1.5 rounded-full bg-accent animate-pulse" />
                      You (Live)
                    </p>
                  </div>
                  <div className="absolute bottom-4 left-4">
                    <AudioVisualizer volume={volume} />
                  </div>
              </div>

              <div className="lg:col-span-1 shadow-lg h-full min-h-0">
                <AIAvatar isSpeaking={isAiSpeaking} />
              </div>
            </div>

            <div className="h-16 shrink-0 flex items-center justify-center">
              <Button 
                variant="destructive" 
                size="lg"
                className="rounded-full px-12 h-12 font-bold gap-3 shadow-lg shadow-destructive/20 hover:scale-105 transition-transform"
                onClick={() => {
                  localStorage.removeItem('session_end_time');
                  onEnd();
                }}
              >
                <StopCircle className="w-5 h-5" />
                End Session
              </Button>
            </div>
          </div>
        </div>

        <div className="w-[400px] h-full border-l shrink-0 flex flex-col overflow-hidden bg-white">
          <SessionTranscript messages={messages} />
        </div>
      </div>
    </div>
  );
};
