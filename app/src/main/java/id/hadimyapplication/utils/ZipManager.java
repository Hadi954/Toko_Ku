package id.hadimyapplication.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipManager {
    private static final int BUFFER = 2048;

    public static void zip(String[] files, String zipFile) throws IOException {
        BufferedInputStream origin = null;
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
        try {
            byte data[] = new byte[BUFFER];
            for (int i = 0; i < files.length; i++) {
                File f = new File(files[i]);
                if (f.exists() && f.isDirectory()) {
                    zipDirectory(f, f.getName(), out);
                } else if (f.exists()) {
                    FileInputStream fi = new FileInputStream(f);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(f.getName());
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }
            }
        } finally {
            out.close();
        }
    }

    private static void zipDirectory(File dir, String baseName, ZipOutputStream out) throws IOException {
        File[] files = dir.listFiles();
        if (files != null) {
            byte data[] = new byte[BUFFER];
            for (File file : files) {
                if (file.isDirectory()) {
                    zipDirectory(file, baseName + "/" + file.getName(), out);
                } else {
                    FileInputStream fi = new FileInputStream(file);
                    BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(baseName + "/" + file.getName());
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }
            }
        }
    }

    public static void unzip(String zipFile, String location) throws IOException {
        File f = new File(location);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
        ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
        try {
            ZipEntry ze;
            while ((ze = zin.getNextEntry()) != null) {
                File file = new File(location, ze.getName());
                if (ze.isDirectory()) {
                    if (!file.isDirectory()) {
                        file.mkdirs();
                    }
                } else {
                    File parent = file.getParentFile();
                    if (parent != null && !parent.isDirectory()) {
                        parent.mkdirs();
                    }
                    FileOutputStream fout = new FileOutputStream(file);
                    try {
                        byte[] buffer = new byte[8192];
                        int count;
                        while ((count = zin.read(buffer)) != -1) {
                            fout.write(buffer, 0, count);
                        }
                    } finally {
                        fout.close();
                    }
                }
                zin.closeEntry();
            }
        } finally {
            zin.close();
        }
    }
}
