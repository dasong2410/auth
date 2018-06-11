package net.dasong.auth;

public class Host
{
    private String hostName;
    private String hostIP;
    private String usrName;
    private String usrPwd;

    public String getHostName()
    {
        return hostName;
    }

    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }

    public String getHostIP()
    {
        return hostIP;
    }

    public void setHostIP(String hostIP)
    {
        this.hostIP = hostIP;
    }

    public String getUsrName()
    {
        return usrName;
    }

    public void setUsrName(String usrName)
    {
        this.usrName = usrName;
    }

    public String getUsrPwd()
    {
        return usrPwd;
    }

    public void setUsrPwd(String usrPwd)
    {
        this.usrPwd = usrPwd;
    }

    public String toString()
    {
        return "HostName: " + hostName + ", HostIP: " + hostIP + ", UsrName: "
                + usrName + ", UsrPwd: " + usrPwd;
    }
}
