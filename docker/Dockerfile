FROM eclipse-temurin:17-jdk

# Install dependencies
RUN apt-get update && apt-get install -y \
    git \
    curl \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Set Gradle version
ENV GRADLE_VERSION=8.5
ENV GRADLE_HOME=/opt/gradle
ENV PATH="${GRADLE_HOME}/bin:${PATH}"

# Install Gradle
RUN curl -fsSL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o gradle.zip \
    && unzip gradle.zip -d /opt \
    && mv "/opt/gradle-${GRADLE_VERSION}" "${GRADLE_HOME}" \
    && rm gradle.zip

# Create non-root user for builds (use higher IDs to avoid conflicts)
RUN groupadd -g 1001 gradle && useradd -u 1001 -g gradle -m gradle

# Set working directory
WORKDIR /project

# Create Gradle cache directory
RUN mkdir -p /home/gradle/.gradle && chown -R gradle:gradle /home/gradle

# Switch to non-root user
USER gradle

# Set Gradle user home
ENV GRADLE_USER_HOME=/home/gradle/.gradle

# Default command
CMD ["gradle", "--version"]
