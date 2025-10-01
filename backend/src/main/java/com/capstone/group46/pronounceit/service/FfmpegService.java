package com.capstone.group46.pronounceit.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FfmpegService {
    private static final Logger logger = LoggerFactory.getLogger(FfmpegService.class);

    public File convertToPcmWav(File inputFile) throws Exception {
        File outputWavFile = File.createTempFile("converted", ".wav");

        // Try bundled ffmpeg in resources first; fall back to system ffmpeg
        String ffmpegPath = getFfmpegExecutablePath();

        ArrayList<String> command = new ArrayList<>(Arrays.asList(
                ffmpegPath,
                "-y",
                "-i",
                inputFile.getAbsolutePath(),
                "-ac", "1",
                "-ar", "16000",
                "-vn",
                "-acodec", "pcm_s16le",
                outputWavFile.getAbsolutePath()
        ));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process p = pb.start();

        // Capture output for debugging
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String logs = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            logger.debug("ffmpeg output: {}", logs);
        }

        int exit = p.waitFor();
        if (exit != 0) {
            throw new RuntimeException("ffmpeg conversion failed with exit code " + exit);
        }

        return outputWavFile;
    }

    private String getFfmpegExecutablePath() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        String resourcePath;
        String exeName;

        if (os.contains("win")) {
            resourcePath = "/ffmpeg/win/ffmpeg.exe";
            exeName = "ffmpeg.exe";
        } else if (os.contains("mac")) {
            resourcePath = "/ffmpeg/mac/ffmpeg";
            exeName = "ffmpeg";
        } else {
            resourcePath = "/ffmpeg/linux/ffmpeg";
            exeName = "ffmpeg";
        }

        // Try to load bundled ffmpeg from resources
        InputStream is = getClass().getResourceAsStream(resourcePath);
        if (is != null) {
            // Extract to temp file and make executable
            Path tempDir = Files.createTempDirectory("pronounceit-ffmpeg");
            File extracted = new File(tempDir.toFile(), exeName);
            try (FileOutputStream out = new FileOutputStream(extracted)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            } finally {
                is.close();
            }
            extracted.setExecutable(true);
            extracted.deleteOnExit();
            return extracted.getAbsolutePath();
        }

        // Fallback to system ffmpeg (must be on PATH)
        return "ffmpeg";
    }
}
