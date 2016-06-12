# test users

# --- !Ups

INSERT INTO USERS VALUES ( 1, 'aloise', 'test@aloise.name', '8f9388a66492d5321d91735945681009efb1b30b', TIMESTAMP '2015-12-31 23:59:59' , FALSE);

# --- !Downs

DELETE FROM USERS WHERE email = 'test@aloise.name';