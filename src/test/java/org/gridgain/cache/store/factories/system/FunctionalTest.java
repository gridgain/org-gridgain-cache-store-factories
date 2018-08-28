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
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/** */
public class FunctionalTest {
    /** */
    @Test
    public void clientSideJdbcCacheStoreConfiguration() throws SQLException, IOException {
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
        }
    }

    /** */
    private static void rmTestDb() throws IOException {
        Path dbPath = Paths.get("out", "test.mv.db");
        if (dbPath.toFile().exists())
            Files.delete(dbPath);
    }
}
