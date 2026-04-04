"use client";

import React, { useState, useEffect, useRef } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Download, Trophy, Lightbulb, Activity, Target, RefreshCw, ChevronLeft, ShieldCheck } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { useApp } from '@/context/AppContext';
import { APP_CONFIG } from '@/constants/config';

interface ReportData {
  session: string;
  strengths: string[];
  weakness: string[];
  'actionable-insights': string[];
  'overall-performance': string;
}

export default function ReportPage() {
  const searchParams = useSearchParams();
  const sessionId = searchParams.get('sessionId');
  const [report, setReport] = useState<ReportData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isDownloading, setIsDownloading] = useState(false);
  const { toast } = useToast();
  const router = useRouter();
  const { setSessionId } = useApp();
  
  // Guard to prevent duplicate fetches
  const fetchedSessionIdRef = useRef<string | null>(null);

  useEffect(() => {
    const fetchReport = async () => {
      if (!sessionId || fetchedSessionIdRef.current === sessionId) {
        return;
      }

      setIsLoading(true);
      try {
        const response = await fetch(`${APP_CONFIG.apiBaseUrl}/session-report?sessionId=${sessionId}`, {
          headers: { 'Accept': 'application/json' }
        });
        
        if (!response.ok) throw new Error('Failed to fetch report');
        
        const data = await response.json();
        setReport(data);
        fetchedSessionIdRef.current = sessionId;
      } catch (error) {
        toast({ 
          variant: "destructive", 
          title: "Assessment Error", 
          description: "Could not retrieve your session report from the server." 
        });
      } finally {
        setIsLoading(false);
      }
    };

    if (sessionId) {
      fetchReport();
    } else {
      setIsLoading(false);
    }
  }, [sessionId, toast]);

  const handleDownload = async () => {
    if (!sessionId) return;
    setIsDownloading(true);
    try {
      const response = await fetch(`${APP_CONFIG.apiBaseUrl}/session-report?sessionId=${sessionId}`, {
        headers: { 'Accept': 'application/pdf' }
      });
      if (!response.ok) throw new Error('Download failed');
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `Communication_Report_${sessionId}.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      toast({ title: "Success", description: "Your PDF report has been downloaded." });
    } catch (error) {
      toast({ 
        variant: "destructive", 
        title: "Download Error", 
        description: "Could not generate PDF report." 
      });
    } finally {
      setIsDownloading(false);
    }
  };

  const onRestart = () => {
    setSessionId(null);
    router.push('/onboarding');
  };

  return (
    <div className="min-h-screen flex flex-col bg-slate-50/50">
      <header className="h-20 border-b bg-white flex items-center px-12 justify-between sticky top-0 z-20">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-primary rounded-lg flex items-center justify-center text-white shadow-lg shadow-primary/20">
            <ShieldCheck className="w-6 h-6" />
          </div>
          <h1 className="font-headline font-bold text-xl tracking-tight text-slate-900">
            Think School <span className="text-primary/70 font-medium">Gym</span>
          </h1>
        </div>
        <Button variant="ghost" onClick={onRestart} className="gap-2 text-slate-500 hover:text-slate-900">
          <ChevronLeft className="w-4 h-4" />
          Back to Home
        </Button>
      </header>

      <main className="flex-1 overflow-auto p-12">
        <div className="max-w-5xl mx-auto space-y-12 animate-in fade-in slide-in-from-bottom-4 duration-700">
          <div className="flex flex-col md:flex-row md:items-end justify-between gap-6">
            <div className="space-y-3">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-primary/5 text-primary text-[10px] font-bold uppercase tracking-widest border border-primary/10">
                Official Assessment Report
              </div>
              <h2 className="text-4xl font-headline font-extrabold text-slate-900 tracking-tight">Performance Summary</h2>
              <p className="text-slate-500 font-medium">Session ID: <span className="font-mono text-primary/80">{sessionId || 'N/A'}</span></p>
            </div>
            <Button 
              onClick={handleDownload}
              disabled={!sessionId || isDownloading}
              className="gap-3 bg-slate-900 hover:bg-slate-800 text-white shadow-xl shadow-slate-900/20 h-14 px-8 rounded-full font-bold"
            >
              {isDownloading ? <RefreshCw className="w-5 h-5 animate-spin" /> : <Download className="w-5 h-5" />}
              Download Detailed Analysis
            </Button>
          </div>

          {isLoading ? (
            <div className="flex flex-col items-center justify-center py-32 space-y-6">
              <div className="relative">
                <div className="w-16 h-16 rounded-full border-4 border-primary/10 border-t-primary animate-spin" />
              </div>
              <p className="text-slate-500 font-bold uppercase tracking-[0.2em] text-xs">Synthesizing Results...</p>
            </div>
          ) : report ? (
            <div className="grid gap-8">
              <Card className="border-0 shadow-xl shadow-slate-200/50 rounded-[2rem] overflow-hidden bg-white">
                <div className="h-2 bg-gradient-to-r from-primary to-primary/40" />
                <CardContent className="p-10 space-y-6">
                  <div className="flex items-center gap-5">
                    <div className="p-4 bg-primary/5 rounded-2xl text-primary border border-primary/10">
                      <Trophy className="w-8 h-8" />
                    </div>
                    <div>
                      <h3 className="text-2xl font-extrabold text-slate-900 tracking-tight">Executive Summary</h3>
                      <p className="text-slate-400 text-sm font-medium">Holistic performance review</p>
                    </div>
                  </div>
                  <p className="text-slate-600 leading-relaxed text-xl font-medium italic">
                    "{report['overall-performance']}"
                  </p>
                </CardContent>
              </Card>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                <Card className="border-0 shadow-lg shadow-slate-200/50 rounded-[2rem] bg-white group hover:shadow-2xl transition-all duration-500">
                  <CardContent className="p-10">
                    <div className="flex items-center gap-5 mb-8">
                      <div className="p-4 bg-emerald-50 rounded-2xl text-emerald-600 border border-emerald-100">
                        <Target className="w-6 h-6" />
                      </div>
                      <h3 className="text-xl font-bold text-slate-900">Key Strengths</h3>
                    </div>
                    <ul className="space-y-5">
                      {report.strengths.map((s, i) => (
                        <li key={i} className="flex gap-4 items-start text-slate-600">
                          <div className="w-2 h-2 rounded-full bg-emerald-400 mt-2.5 shrink-0" />
                          <span className="text-base font-medium leading-snug">{s}</span>
                        </li>
                      ))}
                    </ul>
                  </CardContent>
                </Card>

                <Card className="border-0 shadow-lg shadow-slate-200/50 rounded-[2rem] bg-white group hover:shadow-2xl transition-all duration-500">
                  <CardContent className="p-10">
                    <div className="flex items-center gap-5 mb-8">
                      <div className="p-4 bg-amber-50 rounded-2xl text-amber-600 border border-amber-100">
                        <Lightbulb className="w-6 h-6" />
                      </div>
                      <h3 className="text-xl font-bold text-slate-900">Refinement Areas</h3>
                    </div>
                    <ul className="space-y-5">
                      {report.weakness.map((w, i) => (
                        <li key={i} className="flex gap-4 items-start text-slate-600">
                          <div className="w-2 h-2 rounded-full bg-amber-400 mt-2.5 shrink-0" />
                          <span className="text-base font-medium leading-snug">{w}</span>
                        </li>
                      ))}
                    </ul>
                  </CardContent>
                </Card>
              </div>

              <Card className="border-0 shadow-2xl shadow-primary/10 rounded-[2rem] bg-slate-900 text-white">
                <CardContent className="p-12">
                  <div className="flex items-center gap-5 mb-10">
                    <div className="p-4 bg-white/5 rounded-2xl text-primary-foreground border border-white/10">
                      <Activity className="w-8 h-8" />
                    </div>
                    <div>
                      <h3 className="text-2xl font-bold tracking-tight">Personalized Action Plan</h3>
                      <p className="text-slate-400 text-sm font-medium">Strategic steps for rapid improvement</p>
                    </div>
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                    {report['actionable-insights'].map((insight, i) => (
                      <div key={i} className="bg-white/5 border border-white/10 p-8 rounded-[1.5rem] hover:bg-white/10 transition-colors duration-300">
                        <div className="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center text-primary-foreground font-bold text-xs mb-4">
                          {i + 1}
                        </div>
                        <p className="text-lg leading-relaxed text-slate-200 font-medium">
                          {insight}
                        </p>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>

              <div className="flex justify-center pt-12 pb-20">
                <Button 
                  variant="outline" 
                  onClick={onRestart}
                  className="h-16 px-12 border-2 border-slate-200 text-slate-900 font-bold hover:bg-slate-50 rounded-full text-lg shadow-sm active:scale-95 transition-all"
                >
                  Return to Dashboard
                </Button>
              </div>
            </div>
          ) : (
            <div className="text-center py-20 bg-white rounded-[2rem] shadow-xl border border-slate-100 max-w-lg mx-auto">
              <p className="text-slate-500 mb-8 text-lg font-medium">Session data unavailable.</p>
              <Button onClick={onRestart} className="rounded-full px-10">Restart Setup</Button>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
