# Communication Protocol & WebSockets

The primary interface between the Next.js client UI and the Spring Boot backend is a persistent WebSocket connection located at `/coach-call`.

The protocol heavily relies on serialized JSON messages passing back and forth to keep latency at an absolute minimum given the real-time AI conversation constraints.

## Connection Initialization

When the user first establishes the WebSocket connection, the server immediately responds with a JSON message containing a unique `sessionId`. This ID is critical as it uniquely identifies the communication session. Once the session concludes, the client uses this `sessionId` as a query parameter when calling the `/session-report` HTTP API (e.g., `/session-report?sessionId=<unique-id>`) to retrieve the final AI performance report.

```json
{
  "status": s"connected",
  "sessionId": "a-unique-uuid-string"
}
```

## Client to Server (Upstream)

### Audio & Video Streaming
Media is streamed in continuous chunks. Audio is captured as raw LPCM at 16,000Hz, 16-bit, Mono, sent in base64 strings.
```json
{
  "contentType": "audio | video",
  "content": "BASE64_ENCODED_DATA"
}
```

### Events (e.g., Session End)
When the 5-minute timer expires, the frontend tells the server to trigger evaluation by sending an end session event.
```json
{
  "event": {
    "eventType": "endSession",
    "reason": "timer_expired_or_user_stopped"
  }
}
```

## Server to Client (Downstream)

### Transcript Updates
In order to provide accessible subtitles and logs, the server pushes active dialogue records.
```json
{
  "transcript": {
    "role": "USER | ASSISTANT",
    "text": "The decoded speech text."
  }
}
```

### Audio Push
When Amazon Bedrock formulates a response, the backend synthesizes it and streams the audio buffer back for immediate playback using Web Audio APIs.
```json
{
  "audioOutput": {
    "audio": "BASE64_ENCODED_LPCM_AUDIO"
  }
}
```

### Report Generation
After the completion of a session, a complete statistical performance matrix is generated.
```json
{
  "session": "<sessionId>",
  "strengths": ["Clear diction and pace"],
  "weakness": ["Frequent filler words"],
  "actionable-insights": ["Pause actively between thoughts"],
  "overall-performance": "Solid grounding with room for improved conciseness."
}
```
