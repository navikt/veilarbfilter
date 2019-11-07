FROM navikt/java:8-appdynamics

ADD /target/*.jar ./

ENV APPD_ENABLED=true