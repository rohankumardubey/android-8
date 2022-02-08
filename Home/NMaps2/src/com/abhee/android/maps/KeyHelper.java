package com.abhee.android.maps;

class KeyHelper
{

    private KeyHelper()
    {
    }

    static java.lang.String getSignatureFingerprint(android.content.pm.PackageManager pm, java.lang.String packageName)
    {
        android.content.pm.ApplicationInfo ai;
        android.content.pm.PackageInfo pi;
        byte signature[];
        android.security.MessageDigest md;
        byte digest[];
        try
        {
            ai = pm.getApplicationInfo(packageName, 0);
            if(ai == null)
                return null;
            
            if((ai.flags & 1) != 0)
                return "SYSTEM";
            pi = pm.getPackageInfo(packageName, 64);
            if(pi == null || pi.signatures == null || pi.signatures.length == 0 || pi.signatures[0] == null)
                return null;
            signature = pi.signatures[0].toByteArray();
            md = android.security.MessageDigest.getInstance("MD5");
            if(md == null)
                return null;
            digest = md.digest(signature);
            if(digest == null)
                return null;
        }
        catch(android.content.pm.PackageManager.NameNotFoundException e)
        {
            return null;
        }
        catch(java.security.NoSuchAlgorithmException e)
        {
            return null;
        }
        return com.nader.android.maps.KeyHelper.toHex(digest);
    }

    private static java.lang.String toHex(byte bytes[])
    {
        java.lang.StringBuffer sb = new StringBuffer(bytes.length * 2);
        byte arr[] = bytes;
        int len = arr.length;
        for(int i = 0; i < len; i++)
        {
            byte b = arr[i];
            sb.append(java.lang.String.format("%02x", new java.lang.Object[] {
                java.lang.Byte.valueOf(b)
            }));
        }

        return sb.toString();
    }

    static final java.lang.String SYSTEM = "SYSTEM";
}