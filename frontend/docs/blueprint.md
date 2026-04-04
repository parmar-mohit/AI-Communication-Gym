# **App Name**: Think School Communication Gym

## Core Features:

- Camera & Mic Permission Manager: Manages camera and microphone access, displays a live video feed, and visually indicates audio input levels. The 'Start Session' button remains disabled until all necessary permissions are granted and streams are active.
- WebSocket Audio Streamer: Captures raw audio from the user's microphone, encodes it to Base64 (LPCM, 16000Hz, 16-bit, 1 channel), and streams it in 4096-byte chunks to the backend server via a WebSocket connection.
- WebSocket Video Streamer: Captures the user's camera feed in `video/webm` format, encodes it to Base64, and continuously streams this data to the backend server via the WebSocket connection.
- Dynamic AI Session Interface: Presents the interactive UI for the active assessment session, featuring a Google Meet-style split layout for user camera and AI avatar (with a speaking indicator), a real-time countdown timer, and an 'End Session' button.
- Real-time AI Dialogue & Transcript Tool: Displays a WhatsApp-style chat bubble transcript, dynamically updating with both user input and AI-generated dialogue. Plays back AI-generated verbal responses received as audio from the server through the Web Audio API.
- Session Timer & Reconnection Logic: Manages the 5-minute session countdown, including pausing/resuming during WebSocket connection drops and reconnect attempts. Sends a 'session_complete' event and triggers transition upon timer expiry.
- Comprehensive Assessment Report Viewer: Presents the final communication skills assessment report in a clean, readable format. Includes a functional 'Download Report' button to save the assessment data.

## Style Guidelines:

- Primary color: A deep, professional blue (#305389), reflecting clarity and trust. Chosen for actions, headers, and key interactive elements.
- Background color: A subtly cool, nearly off-white shade (#F4F7F9), providing a clean and open canvas that complements the primary blue without visual distraction.
- Accent color: A fresh, energetic green (#2EB38E), symbolizing growth and progress, used sparingly for indicators, success states, and subtle highlights to provide visual contrast.
- Body and headline font: 'Inter' (sans-serif) for its modern, highly legible, and versatile qualities across various content types, suitable for both concise headings and extended reports.
- Utilize a consistent set of modern, minimalist line icons, such as those from Heroicons or similar libraries, to convey clear actions and statuses (e.g., camera, microphone, download, session controls).
- Employ a responsive, component-driven design with clear visual hierarchy, utilizing a full-screen layout on desktop with dedicated areas for video, chat transcript, and controls, adapting elegantly to different viewport sizes.
- Incorporate subtle, functional animations for UI feedback, such as transitions between screens, activation states for the AI avatar and volume indicator, and visual cues for WebSocket connection status and timer updates.