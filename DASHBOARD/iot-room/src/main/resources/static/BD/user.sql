CREATE USER 'iot_user'@'%' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON iot_room.* TO 'iot_user'@'%';
FLUSH PRIVILEGES;