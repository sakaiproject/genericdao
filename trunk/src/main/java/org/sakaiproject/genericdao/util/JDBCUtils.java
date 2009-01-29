/**
 * $Id$
 * $URL$
 * JDBCUtils.java - genericdao - Aug 4, 2008 6:12:37 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.util;

import java.util.ArrayList;
import java.util.List;

import org.azeckoski.reflectutils.ConversionUtils;
import org.sakaiproject.genericdao.api.mappers.NamesRecord;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;


/**
 * A set of utilities to help working with JDBC
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class JDBCUtils {

    /**
     * Make a partial SQL string for a specific entity type using the associated {@link NamesRecord}
     * and a {@link Search} object
     * @param namesRecord a record of the mapping of property to columns for an entity type
     * @param search a search object
     * @return an object with the partial where SQL query and the params (args)
     */
    public static QueryData makeSQLfromSearch(NamesRecord namesRecord, Search search) {
        // Checks to see if the required params are set and throws exception if not
        if (search == null) {
            throw new IllegalArgumentException("search cannot be null");
        }

        QueryData sd = new QueryData();
        //NamesRecord nr = getNamesRecord(type);
        StringBuilder sql = new StringBuilder();

        // Only add in restrictions if there are some to add
        if (search.getRestrictions() != null && search.getRestrictions().length > 0) {
            // set up the conjunction
            String junction = " and ";
            if (! search.conjunction) {
                // set to use disjunction
                junction = " or ";
            }

            // put in the restrictions
            for (int i = 0; i < search.getRestrictions().length; i++) {
                if (i > 0) {
                    sql.append(junction);
                }
                String property = search.getRestrictions()[i].property;
                Object value = search.getRestrictions()[i].value;
                if (property == null || value == null) {
                    throw new IllegalArgumentException("restrictions property and value cannot be null or empty");            
                }

                String column = namesRecord.getColumnForProperty(property);
                // we cannot recover from the case where the property is invalid
                if (column == null) {
                	throw new IllegalArgumentException("Cannot find a column which matches the property ("+property+") for search: " + search);
                }
                if (value.getClass().isArray()) {
                    // special handling for "in" type comparisons
                    Object[] objectArray = (Object[]) value;
                    if (objectArray.length == 1) {
                        value = objectArray[0];
                    } else if (objectArray.length > 1) {
                        sql.append(column);
                        if (Restriction.NOT_EQUALS == search.getRestrictions()[i].comparison) {
                            sql.append(" not in ");
                        } else {
                            sql.append(" in ");
                        }
                        // need to create this kind of thing: (?,?,?) where there are 3 things in the array
                        sql.append("(");
                        for (int j = 0; j < objectArray.length; j++) {
                            if (j > 0) { sql.append(','); }
                            sql.append('?');
                        }
                        sql.append(") ");
                        // now we need to add the array of values (and convert possibly all of them)
                        for (int j = 0; j < objectArray.length; j++) {
                            sd.addArg( convertColumn(namesRecord, column, objectArray[j]) );
                        }
                        //sd.addArrayArg(objectArray);
                    } else {
                        // do nothing for now, this is slightly invalid but not worth dying over
                    }
                }

                if (! value.getClass().isArray()) {
                    int comparisonConstant = search.getRestrictions()[i].comparison;
                    // convert if needed
                    value = convertColumn(namesRecord, column, value);
                    sql.append( makeComparisonSQL(sd.args, namesRecord.getColumnForProperty(property), comparisonConstant, value) );
                }
            }
        }

        // handle the sorting (sort param can be null for no sort)
        if (search.getOrders() != null) {
            StringBuilder orderSQL = new StringBuilder();
            if (search.getOrders().length > 0) {
                orderSQL.append(" order by ");
                for (int i = 0; i < search.getOrders().length; i++) {
                    if (i > 0) {
                        orderSQL.append(", ");
                    }
                    String property = search.getOrders()[i].property;
                    orderSQL.append(namesRecord.getColumnForProperty(property));
                    if (search.getOrders()[i].ascending) {
                        orderSQL.append(" asc ");
                    } else {
                        orderSQL.append(" desc ");
                    }
                }
            }
            sd.orderSQL = orderSQL.toString();
        }

        sd.whereSQL = sql.toString();
        return sd;
    }

    /**
     * Create comparison SQL but converts the value object to a string
     * 
     * @param column the name of a database column
     * @param comparisonConstant the comparison constant (e.g. EQUALS)
     * @param value the value to compare the property to
     * @return a string representing the SQL snippet (e.g. propA = ?)
     */
    public static String makeComparisonSQL(String column, int comparisonConstant, Object value) {
        String sval = null;
        if (comparisonConstant != Restriction.NOT_NULL 
                && comparisonConstant != Restriction.NULL) {
            if (value.getClass().isAssignableFrom(Boolean.class) 
                    || value.getClass().isAssignableFrom(Number.class)) {
                // special handling for boolean and numbers
                sval = value.toString();
            } else {
                sval = "'" + value.toString() + "'";
            }
        }
        return buildComparisonSQL(column, comparisonConstant, sval);
    }

    /**
     * Create comparison SQL but places the values into the params list
     * 
     * @param params a set of params to add this value to
     * @param column the name of a database column
     * @param comparisonConstant the comparison constant (e.g. EQUALS)
     * @param value the value to compare the property to
     * @return a string representing the SQL snippet (e.g. propA = ?)
     */
    public static String makeComparisonSQL(List<Object> params, String column, int comparisonConstant, Object value) {
        if (comparisonConstant != Restriction.NOT_NULL 
                && comparisonConstant != Restriction.NULL) {
            params.add(value);
        }
        return buildComparisonSQL(column, comparisonConstant, "?");
    }

    /**
     * Convenient way to translate constants into SQL,
     * sVal can be any string value including ?
     */
    protected static String buildComparisonSQL(String column, int comparisonConstant, String sval) {
        switch (comparisonConstant) {
        case Restriction.EQUALS:      return column + " = " + sval;
        case Restriction.GREATER:     return column + " > " + sval;
        case Restriction.LESS:        return column + " < " + sval;
        case Restriction.LIKE:        return column + " like " + sval;
        case Restriction.NOT_EQUALS:  return column + " <> " + sval;
        case Restriction.NOT_NULL:    return column + " is not null";
        case Restriction.NULL:        return column + " is null";
        default: throw new IllegalArgumentException("Invalid comparison constant: " + comparisonConstant);
        }
    }

    /**
     * This helper method will convert the incoming data if it needs to be
     * converted for the given column, otherwise it will do nothing to the value
     * @param namesRecord the names record for the persistent class
     * @param column the name of the column this value is associated with
     * @param value the value to convert
     * @return the converted value or the original value if no conversion needed
     */
    public static Object convertColumn(NamesRecord namesRecord, String column, Object value) {
        if (namesRecord != null && column != null) {
            try {
                Class<?> convertType = namesRecord.getTypeForColumn(column);
                if (convertType != null) {
                    value = ConversionUtils.getInstance().convert(value, convertType);
                }
            } catch (Exception e) {
                // nothing to do but continue on
            }
        }
        return value;
    }



    /**
     * Special class for passing SQL query data for running queries with,
     * has methods to get the where statement and the set of params (args)
     * 
     * @author Aaron Zeckoski (azeckoski@gmail.com)
     */
    public static class QueryData {
        public String whereSQL = "";
        public String orderSQL = "";
        public List<Object> args = new ArrayList<Object>();
        /**
         * Add an argument value to the arg array
         * @param value an object
         */
        public void addArg(Object value) {
            args.add(value);
        }
        /** 
         * Add an array of argument values to the arg array
         * @param values an array of objects
         */
        public void addArrayArg(Object[] values) {
            for (int i = 0; i < values.length; i++) {
                addArg(values[i]);
            }
        }
        /**
         * Get the SQL statement to place after the tables in the SQL statement,
         * typically this is something like "where name like '%aaronz%' order by name"
         * @return the string to place after the table list, will not be null
         */
        public String getAfterTableSQL() {
            String sql = "";
            if (whereSQL != null &&  !("".equals(whereSQL))) {
                sql = " where " + whereSQL;
            }
            if (orderSQL != null &&  !("".equals(orderSQL))) {
                sql += orderSQL;
            }
            return sql;
        }
        /**
         * @return the array of arguments which should replace the ? is the sql string
         */
        public Object[] getArgs() {
            return args.toArray();
        }
    }

}
