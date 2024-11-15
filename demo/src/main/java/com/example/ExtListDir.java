package com.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ExtListDir {
    public static void main(String[] args) throws java.io.IOException {
        try (Context context = Context.newBuilder().allowAllAccess(true).build()) {
            Arguments arguments = arguments(args);
            System.out.println(arguments.script);
            final Value lambda = context.eval(arguments.lang, arguments.script);
            try (Stream<Path> paths = Files.walk(Paths.get("."))) {
                paths.forEach((Path p) -> {
                    File f = p.toFile();
                    Value value = lambda.execute(new FileInfo(f.getName(), f.getAbsolutePath(), f.length(), Files.isRegularFile(p)));
                    if (!value.isNull()) {
                        System.out.println(value);
                    }
                });
            }
        }
    }

    private static Arguments arguments(String[] args) {
        String lang = "js";
        String script = "(fileInfo) => { if (fileInfo.isRegularFile) {return fileInfo.name + \" : \" + fileInfo.size; } else {return \"DIR: \" + fileInfo.name }}";
        if (args.length == 2) {
            lang = args[0];
            script = args[1];
        } else if (args.length == 1) {
            script = args[0];
        }
        return new Arguments(lang, script);
    }

    record Arguments(String lang, String script) {
    }

    public static class FileInfo implements ProxyObject {

        public static final Object[] KEYS = {"name", "size", "isRegularFile"};
        public static final String NAME = "name";
        public static final String SIZE = "size";
        public static final String IS_REGULAR_FILE = "isRegularFile";

        public String absoluteFilePath;
        public String name;
        public long size;
        public boolean isRegularFile;

        FileInfo(String name, String absoluteFile, long size, boolean isRegularFile) {
            this.name = name;
            this.absoluteFilePath = absoluteFile;
            this.size = size;
            this.isRegularFile = isRegularFile;
        }

        @Override
        public Object getMember(String key) {
            if (NAME.equals(key)) {
                return name;
            }
            if (SIZE.equals(key)) {
                return size;
            }
            if (IS_REGULAR_FILE.equals(key)) {
                return isRegularFile;
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getMemberKeys() {
            return KEYS;
        }

        @Override
        public boolean hasMember(String key) {
            for (Object o : KEYS) {
                if (o.equals(key)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void putMember(String key, Value value) {
            throw new UnsupportedOperationException();
        }
    }
}
