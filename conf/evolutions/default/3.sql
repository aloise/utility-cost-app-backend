# test places

# --- !Ups

INSERT INTO PLACES VALUES ( 1, 'Place #1', 'Germany', 'Berlin', 'Brandenburg', '12345', 'Max Platz Strasse', FALSE);
INSERT INTO PLACES VALUES ( 2, 'Place #2', 'Germany', 'Munich', 'Bavaria', '32156', 'ABC Street', FALSE);
INSERT INTO PLACES VALUES ( 3, 'Place #3', 'Germany', 'Stuttgart', 'Bavaria', '423432', 'Franz Kafka', FALSE);

INSERT INTO USERS_PLACES VALUES (1, 1, 'admin' );
INSERT INTO USERS_PLACES VALUES (1, 2, 'admin' );
INSERT INTO USERS_PLACES VALUES (1, 3, 'user' );

# --- !Downs

DELETE FROM PLACES WHERE "id" IN ( 1, 2, 3);
DELETE FROM USERS_PLACES WHERE "user_id" = 1 and "place_id" IN ( 1, 2, 3);