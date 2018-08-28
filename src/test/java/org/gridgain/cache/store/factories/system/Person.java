/*
 *  Copyright (C) GridGain Systems. All Rights Reserved.
 *  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.cache.store.factories.system;

import java.util.Objects;

/** */
public class Person {
    /** Id. */
    private int id;

    /** Name. */
    private String name;

    /** */
    public Person(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /** */
    public int getId() {
        return id;
    }

    /** */
    public void setId(int id) {
        this.id = id;
    }

    /** */
    public String getName() {
        return name;
    }

    /** */
    public void setName(String name) {
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        int res = 11;

        res += 31 * res + id;

        if (name != null)
            res += 31 * res + name.hashCode();

        return res;
    }

    /** {@inheritDoc} */
    @Override public boolean equals(Object obj) {
        if (!(obj instanceof Person))
            return false;

        Person other = (Person)obj;

        return id == other.id && Objects.equals(name, other.name);
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return String.format("Person[id: %s, name: %s]", id, name);
    }
}
