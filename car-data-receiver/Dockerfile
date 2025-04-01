# Use Amazon Corretto (Java 17) as base image
FROM amazoncorretto:17

# Set working directory
WORKDIR /app

# Copy source files
COPY ./src ./src

# Compile the Java file
RUN javac src/main/java/fina/receiver/CarDataReceiver.java

# Run the receiver with full class path
CMD ["java", "-cp", "src/main/java", "fina.receiver.CarDataReceiver"]