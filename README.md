# Ordering System Web Service

<!-- TOC -->
* [Ordering System Web Service](#ordering-system-web-service)
    * [Name](#name)
    * [Description](#description)
    * [Dependencies](#dependencies)
    * [Installation](#installation)
        * [Must do:](#must-do-)
    * [Development](#development)
    * [Database access](#database-access)
<!-- TOC -->

## Name

Ordering System Web Service

## Description

Backend service for ordering system.

## Dependencies

If running with docker (**recommended**):

* docker
* docker-compose

Otherwise:

* maven
* java 17

## Installation

```bash
docker-compose up --build -d
```

<br/>

To test if the backend is up:

```bash
curl http://localhost:9200/
```

If status code is 200 and response message is positive it works!

Or go to http://localhost:9200/

<br/>

To remove containers:

```bash
docker-compose down
```

<br/>

#### Must do:

* Copy contents from application.properties.template to application.properties

<br/>

```bash
# Add roles to the database
docker exec -it ordering-system-db psql -U lorem -W -f /scripts/orderingsystem.sql ordering-system
```

## Development

For testing new features:

```bash
docker-compose down
docker-compose up --build -d
```

## Database access

```bash
# enter database password written in docker-compose.yml and enter postgresql database
docker exec -it ordering-system-db psql -U lorem -W ordering-system  
```