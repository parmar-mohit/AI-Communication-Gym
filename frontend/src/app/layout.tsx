
import type { Metadata } from 'next';
import './globals.css';
import { APP_CONFIG } from '@/constants/config';
import { Toaster } from '@/components/ui/toaster';
import { AppProvider } from '@/context/AppContext';

export const metadata: Metadata = {
  title: APP_CONFIG.appName,
  description: 'AI-powered communication skills assessment.',
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet" />
      </head>
      <body className="font-body antialiased selection:bg-primary/10">
        <AppProvider>
          {children}
          <Toaster />
        </AppProvider>
      </body>
    </html>
  );
}
