package com.hersch.testseafile;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Hersch.
 */
public class FileRooter {
    static Process process = null;

    /**
     * 初始化进程
     */
    private static void initProcess() {
        if (process == null)
            try {
                process = Runtime.getRuntime().exec("su");
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public static void chmod(String filePath) {
        //initProcess();
        //chmodFile(filePath);
        //close();
        root(filePath);
    }

    /**
     * 结束进程
     */
    private static void chmodFile(String filePath) {
        OutputStream out = process.getOutputStream();
        String cmd = "chmod -R 777 " + filePath + " \n";
        try {
            out.write(cmd.getBytes());
            //out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭输出流
     */
    private static void close() {
        if (process != null)
            try {
                process.getOutputStream().close();
                process = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private static void root(String strFilePath) {
        File device = new File(strFilePath);
        if (!device.canRead() || !device.canWrite()) {
            try {
                /* Missing read/write permission, trying to chmod the file */
                Process su;
                su = Runtime.getRuntime().exec("su");
                String cmd = "chmod 776 " + strFilePath + "\n" + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead()
                        || !device.canWrite()) {
                    showNotPermissionDialog(strFilePath);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showNotPermissionDialog(strFilePath);
                return;
            }
        }
    }
    private static void showNotPermissionDialog(String strFilePath){
        System.out.println(strFilePath+" is not rooted!");
    }
}
