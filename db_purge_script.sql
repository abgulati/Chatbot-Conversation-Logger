# 1.Delete table "logs":

DROP TABLE logs;

# 2.Delete database:

DROP DATABASE conversation_logs;

# 3.List all users:

SELECT user FROM mysql.user GROUP BY user;

# 4.Delete a user:

DELETE FROM mysql.user WHERE user = 'logger';

# 5.Flush priviledges post delete:

FLUSH PRIVILEGES;

# 6.To list all users and their privileges:

SELECT * FROM mysql.user;

/*

You should use FLUSH PRIVILEGES; only if you modify the grant tables directly using statements such as INSERT, UPDATE, or DELETE.

If you modify the grant tables directly using statements such as INSERT, UPDATE, or DELETE, your changes have no effect on privilege 
checking until you either restart the server or tell it to reload the tables. If you change the grant tables directly but forget to 
reload them, your changes have no effect until you restart the server. This may leave you wondering why your changes seem to make 
no difference! To tell the server to reload the grant tables, perform a flush-privileges operation. This can be done by issuing a 
FLUSH PRIVILEGES statement or by executing a mysqladmin flush-privileges or mysqladmin reload command. If you modify the grant tables 
indirectly using account-management statements such as GRANT, REVOKE, SET PASSWORD, or RENAME USER, the server notices these changes 
and loads the grant tables into memory again immediately.

These mysql database tables contain grant information:

user: User accounts, global privileges, and other non-privilege columns

db: Database-level privileges

tables_priv: Table-level privileges

columns_priv: Column-level privileges

procs_priv: Stored procedure and function privileges

proxies_priv: Proxy-user privileges

*/