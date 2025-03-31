# 📦 Dockerfile for CarDataImitator (Java)

# Use Amazon Corretto (Java 17) base image
FROM amazoncorretto:17

# Create app directory
WORKDIR /app

# Copy compiled class or JAR
# For single-class run:
COPY car-data-imitator/src/main/java/fina/imitator/CarDataImitator.java ./

# Compile the Java class
RUN javac CarDataImitator.java

# Create the package directory structure
RUN mkdir -p fina/imitator

# Move the compiled class to the correct package directory
RUN mv CarDataImitator.class fina/imitator/

# Run the application using the main class
CMD ["java", "-cp", ".", "fina.imitator.CarDataImitator"]
