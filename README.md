# Serializable Ignite CacheStore Factories

Fully serializable Ignite Cache Store factories to allow complete cache store configuration on the client side.

## MOTIVATION

It is often recommended to split Ignite configuration into server-side configuration and multiple client-side 
configurations corresponding to specific client applications. Server side configuration defines cluster-wide
settings like discovery mechanism and storage architecture. Applications define their data models in the client-side 
configuration files. Sometimes such a "best practice" becomes a requirement: for example, consider Ignite or GridGain 
exposed as a cloud service. The cloud users are not supposed to modify modify server configuration to define their 
data models.

Ignite is designed to allow client-side configuration of the application data models. Ignite client configuration files 
include cache configurations defining the data models. When the client starts, the application-specific cache 
configurations are merged with other cache configurations so that every node in the clusters receives consistent 
set of cache definitions. NOTE: [GridGain Security](https://docs.gridgain.com/docs/authorization-and-permissions) can 
be used to configure cache-level permissions.

The described client-side configuration does not work for some 3-rd party persistence configurations. Specifically, the
Ignite CacheStore factories that include [DataSource](https://docs.oracle.com/javase/9/docs/api/javax/sql/DataSource.html)
configuration do not serialize the DataSource configuration, requiring such factories to be configured on the server side. 
The reason for such a design was the DataSource interface is not marked 
[Serialiable](https://docs.oracle.com/javase/9/docs/api/java/io/Serializable.html). 
Ignite marks the data source field as "transient" to avoid serialization exceptions for non-serializable data source 
implementations. 
Specifically, the [org.apache.ignite.cache.store.jdbc.CacheJdbcPojoStoreFactory](https://github.com/apache/ignite/blob/master/modules/core/src/main/java/org/apache/ignite/cache/store/jdbc/CacheJdbcPojoStoreFactory.java)
cannot be configured on the client side.

## SOLUTION

The reason why `CacheJdbcPojoStoreFactory` requires data source configuration is that the factory uses 
[DataSource](https://docs.oracle.com/javase/9/docs/api/javax/sql/DataSource.html) to create 
[Connection](https://docs.oracle.com/javase/9/docs/api/java/sql/Connection.html).

JDBC has another standard mechanism to create a Connection, which is a
[DriverManager#getConnection](https://docs.oracle.com/javase/7/docs/api/java/sql/DriverManager.html#getConnection(java.lang.String,%20java.util.Properties))
method. The method accepts a mandatory database connection URL and optional connection properties.

Also, `CacheJdbcPojoStoreFactory` has a serializable property `dataSourceFactory` to configure a 
[Factory<DataSource>](https://static.javadoc.io/javax.cache/cache-api/1.0.0/javax/cache/configuration/Factory.html) 
instead of the non-serializable `DataSource`.

The solution is new `org.gridgain.cache.store.factories.JdbcDataSourceFactory` and 
`org.gridgain.cache.store.factories.JdbcDataSource` from this repository. The `JdbcDataSource` uses 
`DriverManager#getConnection()` to create connections and the `JdbcDataSourceFactory` creates `JdbcDataSource` using 
the URL and properties specified in the configuration. Both the URL and the properties map are serializable, which 
allows fully configuring `JdbcDataSourceFactory` on the client side.

## EXAMPLE

The below configuration is a part of `org.gridgain.cache.store.factories.system#clientSideH2JdbcCacheStoreConfiguration`
demo proving that the `JdbcDataSourceFactory` can be configured on the client side only.

```xml
<bean class="org.apache.ignite.configuration.CacheConfiguration">
    <property name="name" value="person"/>
    <property name="cacheMode" value="PARTITIONED"/>
    <property name="atomicityMode" value="ATOMIC"/>

    <property name="cacheStoreFactory">
        <bean class="org.apache.ignite.cache.store.jdbc.CacheJdbcPojoStoreFactory">
            <property name="dataSourceFactory">
                <bean class="org.gridgain.cache.store.factories.JdbcDataSourceFactory">
                    <property name="url" value="jdbc:h2:tcp://localhost:19092/./out/test"/>
                    <property name="properties">
                        <util:map>
                            <entry key="user" value="sa"/>
                            <entry key="password" value=""/>
                        </util:map>
                    </property>
                </bean>
            </property>
            <property name="dialect">
                <bean class="org.apache.ignite.cache.store.jdbc.dialect.H2Dialect"/>
            </property>

            <property name="types">
                <list>
                    <bean class="org.apache.ignite.cache.store.jdbc.JdbcType">
                        <property name="cacheName" value="person"/>
                        <property name="keyType" value="java.lang.Integer"/>
                        <property name="valueType"
                                  value="org.gridgain.cache.store.factories.system.Person"/>
                        <property name="databaseSchema" value=""/>
                        <property name="databaseTable" value="person"/>

                        <property name="keyFields">
                            <list>
                                <bean class="org.apache.ignite.cache.store.jdbc.JdbcTypeField">
                                    <constructor-arg>
                                        <util:constant static-field="java.sql.Types.INTEGER"/>
                                    </constructor-arg>
                                    <constructor-arg value="id"/>
                                    <constructor-arg value="int"/>
                                    <constructor-arg value="id"/>
                                </bean>
                            </list>
                        </property>

                        <property name="valueFields">
                            <list>
                                <bean class="org.apache.ignite.cache.store.jdbc.JdbcTypeField">
                                    <constructor-arg>
                                        <util:constant static-field="java.sql.Types.INTEGER"/>
                                    </constructor-arg>
                                    <constructor-arg value="id"/>
                                    <constructor-arg value="int"/>
                                    <constructor-arg value="id"/>
                                </bean>

                                <bean class="org.apache.ignite.cache.store.jdbc.JdbcTypeField">
                                    <constructor-arg>
                                        <util:constant static-field="java.sql.Types.VARCHAR"/>
                                    </constructor-arg>
                                    <constructor-arg value="name"/>
                                    <constructor-arg value="java.lang.String"/>
                                    <constructor-arg value="name"/>
                                </bean>
                            </list>
                        </property>
                    </bean>
                </list>
            </property>
        </bean>
    </property>

    <property name="readThrough" value="true"/>
    <property name="writeThrough" value="true"/>
</bean>
``` 

## INSTALLATION

1. Clone the repository
2. In build.gradle set `igniteVersion` or `gridgainVersion` to match the Ignite or GridGain version you use.
3. Build the project. By default it builds for Ignite. Add `-PbuildProfile=gridgain` to the gradle command line to build 
   the project for GridGain.
4. Deploy the project JAR to all server nodes and add it as a runtime Maven dependency to your client application. 
