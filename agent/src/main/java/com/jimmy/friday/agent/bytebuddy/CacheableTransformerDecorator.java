/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.jimmy.friday.agent.bytebuddy;

import com.jimmy.friday.agent.enums.ClassCacheMode;
import com.jimmy.friday.agent.utils.FileUtil;
import com.jimmy.friday.agent.utils.IOUtil;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.utility.RandomString;

import java.io.*;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheableTransformerDecorator implements AgentBuilder.TransformerDecorator {

    private final ClassCacheMode cacheMode;
    private ClassCacheResolver cacheResolver;

    public CacheableTransformerDecorator(ClassCacheMode cacheMode) throws IOException {
        this.cacheMode = cacheMode;
        initClassCache();
    }

    private void initClassCache() throws IOException {
        if (this.cacheMode.equals(ClassCacheMode.FILE)) {
            File cacheDir = new File("/tmp/class-cache-" + RandomString.make());
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            if (!cacheDir.exists()) {
                throw new IOException("Create class cache dir failure");
            }

            cacheResolver = new FileCacheResolver(cacheDir);
        } else {
            cacheResolver = new MemoryCacheResolver();
        }
    }

    @Override
    public ResettableClassFileTransformer decorate(ResettableClassFileTransformer classFileTransformer) {
        return new ResettableClassFileTransformer.WithDelegation(classFileTransformer) {

            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                // load from cache
                byte[] classCache = cacheResolver.getClassCache(loader, className);
                if (classCache != null) {
                    return classCache;
                }

                //transform class
                classfileBuffer = classFileTransformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);

                // save to cache
                if (classfileBuffer != null) {
                    cacheResolver.putClassCache(loader, className, classfileBuffer);
                }

                return classfileBuffer;
            }
        };
    }

    private static String getClassLoaderHash(ClassLoader loader) {
        String classloader;
        if (loader != null) {
            classloader = Integer.toHexString(loader.hashCode());
        } else {
            //classloader is null for BootstrapClassLoader
            classloader = "00000000";
        }
        return classloader;
    }

    interface ClassCacheResolver {

        byte[] getClassCache(ClassLoader loader, String className);

        void putClassCache(ClassLoader loader, String className, byte[] classfileBuffer);
    }

    static class MemoryCacheResolver implements ClassCacheResolver {
        // classloaderHashcode@className -> class bytes
        private Map<String, byte[]> classCacheMap = new ConcurrentHashMap<String, byte[]>();

        @Override
        public byte[] getClassCache(ClassLoader loader, String className) {
            String cacheKey = getCacheKey(loader, className);
            return classCacheMap.get(cacheKey);
        }

        @Override
        public void putClassCache(ClassLoader loader, String className, byte[] classfileBuffer) {
            String cacheKey = getCacheKey(loader, className);
            classCacheMap.put(cacheKey, classfileBuffer);
        }

        private String getCacheKey(ClassLoader loader, String className) {
            return getClassLoaderHash(loader) + "@" + className;
        }
    }

    static class FileCacheResolver implements ClassCacheResolver {

        private final File cacheDir;

        FileCacheResolver(File cacheDir) {
            this.cacheDir = cacheDir;
            //clean cache dir on exit
            FileUtil.deleteDirectoryOnExit(cacheDir);
        }

        @Override
        public byte[] getClassCache(ClassLoader loader, String className) {
            // load from cache
            File cacheFile = getCacheFile(loader, className);
            if (cacheFile.exists()) {
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream(cacheFile);
                    return IOUtil.toByteArray(fileInputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    IOUtil.closeQuietly(fileInputStream);
                }
            }
            return null;
        }

        @Override
        public void putClassCache(ClassLoader loader, String className, byte[] classfileBuffer) {
            File cacheFile = getCacheFile(loader, className);
            cacheFile.getParentFile().mkdirs();
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(cacheFile);
                IOUtil.copy(new ByteArrayInputStream(classfileBuffer), output);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtil.closeQuietly(output);
            }
        }

        private File getCacheFile(ClassLoader loader, String className) {
            String filename = getClassLoaderHash(loader) + "/" + className.replace('.', '/') + ".class";
            return new File(cacheDir, filename);
        }
    }
}
