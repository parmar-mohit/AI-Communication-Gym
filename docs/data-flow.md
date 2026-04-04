# Data Flow Lifecycle

This document explains the step-by-step lifecycle of a single 5-minute session in the AI Communication Coach.

## 1. Initialization
- User grants browser permissions for the camera and microphone.
- The Next.js frontend establishes a WebSocket connection with the Spring Boot server (bound to `/coach-call`). 
- A unique `sessionId` is established for comprehensive tracking.

## 2. Live Conversation Phase
- **Continuous Capture**: The browser captures raw audio chunks (16kHz LPCM) and video segments. 
- **Upstream Transit**: These chunks are base64 encoded and pushed continuously through the open socket to the backend.
- **AI Processing**: The backend bridges the incoming voice data to **Amazon Bedrock**. The AI agent processes the intent and synthesizes prompt responses.
- **Downstream Transit**: The backend pushes the returned audio elements back to the client, along with text transcripts.
- **Browser Playback**: The Next.js frontend decodes the base64 audio and leverages the browser's Web Audio API (`AudioBufferSourceNode`) to queue and play the AI's voice sequentially.

## 3. Session Termination
- After the 5-minute conversational boundary is reached, the frontend stops capturing and transmits an `endSession` event.
- The backend closes the real-time processing threads and starts the intensive post-processing phase.

## 4. Final Processing & Archiving
- **Video Storage**: The compiled video stream is pushed strictly to the AWS S3 bucket specified by your environment context (`S3_SESSION_VIDEO_BUCKET_ID`).
- **Transcript Storage**: The total recorded transcript is uploaded to your `S3_SESSION_TRANSCRIPT_BUCKET_ID`.
- **Evaluation Generation**: A final query runs against the AI model parsing the full transcript to generate constructive feedback on strengths, weaknesses, and actionable insights.
- **Report Dispatch**: The JSON evaluation report is dispatched to the client, prompting the frontend to transition and render the final Report Dashboard UI.
