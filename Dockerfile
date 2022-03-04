FROM  docker.pkg.github.com/navikt/pus-nais-java-app/pus-nais-java-app:java17

ENV APPD_ENABLED=true
ENV APP_NAME=veilarbfilter

ENV APPDYNAMICS_CONTROLLER_HOST_NAME=appdynamics.adeo.no
ENV APPDYNAMICS_CONTROLLER_PORT=443
ENV APPDYNAMICS_CONTROLLER_SSL_ENABLED=true
ENV TZ=Europe/Oslo

RUN mkdir /app/lib
RUN mkdir /app/conf

COPY /target/veilarbfilter.jar app.jar