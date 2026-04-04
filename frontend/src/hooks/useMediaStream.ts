import { useState, useCallback, useRef } from 'react';

export function useMediaStream() {
  const [stream, setStream] = useState<MediaStream | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isGranted, setIsGranted] = useState(false);
  const [volume, setVolume] = useState(0);
  const [audioContext, setAudioContext] = useState<AudioContext | null>(null);
  const animationFrameRef = useRef<number | null>(null);

  const requestPermissions = useCallback(async () => {
    try {
      const mediaStream = await navigator.mediaDevices.getUserMedia({
        video: { width: 1280, height: 720 },
        audio: {
          echoCancellation: true,
          noiseSuppression: true,
          autoGainControl: true,
        },
      });
      setStream(mediaStream);
      setIsGranted(true);
      setError(null);

      // Audio analysis for volume indicator
      const ctx = new AudioContext({ sampleRate: 16000 });
      setAudioContext(ctx);
      const source = ctx.createMediaStreamSource(mediaStream);
      const analyser = ctx.createAnalyser();
      analyser.fftSize = 256;
      source.connect(analyser);

      const dataArray = new Uint8Array(analyser.frequencyBinCount);
      const updateVolume = () => {
        analyser.getByteFrequencyData(dataArray);
        const sum = dataArray.reduce((a, b) => a + b, 0);
        const average = sum / dataArray.length;
        setVolume(average);
        animationFrameRef.current = requestAnimationFrame(updateVolume);
      };
      updateVolume();
    } catch (err) {
      setError('Camera or Microphone access denied. Please enable them to proceed.');
      setIsGranted(false);
    }
  }, []);

  const stopStream = useCallback(() => {
    if (stream) {
      stream.getTracks().forEach((track) => track.stop());
      setStream(null);
      setIsGranted(false);
    }
    if (animationFrameRef.current) {
      cancelAnimationFrame(animationFrameRef.current);
    }
    if (audioContext) {
      audioContext.close();
      setAudioContext(null);
    }
  }, [stream, audioContext]);

  return { stream, error, isGranted, volume, audioContext, requestPermissions, stopStream };
}
