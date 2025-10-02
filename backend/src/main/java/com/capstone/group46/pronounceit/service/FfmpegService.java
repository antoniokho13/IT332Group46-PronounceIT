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

        if (os.contains("win")) {
            // Windows: Check if local ffmpeg.exe exists (for development)
            File localFfmpeg = new File("src/main/resources/ffmpeg/win/ffmpeg.exe");
            if (localFfmpeg.exists()) {
                logger.info("Using local Windows ffmpeg: {}", localFfmpeg.getAbsolutePath());
                return localFfmpeg.getAbsolutePath();
            }

            // Fallback: try to load from classpath resources
            String resourcePath = "/ffmpeg/win/ffmpeg.exe";
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) {
                return extractFfmpegToTemp(is, "ffmpeg.exe");
            }

            // Last resort: assume ffmpeg is on PATH
            logger.warn("Local ffmpeg.exe not found, falling back to system ffmpeg");
            return "ffmpeg";

        } else if (os.contains("mac")) {
            // macOS: Try bundled first, then system
            String resourcePath = "/ffmpeg/mac/ffmpeg";
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) {
                return extractFfmpegToTemp(is, "ffmpeg");
            }
            return "ffmpeg";

        } else {
            // Linux (Railway/Docker): Use system ffmpeg installed via Dockerfile
            logger.info("Linux environment detected, using system ffmpeg");
            return "ffmpeg";
        }
    }

    private String extractFfmpegToTemp(InputStream is, String exeName) throws IOException {
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

        logger.info("Extracted ffmpeg to: {}", extracted.getAbsolutePath());
        return extracted.getAbsolutePath();
    }
}