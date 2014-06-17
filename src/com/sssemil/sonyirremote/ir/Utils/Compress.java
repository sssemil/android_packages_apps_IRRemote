/*
 * Copyright (c) 2014 Emil Suleymanov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package com.sssemil.sonyirremote.ir.Utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class Compress {
    private String _zipFile;
    private String _srcDir;

    public Compress(String location, String zipFile) {
        _zipFile = zipFile;
        _srcDir = location;
    }

    private static void addDirToArchive(ZipOutputStream zos, File srcFile) {

        File[] files = srcFile.listFiles();

        Log.i("Compress", "Adding directory: " + srcFile.getName());

        for (int i = 0; i < files.length; i++) {

            // if the file is directory, use recursion
            if (files[i].isDirectory()) {
                addDirToArchive(zos, files[i]);
                continue;
            }

            try {

                Log.i("Compress", "tAdding file: " + files[i].getName());

                // create byte buffer
                byte[] buffer = new byte[1024];

                FileInputStream fis = new FileInputStream(files[i]);

                zos.putNextEntry(new ZipEntry(files[i].getName()));

                int length;

                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }

                zos.closeEntry();

                // close the InputStream
                fis.close();

            } catch (IOException ioe) {
                Log.e("Compress", "IOException :" + ioe);
            }

        }

    }

    public void zip() {
        try {

            FileOutputStream fos = new FileOutputStream(_zipFile);

            ZipOutputStream zos = new ZipOutputStream(fos);

            File srcFile = new File(_srcDir);

            addDirToArchive(zos, srcFile);

            // close the ZipOutputStream
            zos.close();

        } catch (IOException ioe) {
            Log.e("Compress", "Error creating zip file: " + ioe);
        }

    }
}
