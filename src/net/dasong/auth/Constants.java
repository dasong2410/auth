package net.dasong.auth;

public class Constants
{

    // 程序根目录
    public static final String ROOT_DIR = System.getProperty("user.dir");

    // 配置文件目录
    public static final String CFG_DIR = ROOT_DIR + "/cfg";

    // private key file
    public static final String PRI_KEY = "id_rsa";

    public static final String PRI_KEY_FILE = CFG_DIR + "/id_rsa";

    // public key file
    public static final String PUB_KEY = "id_rsa.pub";

    public static final String PUB_KEY_FILE = CFG_DIR + "/id_rsa.pub";

    // 机器列表文件
    public static final String HOST_FILE = CFG_DIR + "/host";
}
