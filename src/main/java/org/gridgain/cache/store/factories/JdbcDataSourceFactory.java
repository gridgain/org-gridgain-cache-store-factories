/*
 *  Copyright (C) GridGain Systems. All Rights Reserved.
 *  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.cache.store.factories;

import javax.cache.configuration.Factory;
import javax.sql.DataSource;
import java.util.Map;
import java.util.Objects;

/**
 * Fully serializable {@link DataSource} factory.
 * Unlike {@link org.apache.ignite.cache.store.jdbc.CacheJdbcPojoStoreFactory}, which includes non-serializable
 * (transient) data source configuration, the {@link JdbcDataSourceFactory} has only serializable fields.
 */
public class JdbcDataSourceFactory implements Factory<DataSource> {
    /** Database connection URL. */
    private String url;

    /** Database connection properties. */
    private Map<Object, Object> props;

    /** {@inheritDoc} */
    @Override public DataSource create() {
        return new JdbcDataSource(getUrl(), getProperties());
    }


    /** @return Database connection URL. */
    public String getUrl() {
        return url;
    }

    /** Set database connection URL. */
    public JdbcDataSourceFactory setUrl(String url) {
        this.url = Objects.requireNonNull(url);

        return this;
    }

    /** @return Database connection properties. */
    public Map<Object, Object> getProperties() {
        return props;
    }

    /** Set database connection properties. */
    public JdbcDataSourceFactory setProperties(Map<Object, Object> props) {
        this.props = props;

        return this;
    }
}
