"use client";

import React, { useEffect, useRef } from 'react';
import { APP_CONFIG } from '@/constants/config';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Camera, Mic, Play, CheckCircle2, Activity, RefreshCw } from 'lucide-react';
import { AudioVisualizer } from '@/components/AudioVisualizer';

interface OnboardingScreenProps {
  isGranted: boolean;
  stream: MediaStream | null;
  volume: number;
  error: string | null;
  isConnecting: boolean;
  onRequestPermissions: () => void;
  onStart: () => void;
}

export const OnboardingScreen: React.FC<OnboardingScreenProps> = ({
  isGranted,
  stream,
  volume,
  error,
  isConnecting,
  onRequestPermissions,
  onStart
}) => {
  const videoRef = useRef<HTMLVideoElement>(null);
  const sessionMinutes = Math.floor(APP_CONFIG.sessionDurationSeconds / 60);

  useEffect(() => {
    if (stream && videoRef.current) {
      videoRef.current.srcObject = stream;
    }
  }, [stream]);

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
      </header>
      
      <main className="flex-1 flex items-center justify-center p-8 bg-slate-50/50 overflow-auto">
        <div className="max-w-4xl w-full grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          <div className="space-y-8">
            <div>
              <h2 className="text-4xl font-headline font-extrabold text-slate-900 leading-tight">
                Refine Your Voice with <span className="text-primary">Real-time</span> Feedback.
              </h2>
              <p className="mt-4 text-slate-500 text-lg">
                Join a {sessionMinutes}-minute session to analyze your speaking patterns, confidence, and clarity with our advanced communication coach.
              </p>
            </div>

            <div className="space-y-4">
              <div className="flex items-start gap-4">
                <div className="w-8 h-8 rounded-full bg-accent/10 flex items-center justify-center text-accent shrink-0">
                  <CheckCircle2 className="w-5 h-5" />
                </div>
                <div>
                  <h4 className="font-semibold text-slate-900">Immediate Feedback</h4>
                  <p className="text-sm text-slate-500">Get real-time insights as you speak.</p>
                </div>
              </div>
              <div className="flex items-start gap-4">
                <div className="w-8 h-8 rounded-full bg-accent/10 flex items-center justify-center text-accent shrink-0">
                  <CheckCircle2 className="w-5 h-5" />
                </div>
                <div>
                  <h4 className="font-semibold text-slate-900">Comprehensive Reporting</h4>
                  <p className="text-sm text-slate-500">Detailed breakdown of strengths and weaknesses.</p>
                </div>
              </div>
            </div>

            {!isGranted ? (
              <Button 
                onClick={onRequestPermissions}
                size="lg"
                className="h-14 px-8 text-lg font-semibold gap-3 shadow-xl shadow-primary/20 hover:scale-105 transition-transform"
              >
                Grant Permissions
              </Button>
            ) : (
              <Button 
                onClick={onStart}
                disabled={isConnecting}
                size="lg"
                className="h-14 px-8 text-lg font-semibold gap-3 bg-accent hover:bg-accent/90 shadow-xl shadow-accent/20 hover:scale-105 transition-transform"
              >
                {isConnecting ? (
                  <>
                    <RefreshCw className="w-5 h-5 animate-spin" />
                    Connecting...
                  </>
                ) : (
                  <>
                    <Play className="fill-current" />
                    Start Session
                  </>
                )}
              </Button>
            )}
            
            {error && (
              <p className="text-destructive text-sm font-medium bg-destructive/5 p-3 rounded-lg border border-destructive/20">
                {error}
              </p>
            )}
          </div>

          <div className="relative group">
            <div className="absolute -inset-4 bg-gradient-to-tr from-primary/10 to-accent/10 rounded-3xl blur-2xl transition-opacity group-hover:opacity-100 opacity-50" />
            <Card className="relative overflow-hidden border-2 bg-slate-900 shadow-2xl rounded-2xl">
              <div className="aspect-video relative bg-slate-800">
                {isGranted ? (
                  <video 
                    ref={videoRef} 
                    autoPlay 
                    muted 
                    playsInline 
                    className="w-full h-full object-cover video-mirror"
                  />
                ) : (
                  <div className="w-full h-full flex flex-col items-center justify-center text-slate-400 gap-4">
                    <Camera className="w-16 h-16 opacity-20" />
                    <p className="text-sm font-medium uppercase tracking-widest">Waiting for camera access</p>
                  </div>
                )}
                
                <div className="absolute bottom-4 left-4 right-4 flex items-center justify-between px-4 py-3 bg-black/40 backdrop-blur-md rounded-xl border border-white/10">
                  <div className="flex items-center gap-4">
                    <div className={`p-2 rounded-full ${isGranted ? 'text-accent' : 'text-slate-500'}`}>
                      <Mic className="w-5 h-5" />
                    </div>
                    <AudioVisualizer volume={volume} />
                  </div>
                  <div className="flex items-center gap-2">
                     <div className={`w-2 h-2 rounded-full ${isGranted ? 'bg-accent animate-pulse' : 'bg-slate-500'}`} />
                     <span className="text-[10px] text-white/70 font-bold uppercase tracking-widest">Live Feed</span>
                  </div>
                </div>
              </div>
            </Card>
          </div>
        </div>
      </main>
    </div>
  );
};
