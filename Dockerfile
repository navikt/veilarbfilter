FROM  docker.pkg.github.com/navikt/poao-baseimages/java:17

ENV APPD_ENABLED=true
ENV APP_NAME=veilarbfilter

ENV APPDYNAMICS_CONTROLLER_HOST_NAME=appdynamics.adeo.no
ENV APPDYNAMICS_CONTROLLER_PORT=443
ENV APPDYNAMICS_CONTROLLER_SSL_ENABLED=true
ENV TZ=Europe/Oslo

COPY /target/veilarbfilter.jar app.jar