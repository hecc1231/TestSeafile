package com.hersch.testseafile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Hersch
 */
public class common {
        public static byte[] m_binArray = null;
        public static byte[] readFile(String filePath) {
            try {
                File file = new File(filePath);
                FileInputStream ins = new FileInputStream(file);
                int countLen = ins.available();
                m_binArray = new byte[countLen];
                BufferedInputStream in = new BufferedInputStream(ins);
                int length = in.read(m_binArray);
                ins.close();
            } catch (Exception e) {
                System.out.println("读取文件内容出错");
                e.printStackTrace();
            }
            return m_binArray;
        }
        public static void writeFile(String filePath, byte[] data) {
            FileOutputStream fop = null;
            try {
                File file = new File(filePath);
                fop = new FileOutputStream(file);
                if (!file.exists()) {
                    file.createNewFile();
                }
                fop.write(data);
                fop.flush();
                fop.close();
                System.out.println("Done");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fop != null) {
                        fop.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    /**
     * 获取文件MD5值
     * @param file
     * @return
     */
    public static String getFileMD5(File file) {
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, 1024);
            }
            in.close();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }
}
