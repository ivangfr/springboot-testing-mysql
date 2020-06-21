#!/usr/bin/env bash

docker exec -i mysql mysql -u root --password=secret userdb < mysql/init-db.sql
