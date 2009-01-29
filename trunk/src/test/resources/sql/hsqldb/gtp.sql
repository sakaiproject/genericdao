-- testing the ability to read from files
CREATE TABLE {TABLENAME:org.sakaiproject.genericdao.test.GenericTestParentObject} ( 
    {ID:GenericTestParentObject} BIGINT NOT NULL IDENTITY PRIMARY KEY, 
    {COLUMNNAME:title} VARCHAR(255) NOT NULL,
    {COLUMNNAME:gto} BIGINT,
    FOREIGN KEY ({COLUMNNAME:gto}) REFERENCES {TABLENAME:GenericTestObject}({ID:GenericTestObject}) );
