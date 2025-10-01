Place platform-specific ffmpeg binaries under this directory so the application can bundle and extract them at runtime.

Windows: put the standalone ffmpeg.exe (not an installer) at:
  ffmpeg/win/ffmpeg.exe

Linux: put the executable at:
  ffmpeg/linux/ffmpeg

macOS: put the executable at:
  ffmpeg/mac/ffmpeg

Notes:
- Use static builds (zip/tar) from trusted sources and copy the ffmpeg binary, do not commit installers.
- After adding binaries, run:
    cd backend
    .\mvnw package -DskipTests
  to rebuild the JAR with the ffmpeg binaries included.
- The app will try system ffmpeg first; the bundled binary is only used if the system one isn't available.
