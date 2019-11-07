FROM navikt/java:8-appdynamics
ADD /target/veilarbfilter /app
ENV APPD_ENABLED=true