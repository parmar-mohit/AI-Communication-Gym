# Amazon Bedrock Integration (Nova Sonic)

The AI Communication Coach backend leverages Amazon Bedrock—specifically the **Amazon Nova Sonic** model—for low-latency, real-time speech-to-speech generative AI capabilities.

In order to maintain conversational pacing without noticeable delays, the Spring Boot environment establishes and persists an asynchronous streaming connection with Amazon Bedrock using the AWS SDK for Java.

## Connection Lifecycle

1. **Initialization**: When the Next.js frontend initiates a session through the primary WebSocket, the Spring Boot backend concurrently negotiates a streaming request with the Bedrock Runtime.
2. **Input Streaming (Upstream)**: As the frontend captures user audio, it sends LPCM chunks to the backend. The backend reconstructs these chunks and wraps them into AWS-specific input events to be streamed directly to the Nova Sonic model in near real-time.
3. **Output Streaming (Downstream)**: As Nova Sonic processes the user's intent and generates a verbal response, it streams output events (containing the synthesized audio bytes and transcripts) back to the Java layer. The backend immediately routes these discrete payloads back to the Next.js frontend.
4. **Termination**: When the `endSession` event is triggered at the 5-minute mark, the Spring Boot backend finalizes the stream, aggregates the transcript, and performs the post-processing necessary to generate the final coaching report.

## Important Reference Links

For detailed architectural understanding, payload construction, and implementation specifics regarding the Nova Sonic integration, refer to the following AWS documentation and resources:

- **Input Requests**  
  Details on constructing and formatting input events sent to Nova Sonic:  
  [Input Events in Nova Sonic](https://docs.aws.amazon.com/nova/latest/userguide/input-events.html)

- **Output Responses**  
  Insights on capturing, parsing, and streaming the output events built and dispatched by Nova Sonic:  
  [Output Events in Nova Sonic](https://docs.aws.amazon.com/nova/latest/userguide/output-events.html)

- **Event Lifecycle**  
  A broader understanding of the streaming lifecycle, constraints, and operational flow within Nova Sonic interactions:  
  [Sonic Event Lifecycle](https://docs.aws.amazon.com/nova/latest/nova2-userguide/sonic-event-lifecycle.html)

- **Java Implementation Reference**  
  An official AWS sample repository providing an end-to-end WebSocket code example demonstrating Amazon Nova 2 Sonic interactions in Java:  
  [amazon-nova-samples: websocket-java](https://github.com/aws-samples/amazon-nova-samples/tree/main/speech-to-speech/amazon-nova-2-sonic/sample-codes/websocket-java)
