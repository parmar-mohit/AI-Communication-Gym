package com.thinkschool.coach.utility;

public class AudioModifier {

    public static byte[] downsample48kTo16k(byte[] input) {

        int inputSampleSize = 2; // PCM16 = 2 bytes
        int inputSamples = input.length / inputSampleSize;

        int outputSamples = inputSamples / 3;
        byte[] output = new byte[outputSamples * inputSampleSize];

        int outputIndex = 0;

        for (int i = 0; outputIndex < output.length; i += 3) {

            int byteIndex = i * 2;

            if (byteIndex + 1 >= input.length) {
                break;
            }

            output[outputIndex++] = input[byteIndex];
            output[outputIndex++] = input[byteIndex + 1];
        }

        return output;
    }
}