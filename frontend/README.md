# AI Communication Coach - Frontend

This is the frontend component of the AI Communication Coach project. It provides the user interface for interacting with the AI agent, displaying real-time transcripts, and showing the detailed final performance report.

## Tech Stack
- **Framework**: Next.js 15 (React 19)
- **Styling**: Tailwind CSS
- **UI Components**: Radix UI
- **Language**: TypeScript

## Prerequisites
- Node.js (v20+ recommended)
- npm or yarn package manager

## Getting Started

### 1. Installation
Navigate to the `frontend` directory and install the required dependencies:
```bash
cd frontend
npm install
```

### 2. Running the Development Server
Start the Next.js development server:
```bash
npm run dev
```
The application will be available at [http://localhost:9002](http://localhost:9002).

## Features
- **Live Conversation UI**: Interfaces with the system camera and microphone to capture and stream media.
- **Real-Time Transcript View**: Renders the ongoing transcription of the conversation as it flows from the server.
- **Report Dashboard**: Displays the final feedback report detailing strengths, weaknesses, actionable insights, and overall performance when the session completes.
