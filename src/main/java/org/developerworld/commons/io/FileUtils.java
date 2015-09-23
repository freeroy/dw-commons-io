package org.developerworld.commons.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

/**
 * 文件处理工具类
 * 
 * @author Roy Huang
 * 
 */
public class FileUtils extends org.apache.commons.io.FileUtils {

	/**
	 * 把输入流输出到文件
	 * 
	 * @param is
	 * @param destFile
	 * @throws IOException
	 */
	public static void copyFile(InputStream is, File destFile)
			throws IOException {
		OutputStream os = openOutputStream(destFile);
		try {
			IOUtils.copy(is, os);
		} finally {
			os.close();
		}
	}

	/**
	 * 获取文件的编码格式
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String getFileCharset(File file) throws IOException {
		String rst = null;
		BufferedInputStream bis = null;
		try {
			byte[] first3Bytes = new byte[3];
			bis = new BufferedInputStream(new FileInputStream(file));
			bis.mark(0);
			int read = bis.read(first3Bytes, 0, 3);
			if (read == -1) {
				rst = "GBK"; // 文件编码为 ANSI
			} else if (first3Bytes[0] == (byte) 0xFF
					&& first3Bytes[1] == (byte) 0xFE) {
				rst = "UTF-16LE"; // 文件编码为 Unicode
			} else if (first3Bytes[0] == (byte) 0xFE
					&& first3Bytes[1] == (byte) 0xFF) {
				rst = "UTF-16BE"; // 文件编码为 Unicode big endian
			} else if (first3Bytes[0] == (byte) 0xEF
					&& first3Bytes[1] == (byte) 0xBB
					&& first3Bytes[2] == (byte) 0xBF) {
				rst = "UTF-8"; // 文件编码为 UTF-8
			}
			bis.reset();
			if (rst == null) {
				rst = "GBK";
				while ((read = bis.read()) != -1) {
					if (read >= 0xF0)
						break;
					else if (0x80 <= read && read <= 0xBF)// 单独出现BF以下的，也算是GBK
						break;
					if (0xC0 <= read && read <= 0xDF) {
						read = bis.read();
						if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
							// (0x80
							// - 0xBF),也可能在GB编码内
							continue;
						else
							break;
					} else if (0xE0 <= read && read <= 0xEF) {// 也有可能出错，但是几率较小
						read = bis.read();
						if (0x80 <= read && read <= 0xBF) {
							read = bis.read();
							if (0x80 <= read && read <= 0xBF) {
								rst = "UTF-8";
								break;
							} else
								break;
						} else
							break;
					}
				}
			}
		} finally {
			if (bis != null)
				bis.close();
		}
		return rst;
	}
}
