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

    /** Database user name. */
    private String username;

    /** Database user password. */
    private String pwd;

    /** Database catalog. */
    private String catalog;

    /** Database schema. */
    private String schema;

    /** Login timeout. */
    private int loginTimeout;

    /** {@inheritDoc} */
    @Override public DataSource create() {
        JdbcDataSource res = new JdbcDataSource()
            .setUrl(getUrl())
            .setUsername(getUsername())
            .setPassword(getPassword())
            .setCatalog(getCatalog())
            .setSchema(getSchema())
            .setProperties(getProperties());

        res.setLoginTimeout(getLoginTimeout());

        return res;
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

    /** @return Database catalog. */
    public String getCatalog() {
        return catalog;
    }

    /** Set database catalog. */
    public JdbcDataSourceFactory setCatalog(String catalog) {
        this.catalog = catalog;

        return this;
    }

    /** @return Database schema. */
    public String getSchema() {
        return schema;
    }

    /** Set database schema. */
    public JdbcDataSourceFactory setSchema(String schema) {
        this.schema = schema;

        return this;
    }

    /** @return Database user name. */
    public String getUsername() {
        return username;
    }

    /** Set database user name. */
    public JdbcDataSourceFactory setUsername(String username) {
        this.username = username;

        return this;
    }

    /** @return Database user password. */
    public String getPassword() {
        return pwd;
    }

    /** Set database user password. */
    public JdbcDataSourceFactory setPassword(String pwd) {
        this.pwd = pwd;

        return this;
    }

    /** @return Login timeout. */
    public int getLoginTimeout() {
        return loginTimeout;
    }

    /** Set login timeout. */
    public JdbcDataSourceFactory setLoginTimeout(int loginTimeout) {
        this.loginTimeout = loginTimeout;

        return this;
    }
}
