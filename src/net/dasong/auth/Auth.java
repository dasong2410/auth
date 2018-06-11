package net.dasong.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Auth
{
    public static void main(String[] arg)
    {
        HashMap<String, ArrayList<Host>> ha = CommonUtl.getHostMap();
        Set<String> hostSet = ha.keySet();
        Iterator<String> it = hostSet.iterator();
        String hostFileName;
        ArrayList<Host> al;

        while (it.hasNext())
        {
            hostFileName = it.next();
            al = ha.get(hostFileName);

            CommonUtl.auth(hostFileName, al);
        }

        System.out.println("Done.");
    }
}
