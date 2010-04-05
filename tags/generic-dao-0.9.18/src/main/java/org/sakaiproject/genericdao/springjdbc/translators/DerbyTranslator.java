/**
 * $Id$
 * $URL$
 * DerbyTranslator.java - genericdao - Apr 26, 2008 2:38:44 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2
 * 
 * A copy of the Apache License, Version 2 has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.springjdbc.translators;

/**
 * Apache Derby database translator
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class DerbyTranslator extends BasicTranslator {

    public String handlesDB() {
        return DBTYPE_DERBY;
    }

    public String makeAutoIdQuery(String tableName, String idColumnName) {
        return "values IDENTITY_VAL_LOCAL()";
    }

    public String makeLimitQuery(String sql, long start, long limit, String tableName) {
        // no real limit support in derby...
        return sql;
        /*
         * 5.2. Does Derby support a LIMIT command?
Derby supports limiting the number of rows returned by a query through JDBC. For example, to fetch the first 5 rows of a large table:

Statement stmt = con.createStatement();
stmt.setMaxRows(5);
ResultSet rs = stmt.executeQuery("SELECT * FROM myLargeTable");

Some related tuning tips are available in this external article.

Starting with the 10.4.1.3 release Derby also supports limiting the number of rows using the ROW_NUMBER function.

For example, to fetch the first 5 rows of a large table:

SELECT * FROM (
    SELECT ROW_NUMBER() OVER() AS rownum, myLargeTable.*
    FROM myLargeTable
) AS tmp
WHERE rownum <= 5;

The ROW_NUMBER function can also be used to select a limited number of rows starting with an offset, for example:

SELECT * FROM (
    SELECT ROW_NUMBER() OVER() AS rownum, myLargeTable.*
    FROM myLargeTable
) AS tmp
WHERE rownum > 200000 AND rownum <= 200005;

For more information, refer to the ROW_NUMBER built-in function in the Derby Reference Manual (available from the Documentation page). Development notes are available on the OLAPRowNumber wiki page.

The LIMIT keyword is not defined in the SQL standard, and is currently not supported.

Testing support which would not work:
create table testing (ID INT GENERATED ALWAYS AS IDENTITY NOT NULL PRIMARY KEY, 
TITLE VARCHAR(255), SOMETHING VARCHAR(255));

insert into testing (title) values ('aaronz');
insert into testing (title) values ('becky');
insert into testing (title) values ('kitty');
insert into testing (title) values ('sally');
insert into testing (title) values ('billy');

SELECT * FROM testing WHERE title is not null order by title

SELECT * FROM ( 
SELECT ROW_NUMBER() OVER () as rownum, testing.* FROM testing WHERE title is not null ORDER BY title
) AS tmp WHERE rownum >= 2 and rownum < 4;
         */
    }

}
