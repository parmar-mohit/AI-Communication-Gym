# AI Communication Coach - Backend

This is the backend service for the AI Communication Coach project. It orchestrates real-time audio and video processing, utilizes Generative AI to formulate conversational responses, and generates a structured communication report at the end of every session.

## Tech Stack
- **Framework**: Spring Boot (Java)
- **AI Integration**: AWS Bedrock (Generative AI & LLM)
- **Cloud Storage**: AWS S3
- **Real-time Communication**: Spring Boot WebSockets
- **Build Tool**: Maven

## System Architecture & Message Protocol
The application runs on an event-driven architecture using WebSockets for real-time interaction.

- **Client to Server**: Streams `audio` or `video` content and sends an `endSession` event for termination.
- **Server to Client**: Streams base64 encoded `audio` response, real-time `transcript` updates, and a final structured `report`.

## Prerequisites
- Java Development Kit (JDK)
- Apache Maven
- AWS Account configured with credentials permitting access to Amazon Bedrock and AWS S3.

## Getting Started

### 1. Environment Variables & AWS Setup
Ensure the application can run successfully by creating an AWS user and setting the required system environment variables:

1. Open your AWS account and create a new IAM user granting the following permissions:
   - `Bedrock access`
   - `S3 access`

2. Create two S3 buckets in your AWS account to store session videos and transcripts.

3. Add the following environment variables to your system:
   - `AWS_ACCESS_KEY_ID`: the access key of the user
   - `AWS_SECRET_ACCESS_KEY`: the secret key of the user
   - `LOG_DIR`: log directory path where generated logs will be stored
   - `SESSION_TRANSCRIPT_PREFIX`: directory path where session transcripts will be stored
   - `SESSION_VIDEO_PREFIX`: directory where user session videos will be stored
   - `S3_SESSION_VIDEO_BUCKET_ID`: id of the bucket where user videos will be stored
   - `S3_SESSION_TRANSCRIPT_BUCKET_ID`: id of bucket where session transcript will be stored

### 2. Installation
Navigate to the `backend` directory and build the project using Maven:
```bash
cd backend
mvn clean install
```

### 3. Running the Server
You can run the application directly with the Spring Boot Maven plugin:
```bash
mvn spring-boot:run
```

The server will initialize and begin listening for incoming WebSocket connections.
