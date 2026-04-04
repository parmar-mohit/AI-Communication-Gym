
"use client";

import React, { createContext, useContext, useState, useRef, useCallback, ReactNode, useEffect } from 'react';
import { APP_CONFIG } from '@/constants/config';
import { useMediaStream } from '@/hooks/useMediaStream';

interface Message {
  speaker: 'USER' | 'ASSISTANT';
  text: string;
}

interface AppContextType {
  stream: MediaStream | null;
  isGranted: boolean;
  volume: number;
  audioContext: AudioContext | null;
  error: string | null;
  requestPermissions: () => Promise<void>;
  stopStream: () => void;
  
  ws: WebSocket | null;
  sessionId: string | null;
  messages: Message[];
  isAiSpeaking: boolean;
  isConnecting: boolean;
  
  connectToCoach: () => Promise<boolean>;
  disconnectCoach: (reason?: string) => void;
  setSessionId: (id: string | null) => void;

  // Audio Mixing for Recording
  recordingMixer: GainNode | null;
  recordingStream: MediaStream | null;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export function AppProvider({ children }: { children: ReactNode }) {
  const media = useMediaStream();
  const [ws, setWs] = useState<WebSocket | null>(null);
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [isAiSpeaking, setIsAiSpeaking] = useState(false);
  const [isConnecting, setIsConnecting] = useState(false);
  
  const [recordingMixer, setRecordingMixer] = useState<GainNode | null>(null);
  const [recordingStream, setRecordingStream] = useState<MediaStream | null>(null);

  const wsRef = useRef<WebSocket | null>(null);
  const audioQueue = useRef<string[]>([]);
  const isAudioPlaying = useRef(false);

  // Sync recording mixer whenever audioContext changes to prevent context mismatch errors
  useEffect(() => {
    if (media.audioContext) {
      const mixer = media.audioContext.createGain();
      const dest = media.audioContext.createMediaStreamDestination();
      mixer.connect(dest);
      setRecordingMixer(mixer);
      setRecordingStream(dest.stream);
    } else {
      setRecordingMixer(null);
      setRecordingStream(null);
    }
  }, [media.audioContext]);

  const processAudioQueue = useCallback(async () => {
    if (isAudioPlaying.current || audioQueue.current.length === 0 || !media.audioContext) {
      if (!isAudioPlaying.current && audioQueue.current.length === 0) {
        setIsAiSpeaking(false);
      }
      return;
    }

    isAudioPlaying.current = true;
    setIsAiSpeaking(true);
    const base64Data = audioQueue.current.shift()!;

    try {
      if (media.audioContext.state === 'suspended') await media.audioContext.resume();
      const binaryString = window.atob(base64Data);
      const bytes = new Uint8Array(binaryString.length);
      for (let i = 0; i < binaryString.length; i++) bytes[i] = binaryString.charCodeAt(i);
      
      const int16 = new Int16Array(bytes.buffer);
      const float32 = new Float32Array(int16.length);
      for (let i = 0; i < int16.length; i++) float32[i] = int16[i] / 32768.0;
      
      const audioBuffer = media.audioContext.createBuffer(1, float32.length, 16000);
      audioBuffer.getChannelData(0).set(float32);
      
      const source = media.audioContext.createBufferSource();
      source.buffer = audioBuffer;
      
      // Output to speakers
      source.connect(media.audioContext.destination);
      
      // Output to recording mixer (ensure same context)
      if (recordingMixer && recordingMixer.context === media.audioContext) {
        source.connect(recordingMixer);
      }

      source.onended = () => {
        isAudioPlaying.current = false;
        processAudioQueue();
      };
      source.start();
    } catch (e) {
      isAudioPlaying.current = false;
      processAudioQueue();
    }
  }, [media.audioContext, recordingMixer]);

  const connectToCoach = async (): Promise<boolean> => {
    setIsConnecting(true);
    const socket = new WebSocket(APP_CONFIG.wsUrl);
    
    return new Promise((resolve) => {
      const timeout = setTimeout(() => {
        socket.close();
        setIsConnecting(false);
        resolve(false);
      }, 5000);

      socket.onopen = () => {
        clearTimeout(timeout);
        wsRef.current = socket;
        setWs(socket);
        setIsConnecting(false);
        setMessages([]);
        resolve(true);
      };

      socket.onerror = () => {
        clearTimeout(timeout);
        setIsConnecting(false);
        resolve(false);
      };

      socket.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          if (data.status === "connected") {
            setSessionId(data.sessionId);
          } else if (data.transcript) {
            const { role, text } = data.transcript;
            setMessages(prev => {
              const last = prev[prev.length - 1];
              if (last && last.speaker === role) {
                return [...prev.slice(0, -1), { ...last, text: `${last.text} ${text}`.trim() }];
              }
              return [...prev, { speaker: role, text }];
            });
          } else if (data.audioOutput) {
            audioQueue.current.push(data.audioOutput.audio);
            processAudioQueue();
          } else if (data.type === "ai_status") {
            setIsAiSpeaking(data.isSpeaking);
          }
        } catch (e) {}
      };
    });
  };

  const disconnectCoach = (reason: string = 'userAction') => {
    if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
      wsRef.current.send(JSON.stringify({ eventType: "endSession", reason }));
      wsRef.current.close();
    }
    setWs(null);
    wsRef.current = null;
    setMessages([]);
  };

  return (
    <AppContext.Provider value={{
      ...media,
      ws,
      sessionId,
      messages,
      isAiSpeaking,
      isConnecting,
      connectToCoach,
      disconnectCoach,
      setSessionId,
      recordingMixer,
      recordingStream
    }}>
      {children}
    </AppContext.Provider>
  );
}

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) throw new Error("useApp must be used within AppProvider");
  return context;
};
