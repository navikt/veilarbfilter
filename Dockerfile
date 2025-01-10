FROM ghcr.io/navikt/baseimages/temurin:21
COPY /target/veilarbfilter.jar app.jar
