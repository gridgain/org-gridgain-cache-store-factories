/*
 *  Copyright (C) GridGain Systems. All Rights Reserved.
 *  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.cache.store.factories;

import org.apache.ignite.cache.store.jdbc.CacheJdbcPojoStore;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * This class is based on {@link CacheJdbcPojoStore} but uses database connection URL and properties instead of
 * {@link DataSource} configuration.
 *
 * This allows the {@see JdbcCacheStoreFactory} factory class creating intances of {@link JdbcCacheStore} to be
 * fully serialized, unlike {@link org.apache.ignite.cache.store.jdbc.CacheJdbcPojoStoreFactory} that cannot
 * serialize data source configuration.
 */
public class JdbcCacheStore<K, V> extends CacheJdbcPojoStore<K, V> {
    /** The error indicates data source configuration is unsupported. */
    private static final RuntimeException dataSrcUnsupportedErr = new UnsupportedOperationException(
        JdbcCacheStore.class.getName() + " does not support data source configuration. Configure connection " +
            "URL and properties instead."
    );

    /** Database connection URL. */
    private String url;

    /** Database connection properties. */
    private Map<Object, Object> props;

    /** {@inheritDoc} */
    @Override public DataSource getDataSource() {
        throw dataSrcUnsupportedErr;
    }

    /** {@inheritDoc} */
    @Override public void setDataSource(DataSource dataSrc) {
        throw dataSrcUnsupportedErr;
    }

    /** {@inheritDoc} */
    @Override public void start() {
        if (url == null || url.length() == 0)
            throw new IllegalStateException(
                "Failed to initialize " + JdbcCacheStore.class.getName() + ": URL is not configured."
            );

        if (dialect == null)
            dialect = resolveDialect();
    }

    /** @return Database connection URL. */
    public String getUrl() {
        return url;
    }

    /** Set database connection URL. */
    public JdbcCacheStore<K, V> setUrl(String url) {
        this.url = Objects.requireNonNull(url);

        return this;
    }

    /** @return Database connection properties. */
    public Map<Object, Object> getProperties() {
        return props;
    }

    /** Set database connection properties. */
    public JdbcCacheStore<K, V> setProperties(Map<Object, Object> props) {
        this.props = props;

        return this;
    }

    /** {@inheritDoc} */
    @Override protected Connection openConnection(boolean autocommit) throws SQLException {
        Connection conn;

        if (props == null)
            conn = DriverManager.getConnection(url);
        else {
            Properties p = new Properties();
            p.putAll(props);

            conn = DriverManager.getConnection(url, p);
        }

        conn.setAutoCommit(autocommit);

        return conn;
    }
}
