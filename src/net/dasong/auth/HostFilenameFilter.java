package net.dasong.auth;

import java.io.File;
import java.io.FilenameFilter;

public class HostFilenameFilter implements FilenameFilter
{

    @Override
    public boolean accept(File file, String name)
    {
        // TODO Auto-generated method stub
        // System.out.println(name);

        return name.endsWith(".host");
    }
}
