# SqlDelight 2.1.x Postgresql Lateral Joins 

https://github.com/cashapp/sqldelight

Snapshot version: 2.1.0-SNAPSHOT

Initial support for lateral joins https://github.com/cashapp/sqldelight/pull/5337

Not supported: Lateral joins on table expressions e.g json, arrays, sets, generate_series

```sql
SELECT
    pledged_usd,
    avg_pledge_usd,
    duration,
    (usd_from_goal / duration) AS usd_needed_daily
FROM Kickstarter_Data,
    LATERAL (SELECT pledged / NULLIF(fx_rate, 0) AS pledged_usd) pu,
    LATERAL (SELECT pledged_usd / NULLIF(backers_count, 0) AS avg_pledge_usd) apu,
    LATERAL (SELECT goal / NULLIF(fx_rate, 0) AS goal_usd) gu,
    LATERAL (SELECT goal_usd - pledged_usd AS usd_from_goal) ufg,
    LATERAL (SELECT (deadline - launched_at) / 86400.00 AS duration) dr;
```

```sql
SELECT
  sp.id salesperson_id,
  sp.full_name,
  sp.home_region_id,
  rg.name AS home_region_name,
  home_region_sales.total_sales
FROM SalesPeople sp
  JOIN Regions rg ON sp.home_region_id = rg.id
  JOIN LATERAL (
    SELECT SUM(amount) AS total_sales
    FROM Sales s
    WHERE s.salesperson_id = sp.id
      AND s.region_id = sp.home_region_id
  ) home_region_sales ON TRUE;
```
----

```shell
createdb laterals &&
./gradlew build &&
./gradlew flywayMigrate
```

Flyway db migrations
https://documentation.red-gate.com/fd/gradle-task-184127407.html
