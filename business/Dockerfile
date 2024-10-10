FROM ringcentral/jdk:8u362

WORKDIR /app

COPY ./target/*.jar /app/app.jar

EXPOSE 9002

CMD ["java", "-jar", "app.jar"]