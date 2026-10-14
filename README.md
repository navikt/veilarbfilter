[![CircleCI](https://circleci.com/gh/navikt/veilarbfilter.svg?style=svg)](https://circleci.com/gh/navikt/veilarbfilter)

Mikrotjeneste som lagrer filtervalg fra veilarbportefoljeflatefs

## For å bygge

`mvn clean install`

## Postgres

For lokal kjøring :
`docker-compose up`
`docker-compose down -V`

## Kontakt og spørsmål

Opprett en issue i GitHub for eventuelle spørsmål.s

Er du ansatt i Nav kan du stille spørsmål på Slack i kanalen #team-obo-poao.

## Kode generert av GitHub Copilot

Dette repoet bruker GitHub Copilot til å generere kode.


## Migrering av filter
For å migrere filter kan man kjøre SQL direkte mot databasen. Et eksempel på hvordan det kan gjøres vises under:

```
BEGIN;

//hent ut idene på filterne du ønsker å migrere
WITH selected_filters AS (
SELECT filter_id
FROM filter
WHERE (valgte_filter::jsonb ->> 'ytelse') = 'AAP_UNNTAK') // velg hvilket filter og evt verdi du ønsker her

// oppdater de selekterte filterne med ny verdi(er)
UPDATE filter
SET valgte_filter = jsonb_set(valgte_filter::jsonb,'{ytelseAapArena}','["HAR_AAP_UNNTAK"]'::jsonb,true) //set verdien(e) du ønsker her
WHERE filter_id IN (SELECT filter_id FROM selected_filters);
```
Du kan nå kjøre spørringer for å se hvordan db vil bli seende ut om sqlen kjøres. 
Eksempel testspørring:
```
select valgte_filter from filter WHERE (valgte_filter::jsonb ->> 'ytelse') = 'AAP_UNNTAK' limit 1;
```

Om alt ser riktig ut, avslutt transaksjonen med å kjøre `commit`. 

## Skrivesperre ved migrering av overblikkvisning

Aktiver Unleash-bryteren `veilarbfilter.overblikkvisning_skrivesperre` for alle brukere
før data migreres til veilarbveileder. Bryteren skal bruke en global strategi uten
brukeravgrensning. Når den er aktiv, returnerer `POST` (lagring og oppdatering) og
`DELETE` på `/api/overblikkvisning` HTTP 503 med en feilmelding. Dette gjelder også
kall som ellers ville blitt sendt videre til veilarbveileder. `GET` fungerer som før.

Vent til bryteren er oppdatert i alle podder og pågående skrivekall er ferdige før
migreringen starter. Sperren stopper nye kall, ikke kall som allerede har passert
sjekken. Deaktiver bryteren etter migreringen for å åpne for skriving igjen.
Når bryteren er av eller ikke finnes, er oppførselen uendret.
