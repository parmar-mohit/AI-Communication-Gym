
"use client";

import React, { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { APP_CONFIG } from '@/constants/config';
import { Button } from '@/components/ui/button';
import { StopCircle, Timer, ShieldCheck } from 'lucide-react';
import { AudioVisualizer } from '@/components/AudioVisualizer';
import { SessionTranscript } from '@/components/SessionTranscript';
import { AIAvatar } from '@/components/AIAvatar';
import { useApp } from '@/context/AppContext';

export default function SessionPage() {
  const { 
    stream, 
    volume, 
    messages, 
    isAiSpeaking, 
    ws, 
    sessionId, 
    disconnectCoach, 
    stopStream,
    audioContext,
    recordingMixer,
    recordingStream
  } = useApp();
  
  const [timeLeft, setTimeLeft] = useState(APP_CONFIG.sessionDurationSeconds);
  const videoRef = useRef<HTMLVideoElement>(null);
  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const router = useRouter();
  const isEndingSession = useRef(false);

  useEffect(() => {
    if (!isEndingSession.current && (!ws || ws.readyState !== WebSocket.OPEN)) {
      router.replace('/onboarding');
    }
  }, [ws, router]);

  useEffect(() => {
    const persistedEndTime = localStorage.getItem('session_end_time');
    let endTime: number;
    
    if (persistedEndTime) {
      endTime = parseInt(persistedEndTime);
      const remaining = Math.max(0, Math.floor((endTime - Date.now()) / 1000));
      if (remaining <= 0) {
        handleEndSession('timerExpired');
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
        handleEndSession('timerExpired');
      }
    }, 1000);

    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    // Ensure all prerequisites are met and nodes belong to the same context
    if (!stream || !ws || !audioContext || !recordingMixer || !recordingStream) return;
    if (recordingMixer.context !== audioContext) return;

    if (videoRef.current) videoRef.current.srcObject = stream;

    // Connect user mic to the recording mixer
    const userMicSource = audioContext.createMediaStreamSource(stream);
    userMicSource.connect(recordingMixer);

    // Combine camera video with mixed audio for recording
    const videoTrack = stream.getVideoTracks()[0];
    const mixedAudioTrack = recordingStream.getAudioTracks()[0];
    
    if (!videoTrack || !mixedAudioTrack) return;

    const combinedRecordingStream = new MediaStream([videoTrack, mixedAudioTrack]);

    const mediaRecorder = new MediaRecorder(combinedRecordingStream, { 
      mimeType: 'video/webm;codecs=vp8,opus' 
    });
    
    mediaRecorderRef.current = mediaRecorder;
    mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0 && ws.readyState === WebSocket.OPEN) {
        const reader = new FileReader();
        reader.onload = () => {
          const base64 = (reader.result as string).split(',').pop();
          if (base64) ws.send(JSON.stringify({ contentType: 'video', content: base64 }));
        };
        reader.readAsDataURL(event.data);
      }
    };
    mediaRecorder.start(1000);

    // Independent raw PCM audio stream for transcript/analysis
    const processor = audioContext.createScriptProcessor(4096, 1, 1);
    processor.onaudioprocess = (e) => {
      if (ws.readyState !== WebSocket.OPEN) return;
      const inputData = e.inputBuffer.getChannelData(0);
      const int16Data = new Int16Array(inputData.length);
      for (let i = 0; i < inputData.length; i++) {
        const s = Math.max(-1, Math.min(1, inputData[i]));
        int16Data[i] = s < 0 ? s * 0x8000 : s * 0x7FFF;
      }
      const base64 = btoa(String.fromCharCode(...new Uint8Array(int16Data.buffer)));
      ws.send(JSON.stringify({ contentType: 'audio', content: base64 }));
    };
    userMicSource.connect(processor);
    processor.connect(audioContext.destination);

    return () => {
      if (mediaRecorder.state !== 'inactive') mediaRecorder.stop();
      userMicSource.disconnect();
      processor.disconnect();
    };
  }, [stream, ws, audioContext, recordingMixer, recordingStream]);

  const handleEndSession = (reason: 'userAction' | 'timerExpired' = 'userAction') => {
    isEndingSession.current = true;
    localStorage.removeItem('session_end_time');
    const currentSessionId = sessionId;
    disconnectCoach(reason);
    stopStream();
    router.push(`/report?sessionId=${currentSessionId || ''}`);
  };

  const formatTime = (seconds: number) => {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  return (
    <div className="h-screen flex flex-col bg-slate-50 overflow-hidden">
      <header className="h-20 border-b bg-white/80 backdrop-blur-md flex items-center px-8 justify-between z-20 shrink-0">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-primary rounded-lg flex items-center justify-center text-white shadow-lg shadow-primary/20">
            <ShieldCheck className="w-6 h-6" />
          </div>
          <h1 className="font-headline font-bold text-xl tracking-tight text-slate-900">
            Communication <span className="text-primary/70 font-medium">Session</span>
          </h1>
        </div>
        
        <div className="flex items-center gap-4">
          <div className={`flex items-center gap-3 px-5 py-2.5 rounded-full font-mono font-bold text-lg shadow-sm border ${timeLeft < 60 ? 'bg-destructive/10 text-destructive border-destructive/20 animate-pulse' : 'bg-white text-slate-900 border-slate-200'}`}>
            <Timer className="w-5 h-5" />
            {formatTime(timeLeft)}
          </div>
        </div>
      </header>

      <div className="flex-1 flex overflow-hidden">
        <div className="flex-1 flex flex-col bg-slate-50/50 p-6 gap-6 overflow-hidden">
          <div className="flex-1 grid grid-cols-1 lg:grid-cols-4 gap-6 min-h-0">
            <div className="lg:col-span-3 relative rounded-[2rem] overflow-hidden bg-slate-900 shadow-2xl border-4 border-white">
              <video 
                ref={videoRef}
                autoPlay 
                muted 
                playsInline 
                className="w-full h-full object-cover video-mirror"
              />
              <div className="absolute top-6 left-6 flex items-center gap-3 glass-morphism px-4 py-2 rounded-full border-white/20">
                <div className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
                <span className="text-[10px] text-slate-800 font-bold uppercase tracking-widest">Self View</span>
              </div>
              <div className="absolute bottom-6 left-6">
                <AudioVisualizer volume={volume} />
              </div>
            </div>

            <div className="lg:col-span-1 min-h-0">
              <AIAvatar isSpeaking={isAiSpeaking} />
            </div>
          </div>

          <div className="shrink-0 flex items-center justify-center">
            <Button 
              variant="destructive" 
              size="lg"
              className="rounded-full px-12 h-14 font-bold text-lg gap-3 shadow-2xl shadow-destructive/20 hover:scale-105 transition-all"
              onClick={() => handleEndSession('userAction')}
            >
              <StopCircle className="w-6 h-6" />
              Finalize Assessment
            </Button>
          </div>
        </div>

        <div className="w-[450px] h-full border-l bg-white shadow-2xl z-10">
          <SessionTranscript messages={messages} />
        </div>
      </div>
    </div>
  );
}
