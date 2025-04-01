# 📦 Dockerfile for CarDataImitator (Java)

# Use Amazon Corretto (Java 17) base image
FROM amazoncorretto:17

# Create app directory
WORKDIR /app

# Copy source files
COPY ./src ./src

# Compile the Java file
RUN javac src/main/java/fina/imitator/CarDataImitator.java

# Run the application using the main class
CMD ["java", "-cp", "src/main/java", "fina.imitator.CarDataImitator"]
