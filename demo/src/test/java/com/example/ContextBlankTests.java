/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class ContextBlankTests {

    /**
     * Context?
     * eval?
     * Source?
     */
    @Test
    void testHelloWorld() {
        try (Context context = Context.create()) {
            context.eval("js", "console.log('Hello Praha (from JS)')");
            Source pythonSource = Source.create("python", "print('Hello Praha (from Python)')");
            context.eval(pythonSource);
        }
    }

    /**
     * Basic Host to Guest Interop?
     * Value?
     * canExecute?
     * asInt?
     */
    @Test
    void twoAndTwoIsFour() {
        try (Context context = Context.create()) {
            Value js = context.eval("js", "(a, b) => { return a + b }");

            Value py = context.eval("python", "lambda a, b : a + b");
        }
    }

    /**
     * Beware language semantics!
     */
    @Test
    void bewareLanguageSemantics() {
        try (Context context = Context.create()) {
            Value js = context.eval("js", "(a) => {return a + 1}");
        }
        try (Context context = Context.create()) {
            Value python = context.eval("python", "lambda a : a + 1");
        }
    }

    /**
     * Advanced Host-Guest Interop - Standard lib types.
     */
    @Test
    void incrementList() {
        try (Context context = Context.newBuilder("js").allowHostAccess(HostAccess.ALL).build()) {
            List<Object> objectList = new ArrayList<>();
            Collections.addAll(objectList, 1, 2, 3, "4", "5", 6.5);
            Value js = context.eval("js", "(a) => {" +
                    "    for (i=0; i < a.length; i++) { " +
                    "        a.set(i, a.get(i) + 1); " +
                    "    } " +
                    "}");
            js.execute(objectList);
        }
    }

    /**
     * Advanced Host-Guest Interop - access restrictions!
     */
    @Test
    void incrementListNoHostAccess() {
        try (Context context = Context.newBuilder("js").build()) {
            List<Object> objectList = new ArrayList<>();
            Collections.addAll(objectList, 1, 2, 3, "4", "5", 6.5);
            Value js = context.eval("js", "(a) => {" +
                    "    for (i=0; i < a.length; i++) { " +
                    "        a.set(i, a.get(i) + 1); " +
                    "    } " +
                    "}");
            js.execute(objectList);
            List<Object> expected = new ArrayList<>();
            Collections.addAll(expected, 1, 2, 3, "4", "5", 6.5);
        }
    }


    /**
     * Advanced Host-Guest Interop - custom types!
     */
    @Test
    void testMyCoolArray() {
        try (Context context = Context.newBuilder("js")
                .allowHostAccess(HostAccess.newBuilder().allowImplementations(MyCoolArray.class).build())
                .build()) {
            MyCoolArray myCoolArray = new MyCoolArray();
            myCoolArray.set(0, 1);
            myCoolArray.set(1, "1");
            myCoolArray.set(2, 1.1);
            Value js = context.eval("js", "(a) => {" +
                    "    for (i = 0; i < a.length; i++) { " +
                    "        a[i] = a[i] + 1; " +
                    "    } " +
                    "}");

            js.execute(myCoolArray);
        }
    }

    static final class MyCoolArray implements ProxyArray {

        Object[] members = new Object[0];

        @Override
        public Object get(long index) {
            return members[((int) index)];
        }

        public void set(long index, Object value) {
            ensureSize(index);
            members[(int) index] = value;
        }

        @Override
        public void set(long index, Value value) {
            ensureSize(index);
            members[(int) index] = value;
        }

        private void ensureSize(long index) {
            if (index >= members.length) {
                members = Arrays.copyOf(members, (int) (index + 1));
            }
        }

        @Override
        public long getSize() {
            return members.length;
        }
    }

    /**
     * Advanced Host-Guest Interop - custom types again!
     */
    @Test
    void testMyCoolArrayWithMyCoolObject() {
        try (Context context = Context.newBuilder("js").allowAllAccess(true).build()) {
            MyCoolArray myCoolArray = new MyCoolArray();
            myCoolArray.set(0, new MyCoolObject(1));
            myCoolArray.set(1, new MyCoolObject("1"));
            myCoolArray.set(2, new MyCoolObject(1.1));
            Value js = context.eval("js", "(a) => {" +
                    "    for (i = 0; i < a.length; i++) { " +
                    "        a[i].increment(); " +
                    "    } " +
                    "}");

            js.execute(myCoolArray);
        }
    }

    static final class MyCoolObject implements ProxyObject {

        Object value;

        public MyCoolObject(Object value) {
            this.value = value;
        }

        @Override
        public Object getMember(String key) {
            if ("value".equals(key)) {
                return value;
            }
            if ("increment".equals(key)) {
                return (ProxyExecutable) arguments -> {
                    increment();
                    return null;
                };
            }
            throw new UnsupportedOperationException();
        }

        private void increment() {
            if (value instanceof Integer intValue) {
                value = intValue + 1;
            }
            if (value instanceof Double doubleValue) {
                value = doubleValue + 1;
            }
            if (value instanceof String string) {
                value = string + "_incrementedInJava";
            }
            if (value instanceof MyCoolObject myCoolObject) {
                myCoolObject.increment();
            }
        }

        @Override
        public Object getMemberKeys() {
            MyCoolArray myCoolArray = new MyCoolArray();
            myCoolArray.set(0, "value");
            myCoolArray.set(1, "increment");
            return myCoolArray;
        }

        @Override
        public boolean hasMember(String key) {
            return "value".equals(key) || "increment".equals(key);
        }

        @Override
        public void putMember(String key, Value value) {
            if ("value".equals(key)) {
                this.value = value;
            } else {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public String toString() {
            return "MyCoolObject: " + value;
        }
    }

    /**
     * Guest language perks: FAST!
     */
    @Test
    void testFast() {
        try (Context context = Context.newBuilder("js")
                .allowExperimentalOptions(true)
                .option("engine.TraceCompilationDetails", "true")
                .allowHostAccess(HostAccess.ALL).build()) {
            MyCoolArray myCoolArray = new MyCoolArray();
            int count = 100_000;
            for (int i = 0; i < count; i++) {
                myCoolArray.set(i, new MyCoolObject(i));
            }
            Value js = context.eval("js", "(a) => {" +
                    "    sum = 0;" +
                    "    for (i = 0; i < a.length; i++) { " +
                    "        sum += a[i].value" +
                    "    } " +
                    "    return sum / a.length;" +
                    "}");
            Assertions.assertTrue(js.canExecute());
            for (int i = 0; i < 20; i++) {
                long start = System.currentTimeMillis();
                js.execute(myCoolArray).asDouble();
                System.out.println("[testFast] Iteration took: " + (System.currentTimeMillis() - start) + " ms");
            }
            System.out.println("[testFast] Preparing for deopt!");
            myCoolArray.set(count, new MyCoolObject("4"));
            for (int i = 0; i < 20; i++) {
                long start = System.currentTimeMillis();
                js.execute(myCoolArray).asDouble();
                System.out.println("[testFast] Iteration took: " + (System.currentTimeMillis() - start) + " ms");
            }
        }
    }

    /**
     * More complex example.
     */
    @Test
    void testLanguageSemanticsWork() {
        try (Context context = Context.newBuilder("js", "python").build()) {
            MyCoolArray myCoolArray = new MyCoolArray();
            myCoolArray.set(0, new MyCoolObject(1));
            myCoolArray.set(1, new MyCoolObject("1"));
            myCoolArray.set(2, new MyCoolObject(1.1));

            Value python = context.eval("python", "lambda x, inc : [ inc(e.value) for e in x ]");

            Value js = context.eval("js", "(a) => { return a + 1 }");

            Value result = python.execute(myCoolArray, js);
        }
    }

    /**
     * More complex example, beware language semantics.
     */
    @Test
    void testLanguageSemanticsFail() {
        try (Context context = Context.newBuilder("js", "python").build()) {
            MyCoolArray myCoolArray = new MyCoolArray();
            myCoolArray.set(0, new MyCoolObject(1));
            myCoolArray.set(1, new MyCoolObject("1"));
            myCoolArray.set(2, new MyCoolObject(1.1));
            Value js = context.eval("js", "(a, inc) => {" +
                    "    for (i = 0; i < a.length; i++) { " +
                    "        inc(a[i]); " +
                    "    } " +
                    "}");
            Value python = context.eval("python", "lambda x : x.value + 1");
        }
    }
}
