/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.fluss.utils.types;

import com.alibaba.fluss.annotation.PublicStable;
import com.alibaba.fluss.utils.StringUtils;

import java.util.Objects;

/* This file is based on source code of Apache Flink Project (https://flink.apache.org/), licensed by the Apache
 * Software Foundation (ASF) under the Apache License, Version 2.0. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership. */

/**
 * A tuple with 2 fields. Tuples are strongly typed; each field may be of a separate type. The
 * fields of the tuple can be accessed directly as public fields (f0, f1, ...). The tuple field
 * positions start at zero.
 *
 * <p>Tuples are mutable types, meaning that their fields can be re-assigned. This allows functions
 * that work with Tuples to reuse objects in order to reduce pressure on the garbage collector.
 *
 * <p>Warning: If you subclass Tuple2, then be sure to either
 *
 * <ul>
 *   <li>not add any new fields, or
 *   <li>make it a POJO, and always declare the element type of your DataStreams/DataSets to your
 *       descendant type. (That is, if you have a "class Foo extends Tuple2", then don't use
 *       instances of Foo in a DataStream&lt;Tuple2&gt; / DataSet&lt;Tuple2&gt;, but declare it as
 *       DataStream&lt;Foo&gt; / DataSet&lt;Foo&gt;.)
 * </ul>
 *
 * @see Tuple
 * @param <T0> The type of field 0
 * @param <T1> The type of field 1
 * @since 0.1
 */
@PublicStable
public class Tuple2<T0, T1> extends Tuple {

    private static final long serialVersionUID = 1L;

    /** Field 0 of the tuple. */
    public T0 f0;
    /** Field 1 of the tuple. */
    public T1 f1;

    /**
     * Creates a new tuple and assigns the given values to the tuple's fields.
     *
     * @param f0 The value for field 0
     * @param f1 The value for field 1
     */
    public Tuple2(T0 f0, T1 f1) {
        this.f0 = f0;
        this.f1 = f1;
    }

    // -------------------------------------------------------------------------------------------------
    // standard utilities
    // -------------------------------------------------------------------------------------------------

    /**
     * Creates a string representation of the tuple in the form (f0, f1), where the individual
     * fields are the value returned by calling {@link Object#toString} on that field.
     *
     * @return The string representation of the tuple.
     */
    @Override
    public String toString() {
        return "("
                + StringUtils.arrayAwareToString(this.f0)
                + ","
                + StringUtils.arrayAwareToString(this.f1)
                + ")";
    }

    /**
     * Deep equality for tuples by calling equals() on the tuple members.
     *
     * @param o the object checked for equality
     * @return true if this is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tuple2)) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        Tuple2 tuple = (Tuple2) o;
        if (!Objects.equals(f0, tuple.f0)) {
            return false;
        }
        return Objects.equals(f1, tuple.f1);
    }

    @Override
    public int hashCode() {
        int result = f0 != null ? f0.hashCode() : 0;
        result = 31 * result + (f1 != null ? f1.hashCode() : 0);
        return result;
    }

    /**
     * Shallow tuple copy.
     *
     * @return A new Tuple with the same fields as this.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Tuple2<T0, T1> copy() {
        return new Tuple2<>(this.f0, this.f1);
    }

    /**
     * Creates a new tuple and assigns the given values to the tuple's fields. This is more
     * convenient than using the constructor, because the compiler can infer the generic type
     * arguments implicitly. For example: {@code Tuple3.of(n, x, s)} instead of {@code new
     * Tuple3<Integer, Double, String>(n, x, s)}
     */
    public static <T0, T1> Tuple2<T0, T1> of(T0 f0, T1 f1) {
        return new Tuple2<>(f0, f1);
    }
}
