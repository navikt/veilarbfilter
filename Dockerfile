FROM navikt/java:8-appdynamics

ADD /target/*.jar /app

ENV APPD_ENABLED=true