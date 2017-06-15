/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.goodocom.gocsdk;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

public class SerialPort {

	private static final String TAG = "SerialPort";

	/*
	 * Do not remove or rename the field mFd: it is used by native method
	 * close();
	 */
	/**
	 * 文件描述符类的实例用作与基础机器有关的某种结构的不透明句柄，该结构表示开放文件、开放套接字或者字节的另一个源或接收者。
	 * 文件描述符的主要实际用途是创建一个包含该结构的 FileInputStream 或 FileOutputStream。
	 */
	private FileDescriptor mFd;
	private FileInputStream mFileInputStream;
	private FileOutputStream mFileOutputStream;

	// 构造方法
	public SerialPort(File device, int baudrate, int flags)
			throws SecurityException, IOException {

		/*
		 * // Check access permission if (!device.canRead() ||
		 * !device.canWrite()) { try { //Missing read/write permission, trying
		 * to chmod the file Process su; su =
		 * Runtime.getRuntime().exec("/system/bin/su"); String cmd =
		 * "chmod 666 " + device.getAbsolutePath() + "\n" + "exit\n";
		 * su.getOutputStream().write(cmd.getBytes()); if ((su.waitFor() != 0)
		 * || !device.canRead() || !device.canWrite()) { throw new
		 * SecurityException(); } } catch (Exception e) { e.printStackTrace();
		 * throw new SecurityException(); } }
		 */
		mFd = open(device.getAbsolutePath(), baudrate, flags);
		if (mFd == null) {
			Log.e(TAG, "native open returns null");
			throw new IOException();
		}
		//通过使用文件描述符 fdObj 创建一个 FileInputStream，该文件描述符表示到文件系统中某个实际文件的现有连接。
		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = new FileOutputStream(mFd);
	}

	// Getters and setters
	public InputStream getInputStream() {
		return mFileInputStream;
	}

	public OutputStream getOutputStream() {
		return mFileOutputStream;
	}

	// JNI
	private native static FileDescriptor open(String path, int baudrate,
			int flags);

	public native void close();

	static {
		System.loadLibrary("serial_port");
	}
}
