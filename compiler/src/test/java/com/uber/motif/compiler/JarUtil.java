package com.uber.motif.compiler;

import java.io.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class JarUtil {

    // https://stackoverflow.com/questions/1281229/how-to-use-jaroutputstream-to-create-a-jar-file
    static void createJar(File inputDir, File outputFile) throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        JarOutputStream target = new JarOutputStream(new FileOutputStream(outputFile), manifest);
        add(inputDir, inputDir, target);
        target.close();
    }

    private static void add(File root, File source, JarOutputStream target) throws IOException {
        String path = root.toPath().relativize(source.toPath()).toString();
        BufferedInputStream in = null;
        try {
            if (source.isDirectory()) {
                if (!path.isEmpty()) {
                    if (!path.endsWith("/"))
                        path += "/";
                    JarEntry entry = new JarEntry(path);
                    entry.setTime(source.lastModified());
                    target.putNextEntry(entry);
                    target.closeEntry();
                }
                for (File nestedFile : source.listFiles())
                    add(root, nestedFile, target);
                return;
            }

            if (!path.endsWith(".class")) {
                return;
            }

            JarEntry entry = new JarEntry(path);
            entry.setTime(source.lastModified());
            target.putNextEntry(entry);
            in = new BufferedInputStream(new FileInputStream(source));

            byte[] buffer = new byte[1024];
            while (true) {
                int count = in.read(buffer);
                if (count == -1)
                    break;
                target.write(buffer, 0, count);
            }
            target.closeEntry();
        } finally {
            if (in != null)
                in.close();
        }
    }
}
