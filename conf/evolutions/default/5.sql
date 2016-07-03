# default currency for place

# --- !Ups

ALTER TABLE PLACES ADD COLUMN "currency" CHAR(3) NOT NULL DEFAULT 'USD';

# --- !Downs

ALTER TABLE PLACES DROP COLUMN "currency";

