[![CircleCI](https://circleci.com/gh/navikt/veilarbfilter.svg?style=svg)](https://circleci.com/gh/navikt/veilarbfilter)

Mikrotjeneste som lagrer filtervalg fra veilarbportefoljeflatefs

## For å bygge

`mvn clean install`

## Postgres

For lokal kjøring :
`docker-compose up`
`docker-compose down -V`

## Tilgang

Last ned VAULT CLI og kjør føljande kommandosar

`export VAULT_ADDR=https://vault.adeo.no USER=NAV_IDENT vault login -method=oidc`

Preprod credentials:

`vault read postgresql/preprod-fss/creds/veilarbfilter-fss15-admin`

Prod credentials:

`vault read postgresql/prod-fss/creds/veilarbfilter-fss13-readonly`

## Kontakt og spørsmål

Opprett en issue i GitHub for eventuelle spørsmål.s

Er du ansatt i NAV kan du stille spørsmål på Slack i kanalen #team-obo-poao.

## Kode generert av GitHub Copilot

Dette repoet bruker GitHub Copilot til å generere kode.
