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

package org.jooq.impl;

import static org.jooq.impl.Util.getAnnotatedGetter;
import static org.jooq.impl.Util.getAnnotatedMembers;
import static org.jooq.impl.Util.getAnnotatedSetters;
import static org.jooq.impl.Util.getMatchingGetter;
import static org.jooq.impl.Util.getMatchingMembers;
import static org.jooq.impl.Util.getMatchingSetters;
import static org.jooq.impl.Util.getPropertyName;
import static org.jooq.impl.Util.hasColumnAnnotations;
import static org.jooq.tools.reflect.Reflect.accessible;

import java.beans.ConstructorProperties;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import org.jooq.ArrayRecord;
import org.jooq.Attachable;
import org.jooq.Converter;
import org.jooq.Field;
import org.jooq.FieldProvider;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.UniqueKey;
import org.jooq.exception.InvalidResultException;
import org.jooq.exception.MappingException;
import org.jooq.tools.Convert;
import org.jooq.tools.reflect.Reflect;

/**
 * @author Lukas Eder
 */
abstract class AbstractRecord extends AbstractStore<Object> implements Record {

    /**
     * Generated UID
     */
    private static final long   serialVersionUID = -6052512608911220404L;

    private final FieldProvider fields;
    private Value<?>[]          values;

    AbstractRecord(FieldProvider fields) {
        this.fields = fields;
    }

    final FieldProvider getFieldProvider() {
        return fields;
    }

    @Override
    public final List<Attachable> getAttachables() {
        List<Attachable> result = new ArrayList<Attachable>();

        int size = getFields().size();
        for (int i = 0; i < size; i++) {
            Object value = getValue0(i).getValue();

            if (value instanceof Attachable) {
                result.add((Attachable) value);
            }
        }

        return result;
    }

    @Override
    public final List<Field<?>> getFields() {
        return fields.getFields();
    }

    @Override
    public final <T> Field<T> getField(Field<T> field) {
        return fields.getField(field);
    }

    @Override
    public final Field<?> getField(String name) {
        return fields.getField(name);
    }

    @Override
    public final Field<?> getField(int index) {
        return fields.getField(index);
    }

    @Override
    public final int size() {
        return getFields().size();
    }

    @SuppressWarnings("unchecked")
    final <T> Value<T> getValue0(int index) {
        return (Value<T>) getValues()[index];
    }

    @SuppressWarnings("unchecked")
    final <T> Value<T> getValue0(Field<T> field) {
        return (Value<T>) getValues()[getIndex(field)];
    }

    final Value<?>[] getValues() {
        if (values == null) {
            init();
        }

        return values;
    }

    @Override
    public final int getIndex(Field<?> field) {
        return fields.getIndex(field);
    }

    private final void init() {
        values = new Value<?>[fields.getFields().size()];

        for (int i = 0; i < values.length; i++) {
            values[i] = new Value<Object>(null);
        }
    }

    @Override
    public final <T> T getValue(Field<T> field) {
        return getValue0(field).getValue();
    }

    @Override
    public final <T> T getValue(Field<T> field, T defaultValue) {
        return getValue0(field).getValue(defaultValue);
    }

    @Override
    public final <T> void setValue(Field<T> field, T value) {
        UniqueKey<?> mainKey = getMainKey();
        Value<T> val = getValue0(field);

        // Normal fields' changed flag is always set to true
        if (mainKey == null || !mainKey.getFields().contains(field)) {
            val.setValue(value);
        }

        // The main key's changed flag might've been set previously
        else if (val.isChanged()) {
            val.setValue(value);
        }

        // [#979] If the main key is being changed, all other fields' flags need
        // to be set to true for in case this record is stored again, an INSERT
        // statement will thus be issued
        else {
            val.setValue(value, true);

            if (val.isChanged()) {
                for (Value<?> other : getValues()) {
                    other.setChanged(true);
                }
            }
        }
    }

    @Override
    public final <T, U> void setValue(Field<T> field, U value, Converter<T, ? super U> converter) {
        setValue(field, converter.to(value));
    }

    final <T> void setValue(Field<T> field, Value<T> value) {
        setValue(getIndex(field), value);
    }

    final <T> void setValue(int index, Value<T> value) {
        getValues()[index] = value;
    }

    /**
     * Subclasses may override this
     */
    UniqueKey<?> getMainKey() {
        return null;
    }

    /**
     * Reset all value flags' changed status
     */
    final void setAllChanged(boolean changed) {
        for (Value<?> value : getValues()) {
            value.setChanged(changed);
        }
    }

    @Override
    public String toString() {
        Result<AbstractRecord> result = new ResultImpl<AbstractRecord>(getConfiguration(), fields);
        result.add(this);
        return result.toString();
    }

    @Override
    public final String getValueAsString(Field<?> field) {
        return getValueAsString(getIndex(field));
    }

    @Override
    public final String getValueAsString(Field<?> field, String defaultValue) {
        return getValueAsString(getIndex(field), defaultValue);
    }

    @Override
    public final Byte getValueAsByte(Field<?> field) {
        return getValueAsByte(getIndex(field));
    }

    @Override
    public final Byte getValueAsByte(Field<?> field, Byte defaultValue) {
        return getValueAsByte(getIndex(field), defaultValue);
    }

    @Override
    public final Short getValueAsShort(Field<?> field) {
        return getValueAsShort(getIndex(field));
    }

    @Override
    public final Short getValueAsShort(Field<?> field, Short defaultValue) {
        return getValueAsShort(getIndex(field), defaultValue);
    }

    @Override
    public final Integer getValueAsInteger(Field<?> field) {
        return getValueAsInteger(getIndex(field));
    }

    @Override
    public final Integer getValueAsInteger(Field<?> field, Integer defaultValue) {
        return getValueAsInteger(getIndex(field), defaultValue);
    }

    @Override
    public final Long getValueAsLong(Field<?> field) {
        return getValueAsLong(getIndex(field));
    }

    @Override
    public final Long getValueAsLong(Field<?> field, Long defaultValue) {
        return getValueAsLong(getIndex(field), defaultValue);
    }

    @Override
    public final BigInteger getValueAsBigInteger(Field<?> field) {
        return getValueAsBigInteger(getIndex(field));
    }

    @Override
    public final BigInteger getValueAsBigInteger(Field<?> field, BigInteger defaultValue)
        {
        return getValueAsBigInteger(getIndex(field), defaultValue);
    }

    @Override
    public final Float getValueAsFloat(Field<?> field) {
        return getValueAsFloat(getIndex(field));
    }

    @Override
    public final Float getValueAsFloat(Field<?> field, Float defaultValue) {
        return getValueAsFloat(getIndex(field), defaultValue);
    }

    @Override
    public final Double getValueAsDouble(Field<?> field) {
        return getValueAsDouble(getIndex(field));
    }

    @Override
    public final Double getValueAsDouble(Field<?> field, Double defaultValue) {
        return getValueAsDouble(getIndex(field), defaultValue);
    }

    @Override
    public final BigDecimal getValueAsBigDecimal(Field<?> field) {
        return getValueAsBigDecimal(getIndex(field));
    }

    @Override
    public final BigDecimal getValueAsBigDecimal(Field<?> field, BigDecimal defaultValue)
        {
        return getValueAsBigDecimal(getIndex(field), defaultValue);
    }

    @Override
    public final Boolean getValueAsBoolean(Field<?> field) {
        return getValueAsBoolean(getIndex(field));
    }

    @Override
    public final Boolean getValueAsBoolean(Field<?> field, Boolean defaultValue) {
        return getValueAsBoolean(getIndex(field), defaultValue);
    }

    @Override
    public final Timestamp getValueAsTimestamp(Field<?> field) {
        return getValueAsTimestamp(getIndex(field));
    }

    @Override
    public final Timestamp getValueAsTimestamp(Field<?> field, Timestamp defaultValue) {
        return getValueAsTimestamp(getIndex(field), defaultValue);
    }

    @Override
    public final Date getValueAsDate(Field<?> field) {
        return getValueAsDate(getIndex(field));
    }

    @Override
    public final Date getValueAsDate(Field<?> field, Date defaultValue) {
        return getValueAsDate(getIndex(field), defaultValue);
    }

    @Override
    public final Time getValueAsTime(Field<?> field) {
        return getValueAsTime(getIndex(field));
    }

    @Override
    public final Time getValueAsTime(Field<?> field, Time defaultValue) {
        return getValueAsTime(getIndex(field), defaultValue);
    }

    @Override
    public final Object getValue(int index) {
        return getValue(getField(index));
    }

    @Override
    public final Object getValue(String fieldName) {
        return getValue(getField(fieldName));
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Object getValue(String fieldName, Object defaultValue) {
        return getValue((Field<Object>) getField(fieldName), defaultValue);
    }

    @Override
    public final <A extends ArrayRecord<T>, T> T[] getValueAsArray(Field<A> field) {
        A result = getValue(field);
        return result == null ? null : result.get();
    }

    @Override
    public final <A extends ArrayRecord<T>, T> T[] getValueAsArray(Field<A> field, T[] defaultValue)
        {
        final T[] result = getValueAsArray(field);
        return result == null ? defaultValue : result;
    }

    @Override
    public final String getValueAsString(String fieldName) {
        return getValueAsString(getField(fieldName));
    }

    @Override
    public final String getValueAsString(String fieldName, String defaultValue) {
        return getValueAsString(getField(fieldName), defaultValue);
    }

    @Override
    public final Byte getValueAsByte(String fieldName) {
        return getValueAsByte(getField(fieldName));
    }

    @Override
    public final Byte getValueAsByte(String fieldName, Byte defaultValue) {
        return getValueAsByte(getField(fieldName), defaultValue);
    }

    @Override
    public final Short getValueAsShort(String fieldName) {
        return getValueAsShort(getField(fieldName));
    }

    @Override
    public final Short getValueAsShort(String fieldName, Short defaultValue) {
        return getValueAsShort(getField(fieldName), defaultValue);
    }

    @Override
    public final Integer getValueAsInteger(String fieldName) {
        return getValueAsInteger(getField(fieldName));
    }

    @Override
    public final Integer getValueAsInteger(String fieldName, Integer defaultValue) {
        return getValueAsInteger(getField(fieldName), defaultValue);
    }

    @Override
    public final Long getValueAsLong(String fieldName) {
        return getValueAsLong(getField(fieldName));
    }

    @Override
    public final Long getValueAsLong(String fieldName, Long defaultValue) {
        return getValueAsLong(getField(fieldName), defaultValue);
    }

    @Override
    public final BigInteger getValueAsBigInteger(String fieldName) {
        return getValueAsBigInteger(getField(fieldName));
    }

    @Override
    public final BigInteger getValueAsBigInteger(String fieldName, BigInteger defaultValue)
        {
        return getValueAsBigInteger(getField(fieldName), defaultValue);
    }

    @Override
    public final Float getValueAsFloat(String fieldName) {
        return getValueAsFloat(getField(fieldName));
    }

    @Override
    public final Float getValueAsFloat(String fieldName, Float defaultValue) {
        return getValueAsFloat(getField(fieldName), defaultValue);
    }

    @Override
    public final Double getValueAsDouble(String fieldName) {
        return getValueAsDouble(getField(fieldName));
    }

    @Override
    public final Double getValueAsDouble(String fieldName, Double defaultValue) {
        return getValueAsDouble(getField(fieldName), defaultValue);
    }

    @Override
    public final BigDecimal getValueAsBigDecimal(String fieldName) {
        return getValueAsBigDecimal(getField(fieldName));
    }

    @Override
    public final BigDecimal getValueAsBigDecimal(String fieldName, BigDecimal defaultValue)
        {
        return getValueAsBigDecimal(getField(fieldName), defaultValue);
    }

    @Override
    public final Boolean getValueAsBoolean(String fieldName) {
        return getValueAsBoolean(getField(fieldName));
    }

    @Override
    public final Boolean getValueAsBoolean(String fieldName, Boolean defaultValue) {
        return getValueAsBoolean(getField(fieldName), defaultValue);
    }

    @Override
    public final Timestamp getValueAsTimestamp(String fieldName) {
        return getValueAsTimestamp(getField(fieldName));
    }

    @Override
    public final Timestamp getValueAsTimestamp(String fieldName, Timestamp defaultValue)
        {
        return getValueAsTimestamp(getField(fieldName), defaultValue);
    }

    @Override
    public final Date getValueAsDate(String fieldName) {
        return getValueAsDate(getField(fieldName));
    }

    @Override
    public final Date getValueAsDate(String fieldName, Date defaultValue) {
        return getValueAsDate(getField(fieldName), defaultValue);
    }

    @Override
    public final Time getValueAsTime(String fieldName) {
        return getValueAsTime(getField(fieldName));
    }

    @Override
    public final Time getValueAsTime(String fieldName, Time defaultValue) {
        return getValueAsTime(getField(fieldName), defaultValue);
    }

    @Override
    public final <T> T getValue(Field<?> field, Class<? extends T> type) {
        return Convert.convert(getValue(field), type);
    }

    @Override
    public final <T> T getValue(Field<?> field, Class<? extends T> type, T defaultValue) {
        final T result = getValue(field, type);
        return result == null ? defaultValue : result;
    }

    @Override
    public final <T> T getValue(String fieldName, Class<? extends T> type) {
        return Convert.convert(getValue(fieldName), type);
    }

    @Override
    public final <Z> Z getValue(String fieldName, Class<? extends Z> type, Z defaultValue) {
        final Z result = getValue(fieldName, type);
        return result == null ? defaultValue : result;
    }

    @Override
    public final <T, U> U getValue(Field<T> field, Converter<? super T, U> converter) {
        return converter.from(getValue(field));
    }

    @Override
    public final <T, U> U getValue(Field<T> field, Converter<? super T, U> converter, U defaultValue) {
        final U result = getValue(field, converter);
        return result == null ? defaultValue : result;
    }

    @Override
    public final <U> U getValue(int index, Converter<?, U> converter) {
        return Convert.convert(getValue(index), converter);
    }

    @Override
    public final <U> U getValue(int index, Converter<?, U> converter, U defaultValue) {
        final U result = getValue(index, converter);
        return result == null ? defaultValue : result;
    }

    @Override
    public final <U> U getValue(String fieldName, Converter<?, U> converter) {
        return Convert.convert(getValue(fieldName), converter);
    }

    @Override
    public final <U> U getValue(String fieldName, Converter<?, U> converter, U defaultValue) {
        final U result = getValue(fieldName, converter);
        return result == null ? defaultValue : result;
    }

    /*
     * This method is overridden covariantly by TableRecordImpl
     */
    @Override
    public Record original() {
        AbstractRecord result = Util.newRecord(getClass(), getFieldProvider(), getConfiguration());
        Value<?>[] v = getValues();

        for (int i = 0; i < v.length; i++) {
            result.setValue(i, new Value<Object>(v[i].getOriginal()));
        }

        return result;
    }

    @Override
    public final boolean changed() {
        for (Value<?> value : getValues()) {
            if (value.isChanged()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public final Object[] intoArray() {
        return into(Object[].class);
    }

    @Override
    public final Map<String, Object> intoMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        List<Field<?>> f = getFields();
        int size = f.size();

        for (int i = 0; i < size; i++) {
            Field<?> field = f.get(i);

            if (map.put(field.getName(), getValue(i)) != null) {
                throw new InvalidResultException("Field " + field.getName() + " is not unique in Record : " + this);
            }
        }

        return map;
    }

    @Override
    public final <E> E into(Class<? extends E> type) {
        try {
            if (type.isArray()) {
                return intoArray(type);
            }
            else {
                return intoPOJO(type);
            }
        }

        // Pass MappingExceptions on to client code
        catch (MappingException e) {
            throw e;
        }

        // All other reflection exceptions are intercepted
        catch (Exception e) {
            throw new MappingException("An error ocurred when mapping record to " + type, e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <E> E into(E object) {
        if (object == null) {
            throw new NullPointerException("Cannot copy Record into null");
        }

        Class<E> type = (Class<E>) object.getClass();

        try {
            if (type.isArray()) {
                return (E) intoArray((Object[]) object, type.getComponentType());
            }
            else {
                return intoMutablePOJO(type, object);
            }
        }

        // Pass MappingExceptions on to client code
        catch (MappingException e) {
            throw e;
        }

        // All other reflection exceptions are intercepted
        catch (Exception e) {
            throw new MappingException("An error ocurred when mapping record to " + type, e);
        }
    }

    /**
     * Convert this record into an array of a given type.
     * <p>
     * The supplied type is usually <code>Object[]</code>, but in some cases, it
     * may make sense to supply <code>String[]</code>, <code>Integer[]</code>
     * etc.
     */
    @SuppressWarnings("unchecked")
    private final <E> E intoArray(Class<? extends E> type) {
        int size = getFields().size();
        Class<?> componentType = type.getComponentType();
        Object[] result = (Object[]) Array.newInstance(componentType, size);

        return (E) intoArray(result, componentType);
    }

    /**
     * Convert this record into an array of a given component type.
     */
    private final Object[] intoArray(Object[] result, Class<?> componentType) {
        int size = getFields().size();

        // Just as in Collection.toArray(Object[]), return a new array in case
        // sizes don't match
        if (size > result.length) {
            result = (Object[]) Array.newInstance(componentType, size);
        }

        for (int i = 0; i < size; i++) {
            result[i] = Convert.convert(getValue(i), componentType);
        }

        return result;
    }

    /**
     * Convert this record into a POJO
     */
    private final <E> E intoPOJO(Class<? extends E> type) throws Exception {

        // If a default, no argument constructor is present, use that one.
        try {
            E result;

            // [#1470] Return a proxy if the supplied type is an interface
            if (Modifier.isAbstract(type.getModifiers())) {
                result = Reflect.on(HashMap.class).create().as(type);
            }

            // [#1340] Allow for using non-public default constructors
            else {
                result = accessible(type.getDeclaredConstructor()).newInstance();
            }

            return intoMutablePOJO(type, result);
        }

        // [#1336] If no default constructor is present, check if there is a
        // "matching" constructor with the same number of fields as this record
        catch (NoSuchMethodException e) {
            return intoImmutablePOJO(type);
        }
    }

    /**
     * Convert this record into an "immutable" POJO (final fields, "matching"
     * constructor).
     */
    @SuppressWarnings("unchecked")
    private final <E> E intoImmutablePOJO(Class<? extends E> type) throws Exception {
        Constructor<E>[] constructors = (Constructor<E>[]) type.getDeclaredConstructors();

        // [#1837] If any java.beans.ConstructorProperties annotations are
        // present use those rather than matching constructors by the number of
        // arguments
        for (Constructor<E> constructor : constructors) {
            ConstructorProperties properties = constructor.getAnnotation(ConstructorProperties.class);

            if (properties != null) {
                return intoImmutablePOJO(type, constructor, properties);
            }
        }

        // Without ConstructorProperties, match constructors by matching
        // argument length
        for (Constructor<E> constructor : constructors) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();

            // Match the first constructor by parameter length
            if (parameterTypes.length == getFields().size()) {
                Object[] converted = Util.convert(parameterTypes, intoArray());
                return accessible(constructor).newInstance(converted);
            }
        }

        throw new MappingException("No matching constructor found on type " + type + " for record " + this);
    }

    /**
     * Create an immutable POJO given a constructor and its associated JavaBeans
     * {@link ConstructorProperties}
     */
    private final <E> E intoImmutablePOJO(Class<? extends E> type, Constructor<E> constructor, ConstructorProperties properties) throws Exception {
        boolean useAnnotations = hasColumnAnnotations(type);
        List<String> propertyNames = Arrays.asList(properties.value());

        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] parameterValues = new Object[parameterTypes.length];

        for (Field<?> field : getFields()) {
            List<java.lang.reflect.Field> members;
            Method method;

            // Annotations are available and present
            if (useAnnotations) {
                members = getAnnotatedMembers(type, field.getName());
                method = getAnnotatedGetter(type, field.getName());
            }

            // No annotations are present
            else {
                members = getMatchingMembers(type, field.getName());
                method = getMatchingGetter(type, field.getName());
            }

            for (java.lang.reflect.Field member : members) {
                int index = propertyNames.indexOf(member.getName());

                if (index >= 0) {
                    parameterValues[index] = getValue(field);
                }
            }

            if (method != null) {
                String name = getPropertyName(method.getName());
                int index = propertyNames.indexOf(name);

                if (index >= 0) {
                    parameterValues[index] = getValue(field);
                }
            }
        }

        Object[] converted = Util.convert(parameterTypes, parameterValues);
        return accessible(constructor).newInstance(converted);
    }

    /**
     * Convert this record into a "mutable" POJO (non-final fields or setters
     * available)
     */
    private final <E> E intoMutablePOJO(Class<? extends E> type, E result) throws Exception {
        boolean useAnnotations = hasColumnAnnotations(type);

        for (Field<?> field : getFields()) {
            List<java.lang.reflect.Field> members;
            List<java.lang.reflect.Method> methods;

            // Annotations are available and present
            if (useAnnotations) {
                members = getAnnotatedMembers(type, field.getName());
                methods = getAnnotatedSetters(type, field.getName());
            }

            // No annotations are present
            else {
                members = getMatchingMembers(type, field.getName());
                methods = getMatchingSetters(type, field.getName());
            }

            for (java.lang.reflect.Field member : members) {

                // [#935] Avoid setting final fields
                if ((member.getModifiers() & Modifier.FINAL) == 0) {
                    into(result, member, field);
                }
            }

            for (java.lang.reflect.Method method : methods) {
                method.invoke(result, getValue(field, method.getParameterTypes()[0]));
            }
        }

        return result;
    }

    @Override
    public final <R extends Record> R into(Table<R> table) {
        try {
            R result = Util.newRecord(table, getConfiguration());

            for (Field<?> targetField : table.getFields()) {
                Field<?> sourceField = getField(targetField);

                if (sourceField != null) {
                    Util.setValue(result, targetField, this, sourceField);
                }
            }

            // [#1522] If the primary key has been fully fetched, then changed
            // flags should all be reset in order for the returned record to be
            // updatable using store()
            if (result instanceof AbstractRecord) {
                UniqueKey<?> key = ((AbstractRecord) result).getMainKey();

                if (key != null) {
                    boolean isKeySet = true;

                    for (Field<?> field : key.getFields()) {
                        isKeySet &= (getField(field) != null);
                    }

                    if (isKeySet) {
                        ((AbstractRecord) result).setAllChanged(false);
                    }
                }
            }

            return result;
        }

        // All reflection exceptions are intercepted
        catch (Exception e) {
            throw new MappingException("An error ocurred when mapping record to " + table, e);
        }
    }

    @Override
    public final <E> E map(RecordMapper<Record, E> mapper) {
        return mapper.map(this);
    }

    @Override
    public final void from(Object source) {
        if (source == null) return;

        Class<?> type = source.getClass();

        try {
            boolean useAnnotations = hasColumnAnnotations(type);

            for (Field<?> field : getFields()) {
                List<java.lang.reflect.Field> members;
                Method method;

                // Annotations are available and present
                if (useAnnotations) {
                    members = getAnnotatedMembers(type, field.getName());
                    method = getAnnotatedGetter(type, field.getName());
                }

                // No annotations are present
                else {
                    members = getMatchingMembers(type, field.getName());
                    method = getMatchingGetter(type, field.getName());
                }

                // Use only the first applicable method or member
                if (method != null) {
                    Util.setValue(this, field, method.invoke(source));
                }
                else if (members.size() > 0) {
                    from(source, members.get(0), field);
                }
            }
        }

        // All reflection exceptions are intercepted
        catch (Exception e) {
            throw new MappingException("An error ocurred when mapping record from " + type, e);
        }
    }

    /**
     * This method was implemented with [#799]. It may be useful to make it
     * public for broader use...?
     */
    protected final void from(Record source) {
        for (Field<?> field : getFields()) {
            Field<?> sourceField = source.getField(field);

            if (sourceField != null) {
                Util.setValue(this, field, source, sourceField);
            }
        }
    }

    private final void into(Object result, java.lang.reflect.Field member, Field<?> field) throws IllegalAccessException {
        Class<?> mType = member.getType();

        if (mType.isPrimitive()) {
            if (mType == byte.class) {
                member.setByte(result, getValue(field, byte.class));
            }
            else if (mType == short.class) {
                member.setShort(result, getValue(field, short.class));
            }
            else if (mType == int.class) {
                member.setInt(result, getValue(field, int.class));
            }
            else if (mType == long.class) {
                member.setLong(result, getValue(field, long.class));
            }
            else if (mType == float.class) {
                member.setFloat(result, getValue(field, float.class));
            }
            else if (mType == double.class) {
                member.setDouble(result, getValue(field, double.class));
            }
            else if (mType == boolean.class) {
                member.setBoolean(result, getValue(field, boolean.class));
            }
            else if (mType == char.class) {
                member.setChar(result, getValue(field, char.class));
            }
        }
        else {
            member.set(result, getValue(field, mType));
        }
    }

    private final void from(Object source, java.lang.reflect.Field member, Field<?> field)
        throws IllegalAccessException {

        Class<?> mType = member.getType();

        if (mType.isPrimitive()) {
            if (mType == byte.class) {
                Util.setValue(this, field, member.getByte(source));
            }
            else if (mType == short.class) {
                Util.setValue(this, field, member.getShort(source));
            }
            else if (mType == int.class) {
                Util.setValue(this, field, member.getInt(source));
            }
            else if (mType == long.class) {
                Util.setValue(this, field, member.getLong(source));
            }
            else if (mType == float.class) {
                Util.setValue(this, field, member.getFloat(source));
            }
            else if (mType == double.class) {
                Util.setValue(this, field, member.getDouble(source));
            }
            else if (mType == boolean.class) {
                Util.setValue(this, field, member.getBoolean(source));
            }
            else if (mType == char.class) {
                Util.setValue(this, field, member.getChar(source));
            }
        }
        else {
            Util.setValue(this, field, member.get(source));
        }
    }
}
