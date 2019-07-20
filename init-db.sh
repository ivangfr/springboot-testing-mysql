#!/usr/bin/env bash
docker exec -i mysql mysql -u root --password=secret userdb < user-service/src/main/resources/init-db.sql