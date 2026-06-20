USE mysql;

SET @user_exists := (
    SELECT COUNT(*)
    FROM mysql.user
    WHERE user = 'iot_user'
      AND host = '%'
);

SET @drop_user_sql := IF(
    @user_exists > 0,
    "DROP USER 'iot_user'@'%'",
    "SELECT 'Utilizador iot_user@% não existia' AS info"
);

PREPARE stmt FROM @drop_user_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE USER 'iot_user'@'%'
IDENTIFIED BY 'password';

GRANT
    SELECT,
    INSERT,
    UPDATE,
    DELETE,
    EXECUTE,
    SHOW VIEW
ON iot_room.*
TO 'iot_user'@'%';

FLUSH PRIVILEGES;

SHOW GRANTS FOR 'iot_user'@'%';