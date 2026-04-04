# Server Handling Documentation

This document describes the expected data formats and handling logic for the "Think School Communication Gym" WebSocket backend at `/coach-call`.

## WebSocket Endpoint
The frontend expects a WebSocket connection at `ws://<your-backend>/coach-call`.

## Data Formats (Frontend -> Server)

### Audio Streaming
The frontend captures raw LPCM audio at 16,000Hz, 16-bit, Mono. It is sent in chunks of 4096 bytes as a JSON object:

```json
{
  "contentType": "audio",
  "content": "BASE64_ENCODED_LPCM_DATA"
}
```

### Video Streaming
The frontend captures the camera feed in `video/webm` format. It is sent as a JSON object:

```json
{
  "contentType": "video",
  "content": "BASE64_ENCODED_WEBM_DATA"
}
```

### Events
When the timer expires, the client sends:
```json
{
  "type": "session_complete"
}
```

## Data Formats (Server -> Frontend)

### AI Dialogue (Transcript)
To update the transcript window, the server should send:
```json
{
  "type": "transcript",
  "speaker": "ai" | "user",
  "text": "The spoken content"
}
```

### AI Audio Response
To play verbal feedback from the AI coach, the server should send LPCM audio data:
```json
{
  "type": "audio_response",
  "content": "BASE64_ENCODED_LPCM_AUDIO"
}
```

### AI Status
To trigger the visual "Speaking" indicator on the AI avatar:
```json
{
  "type": "ai_status",
  "isSpeaking": true | false
}
```

## Audio Playback Logic (Client Side)
The client uses the Web Audio API to decode and play the received base64 audio:
1.  **Decode Base64**: Convert the `content` string to a `Uint8Array`.
2.  **LPCM to Float32**: The raw 16-bit PCM must be normalized to Float32 values (-1.0 to 1.0).
3.  **AudioBuffer**: Create an `AudioBuffer` (1 channel, 16000 sample rate).
4.  **BufferSource**: Create an `AudioBufferSourceNode`, assign the buffer, and connect to `audioContext.destination`.
