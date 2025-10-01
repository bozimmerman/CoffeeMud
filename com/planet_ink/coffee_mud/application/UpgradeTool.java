package com.planet_ink.coffee_mud.application;

import java.io.*;
import java.net.*;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;

/*
Copyright 2025-2025 Bo Zimmerman

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
/**
 * An automatic upgrade tool for CoffeeMud.
 * Usage: java -cp . com.planet_ink.coffee_mud.application.UpgradeTool [version]
 * If no version is specified, the latest stable version will be used.
 * If "HEAD" is specified, the latest HEAD snapshot will be used.
 * The tool will download the specified version, compare it to the current version,
 * and merge changes into the current installation.
 * It will back up any files that are changed or deleted.
 * It requires Java 8 or higher and write permissions to the CoffeeMud directory.
 * @see com.planet_ink.coffee_mud.application.CoffeeMud
 * @author Bo Zimmerman
 */
public class UpgradeTool
{

	private static final String VERSION_URL="http://www.zimmers.net/anonftp/pub/projects/coffeemud/versions.txt";
	private static final String	ZIP_URL_TEMPLATE="http://www.zimmers.net/anonftp/pub/projects/coffeemud/all/CoffeeMud_%s.zip";
	private static final String	ZIP_HEAD_TEMPLATE="http://www.coffeemud.net:8080/svnhead/CoffeeMud_Hourly.zip";


	public static void dumpUsage()
	{
		System.err.println("Usage:           java -cp . com.planet_ink.coffee_mud.application.UpgradeTool [options]");
		System.err.println("");
		System.err.println("--to=[version]   If unspecified, the latest stable version will be used.");
		System.err.println("                 If \"HEAD\" is specified, the latest HEAD snapshot will be used.");
		System.err.println("--from=[version] Set current version.  If unspecified, uses source version.");
		System.err.println("                 Choosing a version far from the actual source will increase conflicts.");
		System.err.println("--merge          Merge changes even if non-common local files have been modified from base.");
		System.err.println("--versions       Dump the available versions and exit.");
		System.err.println("--help Show this help message.");
		System.exit(1);
	}

	/**
	 * Main method to run the upgrade tool.
	 *
	 * @param args Command line arguments. Optionally specify the version to
	 *            upgrade to.
	 */
	public static void main(final String[] args)
	{
		boolean merge = false;
		String latestVer=null;
		String userOverrideVer = null;
		if(args.length>0)
		{
			for(int i=0;i<args.length;i++)
			{
				if(args[i].startsWith("--"))
				{
					if(args[i].equalsIgnoreCase("--help"))
						dumpUsage();
					else
					if (args[i].equalsIgnoreCase("--merge"))
						merge = true;
					else
					if (args[i].equalsIgnoreCase("--versions"))
					{
						try
						{
							final List<String> versions = getVersions();
							System.out.println("Available versions:");
							for (final String v : versions)
								System.out.println("  " + v);
							System.exit(0);
						}
						catch (final IOException e)
						{
							System.err.println("Failed to fetch version list: " + e.getMessage());
							System.exit(1);
						}
					}
					else
					if (args[i].startsWith("--from="))
					{
						String version = args[i].substring(7).trim();
						if (version.startsWith("\"") && version.endsWith("\"") && (version.length()>1))
							version = version.substring(1, version.length() - 1).trim();
						if (!Pattern.matches("\\d+(\\.\\d+){0,3}", version))
						{
							System.err.println("Invalid version format: " + version);
							dumpUsage();
						}
						while(version.split("\\.").length != 4)
							version += ".0";
						userOverrideVer=version;
					}
					else
					if(args[i].equalsIgnoreCase("--to=HEAD"))
						latestVer="HEAD";
					else
					if (args[i].startsWith("--to="))
					{
						String version = args[i].substring(5).trim();
						if (version.startsWith("\"") && version.endsWith("\"") && (version.length()>1))
							version = version.substring(1, version.length() - 1).trim();
						if (!Pattern.matches("\\d+(\\.\\d+){0,3}", version))
						{
							System.err.println("Invalid version format: " + version);
							dumpUsage();
						}
						while(version.split("\\.").length != 4)
							version += ".0";
						latestVer=version;
					}
					else
					{
						System.err.println("Invalid argument: " + args[i]);
						dumpUsage();
					}
				}
				else
				{
					System.err.println("Invalid argument: " + args[i]);
					dumpUsage();
				}
			}
		}
		Path backupZip = null;
		final Path root=Paths.get(".");
		try
		{
			String userVer=userOverrideVer;
			final String foundUserVer = getUserVersion(root);
			if(userVer == null)
				userVer = foundUserVer;
			final List<String> versions = getVersions();
			if(latestVer==null)
				latestVer = versions.get(versions.size()-1);
			if(userVer.equals(latestVer))
			{
				System.out.println("Already at version "+userVer+".");
				System.exit(0);
			}
			if(!versions.contains(userVer))
			{
				for (int i = versions.size() - 1; i >= 0; i--)
				{
					final String v = versions.get(i);
					if (compareVersions(v, userVer) < 0)
					{
						if(i == versions.size()-1)
						{
							System.err.println("Current version " + userVer + " not found, and is beyond the latest.");
							System.err.println("Cancelled.");
							System.exit(1);
						}
						if(userOverrideVer == null)
						{
							System.err.println("Current version " + userVer + " not found.  Next available lowest was " + v + ".");
							System.err.println("No valid current version was specified on the command line, so cancelling.");
							System.exit(1);
						}
						if(!v.equals(userOverrideVer))
						{
							System.err.println("Current version " + userVer + " not found.  Next available lowest was " + v + ".");
							System.err.println("Unable to upgrade from an unknown version.");
							System.exit(1);
						}
						System.out.println("Current version " + userVer + " not found. Assuming upgrade from " + v + ".");
						userVer = v;
						break;
					}
				}
			}
			if((!versions.contains(latestVer))
			&& (!latestVer.equals("HEAD")))
			{
				System.err.println("Requested version " + userVer + " not found in version list.");
				final StringBuilder verList = new StringBuilder();
				for (final String v : versions)
					verList.append(v).append(", ");
				if (verList.length() > 2)
					verList.setLength(verList.length() - 2);
				System.err.println("Available versions: " + verList);
				System.exit(1);
			}

			checkPermissions(root);
			backupZip = root.resolve("CoffeeMud_Backup_" + foundUserVer + ".zip");
			if(!Files.exists(backupZip))
			{
				System.out.println("Creating backup: " + backupZip);
				zipDirectory(root, backupZip);
			}

			if(!Files.exists(root.resolve("com")))
			{
				System.err.println("Cannot find 'com' directory. Ensure you are running this in the CoffeeMud root directory.");
				System.exit(1);
			}

			System.out.println("Upgrading CoffeeMud from "+userVer+" to "+latestVer+"...");

			final Path tempDir=Files.createTempDirectory("cmudupgrade");
			final Path baseZip=tempDir.resolve("base.zip");
			downloadZip(userVer, baseZip);
			try
			{
				final Path baseExtract=tempDir.resolve("base");
				unzip(baseZip, baseExtract);

				if(!merge)
				{
					final List<Path> modified=isModifiedFromBase(baseExtract, root);
					if (modified.size() > 0)
					{
						System.err.println("The current installation has been modified from the base version.");
						System.err.println("The following files differ from the base version:");
						for (final Path p : modified)
							System.err.println("  " + p);
						System.err.println("No --merge was specified, so cancelling.");
						if(Files.exists(tempDir))
							deleteDirectory(tempDir);
						System.exit(1);
					}
				}

				final Path latestZip=tempDir.resolve("latest.zip");
				downloadZip(latestVer, latestZip);
				final Path latestExtract=tempDir.resolve("latest");
				unzip(latestZip, latestExtract);

				Files.walkFileTree(root.resolve("com"), new SimpleFileVisitor<Path>()
				{
					@Override
					public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
					{
						if(file.toString().endsWith(".class"))
							Files.delete(file);
						return FileVisitResult.CONTINUE;
					}
				});
				if(Files.exists(root.resolve("com")))
					mergeJava(root, "com", latestExtract, baseExtract);
				if(Files.exists(root.resolve("lib")))
					mergeJava(root, "lib", latestExtract, baseExtract);
				else
					copyDirectory(latestExtract.resolve("lib"), root.resolve("lib"));

				System.out.println("Compiling Java sources...");
				final JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
				if(compiler==null)
					throw new RuntimeException("No Java compiler available. Ensure JDK is installed.");
				final StandardJavaFileManager fileManager=compiler.getStandardFileManager(null, null, null);
				final List<String> javaFiles = new ArrayList<String>();
				try (Stream<Path> walk = Files.walk(root.resolve("com")))
				{
					final Iterator<Path> it = walk.iterator();
					while (it.hasNext())
					{
						final Path p = it.next();
						if (p.toString().endsWith(".java"))
							javaFiles.add(p.toAbsolutePath().toString());
					}
				}
				final List<String> paths;
				if(Files.exists(root.resolve("lib")))
				{
					paths=Files.list(root.resolve("lib"))
							.filter(p -> p.toString().endsWith(".jar"))
							.map(Path::toAbsolutePath)
							.map(Path::toString)
							.collect(Collectors.toList());
				}
				else
					paths = new ArrayList<String>();
				paths.add(".");
				final String classpath=String.join(File.pathSeparator, paths);
				final Iterable<? extends javax.tools.JavaFileObject> compilationUnits=fileManager.getJavaFileObjectsFromStrings(javaFiles);
				final List<String> options=Arrays.asList("-cp", classpath, "-d", root.toAbsolutePath().toString(), "-Xlint:none");
				final Boolean success=compiler.getTask(null, fileManager, null, options, null, compilationUnits).call();
				if(!success.booleanValue())
					throw new RuntimeException("Compilation failed.");

				System.out.println("Merging root, resource, and web files...");
				mergeRootFiles(baseExtract, latestExtract, root);
				deleteObsoleteRootFiles(baseExtract, latestExtract, root);

				final String[] mergeDirs={ "guides", "resources", "web" };
				for(final String dir : mergeDirs)
				{
					final Path baseDir=baseExtract.resolve(dir);
					final Path latestDir=latestExtract.resolve(dir);
					final Path userDir=root.resolve(dir);
					if(Files.exists(latestDir))
					{
						//System.out.println("Merging directory: "+dir);
						mergeDirectory(baseDir, latestDir, userDir);
						deleteObsoleteFiles(baseDir, latestDir, userDir);
					}
				}
				System.out.println("Upgrade complete.");
			}
			finally
			{
				if(Files.exists(tempDir))
					deleteDirectory(tempDir);
			}
		}
		catch(final Exception e)
		{
			System.err.println("Upgrade failed: "+e.getMessage());
			e.printStackTrace();
			if (backupZip != null && Files.exists(backupZip))
			{
				System.err.println("Restoring from backup: " + backupZip);
				try
				{
					unzip(backupZip, root);
					System.err.println("Restoration complete.");
				}
				catch (final IOException ioe)
				{
					System.err.println("Restoration failed: " + ioe.getMessage());
					ioe.printStackTrace();
				}
			}
			else
				System.err.println("No backup available to restore.");
			System.exit(1);
		}
	}

	private static void mergeJava(final Path root, final String rDir, final Path latestExtract, final Path baseExtract) throws IOException
	{
		final Map<Path, byte[]> preservedFiles = new HashMap<Path, byte[]>();
		Files.walkFileTree(root.resolve(rDir), new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
			{
				if (file.toString().endsWith(".java")||rDir.equals("lib"))
				{
					final Path rel = root.resolve(rDir).relativize(file);
					if (!Files.exists(baseExtract.resolve(rDir).resolve(rel))
					&& !Files.exists(latestExtract.resolve(rDir).resolve(rel)))
						preservedFiles.put(file, Files.readAllBytes(file));
				}
				return FileVisitResult.CONTINUE;
			}
		});
		// Delete existing directory if it exists
		if (Files.exists(root.resolve(rDir)))
			deleteDirectory(root.resolve(rDir));
		// Copy latest directory
		copyDirectory(latestExtract.resolve(rDir), root.resolve(rDir));
		// Copy preserved user Java files back to com directory
		for(final Map.Entry<Path, byte[]> entry : preservedFiles.entrySet())
		{
			final Path dest = root.resolve(entry.getKey());
			Files.createDirectories(dest.getParent());
			Files.write(dest, entry.getValue());
			System.out.println("Preserved user Java file: " + dest);
		}
	}

	/**
	 * Checks if the existing installation is unmodified from the base version.
	 * Includes Java files but skips .class files, ignores new files in the user directory.
	 *
	 * @param baseExtract The extracted base version directory.
	 * @param userDir The user installation directory.
	 * @return an empty list, or those modified from base.
	 * @throws IOException If file access fails.
	 */
	private static List<Path> isModifiedFromBase(final Path baseExtract, final Path userDir) throws IOException
	{
		final List<Path> modified = new Vector<Path>();
		Files.walkFileTree(baseExtract, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException
			{
				final String dirName = dir.getFileName().toString();
				if ( dirName.equals("text"))
					return FileVisitResult.SKIP_SUBTREE;
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
			{
				if (file.getFileName().toString().endsWith(".class")
				||file.getFileName().toString().endsWith(".bat")
				||file.getFileName().toString().endsWith(".sh")
				||file.getFileName().toString().endsWith("intro.jpg")
				||file.getFileName().toString().endsWith("laws.ini")
				||file.getFileName().toString().equals("coffeemud.ini"))
					return FileVisitResult.CONTINUE;
				final Path rel = baseExtract.relativize(file);
				final Path userFile = userDir.resolve(rel);
				if(!Files.exists(userFile) || !sameFiles(file, userFile))
				{
					synchronized (modified)
					{
						modified.add(userFile);
					}
				}
				return FileVisitResult.CONTINUE;
			}
		});
		return modified;
	}

	/**
	 * Check read/write permissions on key directories.
	 *
	 * @param root The root path of the CoffeeMud installation.
	 * @throws IOException If any directory is not readable or writable.
	 */
	private static void checkPermissions(final Path root) throws IOException
	{
		final String[] keyDirs={ "com", "lib", "guides", "resources", "web" };
		for(final String dirName : keyDirs)
		{
			final Path dir=root.resolve(dirName);
			if(Files.exists(dir))
			{
				if(!Files.isReadable(dir) || !Files.isWritable(dir) || !Files.isDirectory(dir))
					throw new AccessDeniedException("Insufficient permissions for directory: "+dir+". Ensure read/write access.");
				final Path testFile=dir.resolve("permission_test.tmp");
				try
				{
					Files.createFile(testFile);
					Files.delete(testFile);
				}
				catch(final IOException e)
				{
					throw new AccessDeniedException("Cannot write to directory: "+dir+". Permission denied.");
				}
			}
		}
		if(!Files.isWritable(root))
			throw new AccessDeniedException("Cannot write to root directory: "+root+". Permission denied.");
	}

	/**
	 * Get the current user version from MUD.java.
	 *
	 * @param root The root path of the CoffeeMud installation.
	 * @return The current version string.
	 * @throws IOException If MUD.java cannot be found or parsed.
	 */
	private static String getUserVersion(final Path root) throws IOException
	{
		final Pattern stringPattern=Pattern.compile("HOST_VERSION\\s*=\\s*\"([^\"]+)\"");
		final Pattern majorPattern=Pattern.compile("HOST_VERSION_MAJOR\\s*=\\s*\\(float\\)\\s*(\\d+\\.\\d+)");
		final Pattern minorPattern=Pattern.compile("HOST_VERSION_MINOR\\s*=\\s*(?:(?:\\(float\\)|long|int|float)\\s*(\\d+\\.\\d+)|(\\d+))");
		Optional<Path> mudJava = Optional.empty();
		final String comPath = (new File("com.bak").exists()) ? "com.bak" : "com";
		try (Stream<Path> walk = Files.walk(root.resolve(comPath)))
		{
			final Iterator<Path> it = walk.iterator();
			while (it.hasNext())
			{
				final Path p = it.next();
				if (p.getFileName().toString().equals("MUD.java"))
				{
					mudJava = Optional.of(p);
					break;
				}
			}
		}
		if(!mudJava.isPresent())
			throw new IOException("Cannot find MUD.java");
		final String content=new String(Files.readAllBytes(mudJava.get()), StandardCharsets.UTF_8);

		final Matcher stringMatcher=stringPattern.matcher(content);
		if(stringMatcher.find())
		{
			final String version=stringMatcher.group(1);
			final String[] parts=version.split("\\.");
			final StringBuilder normalized=new StringBuilder();
			for(int i=0; i<4; i++)
				normalized.append(i<parts.length ? parts[i] : "0").append(".");
			return normalized.substring(0, normalized.length()-1);
		}
		final Matcher majorMatcher=majorPattern.matcher(content);
		final Matcher minorMatcher=minorPattern.matcher(content);
		String major="0";
		String minor="0";
		if(majorMatcher.find())
			major=majorMatcher.group(1);
		if(minorMatcher.find())
			minor=minorMatcher.group(1) != null ? minorMatcher.group(1) : minorMatcher.group(2);
		if(!major.equals("0") || !minor.equals("0"))
		{
			String ver=major+"."+minor;
			while(ver.split("\\.").length != 4)
				ver += ".0";
			return ver;
		}
		throw new IOException("Cannot find HOST_VERSION or HOST_VERSION_MAJOR/MINOR in MUD.java");
	}

	/**
	 * Get the list of available versions from the version URL.
	 * @return A list of version strings.
	 * @throws IOException If the version list cannot be fetched or parsed.
	 */
	private static List<String> getVersions() throws IOException
	{
		final URL url=new URL(VERSION_URL+"?time="+System.currentTimeMillis());
		final HttpURLConnection conn =(HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "text/plain");
		final BufferedReader reader=new BufferedReader(new InputStreamReader(conn.getInputStream()));
		final List<String> versions = new Vector<String>();
		String version;
		while((version=reader.readLine()) != null)
		{
			version = version.trim().replace('_','.');
			if((version.length()>0)&&(version.indexOf('.')>0))
			{
				while(version.split("\\.").length<4)
					version+=".0";
				versions.add(version);
			}
		}
		reader.close();
		if(versions.size()==0)
			throw new IOException("Cannot parse latest version from zimmmrs.net");
		versions.sort(new Comparator<String>() {
			@Override
			public int compare(final String v1, final String v2)
			{
				return compareVersions(v1, v2);
			}
		});
		return versions;
	}

	/**
	 * Recursively zip a directory and its contents.
	 *
	 * @param sourceDir The source directory path to zip.
	 * @param zipFile The destination zip file path.
	 * @throws IOException If zipping fails.
	 */
	private static void zipDirectory(final Path sourceDir, final Path zipFile) throws IOException
	{
		try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile)))
		{
			Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>()
			{
				@Override
				public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException
				{
					final String dirName = dir.getFileName().toString();
					if (dirName.equals(".git")
					|| dirName.equals(".svn")
					|| dirName.equals("com.bak")
					|| dirName.equals("lib.bak"))
						return FileVisitResult.SKIP_SUBTREE;
					zos.putNextEntry(new ZipEntry(sourceDir.relativize(dir).toString() + "/"));
					zos.closeEntry();
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
				{
					final String fileName = file.getFileName().toString();
					if (fileName.endsWith(".zip") && fileName.startsWith("CoffeeMud_Backup_"))
						return FileVisitResult.CONTINUE;
					zos.putNextEntry(new ZipEntry(sourceDir.relativize(file).toString()));
					Files.copy(file, zos);
					zos.closeEntry();
					return FileVisitResult.CONTINUE;
				}
			});
		}
	}

	/**
	 * Compare two version strings in the format "x.y.z.w".
	 *
	 * @param v1 The first version string.
	 * @param v2 The second version string.
	 * @return Negative if v1 < v2, positive if v1 > v2, zero if equal.
	 */
	private static int compareVersions(final String v1, final String v2)
	{
		final String[] p1 = v1.split("\\.");
		final String[] p2 = v2.split("\\.");
		for (int i = 0; i < 4; i++)
		{
			final int n1 = Integer.parseInt(p1[i]);
			final int n2 = Integer.parseInt(p2[i]);
			if (n1 != n2)
				return Integer.compare(n1, n2);
		}
		return 0;
	}

	/**
	 * Download the zip file for the specified version to the destination path.
	 * @param version The version to download, or "HEAD" for the latest snapshot.
	 * @param dest The destination path to save the zip file.
	 * @throws IOException If the download fails.
	 */
	private static void downloadZip(final String version, final Path dest) throws IOException
	{
		final String tag=version.replace(".", "_");
		final URL url;
		if(version.equals("HEAD"))
			url=new URL(ZIP_HEAD_TEMPLATE);
		else
			url=new URL(String.format(ZIP_URL_TEMPLATE, tag));
		final HttpURLConnection conn =(HttpURLConnection) url.openConnection();
		final int contentLength=conn.getContentLength();
		System.out.println("Downloading "+url+" ...");
		try(InputStream in=conn.getInputStream(); OutputStream out=Files.newOutputStream(dest))
		{
			final byte[] buffer=new byte[8192];
			int bytesRead;
			long total=0;
			while((bytesRead=in.read(buffer)) != -1)
			{
				out.write(buffer, 0, bytesRead);
				total += bytesRead;
				if(contentLength>0)
					System.out.print("\rProgress: " +(total * 100 / contentLength)+"%");
			}
			System.out.println();
		}
	}

	/**
	 * Unzip the specified zip file to the destination directory. If the zip
	 * contains a top-level CoffeeMud/ directory, its contents will be extracted
	 * directly into the destination directory.
	 *
	 * @param zipFile The path to the zip file.
	 * @param destDir The destination directory to extract to.
	 * @throws IOException If extraction fails.
	 */
	private static void unzip(final Path zipFile, final Path destDir) throws IOException
	{
		try(ZipInputStream zis=new ZipInputStream(Files.newInputStream(zipFile)))
		{
			boolean hasCoffeeMudDir=false;
			try(ZipInputStream checkZis=new ZipInputStream(Files.newInputStream(zipFile)))
			{
				ZipEntry checkEntry;
				while((checkEntry=checkZis.getNextEntry()) != null)
				{
					if(checkEntry.getName().toLowerCase().startsWith("coffeemud/"))
					{
						hasCoffeeMudDir=true;
						break;
					}
				}
			}
			ZipEntry entry;
			while((entry=zis.getNextEntry()) != null)
			{
				String entryName=entry.getName();
				if(hasCoffeeMudDir && entryName.toLowerCase().startsWith("coffeemud/"))
					entryName=entryName.substring("coffeemud/".length());
				final Path newPath=destDir.resolve(entryName);
				if(entry.isDirectory())
					Files.createDirectories(newPath);
				else
				{
					Files.createDirectories(newPath.getParent());
					try(OutputStream out=Files.newOutputStream(newPath))
					{
						final byte[] buffer=new byte[8192];
						int len;
						while((len=zis.read(buffer))>0)
							out.write(buffer, 0, len);
					}
				}
				zis.closeEntry();
			}
		}
	}

	/**
	 * Recursively copy a directory from source to destination.
	 *
	 * @param src The source directory path.
	 * @param dest The destination directory path.
	 * @throws IOException If copying fails.
	 */
	private static void copyDirectory(final Path src, final Path dest) throws IOException
	{
		Files.walkFileTree(src, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException
			{
				Files.createDirectories(dest.resolve(src.relativize(dir)));
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
			{
				Files.copy(file, dest.resolve(src.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Recursively delete a directory and all its contents.
	 *
	 * @param dir The directory path to delete.
	 * @throws IOException If deletion fails.
	 */
	private static void deleteDirectory(final Path dir) throws IOException
	{
		Files.walkFileTree(dir, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
			{
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException
			{
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Check if a file is a text file by attempting to read it as UTF-8.
	 * @param file The file path to check.
	 * @return True if the file is a text file, false if it is binary.
	 */
	private static boolean isTextFile(final Path file)
	{
		final String fileName = file.getFileName().toString().toLowerCase();
		if (fileName.endsWith(".ini")
		|| fileName.endsWith(".properties")
		|| fileName.endsWith(".cmare")
		|| fileName.endsWith(".txt")
		|| fileName.endsWith(".bat")
		|| fileName.endsWith(".sh")
		|| fileName.endsWith(".xml")
		|| fileName.endsWith(".quest")
		|| fileName.endsWith(".script")
		|| fileName.endsWith(".js"))
			return true;
		try(BufferedReader reader=Files.newBufferedReader(file, StandardCharsets.UTF_8))
		{
			String line;
			long x = 0;
			while((line=reader.readLine()) != null)
				x = line.length() + x;
			return true;
		}
		catch(final IOException e)
		{
			if((e  instanceof MalformedInputException)
			||(e.getCause() instanceof MalformedInputException))
				return false;
			throw new RuntimeException(e);
		}
	}

	/**
	 * Check if two files are identical by comparing their sizes and contents.
	 *
	 * @param file1 The first file path.
	 * @param file2 The second file path.
	 * @return True if the files are identical, false otherwise.
	 * @throws IOException If reading the files fails.
	 */
	private static boolean sameFiles(final Path file1, final Path file2) throws IOException
	{
		if(!Files.exists(file1) || !Files.exists(file2))
			return false;
		if(Files.size(file1) != Files.size(file2))
			return false;
		return Arrays.equals(Files.readAllBytes(file1),  Files.readAllBytes(file2));
	}

	/**
	 * Reads all lines from a file using UTF-8 decoding, tolerantly handling
	 * malformed input by replacing invalid sequences with the Unicode
	 * replacement character (?). This preserves the structure of the lines
	 * while substituting un decodable parts.
	 *
	 * @param file The path to the file to read.
	 * @return A list of strings, each representing a line from the file.
	 * @throws IOException If an I/O error occurs reading from the file.
	 */
	private static List<String> readAllLinesTolerant(final Path file) throws IOException
	{
		final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
		decoder.onMalformedInput(CodingErrorAction.REPLACE);
		decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
		final List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file), decoder)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				lines.add(line);
			}
		}
		return lines;
	}

	/**
	 * Merge three directories and their content, recursively.
	 * @param baseDir The base directory path.
	 * @param latestDir The latest version directory path.
	 * @param userDir The user directory path.
	 * @throws IOException If merging fails.
	 */
	private static void mergeDirectory(final Path baseDir, final Path latestDir, final Path userDir) throws IOException
	{
		Files.walkFileTree(latestDir, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException
			{
				final Path rel=latestDir.relativize(dir);
				final Path userD=userDir.resolve(rel);
				Files.createDirectories(userD);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
			{
				final Path rel=latestDir.relativize(file);
				final Path userFile=userDir.resolve(rel);
				final Path baseFile=baseDir.resolve(rel);
				final Path latestFile=file;

				final String fileNameStr = userFile.getFileName().toString();
				if(userDir.getFileName().toString().equals("guides")
				&&(fileNameStr.toLowerCase().endsWith(".html")))
				{
					Files.copy(latestFile, userFile, StandardCopyOption.REPLACE_EXISTING);
					return FileVisitResult.CONTINUE;
				}

				if(sameFiles(baseFile,userFile))
				{
					Files.copy(latestFile, userFile, StandardCopyOption.REPLACE_EXISTING);
					return FileVisitResult.CONTINUE;
				}
				if(!Files.exists(userFile))
				{
					Files.copy(latestFile, userFile, StandardCopyOption.REPLACE_EXISTING);
					return FileVisitResult.CONTINUE;
				}
				if((!Files.exists(baseFile))
				||(baseFile.getFileName().toString().startsWith("intro")))
				{
					if(!sameFiles(latestFile,userFile))
						System.out.println("Keeping user file: "+userFile);
					return FileVisitResult.CONTINUE;
				}
				if(!isTextFile(latestFile) || !isTextFile(userFile) || !isTextFile(baseFile))
				{
					System.out.println("Keeping user binary file: "+userFile);
					return FileVisitResult.CONTINUE;
				}

				final List<String> baseLines=readAllLinesTolerant(baseFile);
				final List<String> userLines=readAllLinesTolerant(userFile);
				final List<String> latestLines=readAllLinesTolerant(latestFile);

				List<String> merged;
				if(fileNameStr.endsWith(".ini") || fileNameStr.endsWith(".properties"))
					merged=iniThreeWayMerge(baseLines, userLines, latestLines, userFile.toString());
				else
					merged=threeWayMerge(baseLines, userLines, latestLines, userFile.toString());

				Files.write(userFile, merged);

				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Perform a three-way merge of files, not not recursively.
	 *
	 * @param baseDir The base directory path.
	 * @param latestDir The latest version directory path.
	 * @param userDir The user directory path.
	 * @throws IOException If merging fails.
	 */
	private static void mergeRootFiles(final Path baseDir, final Path latestDir, final Path userDir) throws IOException
	{
		try(DirectoryStream<Path> stream=Files.newDirectoryStream(latestDir))
		{
			for(final Path file : stream)
			{
				if(Files.isDirectory(file))
					continue;
				final Path rel=latestDir.relativize(file);
				final Path userFile=userDir.resolve(rel);
				final Path baseFile=baseDir.resolve(rel);
				final Path latestFile=file;

				if(sameFiles(baseFile, userFile))
				{
					Files.copy(latestFile, userFile, StandardCopyOption.REPLACE_EXISTING);
					continue;
				}
				if(!Files.exists(userFile))
				{
					Files.copy(latestFile, userFile, StandardCopyOption.REPLACE_EXISTING);
					continue;
				}
				if(!Files.exists(baseFile))
				{
					if(!sameFiles(latestFile, userFile))
						System.out.println("Keeping user file: "+userFile);
					continue;
				}
				if(!isTextFile(latestFile) || !isTextFile(userFile) || !isTextFile(baseFile))
				{
					System.out.println("Keeping user binary file: "+userFile);
					continue;
				}

				final List<String> baseLines=readAllLinesTolerant(baseFile);
				final List<String> userLines=readAllLinesTolerant(userFile);
				final List<String> latestLines=readAllLinesTolerant(latestFile);

				final String fileNameStr=userFile.getFileName().toString();
				List<String> merged;
				if(fileNameStr.endsWith(".ini") || fileNameStr.endsWith(".properties"))
					merged=iniThreeWayMerge(baseLines, userLines, latestLines, userFile.toString());
				else
					merged=threeWayMerge(baseLines, userLines, latestLines, userFile.toString());

				Files.write(userFile, merged);
			}
		}
	}

	/**
	 * Delete obsolete files in the user directory that existed in the base
	 * version but not in the latest version, if they are unchanged from
	 * the base version.
	 *
	 * @param baseDir The base directory path.
	 * @param latestDir The latest version directory path.
	 * @param userDir The user directory path.
	 * @throws IOException If merging fails.
	 */
	private static void deleteObsoleteRootFiles(final Path baseDir, final Path latestDir, final Path userDir) throws IOException
	{
		try(DirectoryStream<Path> stream=Files.newDirectoryStream(userDir))
		{
			for(final Path userFile : stream)
			{
				if(Files.isDirectory(userFile))
					continue;
				final Path rel=userDir.relativize(userFile);
				final Path baseFile=baseDir.resolve(rel);
				final Path latestFile=latestDir.resolve(rel);
				if(Files.exists(baseFile) && !Files.exists(latestFile))
				{
					final byte[] userBytes=Files.readAllBytes(userFile);
					final byte[] baseBytes=Files.readAllBytes(baseFile);
					if(Arrays.equals(userBytes, baseBytes))
					{
						//System.out.println("Deleting obsolete unchanged file: "+userFile);
						Files.delete(userFile);
					}
					else
						System.out.println("Keeping modified obsolete file: "+userFile);
				}
			}
		}
	}

	/**
	 * Recursively delete obsolete files in the user directory that existed in the base
	 * version but not in the latest version, if they are unchanged from
	 * the base version.
	 *
	 * @param baseDir The base directory path.
	 * @param latestDir The latest version directory path.
	 * @param userDir The user directory path.
	 * @throws IOException If merging fails.
	 */
	private static void deleteObsoleteFiles(final Path baseDir, final Path latestDir, final Path userDir) throws IOException
	{
		Files.walkFileTree(userDir, new HashSet<FileVisitOption>(), Integer.MAX_VALUE, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult visitFile(final Path userFile, final BasicFileAttributes attrs) throws IOException
			{
				final Path rel=userDir.relativize(userFile);
				final Path baseFile=baseDir.resolve(rel);
				final Path latestFile=latestDir.resolve(rel);
				if(Files.exists(baseFile) && !Files.exists(latestFile))
				{
					final byte[] userBytes=Files.readAllBytes(userFile);
					final byte[] baseBytes=Files.readAllBytes(baseFile);
					if(Arrays.equals(userBytes, baseBytes))
					{
						//System.out.println("Deleting obsolete unchanged file: "+userFile);
						Files.delete(userFile);
					}
					else
						System.out.println("Keeping modified obsolete file: "+userFile);
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException
			{
				if(exc==null)
				{
					try(DirectoryStream<Path> stream=Files.newDirectoryStream(dir))
					{
						if(!stream.iterator().hasNext())
						{
							//System.out.println("Deleting empty directory: "+dir);
							Files.delete(dir);
						}
					}
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Class for a section in an INI file.
	 */
	private static class IniSection
	{
		List<String>	comments	= new ArrayList<>();
		String			name;
		String			sectionLine;
		List<IniEntry>	entries		= new ArrayList<>();
	}

	/**
	 * Class for an entry in an INI file.
	 */
	private static class IniEntry
	{
		List<String>	comments	= new ArrayList<>();
		String			key;
		String			value;
		String			entryLine;
	}

	/**
	 * Parses an INI file into sections and entries.
	 *
	 * @param lines The lines of the INI file.
	 * @return A list of IniSection objects representing the parsed INI file.
	 */
	private static List<IniSection> parseIni(final List<String> lines)
	{
		final List<IniSection> sections=new ArrayList<>();
		IniSection currentSection=new IniSection();
		currentSection.name="";
		List<String> comments=new ArrayList<>();
		int index=0;
		while(index<lines.size())
		{
			String original=lines.get(index);
			String trimmed=original.trim();
			if(trimmed.isEmpty())
			{
				comments.add(original);
				index++;
				continue;
			}
			if(trimmed.startsWith("#") || trimmed.startsWith("!"))
			{
				comments.add(original);
				index++;
				continue;
			}
			if(trimmed.startsWith("[") && trimmed.endsWith("]"))
			{
				if(!currentSection.entries.isEmpty() || !currentSection.comments.isEmpty() || !currentSection.name.isEmpty())
					sections.add(currentSection);
				currentSection=new IniSection();
				currentSection.sectionLine=original;
				currentSection.name=trimmed.substring(1, trimmed.length()-1);
				currentSection.comments=comments;
				comments=new ArrayList<>();
				index++;
				continue;
			}
			final List<String> entryLines=new ArrayList<>();
			entryLines.add(original);
			trimmed=original.trim();
			int eq=trimmed.indexOf('=');
			if(eq<0)
			{
				comments.add(original);
				index++;
				continue;
			}
			while(trimmed.endsWith("\\") && (index+1<lines.size()))
			{
				index++;
				original=lines.get(index);
				trimmed=original.trim();
				entryLines.add(original);
			}
			final String entryLine=String.join("\n", entryLines);
			final String fullText=entryLine.replace("\\\n", "");
			eq=fullText.indexOf('=');
			if(eq<0)
			{
				comments.addAll(entryLines);
				index++;
				continue;
			}
			final String key=fullText.substring(0, eq).trim();
			final String value=fullText.substring(eq+1).trim();
			final IniEntry e=new IniEntry();
			e.key=key;
			e.value=value;
			e.entryLine=entryLine;
			e.comments=comments;
			currentSection.entries.add(e);
			comments=new ArrayList<>();
			index++;
		}
		if(!comments.isEmpty())
		{
			if(!currentSection.entries.isEmpty())
				currentSection.entries.get(currentSection.entries.size()-1).comments.addAll(comments);
			else
				currentSection.comments.addAll(comments);
		}
		if((!currentSection.entries.isEmpty())
		||(!currentSection.comments.isEmpty())
		||(!currentSection.name.isEmpty()))
			sections.add(currentSection);
		return sections;
	}

	/**
	 * Perform a three-way merge of INI files.
	 *
	 * @param base The lines of the base file.
	 * @param user The lines of the user file.
	 * @param latest The lines of the latest file.
	 * @param fileName The name of the file being merged (for logging).
	 * @return The merged lines.
	 */
	private static List<String> iniThreeWayMerge(final List<String> base, final List<String> user, final List<String> latest, final String fileName)
	{
		final List<IniSection> baseSections=parseIni(base);
		final List<IniSection> userSections=parseIni(user);
		final List<IniSection> latestSections=parseIni(latest);

		final Map<String, IniSection> baseMap = new HashMap<>();
		for(final IniSection s : baseSections)
		{
			final IniSection existing = baseMap.put(s.name, s);
			if(existing != null)
				System.out.println("Duplicate section found in " + fileName + ": " + s.name + ", keeping last.");
		}

		final Map<String, IniSection> userMap = new HashMap<>();
		for(final IniSection s : userSections)
		{
			final IniSection existing = userMap.put(s.name, s);
			if(existing != null)
				System.out.println("Duplicate section found in " + fileName + ": " + s.name + ", keeping last.");
		}

		final Map<String, IniSection> latestMap = new HashMap<>();
		for(final IniSection s : latestSections)
		{
			final IniSection existing = latestMap.put(s.name, s);
			if(existing != null)
				System.out.println("Duplicate section found in " + fileName + ": " + s.name + ", keeping last.");
		}
		final List<IniSection> mergedSections=userSections.stream().map(us ->
		{
			final IniSection ms=new IniSection();
			ms.name=us.name;
			ms.sectionLine=us.sectionLine;
			ms.comments=new ArrayList<>(us.comments);
			ms.entries=new ArrayList<>(us.entries);
			return ms;
		}).collect(Collectors.toList());

		int i=0;
		while(i<latestSections.size())
		{
			IniSection lSec=latestSections.get(i);
			if(userMap.containsKey(lSec.name) || baseMap.containsKey(lSec.name))
			{
				i++;
				continue;
			}
			final List<IniSection> chain=new ArrayList<>();
			chain.add(lSec);
			i++;
			while(i<latestSections.size())
			{
				lSec=latestSections.get(i);
				if(userMap.containsKey(lSec.name) || baseMap.containsKey(lSec.name))
					break;
				chain.add(lSec);
				i++;
			}
			final int start=i-chain.size();
			String prev=null;
			if(start>0)
				prev=latestSections.get(start-1).name;
			int insertIndex=mergedSections.size();
			boolean found=false;
			if(prev != null)
			{
				for(int j=0; j<mergedSections.size(); j++)
				{
					if(mergedSections.get(j).name.equals(prev))
					{
						insertIndex=j+1;
						found=true;
						break;
					}
				}
			}
			if(!found)
			{
				String next=null;
				if(i<latestSections.size())
					next=latestSections.get(i).name;
				if(next != null)
				{
					for(int j=0; j<mergedSections.size(); j++)
					{
						if(mergedSections.get(j).name.equals(next))
						{
							insertIndex=j;
							break;
						}
					}
				}
			}
			final List<IniSection> chainCopy=chain.stream().map(ls ->
			{
				final IniSection m=new IniSection();
				m.name=ls.name;
				m.sectionLine=ls.sectionLine;
				m.comments=new ArrayList<>(ls.comments);
				m.entries=ls.entries.stream().map(le ->
				{
					final IniEntry me=new IniEntry();
					me.key=le.key;
					me.value=le.value;
					me.entryLine=le.entryLine;
					me.comments=new ArrayList<>(le.comments);
					return me;
				}).collect(Collectors.toList());
				return m;
			}).collect(Collectors.toList());
			mergedSections.addAll(insertIndex, chainCopy);
		}

		for(final IniSection m : mergedSections)
		{
			final String sectionName=m.name;
			final IniSection b=baseMap.get(sectionName);
			final IniSection u=userMap.get(sectionName);
			final IniSection l=latestMap.get(sectionName);
			if(l != null)
			{
				m.comments=new ArrayList<>(l.comments);
				m.sectionLine=l.sectionLine;
			}
			else
			{
				m.comments=new ArrayList<>(u.comments);
				m.sectionLine=u.sectionLine;
			}
			final Map<String, IniEntry> baseEntryMap = new HashMap<>();
			if(b != null)
			{
				for(final IniEntry e : b.entries)
					baseEntryMap.put(e.key, e);
			}

			final Map<String, IniEntry> userEntryMap = new HashMap<>();
			if(u != null)
			{
				for(final IniEntry e : u.entries)
				{
					final IniEntry existing = userEntryMap.put(e.key, e);
					if(existing != null)
						System.out.println("Duplicate entry found in " + fileName + " section [" + sectionName + "]: " + e.key + ", keeping last.");
				}
			}

			final Map<String, IniEntry> latestEntryMap = new HashMap<>();
			if(l != null)
			{
				for(final IniEntry e : l.entries)
				{
					final IniEntry existing = latestEntryMap.put(e.key, e);
					if(existing != null)
						System.out.println("Duplicate entry found in " + fileName + " section [" + sectionName + "]: " + e.key + ", keeping last.");
				}
			}
			final List<IniEntry> mergedEntries=new ArrayList<>(m.entries);
			if(l != null)
			{
				int ii=0;
				while(ii<l.entries.size())
				{
					IniEntry le=l.entries.get(ii);
					if(userEntryMap.containsKey(le.key) || baseEntryMap.containsKey(le.key))
					{
						ii++;
						continue;
					}
					final List<IniEntry> chain=new ArrayList<>();
					chain.add(le);
					ii++;
					while(ii<l.entries.size())
					{
						le=l.entries.get(ii);
						if(userEntryMap.containsKey(le.key) || baseEntryMap.containsKey(le.key))
							break;
						chain.add(le);
						ii++;
					}
					final int start=ii-chain.size();
					String prevKey=null;
					if(start>0)
						prevKey=l.entries.get(start-1).key;
					int insertIndex=mergedEntries.size();
					boolean found=false;
					if(prevKey != null)
					{
						for(int j=0; j<mergedEntries.size(); j++)
						{
							if(mergedEntries.get(j).key.equals(prevKey))
							{
								insertIndex=j+1;
								found=true;
								break;
							}
						}
					}
					if(!found)
					{
						String nextKey=null;
						if(ii<l.entries.size())
							nextKey=l.entries.get(ii).key;
						if(nextKey != null)
						{
							for(int j=0; j<mergedEntries.size(); j++)
							{
								if(mergedEntries.get(j).key.equals(nextKey))
								{
									insertIndex=j;
									break;
								}
							}
						}
					}
					final List<IniEntry> chainCopy=chain.stream().map(ls ->
					{
						final IniEntry me=new IniEntry();
						me.key=ls.key;
						me.value=ls.value;
						me.entryLine=ls.entryLine;
						me.comments=new ArrayList<>(ls.comments);
						return me;
					}).collect(Collectors.toList());
					mergedEntries.addAll(insertIndex, chainCopy);
				}
			}
			for(final IniEntry me : mergedEntries)
			{
				final IniEntry be=baseEntryMap.get(me.key);
				final IniEntry ue=userEntryMap.get(me.key);
				final IniEntry le=latestEntryMap.get(me.key);
				String chosenValue=(ue != null)? ue.value :((le != null)? le.value : "");
				String chosenEntryLine=(ue != null)?ue.entryLine :((le != null)?le.entryLine : "");
				List<String> chosenComments=new ArrayList<>();
				boolean valueConflict=false;
				if(be != null)
				{
					if(le != null)
					{
						chosenComments=new ArrayList<>(le.comments);
						if(((ue != null)&& ue.value.equals(be.value))
						|| (ue==null))
						{
							chosenValue=le.value;
							chosenEntryLine=le.entryLine;
						}
						else
						{
							chosenValue=ue.value;
							chosenEntryLine=ue.entryLine;
							if((!le.value.equals(be.value))&&(!ue.value.equals(le.value)))
								valueConflict=true;
						}
					}
					else
					if(ue != null)
						chosenComments=new ArrayList<>(ue.comments);
				}
				else
				{
					chosenComments=ue != null
							? new ArrayList<>(ue.comments)
							: (le != null) ? new ArrayList<>(le.comments) : new ArrayList<>();
					if((le != null)&&(ue != null)&&(!ue.value.equals(le.value)))
						valueConflict=true;
				}
				me.value=chosenValue;
				me.entryLine=chosenEntryLine;
				me.comments=chosenComments;
				if(valueConflict)
				{
					System.out.println("Conflict in value for key "+me.key+" in section ["+sectionName+"] in " +
							fileName+", keeping user value: "+chosenValue+", discarding latest: "+((le != null)? le.value : "")+".");
				}
			}
			m.entries=mergedEntries;
		}
		for(final IniSection m : mergedSections)
		{
			final String sectionName=m.name;
			final IniSection l=latestMap.getOrDefault(sectionName, null);
			final Map<String, IniEntry> latestEntryMap = new HashMap<>();
			if(l != null)
			{
				for(final IniEntry e : l.entries)
				{
					final IniEntry existing = latestEntryMap.put(e.key, e);
					if(existing != null)
						System.out.println("Duplicate entry found in " + fileName + " section [" + sectionName + "]: " + e.key + ", keeping last.");
				}
			}

			final IniSection b = baseMap.get(sectionName);

			final Map<String, IniEntry> baseEntryMap = new HashMap<>();
			if(b != null)
			{
				for(final IniEntry e : b.entries)
					baseEntryMap.put(e.key, e);
			}
			final List<IniEntry> finalEntries=new ArrayList<>();
			for(final IniEntry me : m.entries)
			{
				if(latestEntryMap.containsKey(me.key))
				{
					finalEntries.add(me);
					continue;
				}
				final IniEntry be=baseEntryMap.get(me.key);
				if(be==null)
				{
					finalEntries.add(me);
					continue;
				}
				if((!me.value.equals(be.value))||(!me.entryLine.equals(be.entryLine))||(!me.comments.equals(be.comments)))
				{
					System.out.println("Keeping modified obsolete entry: "+me.key+" in section ["+sectionName+"] in "+fileName);
					finalEntries.add(me);
				}
				//else
				//	System.out.println("Deleting obsolete unchanged entry: "+me.key+" in section ["+sectionName+"] in "+fileName);
			}
			m.entries=finalEntries;
		}
		final List<IniSection> finalMergedSections=new ArrayList<>();
		for(final IniSection m : mergedSections)
		{
			final String sectionName=m.name.isEmpty() ? "" : m.name;
			if(latestMap.containsKey(sectionName))
			{
				finalMergedSections.add(m);
				continue;
			}
			final IniSection b=baseMap.get(sectionName);
			if(b==null)
			{
				finalMergedSections.add(m);
				continue;
			}
			boolean changed=!m.comments.equals(b.comments) || !m.sectionLine.equals(b.sectionLine) || m.entries.size() != b.entries.size();
			if(!changed)
			{
				for(int ii=0; ii<m.entries.size(); ii++)
				{
					final IniEntry me=m.entries.get(ii);
					final IniEntry be=b.entries.get(ii);
					if(!me.key.equals(be.key) || !me.value.equals(be.value) || !me.entryLine.equals(be.entryLine) || !me.comments.equals(be.comments))
					{
						changed=true;
						break;
					}
				}
			}
			if(changed)
			{
				System.out.println("Keeping modified obsolete section: ["+sectionName+"] in "+fileName);
				finalMergedSections.add(m);
			}
			//else
			//	System.out.println("Deleting obsolete unchanged section: ["+sectionName+"] in "+fileName);
		}

		final List<String> merged=new ArrayList<>();
		for(final IniSection sec : finalMergedSections)
		{
			merged.addAll(sec.comments);
			if(!sec.name.isEmpty() && sec.sectionLine != null)
				merged.add(sec.sectionLine);
			for(final IniEntry e : sec.entries)
			{
				merged.addAll(e.comments);
				merged.add(e.entryLine);
			}
		}
		return merged;
	}

	/**
	 * Class for an operation (insertion or deletion) in the three-way merge
	 * process.
	 */
	private static class Operation implements Comparable<Operation>
	{
		int				pos;
		int				type;	// 0 remove, 1 insert
		List<String>	lines;

		/**
		 * Constructor.
		 *
		 * @param pos The position in the user file.
		 * @param type The type of operation (0 remove, 1 insert).
		 * @param lines The lines to insert (for insert operations).
		 */
		public Operation(final int pos, final int type, final List<String> lines)
		{
			this.pos=pos;
			this.type=type;
			this.lines=lines;
		}

		@Override
		public int compareTo(final Operation o)
		{
			return Integer.compare(this.pos, o.pos);
		}
	}

	/**
	 * Get the index in the user file where lines from the latest file should be
	 * inserted, based on the surrounding lines in the base file.
	 *
	 * @param prevBaseI The index of the previous matching line in the base
	 *            file.
	 * @param nextBaseI The index of the next matching line in the base file.
	 * @param matchingUser The map of matching lines between base and user
	 *            files.
	 * @param size The current size of the user file.
	 * @return The index in the user file for insertion.
	 */
	private static int getInsertIndex(final int prevBaseI, final int nextBaseI, final Map<Integer, Integer> matchingUser, final int size)
	{
		int insertIndex=size;
		boolean found=false;
		if(prevBaseI >= 0)
		{
			final Integer u=matchingUser.get(Integer.valueOf(prevBaseI));
			if(u != null)
			{
				insertIndex=u.intValue()+1;
				found=true;
			}
		}
		if(!found &&(nextBaseI >= 0))
		{
			final Integer u=matchingUser.get(Integer.valueOf(nextBaseI));
			if(u != null)
				insertIndex=u.intValue();
		}
		return insertIndex;
	}

	/**
	 * Perform a three-way merge of text files.
	 *
	 * @param base The lines of the base file.
	 * @param user The lines of the user file.
	 * @param latest The lines of the latest file.
	 * @param fileName The name of the file being merged (for logging).
	 * @return The merged lines.
	 */
	private static List<String> threeWayMerge(final List<String> base, final List<String> user, final List<String> latest, final String fileName)
	{
		if(base.equals(user))
			return new ArrayList<>(latest);

		final LCS<String> lcsUser=new LCS<>(base, user);
		final LCS<String> lcsLatest=new LCS<>(base, latest);
		final Map<Integer, Integer> matchingUser=lcsUser.getMatching();
		final Map<Integer, Integer> matchingLatest=lcsLatest.getMatching();

		final List<String> merged=new ArrayList<>(user);
		for(final Map.Entry<Integer, Integer> entry : matchingUser.entrySet())
		{
			final int baseI=entry.getKey().intValue();
			if(matchingLatest.containsKey(Integer.valueOf(baseI)))
			{
				final int userI=entry.getValue().intValue();
				final int latestI=matchingLatest.get(Integer.valueOf(baseI)).intValue();
				final String baseL=base.get(baseI);
				final String userL=user.get(userI);
				final String latestL=latest.get(latestI);
				if(userL.equals(baseL))
					merged.set(userI, latestL);
				else
					System.out.println("Conflict in "+fileName+", keeping user line: "+userL+", discarding latest: "+latestL);
			}
		}
		final List<Operation> operations=new ArrayList<>();
		for(final Map.Entry<Integer, Integer> entry : matchingUser.entrySet())
		{
			final int baseI=entry.getKey().intValue();
			if(!matchingLatest.containsKey(Integer.valueOf(baseI)))
			{
				final int userI=entry.getValue().intValue();
				operations.add(new Operation(userI, 0, null));
			}
		}
		final List<DiffEntry<String>> latestDiff=lcsLatest.diff();
		List<String> chain=new ArrayList<>();
		int prevBaseI=-1;
		int basePos=0;
		int k=0;
		while(k<latestDiff.size())
		{
			final DiffEntry<String> entry=latestDiff.get(k);
			if(entry.getType()==DiffType.EQUAL)
			{
				if(!chain.isEmpty())
				{
					final int insertIndex=getInsertIndex(prevBaseI, basePos, matchingUser, merged.size());
					operations.add(new Operation(insertIndex, 1, new ArrayList<>(chain)));
					chain=new ArrayList<>();
				}
				prevBaseI=basePos;
				basePos++;
				k++;
				continue;
			}
			if(entry.getType()==DiffType.ADD)
			{
				chain.add(entry.getValue());
				k++;
				continue;
			}
			if(entry.getType()==DiffType.REMOVE)
			{
				basePos++;
				if((k+1<latestDiff.size())&&(latestDiff.get(k+1).getType()==DiffType.ADD))
				{
					final String newLine=latestDiff.get(k+1).getValue();
					final Integer userI=matchingUser.get(Integer.valueOf(basePos-1));
					if(userI != null)
					{
						final String baseL=entry.getValue();
						final String userL=merged.get(userI.intValue());
						if(userL.equals(baseL))
							merged.set(userI.intValue(), newLine);
						else
							System.out.println("Conflict in "+fileName+", keeping user line: "+userL+", discarding latest: "+newLine);
					}
					else
						System.out.println("Conflict in "+fileName+", discarding latest line: "+newLine);
					k += 2;
					continue;
				}
				k++;
			}
		}
		if(!chain.isEmpty())
		{
			final int insertIndex=getInsertIndex(prevBaseI, -1, matchingUser, merged.size());
			operations.add(new Operation(insertIndex, 1, new ArrayList<>(chain)));
		}
		operations.sort(Operation::compareTo);
		int shift=0;
		for(final Operation op : operations)
		{
			final int adjustedPos=op.pos+shift;
			if(op.type==0)
			{
				merged.remove(adjustedPos);
				shift--;
			}
			else
			{
				merged.addAll(adjustedPos, op.lines);
				shift += op.lines.size();
			}
		}

		return merged;
	}

	/**
	 * Class to compute the Longest Common Subsequence (LCS) between two lists.
	 *
	 * @param <VALUE> The type of elements in the lists.
	 */
	public static class LCS<VALUE>
	{
		protected int[][]					lengths;
		protected List<VALUE>				x, y;
		protected List<DiffEntry<VALUE>>	diff;
		protected int						length	= -1;

		/**
		 * Constructor.
		 *
		 * @param x The first list.
		 * @param y The second list.
		 */
		public LCS(final List<VALUE> x, final List<VALUE> y)
		{
			this.x=x;
			this.y=y;
			lengths=new int[x.size()+1][y.size()+1];
		}

		/**
		 * Calculate the lengths table for the LCS algorithm.
		 */
		public void calculateLcs()
		{
			for(int i=1; i<x.size()+1; i++)
			{
				for(int j=1; j<y.size()+1; j++)
				{
					if(x.get(i-1).equals(y.get(j-1)))
						lengths[i][j]=lengths[i-1][j-1]+1;
					else
						lengths[i][j]=Math.max(lengths[i][j-1], lengths[i-1][j]);
				}
			}
		}

		/**
		 * Get the length of the Longest Common Subsequence.
		 *
		 * @return The length of the LCS.
		 */
		public int length()
		{
			if(length<0)
			{
				calculateLcs();
				length=lengths[x.size()][y.size()];
			}
			return length;
		}

		/**
		 * Get the diff between the two lists as a list of DiffEntry objects.
		 *
		 * @return The list of DiffEntry objects representing the diff.
		 */
		public List<DiffEntry<VALUE>> diff()
		{
			calculateLcs();
			if(this.diff==null)
			{
				this.diff=new ArrayList<>();
				diff(x.size(), y.size());
			}
			return this.diff;
		}

		/**
		 * Recursive helper method to compute the diff.
		 *
		 * @param i The current index in the first list.
		 * @param j The current index in the second list.
		 */
		private void diff(final int i, final int j)
		{
			final List<DiffEntry<VALUE>> tempDiff = new ArrayList<>();
			int ci = i;
			int cj = j;
			while (ci > 0 || cj > 0)
			{
				if ((ci > 0) && (cj > 0) && (x.get(ci - 1).equals(y.get(cj - 1))))
				{
					tempDiff.add(new DiffEntry<>(DiffType.EQUAL, x.get(ci - 1)));
					ci--;
					cj--;
				}
				else
				if ((cj > 0) && ((ci == 0) || (lengths[ci][cj - 1] >= lengths[ci - 1][cj])))
				{
					tempDiff.add(new DiffEntry<>(DiffType.ADD, y.get(cj - 1)));
					cj--;
				}
				else
				if ((ci > 0) && ((cj == 0) || (lengths[ci][cj - 1] < lengths[ci - 1][cj])))
				{
					tempDiff.add(new DiffEntry<>(DiffType.REMOVE, x.get(ci - 1)));
					ci--;
				}
			}
			// Reverse the list since we built it from the end
			Collections.reverse(tempDiff);
			this.diff = tempDiff;
		}

		/**
		 * Get a mapping of matching indices between the two lists.
		 *
		 * @return A map where keys are indices in the first list and values are
		 *         corresponding indices in the second list.
		 */
		public Map<Integer, Integer> getMatching()
		{
			calculateLcs();
			final Map<Integer, Integer> matching=new HashMap<>();
			getMatching(x.size(), y.size(), matching);
			return matching;
		}

		/**
		 * Recursive helper method to compute the matching indices.
		 *
		 * @param i The current index in the first list.
		 * @param j The current index in the second list.
		 * @param matching The map to store matching indices.
		 */
		private void getMatching(final int i, final int j, final Map<Integer, Integer> matching)
		{
			int ci = i;
			int cj = j;
			while ((ci > 0)||(cj > 0))
			{
				if ((ci > 0)&&(cj > 0)&&(x.get(ci - 1).equals(y.get(cj - 1))))
				{
					matching.put(Integer.valueOf(ci - 1), Integer.valueOf(cj - 1));
					ci--;
					cj--;
				}
				else
				if((cj > 0)&& ((ci == 0) || (lengths[ci][cj - 1] >= lengths[ci - 1][cj])))
					cj--;
				else
				if ((ci > 0) && ((cj == 0) || (lengths[ci][cj - 1] < lengths[ci - 1][cj])))
					ci--;
			}
		}
	}

	/**
	 * Class representing a single entry in the diff output.
	 *
	 * @param <VALUE> The type of the value in the diff entry.
	 */
	public static class DiffEntry<VALUE>
	{
		private final DiffType	type;
		private final VALUE		value;

		public DiffEntry(final DiffType type, final VALUE value)
		{
			this.type=type;
			this.value=value;
		}

		public DiffType getType()
		{
			return type;
		}

		public VALUE getValue()
		{
			return value;
		}
	}

	/**
	 * Enum representing the type of difference in the diff output.
	 */
	public enum DiffType
	{
		ADD("+ "),
		REMOVE("- "),
		EQUAL("  ");

		public final String sign;
		DiffType(final String sign)
		{
			this.sign=sign;
		}
	}
}