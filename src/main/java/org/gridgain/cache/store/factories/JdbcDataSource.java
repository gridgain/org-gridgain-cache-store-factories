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
    private String url;

    /** Database connection properties. */
    private Map<Object, Object> props;

    /** Database user name. */
    private String username;

    /** Database user password. */
    private String pwd;

    /** Database catalog. */
    private String catalog;

    /** Database schema. */
    private String schema;

    /** Login timeout. */
    private int loginTimeout = 0;

    /** Log stream. */
    private transient PrintWriter logWriter = null;

    /** Constructor. */
    public JdbcDataSource() {
    }

    /** Constructor. */
    public JdbcDataSource(String url, Map<Object, Object> props) {
        this.url = Objects.requireNonNull(url);
        this.props = props;
    }

    /** Constructor. */
    public JdbcDataSource(String url, String username, String pwd) {
        this.url = Objects.requireNonNull(url);
        this.username = username;
        this.pwd = pwd;
    }

    /** {@inheritDoc} */
    @Override public Connection getConnection() throws SQLException {
        return getConnection(getUsername(), getPassword());
    }

    /** {@inheritDoc} */
    @Override public Connection getConnection(String username, String pwd) throws SQLException {
        int origLoginTimeout = DriverManager.getLoginTimeout();
        PrintWriter origLogWriter = DriverManager.getLogWriter();
        try {
            if (getLoginTimeout() > 0)
                DriverManager.setLoginTimeout(getLoginTimeout());

            if (getLogWriter() != null)
                DriverManager.setLogWriter(getLogWriter());

            Properties p = new Properties();

            if (username != null)
                p.setProperty("user", username);

            if (pwd != null)
                p.setProperty("password", pwd);

            if (getProperties() != null)
                p.putAll(getProperties());

            Connection conn = DriverManager.getConnection(getUrl(), p);

            if (getCatalog() != null)
                conn.setCatalog(getCatalog());

            if (getSchema() != null)
                conn.setSchema(getSchema());

            return conn;
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

    /** @return Database connection URL. */
    public String getUrl() {
        return url;
    }

    /** Set database connection URL. */
    public JdbcDataSource setUrl(String url) {
        this.url = Objects.requireNonNull(url);

        return this;
    }

    /** @return Database connection properties. */
    public Map<Object, Object> getProperties() {
        return props;
    }

    /** Set database connection properties. */
    public JdbcDataSource setProperties(Map<Object, Object> props) {
        this.props = props;

        return this;
    }

    /** @return Database catalog. */
    public String getCatalog() {
        return catalog;
    }

    /** Set database catalog. */
    public JdbcDataSource setCatalog(String catalog) {
        this.catalog = catalog;

        return this;
    }

    /** @return Database schema. */
    public String getSchema() {
        return schema;
    }

    /** Set database schema. */
    public JdbcDataSource setSchema(String schema) {
        this.schema = schema;

        return this;
    }

    /** @return Database user name. */
    public String getUsername() {
        return username;
    }

    /** Set database user name. */
    public JdbcDataSource setUsername(String username) {
        this.username = username;

        return this;
    }

    /** @return Database user password. */
    public String getPassword() {
        return pwd;
    }

    /** Set database user password. */
    public JdbcDataSource setPassword(String pwd) {
        this.pwd = pwd;

        return this;
    }
}
