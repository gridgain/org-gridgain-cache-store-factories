/*
 *  Copyright (C) GridGain Systems. All Rights Reserved.
 *  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.cache.store.factories.system;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cache.store.jdbc.CacheJdbcPojoStoreFactory;
import org.apache.ignite.cache.store.jdbc.JdbcType;
import org.apache.ignite.cache.store.jdbc.JdbcTypeField;
import org.apache.ignite.cache.store.jdbc.dialect.MySQLDialect;
import org.apache.ignite.configuration.CacheConfiguration;
import org.gridgain.cache.store.factories.JdbcDataSourceFactory;
import org.junit.Test;

import javax.cache.CacheException;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

/** */
public class FunctionalTest {
    /**
     * Data Source factory configuration on the client side to use H2 database as Ignite cache store.
     *
     * The test starts a real H2 server, adds some data and uses a cache already defined in ignite-client.xml with the
     * H2 server URL and properties configured.
     */
    @Test
    public void clientSideH2JdbcCacheStoreConfiguration() throws SQLException, IOException {
        Collection<Person> input = Arrays.asList(new Person(1, "John"), new Person(2, "Mike"));

        rmTestDb();
        org.h2.tools.Server dbSrv = org.h2.tools.Server.createTcpServer("-tcpPort", "19092").start();

        try (Connection dbConn = DriverManager.getConnection("jdbc:h2:./out/test", "sa", "");
             Statement stmt = dbConn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE person(id INT, name VARCHAR, PRIMARY KEY (id))");

            for (Person p : input)
                stmt.executeUpdate(
                    String.format("INSERT INTO person(id, name) VALUES(%s, '%s')", p.getId(), p.getName())
                );

            try (Ignite ignored = Ignition.start("ignite-server.xml");
                 Ignite igniteClient = Ignition.start("ignite-client.xml")) {
                IgniteCache<Integer, Person> cache = igniteClient.cache("person");

                assertEquals(0, cache.size(CachePeekMode.PRIMARY));

                cache.loadCache(null);

                assertEquals(input.size(), cache.size(CachePeekMode.PRIMARY));

                for (Person p : input)
                    assertEquals(p, cache.get(p.getId()));
            }
        }
        finally {
            dbSrv.stop();
            rmTestDb();
        }
    }

    /**
     * Data Source factory configuration on the client side to use MySQL database as Ignite cache store.
     *
     * The test does not start a real server. Thus, the success criteria is a failure and expected
     * {@link ConnectException} as a root cause of the failure.
     */
    @Test(expected = ConnectException.class)
    public void clientSideMySqlJdbcCacheStoreConfiguration() throws Throwable {
        try (Ignite ignored = Ignition.start("ignite-server.xml");
             Ignite igniteClient = Ignition.start("ignite-client.xml")) {
            JdbcTypeField idField = new JdbcTypeField(java.sql.Types.INTEGER, "id", int.class, "id");

            CacheJdbcPojoStoreFactory<Integer, Integer> cacheStoreFactory =
                new CacheJdbcPojoStoreFactory<Integer, Integer>()
                    .setDataSourceFactory(
                        new JdbcDataSourceFactory()
                            .setUrl("jdbc:mysql://localhost:3306/foobar")
                            .setProperties(Stream.of(
                                new SimpleEntry<>("user", "foo"),
                                new SimpleEntry<>("password", "bar")
                            ).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue)))
                    );

            cacheStoreFactory
                .setDialect(new MySQLDialect())
                .setTypes(
                    new JdbcType()
                        .setCacheName("foobar")
                        .setKeyType(int.class)
                        .setValueType(int.class)
                        .setDatabaseSchema("")
                        .setDatabaseTable("foobar")
                        .setKeyFields(idField)
                        .setValueFields(idField)
                );

            IgniteCache<Integer, Integer> foobar = igniteClient.createCache(
                new CacheConfiguration<Integer, Integer>("foobar")
                    .setCacheStoreFactory(cacheStoreFactory)
            );

            try {
                foobar.loadCache(null);
            }
            catch (CacheException ex) {
                Throwable cause;
                Throwable rootCause = ex;

                while (null != (cause = rootCause.getCause()) && (rootCause != cause))
                    rootCause = cause;

                throw rootCause;
            }
        }
    }

    /** */
    private static void rmTestDb() throws IOException {
        Path dbPath = Paths.get("out", "test.mv.db");
        if (dbPath.toFile().exists())
            Files.delete(dbPath);
    }
}
