FROM openjdk:11

WORKDIR /app

COPY /build/libs/text4shell-poc.jar /app

EXPOSE 8080

CMD ["java", "-jar", "text4shell-scanner.jar", "/"]
