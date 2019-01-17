# 1.Create new database from root:

CREATE conversation_logs;

# 2.Switch to the database to be used:

USE conversation_logs;

# 3.Create the table from root:

Create table bot1_logs (
chat_date DATE,
chat_time TIME,
inserted_date DATE,
inserted_time TIME,
text VARCHAR(1000),
choices VARCHAR(1000),
source VARCHAR(1000),
userId VARCHAR(50),
sessionId VARCHAR(50),
botId VARCHAR(50),
channelType VARCHAR(100),
channelId VARCHAR(100),
payload VARCHAR(11000),
text_id VARCHAR(25) NOT NULL,
PRIMARY KEY (text_id)
);

Create table bot2_logs (
chat_date DATE,
chat_time TIME,
inserted_date DATE,
inserted_time TIME,
text VARCHAR(1000),
choices VARCHAR(1000),
source VARCHAR(1000),
userId VARCHAR(50),
sessionId VARCHAR(50),
botId VARCHAR(50),
channelType VARCHAR(100),
channelId VARCHAR(100),
payload VARCHAR(11000),
text_id VARCHAR(25) NOT NULL,
PRIMARY KEY (text_id)
);

Create table bot3_logs (
chat_date DATE,
chat_time TIME,
inserted_date DATE,
inserted_time TIME,
text VARCHAR(1000),
choices VARCHAR(1000),
source VARCHAR(1000),
userId VARCHAR(50),
sessionId VARCHAR(50),
botId VARCHAR(50),
channelType VARCHAR(100),
channelId VARCHAR(100),
payload VARCHAR(11000),
text_id VARCHAR(25) NOT NULL,
PRIMARY KEY (text_id)
);

# 4.Create user account "logger":

CREATE USER 'logger'@'localhost' IDENTIFIED BY 'logger#123';


# 5.Grant user "logger" permissions from root:

GRANT ALL PRIVILEGES ON conversation_logs.* TO 'logger'@'localhost' 
	WITH GRANT OPTION;

# 6.To grant only specific priviledges on a specific table to a user:

GRANT CREATE, SELECT, INSERT, DELETE, DROP ON conversation_logs.logs TO 'logger'@'localhost'; 
							  #hostname may also be left as a wildcard: 'logger'@'%'