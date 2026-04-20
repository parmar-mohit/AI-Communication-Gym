import React, { useEffect, useRef } from 'react';
import { ScrollArea } from '@/components/ui/scroll-area';
import { cn } from '@/lib/utils';
import { User, Bot, Activity } from 'lucide-react';

interface Message {
  speaker: 'USER' | 'ASSISTANT';
  text: string;
}

interface SessionTranscriptProps {
  messages: Message[];
}

export const SessionTranscript: React.FC<SessionTranscriptProps> = ({ messages }) => {
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (scrollRef.current) {
      const viewport = scrollRef.current.querySelector('[data-radix-scroll-area-viewport]');
      if (viewport) {
        viewport.scrollTo({
          top: viewport.scrollHeight,
          behavior: 'smooth'
        });
      }
    }
  }, [messages]);

  return (
    <div className="flex flex-col h-full bg-white overflow-hidden">
      <div className="p-6 border-b flex items-center justify-between shrink-0 bg-slate-50/50">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center text-primary">
            <Bot className="w-4 h-4" />
          </div>
          <h3 className="font-bold text-sm tracking-tight text-slate-900">Live Feedback Stream</h3>
        </div>
        <div className="px-2.5 py-1 rounded-full bg-emerald-50 text-emerald-600 text-[10px] font-bold uppercase tracking-wider border border-emerald-100">
          Real-time
        </div>
      </div>
      
      <ScrollArea className="flex-1" ref={scrollRef}>
        <div className="p-8 flex flex-col gap-8 min-h-full">
          {messages.map((msg, idx) => {
            const isUser = msg.speaker === 'USER';
            return (
              <div
                key={idx}
                className={cn(
                  "flex gap-4 max-w-[90%] animate-in fade-in slide-in-from-bottom-4 duration-500",
                  isUser ? "ml-auto flex-row-reverse" : "mr-auto flex-row"
                )}
              >
                <div className={cn(
                  "w-10 h-10 rounded-2xl flex items-center justify-center shrink-0 shadow-sm border",
                  isUser ? "bg-slate-900 text-white border-slate-800" : "bg-white text-slate-400"
                )}>
                  {isUser ? <User className="w-5 h-5" /> : <Bot className="w-5 h-5" />}
                </div>
                
                <div className={cn(
                  "flex flex-col gap-2",
                  isUser ? "items-end text-right" : "items-start text-left"
                )}>
                  <div
                    className={cn(
                      "px-5 py-4 rounded-[1.5rem] text-sm leading-relaxed font-medium shadow-sm border",
                      isUser 
                        ? "bg-slate-900 text-white border-slate-800 rounded-tr-none" 
                        : "bg-white text-slate-600 border-slate-100 rounded-tl-none"
                    )}
                  >
                    {msg.text}
                  </div>
                  <span className="text-[10px] text-slate-300 font-bold uppercase tracking-widest px-1">
                    {isUser ? 'You' : 'Coach Assistant'}
                  </span>
                </div>
              </div>
            );
          })}
          
          {messages.length === 0 && (
            <div className="flex-1 flex flex-col items-center justify-center py-20 px-10 text-center opacity-30">
              <div className="w-16 h-16 rounded-[1.5rem] border-2 border-dashed border-slate-300 mb-6 animate-pulse flex items-center justify-center">
                <Activity className="w-6 h-6 text-slate-400" />
              </div>
              <p className="text-slate-500 text-xs font-bold uppercase tracking-[0.2em] leading-relaxed">
                Awaiting Dialogue...
              </p>
            </div>
          )}
          
          <div className="h-10 shrink-0" />
        </div>
      </ScrollArea>
    </div>
  );
};
