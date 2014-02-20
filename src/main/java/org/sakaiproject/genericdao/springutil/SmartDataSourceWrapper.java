/**
 * $Id$
 * $URL$
 * SmartDataSourceWrapper.java - genericdao - Nov 6, 2008 11:07:58 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.springutil;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.SmartDataSource;


/**
 * This allows us to trick spring into not handling our dataSources for us,
 * it keeps spring from automatically closing every non-spring DataSource connection
 * after each method is executed
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class SmartDataSourceWrapper implements DataSource, SmartDataSource {

    private final DataSource dataSource;

    /**
     * Create a new data-source wrapper,
     * all connections are set to auto-commit false by default
     * 
     * @param dataSource any non-spring {@link DataSource}
     */
    public SmartDataSourceWrapper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private boolean autoCommitConnection = false;
    /**
     * @param autoCommitConnection the auto-commit for all connections coming out is set to this
     */
    public void setAutoCommitConnection(boolean autoCommitConnection) {
        this.autoCommitConnection = autoCommitConnection;
    }
    /**
     * Fixes up the auto-commit to be equal to the setting for this connection if possible,
     * will not cause an exception though if it fails to do it
     * @param connection the connection
     * @return the same connection that was input
     */
    private Connection fixAutoCommit(Connection connection) {
        try {
            if (connection.getAutoCommit() != autoCommitConnection) {
                connection.setAutoCommit(autoCommitConnection);
            }
        } catch (SQLException e) {
            // do nothing
        }
        return connection;
    }

    // force spring to not close out this connection for us
    public boolean shouldClose(Connection con) {
        return false;
    }


    public Connection getConnection() throws SQLException {
        return fixAutoCommit( dataSource.getConnection() );
    }
    public Connection getConnection(String username, String password) throws SQLException {
        return fixAutoCommit( dataSource.getConnection(username, password) );
    }
    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }
    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }
    public void setLogWriter(PrintWriter out) throws SQLException {
        dataSource.setLogWriter(out);
    }
    public void setLoginTimeout(int seconds) throws SQLException {
        dataSource.setLoginTimeout(seconds);
    }
    // Java 6 compatible
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return dataSource.isWrapperFor(iface);
    }
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return dataSource.unwrap(iface);
    }
    // Java 7 compatible
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getAnonymousLogger();
    }

}
