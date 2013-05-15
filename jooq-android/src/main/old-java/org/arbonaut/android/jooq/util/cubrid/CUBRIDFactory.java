/**
 * Copyright (c) 2009-2012, Lukas Eder, lukas.eder@gmail.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * . Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * . Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * . Neither the name of the "jOOQ" nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.jooq.util.cubrid;

import java.sql.Connection;

import javax.sql.DataSource;


import org.jooq.Field;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.Factory;

/**
 * A {@link SQLDialect#CUBRID} specific factory
 *
 * @author Lukas Eder
 */
public class CUBRIDFactory extends Factory {

    /**
     * Generated UID
     */
    private static final long serialVersionUID = 6530433807914995633L;

    /**
     * Create a factory with connection and a schema mapping configured
     *
     * @param connection The connection to use with objects created from this
     *            factory
     * @param mapping The schema mapping to use with objects created from this
     *            factory
     * @deprecated - 2.0.5 - Use {@link #CUBRIDFactory(Connection, Settings)}
     *             instead
     */
    @Deprecated
    public CUBRIDFactory(Connection connection, org.jooq.SchemaMapping mapping) {
        super(connection, SQLDialect.CUBRID, mapping);
    }

    /**
     * Create a factory with connection and a settings configured
     *
     * @param connection The connection to use with objects created from this
     *            factory
     * @param settings The runtime settings to apply to objects created from
     *            this factory
     */
    public CUBRIDFactory(Connection connection, Settings settings) {
        super(connection, SQLDialect.CUBRID, settings);
    }

    /**
     * Create a factory with a data source and a settings configured
     *
     * @param dataSource The data source to use with objects created from this
     *            factory
     * @param settings The runtime settings to apply to objects created from
     *            this factory
     */
    public CUBRIDFactory(DataSource dataSource, Settings settings) {
        super(dataSource, SQLDialect.CUBRID, settings);
    }

    /**
     * Create a factory with connection
     *
     * @param connection The connection to use with objects created from this
     *            factory
     */
    public CUBRIDFactory(Connection connection) {
        super(connection, SQLDialect.CUBRID);
    }

    /**
     * Create a factory with a data source
     *
     * @param dataSource The data source to use with objects created from this
     *            factory
     */
    public CUBRIDFactory(DataSource dataSource) {
        super(dataSource, SQLDialect.CUBRID);
    }

    /**
     * Create a factory with settings configured
     * <p>
     * Without a connection, this factory cannot execute queries. Use it to
     * render SQL only.
     *
     * @param settings The runtime settings to apply to objects created from
     *            this factory
     */
    public CUBRIDFactory(Settings settings) {
        super(SQLDialect.CUBRID, settings);
    }

    /**
     * Create a connection-less factory
     * <p>
     * Without a connection, this factory cannot execute queries. Use it to
     * render SQL only.
     */
    public CUBRIDFactory() {
        super(SQLDialect.CUBRID);
    }

    // -------------------------------------------------------------------------
    // MySQL-specific functions
    // -------------------------------------------------------------------------

    /**
     * Use the CUBRID-specific <code>INCR()</code> function.
     * <p>
     * This function can be used to increment a field value in a
     * <code>SELECT</code> statement as such: <code><pre>
     * SELECT article, INCR(read_count)
     * FROM article_table
     * WHERE article_id = 130,987</pre></code>
     */
    public static <T> Field<T> incr(Field<T> field) {
        return field("{incr}({0})", field.getDataType(), field);
    }

    /**
     * Use the CUBRID-specific <code>DECR()</code> function.
     * <p>
     * This function can be used to increment a field value in a
     * <code>SELECT</code> statement as such: <code><pre>
     * SELECT article, DECR(read_count)
     * FROM article_table
     * WHERE article_id = 130,987</pre></code>
     */
    public static <T> Field<T> decr(Field<T> field) {
        return field("{decr}({0})", field.getDataType(), field);
    }
}
