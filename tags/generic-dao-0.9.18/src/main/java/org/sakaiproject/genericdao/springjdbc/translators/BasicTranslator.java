/**
 * $Id$
 * $URL$
 * BasicTranslator.java - genericdao - Apr 26, 2008 1:52:06 PM - azeckoski
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

import java.lang.annotation.Annotation;

import org.azeckoski.reflectutils.ClassFields;
import org.azeckoski.reflectutils.ReflectUtils;
import org.sakaiproject.genericdao.api.annotations.PersistentTableName;
import org.sakaiproject.genericdao.api.translators.DatabaseTranslator;


/**
 * A basic translator class to extend
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public abstract class BasicTranslator implements DatabaseTranslator {

    /**
     * Create a tablename from a given class
     */
    public static String makeTableNameFromClass(Class<?> pClass) {
        String tableName = null;
        // try to get from class annotations first
        ClassFields<?> classFields = ReflectUtils.getInstance().analyzeClass(pClass);
        if (classFields.getClassAnnotations().contains(PersistentTableName.class)) {
            for (Annotation classAnnote : classFields.getClassAnnotations()) {
                if (PersistentTableName.class.equals(classAnnote.annotationType())) {
                    tableName = ((PersistentTableName)classAnnote).value();
                    break;
                }
            }
        }
        if (tableName == null) {
            String name = pClass.getSimpleName();
            tableName = makeDBNameFromCamelCase(name);
            if (tableName.startsWith("_") && tableName.length() > 1) {
                tableName = tableName.substring(1);
            }
        }
        return tableName;
    }

    /**
     * Create a string that can be used for a DB name that is compatible with most databases (30 chars or less)
     * @param camelCase a string in camelCase (e.g. someString, MyString)
     * @return a string in DB style (e.g SOME_STRING, _MY_STRING)
     */
    public static String makeDBNameFromCamelCase(String camelCase) {
        String dbCase;
        if (camelCase.equals(camelCase.toUpperCase())) {
            // name is already uppercase
            dbCase = camelCase;
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < camelCase.length(); i++) {
                char c = camelCase.charAt(i);
                if (Character.isUpperCase(c)) {
                    sb.append("_");
                }
                sb.append(c);
            }
            dbCase = sb.toString().toUpperCase();
        }
        return chopString(dbCase, 30);
    }

    /**
     * @param dbName a string in DB style (e.g SOME_STRING, MY_STRING, _MY_STRING)
     * @return a string in camelCase (e.g. someString, myString, MyString)
     */
    public static String makeCamelCaseFromDBName(String dbName) {
        String camelCase;
        if (! dbName.equals(dbName.toUpperCase()) 
                && dbName.indexOf('_') == -1) {
            // name is probably already fine
            camelCase = dbName;
        } else {
            StringBuilder sb = new StringBuilder();
            dbName = dbName.toLowerCase();
            for (int i = 0; i < dbName.length(); i++) {
                char c = dbName.charAt(i);
                if (c == '_') {
                    i++;
                    try {
                        c = Character.toUpperCase(dbName.charAt(i));
                    } catch (IndexOutOfBoundsException e) {
                        // end of string
                        break;
                    }
                }
                sb.append(c);
            }
            camelCase = sb.toString();
        }
        return camelCase;
    }

    /**
     * Chop a string to the mexLength if it is longer and return the chopped string
     * @param str
     * @param maxLength
     * @return the chopped string
     */
    public static String chopString(String str, int maxLength) {
        if (str == null || "".equals(str)) { return ""; }
        if (str.length() > maxLength) {
            str = str.substring(0, 29);
        }
        return str;
    }

}
