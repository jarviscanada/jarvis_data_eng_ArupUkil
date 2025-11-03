# Introduction
This project was mainly a learning activity to understand SQL. This was done by writing queries to questions asked on https://pgexercises.com/. The queries covered a wide variety of topics on SQL such as basic CRUD requests, joins, string manipulation, and aggregate and window functions. This README contains possible table setup DDL requests and then solution queries for 28 questions. The final solutions were given in the website which I used to verify my solutions were correct by running my queries on a psql docker container that used the clubdata.sql file to create the initial database. Specifically, I ran `psql -h localhost -U postgres -d exercises -f ./queries.sql` command after getting the psql container running.
# SQL Queries

###### Table Setup (DDL)
```sql
CREATE TABLE IF NOT EXISTS cd.members (
    memid INTEGER PRIMARY KEY,
    surname VARCHAR(200) NOT NULL,
    firstname VARCHAR(200) NOT NULL,
    address VARCHAR(300) NOT NULL,
    zipcode INTEGER NOT NULL,
    telephone VARCHAR(20) NOT NULL,
    recommendedby INTEGER,
    joindate TIMESTAMP NOT NULL,
    FOREIGN KEY (recommendedby) REFERENCES cd.members(memid) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS cd.facilities (
    facid INTEGER PRIMARY KEY,
    "name" VARCHAR(100) NOT NULL,
    membercost NUMERIC NOT NULL,
    guestcost NUMERIC NOT NULL,
    initialoutlay NUMERIC NOT NULL,
    monthlymaintenance NUMERIC NOT NULL
);

CREATE TABLE IF NOT EXISTS cd.bookings (
    bookid INTEGER PRIMARY KEY,
    facid INTEGER NOT NULL,
    memid INTEGER NOT NULL,
    starttime TIMESTAMP NOT NULL,
    slots INTEGER NOT NULL,
    FOREIGN KEY (facid) REFERENCES cd.facilities(facid),
    FOREIGN KEY (memid) REFERENCES cd.members(memid)
);
```

###### Question 1: Insert

```sql
INSERT INTO cd.facilities
    (facid, name, membercost, guestcost, initialoutlay, monthlymaintenance)
    VALUES (9, 'Spa', 20, 30, 100000, 800);
```

###### Question 2: Select in Insert

```sql
INSERT INTO cd.facilities
    (facid, name, membercost, guestcost, initialoutlay, monthlymaintenance)
    VALUES ((SELECT MAX(facid) FROM cd.facilities) + 1, 'Spa', 20, 30, 100000, 800);
```

###### Question 3: Update

```sql
UPDATE cd.facilities
SET initialoutlay = 10000
WHERE name = 'Tennis Court 2';
```

###### Question 4: Update with Calculation

```sql
UPDATE cd.facilities
SET membercost = (
        SELECT membercost
        FROM cd.facilities
        WHERE name = 'Tennis Court 1'
        ) * 1.1,
    guestcost = (
        SELECT guestcost
        FROM cd.facilities
        WHERE name = 'Tennis Court 1'
        ) * 1.1
WHERE name = 'Tennis Court 2';
```

###### Question 5: Delete All

```sql
DELETE FROM cd.bookings;
```

###### Question 6: Delete Condition

```sql
DELETE FROM cd.members
WHERE memid = 37;
```

###### Question 7: Where 2

```sql
SELECT facid, name, membercost, monthlymaintenance
FROM cd.facilities
WHERE membercost > 0 AND (membercost < monthlymaintenance / 50);
```

###### Question 8: Where 3

```sql
SELECT *
FROM cd.facilities
WHERE name LIKE '%Tennis%';
```

###### Question 9: Where 4

```sql
SELECT *
FROM cd.facilities
WHERE facid IN (1, 5);
```

###### Question 10: Date

```sql
SELECT memid, surname, firstname, joindate
FROM cd.members
WHERE joindate >= '2012-09-01';
```

###### Question 11: Union

```sql
SELECT surname
FROM cd.members
UNION
SELECT name
FROM cd.facilities;
```

###### Question 12: Simple Join

```sql
SELECT book.starttime
FROM cd.members mem INNER JOIN cd.bookings book ON mem.memid = book.memid
WHERE firstname = 'David' AND surname = 'Farrell';
```

###### Question 13: Simple Join 2

```sql
SELECT book.starttime as start, fac.name as name
FROM cd.facilities fac INNER JOIN cd.bookings book ON fac.facid = book.facid
WHERE
    fac.name in ('Tennis Court 2','Tennis Court 1') AND
    book.starttime >= '2012-09-21' AND
    book.starttime < '2012-09-22'
ORDER BY book.starttime ASC;
```

###### Question 14: Self 2

```sql
SELECT mem.firstname AS memfname, mem.surname AS memsname, rec.firstname AS recfname, rec.surname AS recsname
FROM cd.members mem LEFT JOIN cd.members rec ON mem.recommendedby = rec.memid
ORDER BY memsname, memfname;
```

###### Question 15: Self

```sql
SELECT DISTINCT rec.firstname, rec.surname
FROM cd.members mem INNER JOIN cd.members rec ON mem.recommendedby = rec.memid
ORDER BY surname, firstname;
```

###### Question 16: Subquery and Join

```sql
SELECT DISTINCT mem.firstname || ' ' || mem.surname AS member, 
    (SELECT rec.firstname || ' ' || rec.surname FROM cd.members rec WHERE rec.memid = mem.recommendedby) AS recommender
FROM cd.members mem
ORDER BY member, recommender;
```

###### Question 17: Count 3

```sql
SELECT recommendedby, COUNT(*) AS count
FROM cd.members
WHERE recommendedby IS NOT NULL
GROUP BY recommendedby
ORDER BY recommendedby;
```

###### Question 18: Fachours

```sql
SELECT facid, SUM(slots) AS "Total Slots"
FROM cd.bookings
GROUP BY facid
ORDER BY facid;
```

###### Question 19: Fachours by Month

```sql
SELECT facid, SUM(slots) AS "Total Slots"
FROM cd.bookings
WHERE starttime >= '2012-09-01' AND starttime < '2012-10-01'
GROUP BY facid
ORDER BY "Total Slots";
```

###### Question 20: Fachours by Month 2

```sql
SELECT facid, EXTRACT(month FROM starttime) AS month, SUM(slots) AS "Total Slots"
FROM cd.bookings
WHERE EXTRACT(year FROM starttime) = 2012
GROUP BY facid, month
ORDER BY facid, month;
```

###### Question 21: Count Distinct

```sql
SELECT COUNT(DISTINCT memid) AS count
FROM cd.bookings;
```

###### Question 22: N Booking

```sql
SELECT mem.surname, mem.firstname, mem.memid, MIN(book.starttime) AS starttime
FROM cd.members mem INNER JOIN cd.bookings book ON mem.memid = book.memid
WHERE book.starttime > '2012-09-01'
GROUP BY mem.surname, mem.firstname, mem.memid
ORDER BY memid;
```

###### Question 23: Count Members

```sql
SELECT COUNT(*) OVER(), firstname, surname
FROM cd.members;
```

###### Question 24: Num Members

```sql
SELECT COUNT(*) OVER(ORDER BY joindate), firstname, surname
FROM cd.members;
```

###### Question 25: Fachours 4

```sql
SELECT facid, total
FROM (
    SELECT facid, SUM(slots) total, RANK() OVER (ORDER BY SUM(slots) DESC) rank
    FROM cd.bookings
    GROUP BY facid
) AS ranked
WHERE rank = 1;
```

###### Question 26: Concat

```sql
SELECT surname || ', ' || firstname AS name
FROM cd.members;
```

###### Question 27: Regular Expression

```sql
SELECT memid, telephone
FROM cd.members
WHERE telephone ~ '^\(\d{3}\) \d{3}-\d{4}$';
```

###### Question 28: Substring

```sql
SELECT SUBSTR(surname, 1, 1) AS letter, COUNT(*) AS count
FROM cd.members
GROUP BY letter
ORDER BY letter;

-- another way:
SELECT LEFT(surname, 1) AS letter, COUNT(*) AS count
FROM cd.members
GROUP BY letter
ORDER BY letter;
```