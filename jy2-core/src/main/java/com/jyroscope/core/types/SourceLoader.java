package com.jyroscope.core.types;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.*;
import javax.tools.*;

public class SourceLoader {

	private static final Logger log = Logger.getLogger(SourceLoader.class.getCanonicalName());
	
    private static final MemoryClassLoader classLoader;
    private static final JavaCompiler compiler;
    private static final MemoryJavaFileManager memoryManager;
    
    static {
        compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.ENGLISH, null);
        memoryManager = new MemoryJavaFileManager(fileManager);
        classLoader = new MemoryClassLoader(memoryManager);
    }
    

    public static Object create(String name, String source) {
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        
        JavaFileObject file = new JavaSource(name, source);
        JavaCompiler.CompilationTask task = compiler.getTask(null, memoryManager, diagnostics, null, null, Arrays.asList(file));
        
        boolean success = task.call();
        
        if (success) {
            try {
                Class<?> clazz = classLoader.findClass(name);
                return clazz.newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            	log.log(Level.SEVERE, "Could not instantiate dynamically compiled file " + name, e);
                return null;
            }
        } else {
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                log.info(diagnostic.getMessage(null));
            }
            log.info("Failed to dynamically compile file " + name);
            return null;
        }
    }
    
    private static class JavaSource extends SimpleJavaFileObject {
        
        private final String source;
        
        private JavaSource(String name, String source) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.source = source;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return source;
        }
    
    }
    
    private static class JavaByteCode implements JavaFileObject {
    
        private final String name;
        private final JavaFileObject.Kind kind;
        private ByteArrayOutputStream buffer;

        private JavaByteCode(String name, JavaFileObject.Kind kind) {
            this.name = name;
            this.kind = kind;
        }

        public byte[] getByteCode() {
            return buffer.toByteArray();
        }

        @Override
        public JavaFileObject.Kind getKind() {
            return kind;
        }

        @Override
        public boolean isNameCompatible(String simpleName, JavaFileObject.Kind kind) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public NestingKind getNestingKind() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Modifier getAccessLevel() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public URI toUri() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return new ByteArrayInputStream(buffer.toByteArray());
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            buffer = new ByteArrayOutputStream();
            return buffer;
        }

        @Override
        public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
            return new InputStreamReader(openInputStream());
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Writer openWriter() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long getLastModified() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean delete() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
    
    private static class MemoryJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

        private final HashMap<String,JavaByteCode> classes;

        private MemoryJavaFileManager(JavaFileManager fileManager) {
            super(fileManager);
            classes = new HashMap<>();
        }

        @Override
        public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
            return super.list(location, packageName, kinds, recurse);
        }

        @Override
        public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
            return super.getFileForInput(location, packageName, relativeName);
        }

        @Override
        public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
            return super.getJavaFileForInput(location, className, kind);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            JavaByteCode classFile = new JavaByteCode(className, kind);
            classes.put(className, classFile);
            return classFile;
        }

        public JavaByteCode getSavedFile(String className) {
            return classes.get(className);
        }
    }
    
    
    private static class MemoryClassLoader extends ClassLoader {

        private final MemoryJavaFileManager fileManager;

        private MemoryClassLoader(MemoryJavaFileManager fileManager) {
            this.fileManager = fileManager;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            JavaByteCode file = fileManager.getSavedFile(name);
            if (file == null)
                return super.findClass(name);
            else {
                byte[] byteCode = file.getByteCode();
                return super.defineClass(name, byteCode, 0, byteCode.length);
            }
        }

    }


    
}
