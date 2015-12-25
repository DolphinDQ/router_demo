CREATE TABLE mr_smart_home.response_cache(
    ID KEY  PRIMARY KEY(one or more columns),
    TYPE TEXT ,
    LAST_UPDATE_TIME INTEGER,
    ROUTER_ID KEY NOT NULL,
    DATA BLOB,
    FOREIGN KEY(ROUTER_ID) REFERENCES router_config(ID) on delete cascade
);