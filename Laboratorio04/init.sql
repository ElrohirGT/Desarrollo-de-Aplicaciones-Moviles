DROP DATABASE lab04;
CREATE DATABASE lab04;
\c lab04;

CREATE  TABLE "User" (
	user_id VARCHAR(64) PRIMARY KEY NOT NULL,
	username VARCHAR(50) NOT NULL,
	email VARCHAR(64) NOT NULL,
	password VARCHAR(64) NOT NULL
);

CREATE TABLE "Session" (
	session_id VARCHAR(64) PRIMARY KEY NOT NULL,
	user_id VARCHAR(64) NOT NULL references "User"(user_id),
	expire_date TIMESTAMP NOT NULL
);
