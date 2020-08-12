FROM docker.pkg.github.com/navikt/pus-nais-java-app/pus-nais-java-app:java11

ADD /target/veilarbfilter-1-jar-with-dependencies.jar app.jar
