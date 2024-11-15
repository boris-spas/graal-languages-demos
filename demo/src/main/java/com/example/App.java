/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

public class App {

    public static void main(String[] args) {
        try (Context context = Context.create()) {
            context.eval("js", "console.log('Hello from GraalJS!')");
        }
    }

    public static int addNumbers(int a, int b) {
        try (Context context = Context.create()) {
        }
        throw new IllegalStateException();
    }

    public static Value increment(Object a) {
        try (Context context = Context.create()) {
            Value js = context.eval("js", "(a) => {return a + 1}");
            if (js.canExecute()) {
                return js.execute(a);
            }
        }
        throw new IllegalStateException();
    }
}
