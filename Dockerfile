FROM  docker.pkg.github.com/navikt/pus-nais-java-app/pus-nais-java-app:java17
COPY /target/veilarbfilter.jar app.jar