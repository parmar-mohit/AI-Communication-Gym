
"use client";

import React, { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Download, Trophy, Lightbulb, Activity, Target, RefreshCw } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';

interface ReportData {
  session: string;
  strengths: string[];
  weakness: string[];
  'actionable-insights': string[];
  'overall-performance': string;
}

interface ReportScreenProps {
  sessionId: string | null;
  onRestart: () => void;
}

export const ReportScreen: React.FC<ReportScreenProps> = ({ sessionId, onRestart }) => {
  const [report, setReport] = useState<ReportData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isDownloading, setIsDownloading] = useState(false);
  const { toast } = useToast();

  useEffect(() => {
    const fetchReport = async () => {
      try {
        const response = await fetch(`/report?sessionId=${sessionId}`, {
          headers: { 'Accept': 'application/json' }
        });
        if (!response.ok) throw new Error('Failed to fetch report');
        const data = await response.json();
        setReport(data);
      } catch (error) {
        toast({ variant: "destructive", title: "Error", description: "Could not load report data." });
      } finally {
        setIsLoading(false);
      }
    };

    if (sessionId) fetchReport();
    else setIsLoading(false);
  }, [sessionId, toast]);

  const handleDownload = async () => {
    setIsDownloading(true);
    try {
      const response = await fetch(`/report?sessionId=${sessionId}`, {
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
      toast({ title: "Success", description: "Report downloaded successfully." });
    } catch (error) {
      toast({ variant: "destructive", title: "Download Error", description: "Could not download PDF report." });
    } finally {
      setIsDownloading(false);
    }
  };

  return (
    <div className="flex-1 overflow-auto p-8 bg-slate-50/50">
      <div className="max-w-4xl mx-auto space-y-8">
        <div className="flex items-end justify-between">
          <div className="space-y-1">
            <p className="text-accent font-bold text-sm uppercase tracking-widest">Assessment Complete</p>
            <h2 className="text-3xl font-headline font-extrabold text-slate-900">Your Communication Report</h2>
          </div>
          <Button 
            onClick={handleDownload}
            disabled={!sessionId || isDownloading}
            className="gap-2 bg-primary hover:bg-primary/90 shadow-lg shadow-primary/20 h-12 px-6"
          >
            {isDownloading ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Download className="w-4 h-4" />}
            Download Detailed PDF
          </Button>
        </div>

        {isLoading ? (
          <Card className="border-none shadow-none bg-transparent">
            <CardContent className="flex flex-col items-center justify-center py-24 space-y-4">
              <RefreshCw className="w-12 h-12 text-primary animate-spin" />
              <p className="text-slate-500 font-medium">Generating your personalized assessment...</p>
            </CardContent>
          </Card>
        ) : report ? (
          <div className="grid gap-6">
            <Card className="border-none shadow-md overflow-hidden bg-white">
              <div className="h-2 bg-primary" />
              <CardContent className="p-8">
                <div className="flex items-center gap-4 mb-4">
                  <div className="p-3 bg-primary/10 rounded-xl text-primary">
                    <Trophy className="w-6 h-6" />
                  </div>
                  <h3 className="text-xl font-bold text-slate-900">Overall Performance</h3>
                </div>
                <p className="text-slate-600 leading-relaxed text-lg">
                  {report['overall-performance']}
                </p>
              </CardContent>
            </Card>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <Card className="border-none shadow-md bg-white">
                <CardContent className="p-8">
                  <div className="flex items-center gap-4 mb-6">
                    <div className="p-3 bg-accent/10 rounded-xl text-accent">
                      <Target className="w-6 h-6" />
                    </div>
                    <h3 className="text-lg font-bold text-slate-900">Key Strengths</h3>
                  </div>
                  <ul className="space-y-4">
                    {report.strengths.map((s, i) => (
                      <li key={i} className="flex gap-3 text-slate-600">
                        <div className="w-1.5 h-1.5 rounded-full bg-accent mt-2 shrink-0" />
                        <span className="text-sm font-medium">{s}</span>
                      </li>
                    ))}
                  </ul>
                </CardContent>
              </Card>

              <Card className="border-none shadow-md bg-white">
                <CardContent className="p-8">
                  <div className="flex items-center gap-4 mb-6">
                    <div className="p-3 bg-destructive/10 rounded-xl text-destructive">
                      <Lightbulb className="w-6 h-6" />
                    </div>
                    <h3 className="text-lg font-bold text-slate-900">Areas for Growth</h3>
                  </div>
                  <ul className="space-y-4">
                    {report.weakness.map((w, i) => (
                      <li key={i} className="flex gap-3 text-slate-600">
                        <div className="w-1.5 h-1.5 rounded-full bg-destructive mt-2 shrink-0" />
                        <span className="text-sm font-medium">{w}</span>
                      </li>
                    ))}
                  </ul>
                </CardContent>
              </Card>
            </div>

            <Card className="border-none shadow-md bg-slate-900 text-white">
              <CardContent className="p-8">
                <div className="flex items-center gap-4 mb-6">
                  <div className="p-3 bg-white/10 rounded-xl text-accent">
                    <Activity className="w-6 h-6" />
                  </div>
                  <h3 className="text-xl font-bold">Actionable Coaching</h3>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {report['actionable-insights'].map((insight, i) => (
                    <div key={i} className="bg-white/5 border border-white/10 p-4 rounded-xl">
                      <p className="text-sm leading-relaxed text-slate-300">
                        {insight}
                      </p>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            <div className="flex justify-center pt-8">
              <Button 
                variant="outline" 
                onClick={onRestart}
                className="h-12 px-8 border-2 border-primary text-primary font-bold hover:bg-primary/5"
              >
                Start New Assessment
              </Button>
            </div>
          </div>
        ) : (
          <div className="text-center py-12">
            <p className="text-slate-500 mb-4">No report found for this session.</p>
            <Button onClick={onRestart}>Back to Home</Button>
          </div>
        )}
      </div>
    </div>
  );
};
