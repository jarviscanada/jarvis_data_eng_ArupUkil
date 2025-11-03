-- Question 1: Insert

INSERT INTO cd.facilities
    (facid, name, membercost, guestcost, initialoutlay, monthlymaintenance)
    VALUES (9, 'Spa', 20, 30, 100000, 800);

-- Question 2: Select in Insert

INSERT INTO cd.facilities
   (facid, name, membercost, guestcost, initialoutlay, monthlymaintenance)
   VALUES ((SELECT MAX(facid) FROM cd.facilities) + 1, 'Spa', 20, 30, 100000, 800);

-- Question 3: Update

UPDATE cd.facilities
SET initialoutlay = 10000
WHERE name = 'Tennis Court 2';

-- Question 4: Update with Calculation

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

-- Question 5: Delete All

DELETE FROM cd.bookings;

-- Question 6: Delete Condition

DELETE FROM cd.members
WHERE memid = 37;

-- Question 7: Where 2

SELECT facid, name, membercost, monthlymaintenance
FROM cd.facilities
WHERE membercost > 0 AND (membercost < monthlymaintenance / 50);

-- Question 8: Where 3

SELECT *
FROM cd.facilities
WHERE name LIKE '%Tennis%';

-- Question 9: Where 4

SELECT *
FROM cd.facilities
WHERE facid IN (1, 5);

-- Question 10: Date

SELECT memid, surname, firstname, joindate
FROM cd.members
WHERE joindate >= '2012-09-01';

-- Question 11: Union

SELECT surname
FROM cd.members
UNION
SELECT name
FROM cd.facilities;

-- Question 12: Simple Join

SELECT book.starttime
FROM cd.members mem INNER JOIN cd.bookings book ON mem.memid = book.memid
WHERE firstname = 'David' AND surname = 'Farrell';

-- Question 13: Simple Join 2

SELECT book.starttime as start, fac.name as name
FROM cd.facilities fac INNER JOIN cd.bookings book ON fac.facid = book.facid
WHERE
    fac.name in ('Tennis Court 2','Tennis Court 1') AND
    book.starttime >= '2012-09-21' AND
    book.starttime < '2012-09-22'
ORDER BY book.starttime ASC;

-- Question 14: Self 2

SELECT mem.firstname AS memfname, mem.surname AS memsname, rec.firstname AS recfname, rec.surname AS recsname
FROM cd.members mem LEFT JOIN cd.members rec ON mem.recommendedby = rec.memid
ORDER BY memsname, memfname;

-- Question 15: Self

SELECT DISTINCT rec.firstname, rec.surname
FROM cd.members mem INNER JOIN cd.members rec ON mem.recommendedby = rec.memid
ORDER BY surname, firstname;

-- Question 16: Subquery and Join

SELECT DISTINCT mem.firstname || ' ' || mem.surname AS member,
    (SELECT rec.firstname || ' ' || rec.surname FROM cd.members rec WHERE rec.memid = mem.recommendedby) AS recommender
FROM cd.members mem
ORDER BY member, recommender;

-- Question 17: Count 3

SELECT recommendedby, COUNT(*) AS count
FROM cd.members
WHERE recommendedby IS NOT NULL
GROUP BY recommendedby
ORDER BY recommendedby;

-- Question 18: Fachours

SELECT facid, SUM(slots) AS "Total Slots"
FROM cd.bookings
GROUP BY facid
ORDER BY facid;

-- Question 19: Fachours by Month

SELECT facid, SUM(slots) AS "Total Slots"
FROM cd.bookings
WHERE starttime >= '2012-09-01' AND starttime < '2012-10-01'
GROUP BY facid
ORDER BY "Total Slots";

-- Question 20: Fachours by Month 2

SELECT facid, EXTRACT(month FROM starttime) AS month, SUM(slots) AS "Total Slots"
FROM cd.bookings
WHERE EXTRACT(year FROM starttime) = 2012
GROUP BY facid, month
ORDER BY facid, month;

-- Question 21: Count Distinct

SELECT COUNT(DISTINCT memid) AS count
FROM cd.bookings;

-- Question 22: N Booking

SELECT mem.surname, mem.firstname, mem.memid, MIN(book.starttime) AS starttime
FROM cd.members mem INNER JOIN cd.bookings book ON mem.memid = book.memid
WHERE book.starttime > '2012-09-01'
GROUP BY mem.surname, mem.firstname, mem.memid
ORDER BY memid;

-- Question 23: Count Members

SELECT COUNT(*) OVER(), firstname, surname
FROM cd.members;

-- Question 24: Num Members

SELECT COUNT(*) OVER(ORDER BY joindate), firstname, surname
FROM cd.members;

-- Question 25: Fachours 4

SELECT facid, total
FROM (
    SELECT facid, SUM(slots) total, RANK() OVER (ORDER BY SUM(slots) DESC) rank
    FROM cd.bookings
    GROUP BY facid
) AS ranked
WHERE rank = 1;

-- Question 26: Concat

SELECT surname || ', ' || firstname AS name
FROM cd.members;

-- Question 27: Regular Expression

SELECT memid, telephone
FROM cd.members
WHERE telephone ~ '^\(\d{3}\) \d{3}-\d{4}$';

-- Question 28: Substring

SELECT SUBSTR(surname, 1, 1) AS letter, COUNT(*) AS count
FROM cd.members
GROUP BY letter
ORDER BY letter;