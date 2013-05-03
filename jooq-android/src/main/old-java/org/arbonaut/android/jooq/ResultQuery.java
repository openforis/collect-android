/**
 * Copyright (c) 2009-2012, Lukas Eder, lukas.eder@gmail.com
 * All rights reserved.
 *
 * This software is licensed to you under the Apache License, Version 2.0
 * (the "License"); You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
 * . Neither the name "jOOQ" nor the names of its contributors may be
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

package org.jooq;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;


import org.jooq.conf.Settings;
import org.jooq.exception.DataAccessException;
import org.jooq.exception.DataTypeException;
import org.jooq.exception.InvalidResultException;
import org.jooq.exception.MappingException;

/**
 * A query that can return results. Mostly, this is a {@link Select} query used
 * for a <code>SELECT</code> statement.
 * <p>
 * However, some RDBMS also allow for other constructs, such as Postgres'
 * <code>FETCH ALL IN {cursor-name}</code>. The easiest way to execute such a
 * query is by using <code><pre>
 * Factory create = new PostgresFactory(connection);
 * Result&lt;Record&gt; result = create.fetch("FETCH ALL IN \"&lt;unnamed cursor 1&gt;\"");
 * </pre></code> Another example (for SQLite): <code><pre>
 * Result&lt;Record&gt; result = create.fetch("pragma table_info('my_table')");
 * </pre></code>
 *
 * @author Lukas Eder
 */
public interface ResultQuery<R extends Record> extends Query {

    /**
     * Return the result generated by a previous call to execute();
     *
     * @return The result or <code>null</code> if no call to execute() was done
     *         previously.
     */
    Result<R> getResult();

    /**
     * Execute the query and return the generated result
     * <p>
     * This is the same as calling {@link #execute()} and then
     * {@link #getResult()}
     * <p>
     * The result and its contained records are attached to the original
     * {@link Configuration} by default. Use {@link Settings#isAttachRecords()}
     * to override this behaviour.
     *
     * @return The result.
     * @throws DataAccessException if something went wrong executing the query
     */
    Result<R> fetch() throws DataAccessException;

    /**
     * Execute the query and return the generated result as a JDBC
     * {@link ResultSet}
     * <p>
     * This will return the {@link ResultSet} returned by the JDBC driver,
     * leaving it untouched. Use this method when you want to use jOOQ for query
     * execution, but not for result fetching.
     * <p>
     * The returned <code>ResultSet</code> can be used with
     * {@link FactoryOperations#fetch(ResultSet)}
     *
     * @return The result.
     * @throws DataAccessException if something went wrong executing the query
     */
    ResultSet fetchResultSet() throws DataAccessException;

    /**
     * Execute the query and "lazily" return the generated result
     * <p>
     * The returned {@link Cursor} holds a reference to the executed
     * {@link PreparedStatement} and the associated {@link ResultSet}. Data can
     * be fetched (or iterated over) lazily, fetching records from the
     * {@link ResultSet} one by one.
     * <p>
     * Depending on your JDBC driver's default behaviour, this may load the
     * whole database result into the driver's memory. In order to indicate to
     * the driver that you may not want to fetch all records at once, use
     * {@link #fetchLazy(int)}
     * <p>
     * Client code is responsible for closing the cursor after use.
     *
     * @return The resulting cursor.
     * @throws DataAccessException if something went wrong executing the query
     * @see #fetchLazy(int)
     */
    Cursor<R> fetchLazy() throws DataAccessException;

    /**
     * Execute the query and "lazily" return the generated result
     * <p>
     * The returned {@link Cursor} holds a reference to the executed
     * {@link PreparedStatement} and the associated {@link ResultSet}. Data can
     * be fetched (or iterated over) lazily, fetching records from the
     * {@link ResultSet} one by one.
     * <p>
     * Depending on your JDBC driver's behaviour, this will load only
     * <code>fetchSize</code> records from the database into memory at once. For
     * more details, see also {@link Statement#setFetchSize(int)}
     * <p>
     * Client code is responsible for closing the cursor after use.
     *
     * @return The resulting cursor.
     * @throws DataAccessException if something went wrong executing the query
     * @see #fetchLazy()
     * @see Statement#setFetchSize(int)
     */
    Cursor<R> fetchLazy(int fetchSize) throws DataAccessException;

    /**
     * Execute a query, possibly returning several result sets.
     * <p>
     * Example (Sybase ASE):
     * <p>
     * <code><pre>
     * String sql = "sp_help 'my_table'";</pre></code>
     * <p>
     * The result and its contained records are attached to the original
     * {@link Configuration} by default. Use {@link Settings#isAttachRecords()}
     * to override this behaviour.
     *
     * @return The resulting records
     * @throws DataAccessException if something went wrong executing the query
     */
    List<Result<Record>> fetchMany() throws DataAccessException;

    /**
     * Execute the query and return all values for a field from the generated
     * result.
     * <p>
     * This is the same as calling {@link #fetch()} and then
     * {@link Result#getValues(Field)}
     *
     * @return The resulting values.
     * @throws DataAccessException if something went wrong executing the query
     */
    <T> List<T> fetch(Field<T> field) throws DataAccessException;

    /**
     * Execute the query and return all values for a field from the generated
     * result.
     * <p>
     * This is the same as calling {@link #fetch()} and then
     * {@link Result#getValues(Field, Class)}
     *
     * @return The resulting values.
     * @throws DataAccessException if something went wrong executing the query
     * @see Record#getValue(Field, Class)
     */
    <T> List<T> fetch(Field<?> field, Class<? extends T> type) throws DataAccessException;

    /**
     * Execute the query and return all values for a field from the generated
     * result.
     * <p>
     * This is the same as calling {@link #fetch()} and then
     * {@link Result#getValues(Field, Converter)}
     *
     * @return The resulting values.
     * @throws DataAccessException if something went wrong executing the query
     * @see Record#getValue(Field, Converter)
     */
    <T, U> List<U> fetch(Field<T> field, Converter<? super T, U> converter) throws DataAccessException;

    /**
     * Execute the query and return all values for a field index from the
     * generated result.
     * <p>
     * This is the same as calling {@link #fetch()} and then
     * {@link Result#getValues(int)}
     *
     * @return The resulting values.
     * @throws DataAccessException if something went wrong executing the query
     */
    List<?> fetch(int fieldIndex) throws DataAccessException;

    /**
     * Execute the query and return all values for a field index from the
     * generated result.
     * <p>
     * This is the same as calling {@link #fetch()} and then
     * {@link Result#getValues(int, Class)}
     *
     * @return The resulting values.
     * @throws DataAccessException if something went wrong executing the query
     * @see Record#getValue(int, Class)
     */
    <T> List<T> fetch(int fieldIndex, Class<? extends T> type) throws DataAccessException;

    /**
     * Execute the query and return all values for a field index from the
     * generated result.
     * <p>
     * This is the same as calling {@link #fetch()} and then
     * {@link Result#getValues(int, Converter)}
     *
     * @return The resulting values.
     * @throws DataAccessException if something went wrong executing the query
     * @see Record#getValue(int, Converter)
     */
    <U> List<U> fetch(int fieldIndex, Converter<?, U> converter) throws DataAccessException;

    /**
     * Execute the query and return all values for a field name from the
     * generated result.
     * <p>
     * This is the same as calling {@link #fetch()} and then
     * {@link Result#getValues(String)}
     *
     * @return The resulting values.
     * @throws DataAccessException if something went wrong executing the query
     */
    List<?> fetch(String fieldName) throws DataAccessException;

    /**
     * Execute the query and return all values for a field name from the
     * generated result.
     * <p>
     * This is the same as calling {@link #fetch()} and then
     * {@link Result#getValues(String, Class)}
     *
     * @return The resulting values.
     * @throws DataAccessException if something went wrong executing the query
     * @see Record#getValue(String, Class)
     */
    <T> List<T> fetch(String fieldName, Class<? extends T> type) throws DataAccessException;

    /**
     * Execute the query and return all values for a field name from the
     * generated result.
     * <p>
     * This is the same as calling {@link #fetch()} and then
     * {@link Result#getValues(String, Converter)}
     *
     * @return The resulting values.
     * @throws DataAccessException if something went wrong executing the query
     * @see Record#getValue(String, Converter)
     */
    <U> List<U> fetch(String fieldName, Converter<?, U> converter) throws DataAccessException;

    /**
     * Execute the query and return return at most one resulting value for a
     * field from the generated result.
     * <p>
     * This is the same as calling {@link #fetchOne()} and then
     * {@link Record#getValue(Field)}
     *
     * @return The resulting value or <code>null</code> if the query returned no
     *         records.
     * @throws DataAccessException if something went wrong executing the query
     * @throws InvalidResultException if the query returned more than one record
     */
    <T> T fetchOne(Field<T> field) throws DataAccessException;

    /**
     * Execute the query and return return at most one resulting value for a
     * field from the generated result.
     * <p>
     * This is the same as calling {@link #fetchOne()} and then
     * {@link Record#getValue(Field, Class)}
     *
     * @return The resulting value or <code>null</code> if the query returned no
     *         records.
     * @throws DataAccessException if something went wrong executing the query
     * @throws InvalidResultException if the query returned more than one record
     */
    <T> T fetchOne(Field<?> field, Class<? extends T> type) throws DataAccessException;

    /**
     * Execute the query and return return at most one resulting value for a
     * field from the generated result.
     * <p>
     * This is the same as calling {@link #fetchOne()} and then
     * {@link Record#getValue(Field, Converter)}
     *
     * @return The resulting value or <code>null</code> if the query returned no
     *         records.
     * @throws DataAccessException if something went wrong executing the query
     * @throws InvalidResultException if the query returned more than one record
     */
    <T, U> U fetchOne(Field<T> field, Converter<? super T, U> converter) throws DataAccessException;

    /**
     * Execute the query and return return at most one resulting value for a
     * field index from the generated result.
     * <p>
     * This is the same as calling {@link #fetchOne()} and then
     * {@link Record#getValue(int)}
     *
     * @return The resulting value or <code>null</code> if the query returned no
     *         records.
     * @throws DataAccessException if something went wrong executing the query
     * @throws InvalidResultException if the query returned more than one record
     */
    Object fetchOne(int fieldIndex) throws DataAccessException;

    /**
     * Execute the query and return return at most one resulting value for a
     * field index from the generated result.
     * <p>
     * This is the same as calling {@link #fetchOne()} and then
     * {@link Record#getValue(int, Class)}
     *
     * @return The resulting value or <code>null</code> if the query returned no
     *         records.
     * @throws DataAccessException if something went wrong executing the query
     * @throws InvalidResultException if the query returned more than one record
     */
    <T> T fetchOne(int fieldIndex, Class<? extends T> type) throws DataAccessException;

    /**
     * Execute the query and return return at most one resulting value for a
     * field index from the generated result.
     * <p>
     * This is the same as calling {@link #fetchOne()} and then
     * {@link Record#getValue(int, Converter)}
     *
     * @return The resulting value or <code>null</code> if the query returned no
     *         records.
     * @throws DataAccessException if something went wrong executing the query
     * @throws InvalidResultException if the query returned more than one record
     */
    <U> U fetchOne(int fieldIndex, Converter<?, U> converter) throws DataAccessException;

    /**
     * Execute the query and return return at most one resulting value for a
     * field name from the generated result.
     * <p>
     * This is the same as calling {@link #fetchOne()} and then
     * {@link Record#getValue(int)}
     *
     * @return The resulting value or <code>null</code> if the query returned no
     *         records.
     * @throws DataAccessException if something went wrong executing the query
     * @throws InvalidResultException if the query returned more than one record
     */
    Object fetchOne(String fieldName) throws DataAccessException;

    /**
     * Execute the query and return return at most one resulting value for a
     * field name from the generated result.
     * <p>
     * This is the same as calling {@link #fetchOne()} and then
     * {@link Record#getValue(String, Class)}
     *
     * @return The resulting value or <code>null</code> if the query returned no
     *         records.
     * @throws DataAccessException if something went wrong executing the query
     * @throws InvalidResultException if the query returned more than one record
     */
    <T> T fetchOne(String fieldName, Class<? extends T> type) throws DataAccessException;

    /**
     * Execute the query and return return at most one resulting value for a
     * field name from the generated result.
     * <p>
     * This is the same as calling {@link #fetchOne()} and then
     * {@link Record#getValue(String, Converter)}
     *
     * @return The resulting value or <code>null</code> if the query returned no
     *         records.
     * @throws DataAccessException if something went wrong executing the query
     * @throws InvalidResultException if the query returned more than one record
     */
    <U> U fetchOne(String fieldName, Converter<?, U> converter) throws DataAccessException;

    /**
     * Execute the query and return at most one resulting record.
     * <p>
     * The resulting record is attached to the original {@link Configuration} by
     * default. Use {@link Settings#isAttachRecords()} to override this
     * behaviour.
     *
     * @return The resulting record or <code>null</code> if the query returns no
     *         records.
     * @throws DataAccessException if something went wrong executing the query
     * @throws InvalidResultException if the query returned more than one record
     */
    R fetchOne() throws DataAccessException;

    /**
     * Execute the query and return at most one resulting record.
     * <p>
     * The resulting record is attached to the original {@link Configuration} by
     * default. Use {@link Settings#isAttachRecords()} to override this
     * behaviour.
     *
     * @return The first resulting record or <code>null</code> if the query
     *         returns no records.
     * @throws DataAccessException if something went wrong executing the query
     */
    R fetchAny() throws DataAccessException;

    /**
     * Execute the query and return the generated result as a list of name/value
     * maps.
     *
     * @return The result.
     * @throws DataAccessException if something went wrong executing the query
     * @throws InvalidResultException if the key field returned two or more
     *             equal values from the result set.
     * @see Result#intoMaps()
     * @see Record#intoMap()
     */
    List<Map<String, Object>> fetchMaps() throws DataAccessException;

    /**
     * Execute the query and return at most one resulting record as a name/value
     * map.
     *
     * @return The resulting record or <code>null</code> if the query returns no
     *         records.
     * @throws DataAccessException if something went wrong executing the query
     * @throws InvalidResultException if the query returned more than one record
     * @see Result#intoMaps()
     * @see Record#intoMap()
     */
    Map<String, Object> fetchOneMap() throws DataAccessException;

    /**
     * Execute the query and return a {@link Map} with one of the result's
     * columns as key and the corresponding records as value.
     * <p>
     * An exception is thrown, if the key turns out to be non-unique in the
     * result set. Use {@link #fetchGroups(Field)} instead, if your keys are
     * non-unique
     * <p>
     * The resulting records are attached to the original {@link Configuration}
     * by default. Use {@link Settings#isAttachRecords()} to override this
     * behaviour.
     *
     * @param <K> The key's generic field type
     * @param key The key field. Client code must assure that this field is
     *            unique in the result set.
     * @return A Map containing the results
     * @throws DataAccessException if something went wrong executing the query
     * @throws InvalidResultException if the key field returned two or more
     *             equal values from the result set.
     * @see Result#intoMap(Field)
     */
    <K> Map<K, R> fetchMap(Field<K> key) throws DataAccessException;

    /**
     * Execute the query and return a {@link Map} with one of the result's
     * columns as key and another one of the result's columns as value
     * <p>
     * An exception is thrown, if the key turns out to be non-unique in the
     * result set. Use {@link #fetchGroups(Field, Field)} instead, if your keys
     * are non-unique
     *
     * @param <K> The key's generic field type
     * @param <V> The value's generic field type
     * @param key The key field. Client code must assure that this field is
     *            unique in the result set.
     * @param value The value field
     * @return A Map containing the results
     * @throws DataAccessException if something went wrong executing the query
     * @throws InvalidResultException if the key field returned two or more
     *             equal values from the result set.
     * @see Result#intoMap(Field, Field)
     */
    <K, V> Map<K, V> fetchMap(Field<K> key, Field<V> value) throws DataAccessException;

    /**
     * Execute the query and return a {@link Map} with keys as a map key and the
     * corresponding record as value.
     * <p>
     * An exception is thrown, if the keys turn out to be non-unique in the
     * result set. Use {@link #fetchGroups(Field[])} instead, if your keys are
     * non-unique.
     *
     * @param keys The keys. Client code must assure that keys are unique in the
     *            result set.
     * @return A Map containing the results.
     * @throws DataAccessException if something went wrong executing the query
     * @throws InvalidResultException if the key list is non-unique in the
     *             result set.
     * @see Result#intoMap(Field[])
     */
    Map<Record, R> fetchMap(Field<?>[] keys) throws DataAccessException;

    /**
     * Execute the query and return a {@link Map} with results grouped by the
     * given keys and mapped into the given entity type.
     * <p>
     * An {@link InvalidResultException} is thrown, if the keys are non-unique
     * in the result set. Use {@link #fetchGroups(Field[], Class)} instead, if
     * your keys are non-unique.
     *
     * @param keys The keys. Client code must assure that keys are unique in the
     *            result set. If this is <code>null</code> or an empty array,
     *            the resulting map will contain at most one entry.
     * @return A Map containing the results.
     * @throws DataAccessException if something went wrong executing the query
     * @throws InvalidResultException if the keys are non-unique in the result
     *             set.
     * @throws MappingException wrapping any reflection or data type conversion
     *             exception that might have occurred while mapping records
     * @see Result#intoMap(Field[], Class)
     */
    <E> Map<List<?>, E> fetchMap(Field<?>[] keys, Class<? extends E> type) throws MappingException;

    /**
     * Execute the query and return a {@link Map} with results grouped by the
     * given key and mapped into the given entity type.
     * <p>
     * An exception is thrown, if the key turn out to be non-unique in the
     * result set. Use {@link #fetchGroups(Field, Class)} instead, if your key
     * is non-unique.
     *
     * @param key The key. Client code must assure that key is unique in the
     *            result set.
     * @return A Map containing the result.
     * @throws DataAccessException if something went wrong executing the query
     * @throws InvalidResultException if the key is non-unique in the result
     *             set.
     * @see Result#intoMap(Field, Class)
     */
    <K, E> Map<K, E> fetchMap(Field<K> key, Class<? extends E> type) throws DataAccessException;

    /**
     * Execute the query and return a {@link Map} with one of the result's
     * columns as key and a list of corresponding records as value.
     * <p>
     * Unlike {@link #fetchMap(Field)}, this method allows for non-unique keys
     * in the result set.
     * <p>
     * The resulting records are attached to the original {@link Configuration}
     * by default. Use {@link Settings#isAttachRecords()} to override this
     * behaviour.
     *
     * @param <K> The key's generic field type
     * @param key The key field.
     * @return A Map containing the results
     * @throws DataAccessException if something went wrong executing the query
     * @see Result#intoGroups(Field)
     */
    <K> Map<K, Result<R>> fetchGroups(Field<K> key) throws DataAccessException;

    /**
     * Execute the query and return a {@link Map} with one of the result's
     * columns as key and another one of the result's columns as value
     * <p>
     * Unlike {@link #fetchMap(Field, Field)}, this method allows for non-unique
     * keys in the result set.
     *
     * @param <K> The key's generic field type
     * @param <V> The value's generic field type
     * @param key The key field.
     * @param value The value field
     * @return A Map containing the results
     * @throws DataAccessException if something went wrong executing the query
     * @see Result#intoGroups(Field, Field)
     */
    <K, V> Map<K, List<V>> fetchGroups(Field<K> key, Field<V> value) throws DataAccessException;

    /**
     * Execute the query and return a {@link Map} with the result grouped by the
     * given keys.
     * <p>
     * Unlike {@link #fetchMap(Field[])}, this method allows for non-unique keys
     * in the result set.
     *
     * @param keys The keys used for result grouping.
     * @return A Map containing grouped results
     * @throws DataAccessException if something went wrong executing the query
     * @see Result#intoGroups(Field[])
     */
    Map<Record, Result<R>> fetchGroups(Field<?>[] keys) throws DataAccessException;

    /**
     * Execute the query and return a {@link Map} with results grouped by the
     * given keys and mapped into the given entity type.
     * <p>
     * Unlike {@link #fetchMap(Field[], Class)}, this method allows for
     * non-unique keys in the result set.
     *
     * @param keys The keys. If this is <code>null</code> or an empty array, the
     *            resulting map will contain at most one entry.
     * @return A Map containing grouped results
     * @throws DataAccessException if something went wrong executing the query
     * @throws MappingException wrapping any reflection or data type conversion
     *             exception that might have occurred while mapping records
     * @see Result#intoGroups(Field[], Class)
     */
    <E> Map<Record, List<E>> fetchGroups(Field<?>[] keys, Class<? extends E> type) throws MappingException;

    /**
     * Return a {@link Map} with results grouped by the given key and mapped
     * into the given entity type.
     *
     * @param <K> The key's generic field type
     * @param <E> The generic entity type.
     * @param key The key field.
     * @param type The entity type.
     * @throws DataAccessException if something went wrong executing the query
     * @throws MappingException wrapping any reflection or data type conversion
     *             exception that might have occurred while mapping records
     * @see Result#intoGroups(Field, Class)
     */
    <K, E> Map<K, List<E>> fetchGroups(Field<K> key, Class<? extends E> type) throws DataAccessException,
        MappingException;

    /**
     * Execute the query and return the generated result as an Object matrix
     * <p>
     * You can access data like this
     * <code><pre>query.fetchArray()[recordIndex][fieldIndex]</pre></code>
     *
     * @return The result.
     * @throws DataAccessException if something went wrong executing the query
     * @see Result#intoArray()
     */
    Object[][] fetchArrays() throws DataAccessException;

    /**
     * Execute the query and return all values for a field index from the
     * generated result.
     * <p>
     * You can access data like this
     * <code><pre>query.fetchArray(fieldIndex)[recordIndex]</pre></code>
     *
     * @return The resulting values. This may be an array type more concrete
     *         than <code>Object[]</code>, depending on whether jOOQ has any
     *         knowledge about <code>fieldIndex</code>'s actual type.
     * @throws DataAccessException if something went wrong executing the query
     * @see Result#intoArray(int)
     */
    Object[] fetchArray(int fieldIndex) throws DataAccessException;

    /**
     * Execute the query and return all values for a field index from the
     * generated result.
     * <p>
     * You can access data like this
     * <code><pre>query.fetchArray(fieldIndex)[recordIndex]</pre></code>
     *
     * @return The resulting values.
     * @throws DataAccessException if something went wrong executing the query
     * @see Result#intoArray(int, Class)
     */
    <T> T[] fetchArray(int fieldIndex, Class<? extends T> type) throws DataAccessException;

    /**
     * Execute the query and return all values for a field index from the
     * generated result.
     * <p>
     * You can access data like this
     * <code><pre>query.fetchArray(fieldIndex)[recordIndex]</pre></code>
     *
     * @return The resulting values.
     * @throws DataAccessException if something went wrong executing the query
     * @see Result#intoArray(int, Converter)
     */
    <U> U[] fetchArray(int fieldIndex, Converter<?, U> converter) throws DataAccessException;

    /**
     * Execute the query and return all values for a field name from the
     * generated result.
     * <p>
     * You can access data like this
     * <code><pre>query.fetchArray(fieldName)[recordIndex]</pre></code>
     *
     * @return The resulting values. This may be an array type more concrete
     *         than <code>Object[]</code>, depending on whether jOOQ has any
     *         knowledge about <code>fieldName</code>'s actual type.
     * @throws DataAccessException if something went wrong executing the query
     * @see Result#intoArray(String)
     */
    Object[] fetchArray(String fieldName) throws DataAccessException;

    /**
     * Execute the query and return all values for a field name from the
     * generated result.
     * <p>
     * You can access data like this
     * <code><pre>query.fetchArray(fieldName)[recordIndex]</pre></code>
     *
     * @return The resulting values.
     * @throws DataAccessException if something went wrong executing the query
     * @see Result#intoArray(String, Converter)
     */
    <T> T[] fetchArray(String fieldName, Class<? extends T> type) throws DataAccessException;

    /**
     * Execute the query and return all values for a field name from the
     * generated result.
     * <p>
     * You can access data like this
     * <code><pre>query.fetchArray(fieldName)[recordIndex]</pre></code>
     *
     * @return The resulting values.
     * @throws DataAccessException if something went wrong executing the query
     * @see Result#intoArray(String, Class)
     */
    <U> U[] fetchArray(String fieldName, Converter<?, U> converter) throws DataAccessException;

    /**
     * Execute the query and return all values for a field from the generated
     * result.
     * <p>
     * You can access data like this
     * <code><pre>query.fetchArray(field)[recordIndex]</pre></code>
     *
     * @return The resulting values.
     * @throws DataAccessException if something went wrong executing the query
     * @see Result#intoArray(Field)
     */
    <T> T[] fetchArray(Field<T> field) throws DataAccessException;

    /**
     * Execute the query and return all values for a field from the generated
     * result.
     * <p>
     * You can access data like this
     * <code><pre>query.fetchArray(field)[recordIndex]</pre></code>
     *
     * @return The resulting values.
     * @throws DataAccessException if something went wrong executing the query
     * @see Result#intoArray(Field, Class)
     */
    <T> T[] fetchArray(Field<?> field, Class<? extends T> type) throws DataAccessException;

    /**
     * Execute the query and return all values for a field from the generated
     * result.
     * <p>
     * You can access data like this
     * <code><pre>query.fetchArray(field)[recordIndex]</pre></code>
     *
     * @return The resulting values.
     * @throws DataAccessException if something went wrong executing the query
     * @see Result#intoArray(Field, Converter)
     */
    <T, U> U[] fetchArray(Field<T> field, Converter<? super T, U> converter) throws DataAccessException;

    /**
     * Execute the query and return at most one resulting record as an array
     * <p>
     * You can access data like this
     * <code><pre>query.fetchOneArray()[fieldIndex]</pre></code>
     *
     * @return The resulting record or <code>null</code> if the query returns no
     *         records.
     * @throws DataAccessException if something went wrong executing the query
     * @throws InvalidResultException if the query returned more than one record
     */
    Object[] fetchOneArray() throws DataAccessException;

    /**
     * Map resulting records onto a custom type.
     * <p>
     * This is the same as calling <code>fetch().into(type)</code>. See
     * {@link Record#into(Class)} for more details
     *
     * @param <E> The generic entity type.
     * @param type The entity type.
     * @see Record#into(Class)
     * @see Result#into(Class)
     * @throws DataAccessException if something went wrong executing the query
     * @throws MappingException wrapping any reflection or data type conversion
     *             exception that might have occurred while mapping records
     */
    <E> List<E> fetchInto(Class<? extends E> type) throws DataAccessException, MappingException;

    /**
     * Map resulting records onto a custom record.
     * <p>
     * This is the same as calling <code>fetch().into(table)</code>. See
     * {@link Record#into(Table)} for more details
     * <p>
     * The result and its contained records are attached to the original
     * {@link Configuration} by default. Use {@link Settings#isAttachRecords()}
     * to override this behaviour.
     *
     * @param <Z> The generic table record type.
     * @param table The table type.
     * @see Record#into(Table)
     * @see Result#into(Table)
     * @throws DataAccessException if something went wrong executing the query
     */
    <Z extends Record> Result<Z> fetchInto(Table<Z> table) throws DataAccessException;

    /**
     * Fetch results into a custom handler callback
     * <p>
     * The resulting records are attached to the original {@link Configuration}
     * by default. Use {@link Settings#isAttachRecords()} to override this
     * behaviour.
     *
     * @param handler The handler callback
     * @return Convenience result, returning the parameter handler itself
     * @throws DataAccessException if something went wrong executing the query
     */
    <H extends RecordHandler<R>> H fetchInto(H handler) throws DataAccessException;

    /**
     * Fetch results into a custom mapper callback
     *
     * @param mapper The mapper callback
     * @return The custom mapped records
     * @throws DataAccessException if something went wrong executing the query
     */
    <E> List<E> fetch(RecordMapper<? super R, E> mapper) throws DataAccessException;

    /**
     * Fetch results asynchronously.
     * <p>
     * This method wraps fetching of records in a
     * {@link java.util.concurrent.Future}, such that you can access the actual
     * records at a future instant. This is especially useful when
     * <ul>
     * <li>You want to load heavy data in the background, for instance when the
     * user logs in and accesses a pre-calculated dashboard screen, before they
     * access the heavy data.</li>
     * <li>You want to parallelise several independent OLAP queries before
     * merging all data into a single report</li>
     * <li>...</li>
     * </ul>
     * <p>
     * This will internally create a "single thread executor", that is shut down
     * at the end of the {@link FutureResult}'s lifecycle. Use
     * {@link #fetchLater(ExecutorService)} instead, if you want control over
     * your executing threads.
     * <p>
     * The result and its contained records are attached to the original
     * {@link Configuration} by default. Use {@link Settings#isAttachRecords()}
     * to override this behaviour.
     *
     * @return A future result
     * @throws DataAccessException if something went wrong executing the query
     */
    FutureResult<R> fetchLater() throws DataAccessException;

    /**
     * Fetch results asynchronously.
     * <p>
     * This method wraps fetching of records in a
     * {@link java.util.concurrent.Future}, such that you can access the actual
     * records at a future instant. This is especially useful when
     * <ul>
     * <li>You want to load heavy data in the background, for instance when the
     * user logs in and accesses a pre-calculated dashboard screen, before they
     * access the heavy data.</li>
     * <li>You want to parallelise several independent OLAP queries before
     * merging all data into a single report</li>
     * <li>...</li>
     * </ul>
     * <p>
     * Use this method rather than {@link #fetchLater()}, in order to keep
     * control over thread lifecycles, if you manage threads in a J2EE container
     * or with Spring, for instance.
     * <p>
     * The result and its contained records are attached to the original
     * {@link Configuration} by default. Use {@link Settings#isAttachRecords()}
     * to override this behaviour.
     *
     * @param executor A custom executor
     * @return A future result
     * @throws DataAccessException if something went wrong executing the query
     */
    FutureResult<R> fetchLater(ExecutorService executor) throws DataAccessException;

    /**
     * The record type produced by this query
     */
    Class<? extends R> getRecordType();

    /**
     * {@inheritDoc}
     */
    @Override
    ResultQuery<R> bind(String param, Object value) throws IllegalArgumentException, DataTypeException;

    /**
     * {@inheritDoc}
     */
    @Override
    ResultQuery<R> bind(int index, Object value) throws IllegalArgumentException, DataTypeException;

    // ------------------------------------------------------------------------
    // JDBC methods
    // ------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    ResultQuery<R> queryTimeout(int timeout);

    /**
     * Specify the maximum number of rows returned by the underlying
     * {@link Statement}
     * <p>
     * This is not the same as setting a <code>LIMIT .. OFFSET</code> clause
     * onto the statement, where the result set is restricted within the
     * database.
     *
     * @see Statement#setMaxRows(int)
     */
    ResultQuery<R> maxRows(int rows);

}
