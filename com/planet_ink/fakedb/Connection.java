package fakedb;

import java.util.Properties;
import java.lang.ref.WeakReference;

public class Connection implements java.sql.Connection
{
   static private java.util.Map databases = new java.util.HashMap();

   static private void log(String x) {
      System.err.println("Connection: "+x);
   }

   private Backend backend;

   Backend getBackend() { return backend; }

   public Connection(String path) throws java.sql.SQLException
   {
      try {
         path=(new java.io.File(path)).getCanonicalPath();
      } catch (java.io.IOException e) {}

      synchronized (databases) {
         WeakReference ref=(WeakReference)databases.get(path);
         Backend       backend=null;
         if (ref!=null)
            backend=(Backend)ref.get();
         if (backend==null) {
            backend=new Backend();
            if (!backend.open(new java.io.File(path)))
               throw new java.sql.SQLException("unable to open database");
            databases.put(path,new WeakReference(backend));
         }
         this.backend=backend;
      }
   }

   public java.sql.Statement createStatement() throws java.sql.SQLException
   {
      return new Statement(this);
   }
   public java.sql.Statement createStatement(int a,int b) throws java.sql.SQLException
   { return createStatement(); }
   public java.sql.Statement createStatement(int a,int b,int c) throws java.sql.SQLException
   { return createStatement(); }

   public java.sql.PreparedStatement prepareStatement(String sql) throws java.sql.SQLException
   {
      log("prepareStatement");
      // XXX
      return null;
   }
   public java.sql.PreparedStatement prepareStatement(String sql,int a) throws java.sql.SQLException
   { return prepareStatement(sql); }
   public java.sql.PreparedStatement prepareStatement(String sql,int[] a) throws java.sql.SQLException
   { return prepareStatement(sql); }
   public java.sql.PreparedStatement prepareStatement(String sql,String[] a) throws java.sql.SQLException
   { return prepareStatement(sql); }
   public java.sql.PreparedStatement prepareStatement(String sql,int a,int b) throws java.sql.SQLException
   { return prepareStatement(sql); }
   public java.sql.PreparedStatement prepareStatement(String sql,int a,int[] b) throws java.sql.SQLException
   { return prepareStatement(sql); }
   public java.sql.PreparedStatement prepareStatement(String sql,int a,int b,int c) throws java.sql.SQLException
   { return prepareStatement(sql); }

   public java.sql.CallableStatement prepareCall(String sql) throws java.sql.SQLException
   {
      log("prepareCall");
      throw new java.sql.SQLException("Callable statments not suppoted.", "S1C00");
   }
   public java.sql.CallableStatement prepareCall(String sql,int a,int b) throws java.sql.SQLException
   { return prepareCall(sql); }
   public java.sql.CallableStatement prepareCall(String sql,int a,int b,int c) throws java.sql.SQLException
   { return prepareCall(sql); }

   public String nativeSQL(String sql) throws java.sql.SQLException
   {
      return sql;
   }
   public void setAutoCommit(boolean autoCommit) throws java.sql.SQLException
   {
      log("setAutoCommit");
      if (!autoCommit)
	  throw new java.sql.SQLException("Cannot disable AUTO_COMMIT", "08003");
      return;
   }
   public boolean getAutoCommit() throws java.sql.SQLException
   {
      return true;
   }
   public void commit() throws java.sql.SQLException
   {
      log("commit");
   }
   public void rollback() throws java.sql.SQLException
   {
      log("rollback");
   }
   public void close() throws java.sql.SQLException
   {
      log("close");
   }
   public boolean isClosed() throws java.sql.SQLException
   {
      log("isClosed");
      return false;
   }

   public java.sql.DatabaseMetaData getMetaData() throws java.sql.SQLException
   {
      log("getMetaData");
      // XXX?
      return null;
   }
   public void setReadOnly (boolean readOnly) throws java.sql.SQLException
   {
      log("setReadOnly");
   }
   public boolean isReadOnly() throws java.sql.SQLException
   {
      return false;
   }
   public void setCatalog(String Catalog) throws java.sql.SQLException
   {
      log("setCatalog");
   }
   public String getCatalog() throws java.sql.SQLException
   {
      log("getCatalog");
      return "FAKEDB";
   }
   public void setTransactionIsolation(int level) throws java.sql.SQLException
   {
      log("setTransactionIsolation");
      throw new java.sql.SQLException("Transaction Isolation Levels are not supported.", "S1C00");
   }
   public int getTransactionIsolation() throws java.sql.SQLException
   {
      return java.sql.Connection.TRANSACTION_NONE;
   }
   public java.sql.SQLWarning getWarnings() throws java.sql.SQLException
   {
      log("getWarnings");
      return null;
   }
   public void clearWarnings() throws java.sql.SQLException
   {
      log("clearWarnings");
   }


   public void setHoldability(int holdability) throws java.sql.SQLException
      {}
   public java.util.Map getTypeMap() throws java.sql.SQLException
      { return new java.util.HashMap(); }
   public void setTypeMap(java.util.Map map) throws java.sql.SQLException
      {}

   // JDK 1.4 stuff
/*
   public int getHoldability() throws java.sql.SQLException
      { return java.sql.ResultSet.CLOSE_CURSORS_AT_COMMIT; }
   public void releaseSavepoint(java.sql.Savepoint savepoint) throws java.sql.SQLException
      {}
   public java.sql.Savepoint setSavepoint() throws java.sql.SQLException
      { return null; }
   public java.sql.Savepoint setSavepoint(String name) throws java.sql.SQLException
      { return null; }
   public void rollback(java.sql.Savepoint p) throws java.sql.SQLException
   { rollback(); }
*/
}
