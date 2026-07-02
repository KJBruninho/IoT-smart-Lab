FROM gradle:8.10.2-jdk17

WORKDIR /workspace

# Build-only image for Android/Gradle projects.
# The project is mounted at runtime by docker-compose.
# If Android Gradle Plugin needs a full Android SDK image, use this as a base template.

CMD ["./gradlew", "assembleDebug"]
