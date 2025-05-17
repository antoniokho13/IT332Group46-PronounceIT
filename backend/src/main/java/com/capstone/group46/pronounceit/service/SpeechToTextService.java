package com.capstone.group46.pronounceit.service; // Use your actual package

import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service // Marks this class as a Spring Service component
public class SpeechToTextService {

    /**
     * Transcribes audio data into text using Google Cloud Speech-to-Text API.
     * Assumes audio is short (< ~1 minute). For longer audio, use asynchronous recognition.
     * @param audioData The audio data as a byte array.
     * @param languageCode The language code (e.g., "en-US", "fil-PH").
     * @param encoding The audio encoding (e.g., RecognitionConfig.AudioEncoding.LINEAR16, RecognitionConfig.AudioEncoding.MP3).
     * @param sampleRateHertz The sample rate of the audio in Hertz (e.g., 16000).
     * @return The transcribed text, or null if no transcription is found.
     * @throws IOException If an error occurs during the API call.
     */
    public String transcribeAudio(byte[] audioData, String languageCode, RecognitionConfig.AudioEncoding encoding, int sampleRateHertz) throws IOException {

        // Use try-with-resources to ensure the client is closed properly
        try (SpeechClient speechClient = SpeechClient.create()) {

            // Configure the recognition request
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setLanguageCode(languageCode) // e.g., "en-US", "fil-PH"
                    .setEncoding(encoding) // e.g., RecognitionConfig.AudioEncoding.LINEAR16
                    .setSampleRateHertz(sampleRateHertz) // e.g., 16000
                    .build();

            // Set the audio data
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(ByteString.copyFrom(audioData))
                    .build();

            // Perform the synchronous speech recognition
            RecognizeResponse response = speechClient.recognize(config, audio);

            // Process the results
            StringBuilder transcription = new StringBuilder();
            List<SpeechRecognitionResult> results = response.getResultsList();
            if (results != null && !results.isEmpty()) {
                // Just take the first result for simplicity in this use case
                SpeechRecognitionResult result = results.get(0);
                List<SpeechRecognitionAlternative> alternatives = result.getAlternativesList();
                if (alternatives != null && !alternatives.isEmpty()) {
                    // Get the most likely alternative
                    SpeechRecognitionAlternative alternative = alternatives.get(0);
                    transcription.append(alternative.getTranscript());
                }
            }


            // Return the complete transcription or null if empty
            return transcription.length() > 0 ? transcription.toString() : null;

        } // The client is automatically closed here
    }

    // You might add helper methods for common audio formats or configurations
    // Example: Transcribe assuming LINEAR16, 16000Hz, en-US
    public String transcribeEnglishLinear16(byte[] audioData) throws IOException {
        // You will need to know the sample rate used by your frontend recording
        // For LINEAR16, 16000 Hz is common for speech.
        int sampleRateHertz = 16000; // Confirm this with your frontend
        return transcribeAudio(audioData, "en-US", RecognitionConfig.AudioEncoding.LINEAR16, sampleRateHertz);
    }

    // Example: Transcribe assuming MP3, en-US
    public String transcribeEnglishMp3(byte[] audioData, int sampleRateHertz) throws IOException {
        // You MUST know the sample rate of the MP3 audio recorded by your frontend.
        // MP3 can have various sample rates.
        return transcribeAudio(audioData, "en-US", RecognitionConfig.AudioEncoding.MP3, sampleRateHertz);
    }
}