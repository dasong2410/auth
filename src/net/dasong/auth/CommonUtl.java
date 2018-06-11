package net.dasong.auth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class CommonUtl
{
    /*
     * 生成key文件
     */
    public static void genKeyPair(String host)
    {
        JSch jsch = new JSch();

        try
        {
            KeyPair kpair = KeyPair.genKeyPair(jsch, KeyPair.RSA);
            kpair.writePrivateKey(Constants.PRI_KEY_FILE);
            kpair.writePublicKey(Constants.PUB_KEY_FILE, host);

            System.out.println("    Finger print: " + kpair.getFingerPrint());
            kpair.dispose();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }

    /*
     * 远程机器执行命令
     */
    public static void exec(String user, String password, String host,
            String cmd)
    {
        try
        {
            JSch jsch = new JSch();

            Session session = jsch.getSession(user, host, 22);
            session.setPassword(password);

            UserInfo ui = new MyUserInfo();
            session.setUserInfo(ui);
            session.connect();

            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(cmd);

            InputStream in = channel.getInputStream();
            OutputStream out = channel.getOutputStream();
            ((ChannelExec) channel).setErrStream(out);

            channel.connect();
            out.write(("\n").getBytes());
            out.flush();

            byte[] tmp = new byte[1024];
            while (true)
            {

                while (in.available() > 0)
                {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0)
                        break;

                    // System.out.println(new String(tmp, 0, i));
                }
                if (channel.isClosed())
                {
                    if (in.available() > 0)
                        continue;
                    System.out.println("    Exit-status: "
                            + channel.getExitStatus() + " [" + cmd + "]");
                    break;
                }
                try
                {
                    Thread.sleep(1000);
                }
                catch (Exception ee)
                {
                }
            }
            channel.disconnect();
            session.disconnect();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }

    /*
     * 获取信任关系机器列表
     */
    public static HashMap<String, ArrayList<Host>> getHostMap()
    {
        File hostDir = new File(Constants.HOST_FILE);
        File[] files = hostDir.listFiles(new HostFilenameFilter());

        String hostInfo;
        String fileName;
        String[] hostField;
        Host host;
        ArrayList<Host> al;
        HashMap<String, ArrayList<Host>> hm = new HashMap<String, ArrayList<Host>>();

        for (File file : files)
        {
            al = new ArrayList<Host>();
            fileName = file.getName();

            try
            {
                BufferedReader br = new BufferedReader(new FileReader(file));
                while ((hostInfo = br.readLine()) != null)
                {
                    // System.out.println(hostInfo);

                    if (hostInfo.startsWith("#"))
                    {
                        continue;
                    }

                    hostField = hostInfo.split(" ");

                    host = new Host();
                    host.setHostName(hostField[0]);
                    host.setHostIP(hostField[1]);
                    host.setUsrName(hostField[2]);
                    host.setUsrPwd(hostField[3]);

                    al.add(host);
                }

                br.close();
            }
            catch (FileNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            hm.put(fileName, al);
        }

        return hm;
    };

    /*
     * 建立信任关系
     */
    public static void auth(String fileName, ArrayList<Host> al)
    {
        Host src;
        Host dest;
        int len = al.size();

        // 打印机器列表文件名
        System.out.println("Host File Name: " + fileName);

        for (int i = 0; i < len; i++)
        {
            // 源机器
            src = al.get(i);

            // 源机器信息打印
            System.out.println("  Src " + src.toString());

            // 每台机器重新生成一对密码文件
            genKeyPair(src.getHostIP());

            // 源机器创建.ssh目录（此目录可能会不存在）
            exec(src.getUsrName(), src.getUsrPwd(), src.getHostIP(),
                    "mkdir ~/.ssh");

            // copy公钥密钥到源机器
            ScpTo.send(src.getUsrName(), src.getUsrPwd(), src.getHostIP(),
                    Constants.PRI_KEY_FILE, "~/.ssh/" + Constants.PRI_KEY);
            ScpTo.send(src.getUsrName(), src.getUsrPwd(), src.getHostIP(),
                    Constants.PUB_KEY_FILE, "~/.ssh/" + Constants.PUB_KEY);

            // 修改密钥权限
            exec(src.getUsrName(), src.getUsrPwd(), src.getHostIP(),
                    "chmod 600 ~/.ssh/" + Constants.PRI_KEY);

            // 输出换行
            System.out.println();

            for (int j = 0; j < len; j++)
            {
                dest = al.get(j);

                System.out.println("    Dest " + dest.toString());

                // 目标机器创建.ssh目录
                exec(dest.getUsrName(), dest.getUsrPwd(), dest.getHostIP(),
                        "mkdir ~/.ssh");

                // copy公钥到目标机器
                ScpTo.send(dest.getUsrName(), dest.getUsrPwd(),
                        dest.getHostIP(), Constants.PUB_KEY_FILE, "/tmp/"
                                + Constants.PUB_KEY);

                // 目标机器公钥合并，并删除原文件

                exec(dest.getUsrName(), dest.getUsrPwd(), dest.getHostIP(),
                        "sed -i '/" + src.getHostIP()
                                + "/d' ~/.ssh/authorized_keys; cat /tmp/"
                                + Constants.PUB_KEY
                                + " >> ~/.ssh/authorized_keys && rm -rf /tmp/"
                                + Constants.PUB_KEY);

                // 源机器添加known_hosts
                exec(src.getUsrName(), src.getUsrPwd(), src.getHostIP(),
                        "sed -i '/" + dest.getHostName()
                                + "/d' ~/.ssh/known_hosts; ssh-keyscan -t rsa "
                                + dest.getHostName() + " >> ~/.ssh/known_hosts");
                exec(src.getUsrName(), src.getUsrPwd(), src.getHostIP(),
                        "sed -i '/" + dest.getHostIP()
                                + "/d' ~/.ssh/known_hosts; ssh-keyscan -t rsa "
                                + dest.getHostIP() + " >> ~/.ssh/known_hosts");

                // 输出换行
                System.out.println();
            }
        }
    }
}
