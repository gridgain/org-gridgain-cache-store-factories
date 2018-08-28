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
import org.apache.ignite.cache.store.jdbc.CacheJdbcPojoStoreFactory;

import javax.cache.configuration.Factory;
import javax.sql.DataSource;
import java.util.Map;
import java.util.Objects;

/**
 * This class is based on {@link CacheJdbcPojoStoreFactory} but uses database connection URL and properties instead of
 * {@link DataSource} configuration.
 *
 * This allows the class to be fully serialized, unlike {@link CacheJdbcPojoStoreFactory} that cannot serialize data
 * source configuration.
 */
public class JdbcCacheStoreFactory<K, V> extends CacheJdbcPojoStoreFactory<K, V> {
    /** The error indicates data source configuration is unsupported. */
    private static final RuntimeException dataSrcUnsupportedErr = new UnsupportedOperationException(
        JdbcCacheStoreFactory.class.getName() + " does not support data source configuration. Configure connection " +
            "URL and properties instead."
    );

    /** Database connection URL. */
    private String url;

    /** Database connection properties. */
    private Map<Object, Object> props;

    /** {@inheritDoc} */
    @Override public CacheJdbcPojoStore<K, V> create() {
        JdbcCacheStore<K, V> store = new JdbcCacheStore<>();

        store.setBatchSize(getBatchSize());
        store.setDialect(getDialect());
        store.setMaximumPoolSize(getMaximumPoolSize());
        store.setMaximumWriteAttempts(getMaximumWriteAttempts());
        store.setParallelLoadCacheMinimumThreshold(getParallelLoadCacheMinimumThreshold());
        store.setTypes(getTypes());
        store.setHasher(getHasher());
        store.setTransformer(getTransformer());
        store.setSqlEscapeAll(isSqlEscapeAll());
        store.setUrl(getUrl());
        store.setProperties(getProperties());

        return store;
    }

    /** {@inheritDoc} */
    @Override public String getDataSourceBean() {
        throw dataSrcUnsupportedErr;
    }

    /** {@inheritDoc} */
    @Override public JdbcCacheStoreFactory<K, V> setDataSourceBean(String dataSrcBean) {
        throw dataSrcUnsupportedErr;
    }

    /** {@inheritDoc} */
    @Override public Factory<DataSource> getDataSourceFactory() {
        throw dataSrcUnsupportedErr;
    }

    /** {@inheritDoc} */
    @Override public JdbcCacheStoreFactory<K, V> setDataSourceFactory(Factory<DataSource> dataSrcFactory) {
        throw dataSrcUnsupportedErr;
    }

    /** @return Database connection URL. */
    public String getUrl() {
        return url;
    }

    /** Set database connection URL. */
    public JdbcCacheStoreFactory<K, V> setUrl(String url) {
        this.url = Objects.requireNonNull(url);

        return this;
    }

    /** @return Database connection properties. */
    public Map<Object, Object> getProperties() {
        return props;
    }

    /** Set database connection properties. */
    public JdbcCacheStoreFactory<K, V> setProperties(Map<Object, Object> props) {
        this.props = props;

        return this;
    }
}

