# System Architecture

The AI Communication Coach is a two-tier application built for low-latency, real-time media streaming and generative AI evaluation. 

## High-Level Components

### 1. Client / Frontend (Next.js)
- **Role**: Handles user interactions, media capture (microphones, cameras), and rendering AI responses.
- **Key Tech**: Next.js 15, React 19, Tailwind CSS, Radix UI.
- **Responsibility**: Maintains a persistent WebSocket connection to the server, streams raw audio in LPCM chunks, captures video uniformly, and plays back received audio buffer streams using the Web Audio API.

### 2. Server / Backend (Spring Boot)
- **Role**: Orchestrates external service calls, processes incoming media, controls the timing of sessions, and generates final evaluation reports.
- **Key Tech**: Java Spring Boot, Spring WebSockets, Maven.
- **Responsibility**: Listens for WebSocket binary/text events from the frontend, coordinates event stream processing, and dispatches data seamlessly to AWS components.

### 3. AWS Integrations
- **Amazon Bedrock**: Serves as the brain of the communication coach. It handles large language model inference, evaluating the user's speech, and formulating a coherent conversational response in real-time. 
- **Amazon S3**: Used for robust blob storage. The application securely uploads session videos and transcripts to designated buckets for archival and subsequent analysis.