Backend Docker image and deployment notes

This document explains how to build a Docker image for the PronounceIT backend that includes ffmpeg. Using a container is the recommended approach when deploying to AWS (ECS, EKS, or Fargate) because it ensures the runtime environment includes system-level dependencies like ffmpeg.

Build locally

1. Build the jar (from the backend folder):

```powershell
cd backend
.\mvnw -DskipTests package
```

2. Build the Docker image:

```powershell
docker build -t pronounceit-backend:latest -f backend/Dockerfile .
```

3. Run the image locally and test on http://localhost:8080:

```powershell
docker run --rm -p 8080:8080 pronounceit-backend:latest
```

Notes for AWS

- ECS / Fargate / ECR:
  - Tag the image and push to ECR, then reference the image in your ECS task definition.
  - The image already contains ffmpeg via apt so no extra provisioning is required.

- EKS:
  - Deploy as a normal Kubernetes Deployment. The container includes ffmpeg.

- EC2 / Elastic Beanstalk (without Docker):
  - Make sure the target host installs ffmpeg via package manager or add a provisioning step to install it.

Architecture and binary concerns

- If you prefer embedding a native Linux ffmpeg binary in the JAR resources, make sure you include the correct CPU architecture (x86_64 vs arm64) used by your AWS instances.
- Using a Docker image that installs ffmpeg is simpler and more portable.
