package com.capstone.group46.pronounceit.service; // Use your actual package

import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service // Marks this class as a Spring Service component
public class TextToSpeechService {

    /**
     * Synthesizes text into audio using Google Cloud Text-to-Speech API.
     * @param text The text to synthesize.
     * @return A byte array containing the audio data.
     * @throws IOException If an error occurs during the API call.
     */
    public byte[] synthesizeText(String text) throws IOException {
        // TextToSpeechClient implements AutoCloseable, so we can use try-with-resources
        // This automatically handles closing the client when done.
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {

            // Set the text input to be synthesized
            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(text)
                    .build();

            // Select the voice parameters.
            // Choose a languageCode (e.g., "en-US", "en-GB", "fil-PH").
            // Choose an SsmlVoiceGender (NEUTRAL, MALE, FEMALE).
            // You can also specify a specific voice name if desired.
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode("en-US")
                    .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                    // .setName("en-US-Wavenet-D") // Example: Specify a specific voice name
                    .build();

            // Set the audio configuration.
            // Choose an AudioEncoding (MP3, LINEAR16, OGG_OPUS).
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)
                    // .setSpeakingRate(1.0) // Example: Adjust speaking rate
                    // .setPitch(0.0)       // Example: Adjust pitch
                    .build();

            // Perform the text-to-speech request
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // Get the audio contents from the response
            ByteString audioContents = response.getAudioContent();

            // Convert ByteString to byte array
            return audioContents.toByteArray();

        } // The client is automatically closed here
    }
}