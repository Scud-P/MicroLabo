CREATE TABLE IF NOT EXISTS patient (
    id INT(11) AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(45),
    last_name VARCHAR(45),
    birthdate DATE,
    gender VARCHAR(1),
    address VARCHAR(45),
    phone_number VARCHAR(45)
);

INSERT INTO patient (id, first_name, last_name, birthdate, gender, address, phone_number) VALUES
(1, 'Test', 'TestNone', '1966-12-31', 'F', '1 Brookside St', '100-222-3333'),
(2, 'Test', 'TestBorderline', '1945-06-24', 'M', '2 High St', '200-333-4444'),
(3, 'Test', 'TestInDanger', '2004-06-18', 'M', '3 Club Road', '300-444-5555'),
(4, 'Test', 'TestEarlyOnset', '2002-06-28', 'F', '4 Valley Dr', '400-555-6666');


CREATE TABLE IF NOT EXISTS user_credentials (
    id INT(11) AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(45),
    email VARCHAR(45),
    password VARCHAR(200)
);

INSERT INTO user_credentials (id, name, email, password) VALUES
(1, 'Bob', 'email', '$2a$10$0R7ndDhSMPOJMx7Ql/Rs3.L6DN9eoMghO3kOImbH0CW6vQI9.MDpC'),
(2, 'John', 'email', '$2a$10$LyY2cLY8aimTRDn/4Kjq1uDcue1zp1/JWUYJiCQsgqIs2w4x77JAq');
