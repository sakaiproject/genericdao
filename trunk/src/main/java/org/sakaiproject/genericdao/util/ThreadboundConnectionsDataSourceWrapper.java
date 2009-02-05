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

package org.sakaiproject.genericdao.util;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;


/**
 * This will bind the connections coming out of this dataSource to the current thread
 * so there is only one connection per thread
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class ThreadboundConnectionsDataSourceWrapper implements DataSource {

    private final DataSource dataSource;
    /**
     * This holds the connection in the current thread to ensure we get the same one
     * each time we ask for it
     */
    private final ThreadLocal<Connection> threadedConnection = new ThreadLocal<Connection>();

    /**
     * Create a new data-source wrapper,
     * auto-commit is set to false for all connections fed out of this datasource
     * 
     * @param dataSource any non-spring {@link DataSource}
     */
    public ThreadboundConnectionsDataSourceWrapper(DataSource dataSource) {
        this(dataSource, false);
    }

    /**
     * Create a new data-source wrapper
     * 
     * @param dataSource any non-spring {@link DataSource}
     * @param autoCommitConnection sets the auto-commit for all connections to this
     */
    public ThreadboundConnectionsDataSourceWrapper(DataSource dataSource, boolean autoCommitConnection) {
        this.dataSource = dataSource;
        this.autoCommitConnection = autoCommitConnection;
        // removed this because it was causing problems
//        // add shutdown hook
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            public void run() {
//                clearConnection();
//            }
//        });
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

    /**
     * Get rid of all resources this thing might be holding onto
     */
    public void shutdown() {
        clearConnection();
        System.out.println("Shutting down the ThreadboundConnectionsDataSourceWrapper: " + this);
    }

    // this stuff will bind the connection to the current thread

    /**
     * This will close the current thread-bound connection (if there is one) and then clear the threadlocal,
     * it is ok to call this multiple times
     */
    public final void clearConnection() {
        Connection threaded = threadedConnection.get();
        if (threaded != null) {
            threadedConnection.remove();
            try {
                threaded.close();
            } catch (SQLException e) {
                // ignore this
                e.getMessage();
            }
        }
    }

    /**
     * Creates the new connection, wraps it, and binds it to the current thread
     * @param username
     * @param password
     * @throws SQLException
     */
    private Connection createBindWrapConnection(String username, String password) throws SQLException {
        Connection newConn = getNewConnection(username, password);
        // use a connection wrapper to ensure that the connection close does not hold things open
        newConn = new CloseHookConnectionWrapper(newConn, 
                new Runnable() {
                    public void run() {
                        clearConnection();
                    }
        });
        threadedConnection.set( newConn );
        return newConn;
    }

    /**
     * Initialize a connection correctly depending on whether a username/password is provided
     * 
     * @param username
     * @param password
     * @return a new connection
     * @throws SQLException if connection cannot be obtained
     */
    private Connection getNewConnection(String username, String password) throws SQLException {
        Connection conn = null;
        if (username == null && password == null) {
            conn = dataSource.getConnection();
        } else {
            conn = dataSource.getConnection(username, password);
        }
        return fixAutoCommit(conn);
    }


    // DataSource methods

    public Connection getConnection() throws SQLException {
        // pass-through
        return getConnection(null, null);
    }

    public Connection getConnection(String username, String password) throws SQLException {
        Connection conn;
        Connection threaded = threadedConnection.get();
        if (threaded == null) {
            conn = createBindWrapConnection(username, password);
        } else {
            boolean closed = false;
            try {
                closed = threaded.isClosed();
            } catch (SQLException e) {
                // stupid thing, we pretend it is closed then since it is invalid
                try {
                    threaded.close();
                } catch (SQLException e1) {
                    // nothing
                }
                closed = true;
            }
            if (closed) {
                // clear the thread-local and make a new connection
                threadedConnection.remove();
                conn = createBindWrapConnection(username, password);
            } else {
                conn = threaded;
            }
        }
        return conn;
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
        return false;
    }
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("Not a Wrapper for " + iface);
    }

}
