package fakedb;

import java.util.Properties;

/** Tiny (nearly) fake DB
  * (c) 2001 Thomas Neumann
  */

public class Driver implements java.sql.Driver
{
   static {
      try {
         java.sql.DriverManager.registerDriver(new Driver());
      } catch (java.sql.SQLException E) {
         E.printStackTrace();
      }
   }

   public Driver() throws java.sql.SQLException
   {
   }

   // Protocol style: jdbc:fakedb:path
   public synchronized java.sql.Connection connect(String url,Properties info) throws java.sql.SQLException {
      Properties p=parseUrl(url,info);
      if (p==null) return null;
      return new Connection(p.getProperty("PATH"));
   }

   public synchronized boolean acceptsURL(String url) throws java.sql.SQLException {
      return parseUrl(url,null)!=null;
   }

   public java.sql.DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws java.sql.SQLException {
      return new java.sql.DriverPropertyInfo[0];
   }

   public int getMajorVersion() { return 0; }
   public int getMinorVersion() { return 1; }
   public boolean jdbcCompliant() { return false; }

   private Properties parseUrl(String url, Properties defaults)
   {
      if (!url.startsWith("jdbc:fakedb:")) return null;

      String path=url.substring(12);
      if ((path.length()>0)&&(!path.endsWith(java.io.File.separator)))
         path=path+java.io.File.separator;

      Properties result=new Properties(defaults);
      result.put("PATH",path);

      return result;
   }
}
