"use client";

import React, { useEffect, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { APP_CONFIG } from '@/constants/config';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Camera, Mic, Play, CheckCircle2, ShieldCheck, ArrowRight, Loader2 } from 'lucide-react';
import { AudioVisualizer } from '@/components/AudioVisualizer';
import { useApp } from '@/context/AppContext';
import { useToast } from '@/hooks/use-toast';

export default function OnboardingPage() {
  const { isGranted, stream, volume, error, isConnecting, requestPermissions, connectToCoach } = useApp();
  const videoRef = useRef<HTMLVideoElement>(null);
  const router = useRouter();
  const { toast } = useToast();
  const [sessionMinutes, setSessionMinutes] = useState<number | null>(null);

  useEffect(() => {
    if (stream && videoRef.current) {
      videoRef.current.srcObject = stream;
    }
  }, [stream]);

  useEffect(() => {
    setSessionMinutes(Math.floor(APP_CONFIG.sessionDurationSeconds / 60));
  }, []);

  const handleStart = async () => {
    const success = await connectToCoach();
    if (success) {
      router.push('/session');
    } else {
      toast({
        variant: "destructive",
        title: "Connection Failed",
        description: "Could not connect to the coach server. Please try again."
      });
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-slate-50/30">
      <header className="h-20 border-b bg-white/80 backdrop-blur-md flex items-center px-12 justify-between sticky top-0 z-20">
        <Link href="/onboarding" className="flex items-center gap-3 group transition-opacity hover:opacity-80">
          <div className="w-10 h-10 bg-primary rounded-lg flex items-center justify-center text-white shadow-lg shadow-primary/20">
            <ShieldCheck className="w-6 h-6" />
          </div>
          <h1 className="font-headline font-bold text-xl tracking-tight text-slate-900">
            AI Communication <span className="text-primary/70 font-medium">Gym</span>
          </h1>
        </Link>
      </header>
      
      <main className="flex-1 max-w-7xl mx-auto w-full px-12 py-16 grid grid-cols-1 lg:grid-cols-2 gap-20 items-center">
        <div className="space-y-10 animate-in fade-in slide-in-from-left-4 duration-700">
          <div className="space-y-6">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-primary/5 border border-primary/10 text-primary text-xs font-bold uppercase tracking-wider">
              <span className="relative flex h-2 w-2">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-primary opacity-75"></span>
                <span className="relative inline-flex rounded-full h-2 w-2 bg-primary"></span>
              </span>
              AI-Powered Assessment
            </div>
            <h2 className="text-5xl font-headline font-extrabold text-slate-900 leading-[1.1] tracking-tight">
              Master the Art of <span className="text-primary">Communication</span>.
            </h2>
            <p className="text-slate-500 text-xl leading-relaxed max-w-lg">
              {sessionMinutes !== null ? (
                `Join a ${sessionMinutes}-minute session to analyze your speaking patterns, confidence, and clarity with our advanced communication coach.`
              ) : (
                'Loading session details...'
              )}
            </p>
          </div>

          <div className="space-y-5">
            {[
              { title: "Real-time Feedback", desc: "Instant insights into your speaking style." },
              { title: "Confidence Scoring", desc: "Measure your presence and delivery impact." },
              { title: "Personalized Roadmap", desc: "Get actionable steps to improve your skills." }
            ].map((item, i) => (
              <div key={i} className="flex items-start gap-4">
                <div className="w-6 h-6 rounded-full bg-primary/10 flex items-center justify-center text-primary shrink-0 mt-1">
                  <CheckCircle2 className="w-4 h-4" />
                </div>
                <div>
                  <h4 className="font-bold text-slate-900">{item.title}</h4>
                  <p className="text-sm text-slate-500">{item.desc}</p>
                </div>
              </div>
            ))}
          </div>

          <div className="pt-4 flex flex-col sm:flex-row gap-4">
            {!isGranted ? (
              <Button 
                onClick={requestPermissions}
                size="lg"
                className="h-14 px-10 text-lg font-bold rounded-full shadow-xl shadow-primary/20 hover:scale-[1.02] transition-all active:scale-95"
              >
                Setup My Studio
                <ArrowRight className="ml-2 w-5 h-5" />
              </Button>
            ) : (
              <Button 
                onClick={handleStart}
                disabled={isConnecting}
                size="lg"
                className="h-14 px-10 text-lg font-bold rounded-full bg-slate-900 hover:bg-slate-800 text-white shadow-xl shadow-slate-900/20 hover:scale-[1.02] transition-all active:scale-95"
              >
                {isConnecting ? (
                  <>
                    <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                    Connecting...
                  </>
                ) : (
                  <>
                    Start Session
                    <Play className="ml-2 w-4 h-4 fill-current" />
                  </>
                )}
              </Button>
            )}
          </div>
          
          {error && (
            <div className="p-4 rounded-xl bg-destructive/5 border border-destructive/20 text-destructive text-sm font-medium animate-in fade-in slide-in-from-top-2">
              {error}
            </div>
          )}
        </div>

        <div className="relative animate-in fade-in zoom-in-95 duration-1000 delay-200">
          <div className="absolute -inset-10 bg-gradient-to-tr from-primary/10 to-transparent rounded-full blur-3xl opacity-50" />
          <Card className="relative overflow-hidden border-0 bg-slate-900 shadow-[0_32px_64px_-16px_rgba(0,0,0,0.3)] rounded-[2rem]">
            <div className="aspect-[4/3] relative bg-slate-800">
              {isGranted ? (
                <video 
                  ref={videoRef} 
                  autoPlay 
                  muted 
                  playsInline 
                  className="w-full h-full object-cover video-mirror"
                />
              ) : (
                <div className="w-full h-full flex flex-col items-center justify-center text-slate-500 gap-6">
                  <div className="w-20 h-20 rounded-full bg-slate-800 flex items-center justify-center border border-white/5">
                    <Camera className="w-8 h-8 opacity-40" />
                  </div>
                  <p className="text-xs font-bold uppercase tracking-[0.2em] opacity-40">Camera Inactive</p>
                </div>
              )}
              
              <div className="absolute bottom-6 left-6 right-6 p-5 glass-morphism rounded-2xl flex items-center justify-between">
                <div className="flex items-center gap-5">
                  <div className={`p-2.5 rounded-full ${isGranted ? 'bg-primary/10 text-primary' : 'bg-slate-200 text-slate-400'}`}>
                    <Mic className="w-5 h-5" />
                  </div>
                  <AudioVisualizer volume={volume} />
                </div>
                <div className="flex items-center gap-2">
                   <div className={`w-2.5 h-2.5 rounded-full ${isGranted ? 'bg-emerald-500 shadow-[0_0_12px_rgba(16,185,129,0.5)]' : 'bg-slate-300'}`} />
                   <span className="text-[10px] text-slate-600 font-bold uppercase tracking-widest">Live Setup</span>
                </div>
              </div>
            </div>
          </Card>
        </div>
      </main>
    </div>
  );
}