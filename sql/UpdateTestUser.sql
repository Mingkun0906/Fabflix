-- Create user with native password authentication
CREATE USER 'mytestuser'@'localhost' IDENTIFIED WITH mysql_native_password BY 'My6$Password';

-- Grant privileges
GRANT ALL PRIVILEGES ON moviedb.* TO 'mytestuser'@'localhost';

-- Apply changes
FLUSH PRIVILEGES;