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

package com.sssemil.sonyirremote.ir;

import java.io.File;
import java.io.IOException;

public class IRCommon {
    static {
        System.loadLibrary("jni_sonyopenir");
    }

    private static IRCommon instance = null;

    protected IRCommon() {
    }

    public static IRCommon getInstance() {
        if (instance == null) {
            instance = new IRCommon();
        }
        return instance;
    }

    public static void delete(File file)
            throws IOException {

        if (file.isDirectory()) {

            //directory is empty, then delete it
            if (file.list().length == 0) {

                file.delete();
                System.out.println("Directory is deleted : "
                        + file.getAbsolutePath());

            } else {

                //list all the directory contents
                String files[] = file.list();

                for (String temp : files) {
                    //construct the file structure
                    File fileDelete = new File(file, temp);

                    //recursive delete
                    delete(fileDelete);
                }

                //check the directory again, if empty then delete it
                if (file.list().length == 0) {
                    file.delete();
                    System.out.println("Directory is deleted : "
                            + file.getAbsolutePath());
                }
            }

        } else {
            //if file, then delete it
            file.delete();
            System.out.println("File is deleted : " + file.getAbsolutePath());
        }
    }

    private native int startIR();

    private native int stopIR();

    private native int learnKey(String filename);

    private native int sendKey(String filename);

    private native int sendRawKey(String key, int length);

    public int start() {
        return startIR();
    }

    public int stop() {
        return stopIR();
    }

    public int send(String filename) {
        return sendKey(filename);
    }

    public int sendRaw(String key, int length) {
        return sendRawKey(key, length);
    }

    public int learn(String filename) {
        return learnKey(filename);
    }

    public void restart() {
        stopIR();
        startIR();
    }
}
