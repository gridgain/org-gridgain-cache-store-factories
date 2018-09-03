/*
 *  Copyright (C) GridGain Systems. All Rights Reserved.
 *  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.cache.store.factories;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * {@link DataSource} implementation that uses a mandatory JDBC connection URL and optional connection properties to
 * create connection.
 */
public class JdbcDataSource implements DataSource {
    /** Database connection URL. */
    private final String url;

    /** Database connection properties. */
    private final Map<Object, Object> props;

    /** Login timeout. */
    private int loginTimeout = 0;

    /** Log stream. */
    private transient PrintWriter logWriter = null;

    /** Constructor. */
    public JdbcDataSource(String url, Map<Object, Object> props) {
        this.url = Objects.requireNonNull(url);
        this.props = props;
    }

    /** {@inheritDoc} */
    @Override public Connection getConnection() throws SQLException {
        Connection conn;

        int origLoginTimeout = DriverManager.getLoginTimeout();
        PrintWriter origLogWriter = DriverManager.getLogWriter();
        try {
            if (getLoginTimeout() > 0)
                DriverManager.setLoginTimeout(getLoginTimeout());

            if (getLogWriter() != null)
                DriverManager.setLogWriter(getLogWriter());

            if (props == null)
                conn = DriverManager.getConnection(url);
            else {
                Properties p = new Properties();
                p.putAll(props);

                conn = DriverManager.getConnection(url, p);
            }
        }
        finally {
            DriverManager.setLoginTimeout(origLoginTimeout);
            DriverManager.setLogWriter(origLogWriter);
        }

        return conn;
    }

    /** {@inheritDoc} */
    @Override public Connection getConnection(String user, String pwd) throws SQLException {
        Properties p = new Properties();

        if (props != null)
            p.putAll(props);

        // The implementation assumes user and password connection property names are "user" and "password"
        // correspondingly. This is just an assumption and not part of the JDBC spec although many JDBC drivers do
        // use such names.
        if (user != null)
            p.setProperty("user", user);

        if (pwd != null)
            p.setProperty("password", pwd);

        int origLoginTimeout = DriverManager.getLoginTimeout();
        PrintWriter origLogWriter = DriverManager.getLogWriter();
        try {
            if (getLoginTimeout() > 0)
                DriverManager.setLoginTimeout(getLoginTimeout());

            if (getLogWriter() != null)
                DriverManager.setLogWriter(getLogWriter());
            return DriverManager.getConnection(url, p);
        }
        finally {
            DriverManager.setLoginTimeout(origLoginTimeout);
            DriverManager.setLogWriter(origLogWriter);
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isAssignableFrom(getClass()))
            return (T)this;

        throw new SQLFeatureNotSupportedException();
    }

    /** {@inheritDoc} */
    @Override public boolean isWrapperFor(Class<?> iface) {
        return iface.isInstance(this);
    }

    /** {@inheritDoc} */
    @Override public PrintWriter getLogWriter() {
        return logWriter;
    }

    /** {@inheritDoc} */
    @Override public void setLogWriter(PrintWriter out) {
        logWriter = out;
    }

    /** {@inheritDoc} */
    @Override public void setLoginTimeout(int seconds) {
        loginTimeout = seconds;
    }

    /** {@inheritDoc} */
    @Override public int getLoginTimeout() {
        return loginTimeout;
    }

    /** {@inheritDoc} */
    @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
}
