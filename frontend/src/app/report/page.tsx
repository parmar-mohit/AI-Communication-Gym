"use client";

import React, { useState } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { ShieldCheck, Loader2, CheckCircle2, Mail, User, Send, Home } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { APP_CONFIG } from '@/constants/config';
import { useApp } from '@/context/AppContext';

const formSchema = z.object({
  name: z.string().min(2, { message: "Name must be at least 2 characters." }),
  email: z.string().email({ message: "Please enter a valid email address." }),
});

type FormValues = z.infer<typeof formSchema>;

export default function ReportPage() {
  const searchParams = useSearchParams();
  const sessionId = searchParams.get('sessionId');
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { toast } = useToast();
  const router = useRouter();
  const { setSessionId } = useApp();

  const form = useForm<FormValues>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      name: "",
      email: "",
    },
  });

  async function onSubmit(values: FormValues) {
    if (!sessionId) {
      toast({
        variant: "destructive",
        title: "Session Error",
        description: "No active session ID found. Please restart your assessment."
      });
      return;
    }

    setIsSubmitting(true);
    try {
      const response = await fetch(`${APP_CONFIG.apiBaseUrl}/session-report`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          sessionId: sessionId,
          userName: values.name,
          userEmail: values.email,
        }),
      });

      if (!response.ok) throw new Error("Server communication failed.");
      
      const data = await response.json();

      if (data.status === "Success") {
        setIsSubmitted(true);
        setSessionId(null);
        // Automatic redirection removed as per user request
      } else {
        throw new Error(data.message || "Failed to submit report request.");
      }
    } catch (error) {
      toast({
        variant: "destructive",
        title: "Submission Failed",
        description: "There was an error processing your request. Please try again."
      });
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="min-h-screen flex flex-col bg-slate-50/50">
      <header className="h-20 border-b bg-white flex items-center px-12 justify-between sticky top-0 z-20">
        <Link href="/onboarding" className="flex items-center gap-3 group transition-opacity hover:opacity-80">
          <div className="w-10 h-10 bg-primary rounded-lg flex items-center justify-center text-white shadow-lg shadow-primary/20">
            <ShieldCheck className="w-6 h-6" />
          </div>
          <h1 className="font-headline font-bold text-xl tracking-tight text-slate-900">
            AI Communication <span className="text-primary/70 font-medium">Gym</span>
          </h1>
        </Link>
        <Button variant="ghost" asChild className="gap-2 font-bold text-slate-600">
          <Link href="/onboarding">
            <Home className="w-4 h-4" />
            Home
          </Link>
        </Button>
      </header>

      <main className="flex-1 flex items-center justify-center p-6">
        <div className="max-w-xl w-full animate-in fade-in slide-in-from-bottom-4 duration-700">
          {!isSubmitted ? (
            <Card className="border-0 shadow-2xl shadow-slate-200/50 rounded-[2rem] overflow-hidden bg-white">
              <div className="h-2 bg-gradient-to-r from-primary to-primary/40" />
              <CardHeader className="p-10 pb-2 text-center">
                <div className="mx-auto w-16 h-16 bg-primary/5 rounded-2xl flex items-center justify-center text-primary mb-6">
                  <Mail className="w-8 h-8" />
                </div>
                <CardTitle className="text-3xl font-extrabold text-slate-900 tracking-tight">Receive Your Results</CardTitle>
                <CardDescription className="text-slate-500 text-lg font-medium mt-2">
                  Enter your details to receive your comprehensive AI-driven communication analysis via email.
                </CardDescription>
              </CardHeader>
              <CardContent className="p-10 pt-8">
                <Form {...form}>
                  <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                    <FormField
                      control={form.control}
                      name="name"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel className="text-slate-700 font-bold text-sm uppercase tracking-wider">Full Name</FormLabel>
                          <FormControl>
                            <div className="relative">
                              <User className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
                              <Input 
                                placeholder="John Doe" 
                                {...field} 
                                className="pl-12 h-14 rounded-xl border-slate-200 focus:ring-primary focus:border-primary text-lg"
                              />
                            </div>
                          </FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                    <FormField
                      control={form.control}
                      name="email"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel className="text-slate-700 font-bold text-sm uppercase tracking-wider">Email Address</FormLabel>
                          <FormControl>
                            <div className="relative">
                              <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
                              <Input 
                                placeholder="john@example.com" 
                                type="email"
                                {...field} 
                                className="pl-12 h-14 rounded-xl border-slate-200 focus:ring-primary focus:border-primary text-lg"
                              />
                            </div>
                          </FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                    <div className="space-y-4 pt-2">
                      <Button 
                        type="submit" 
                        disabled={isSubmitting}
                        className="w-full h-16 rounded-full text-lg font-bold bg-slate-900 hover:bg-slate-800 text-white shadow-xl shadow-slate-900/20 transition-all hover:scale-[1.01] active:scale-[0.98] gap-3"
                      >
                        {isSubmitting ? (
                          <>
                            <Loader2 className="w-6 h-6 animate-spin" />
                            Processing...
                          </>
                        ) : (
                          <>
                            Request Detailed Report
                            <Send className="w-5 h-5" />
                          </>
                        )}
                      </Button>
                      <Button variant="outline" asChild className="w-full h-14 rounded-full font-bold border-2 hover:bg-slate-50 transition-all">
                        <Link href="/onboarding">Cancel and Return Home</Link>
                      </Button>
                    </div>
                  </form>
                </Form>
              </CardContent>
            </Card>
          ) : (
            <div className="text-center space-y-8 py-12">
              <div className="mx-auto w-24 h-24 bg-emerald-50 rounded-full flex items-center justify-center text-emerald-500 shadow-inner border border-emerald-100">
                <CheckCircle2 className="w-12 h-12" />
              </div>
              <div className="space-y-4">
                <h2 className="text-4xl font-extrabold text-slate-900 tracking-tight">Report Requested!</h2>
                <p className="text-xl text-slate-600 font-medium leading-relaxed max-w-md mx-auto">
                  Your communication assessment is being generated and will be delivered to your inbox shortly.
                </p>
              </div>
              <div className="pt-8">
                <Button variant="default" asChild className="h-14 px-10 rounded-full font-bold shadow-lg shadow-primary/20">
                  <Link href="/onboarding">Return Home</Link>
                </Button>
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
