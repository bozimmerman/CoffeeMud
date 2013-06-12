package com.planet_ink.miniweb.http;

import java.util.Hashtable;

/*
Copyright 2012-2013 Bo Zimmerman

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
 * Static enum of the supported mime types and a mapping mechanism.
 * 
 * @author Bo Zimmerman
 *
 */
public enum MIMEType
{
	h323("323","text/h323"),
	DEFAULT("*","application/octet-stream"),
	acx("acx","application/internet-property-stream"),
	adobeJson("json","application/vnd.adobe.dex+json"),
	ai("ai","application/postscript"),
	aif("aif","audio/x-aiff"),
	aifc("aifc","audio/x-aiff"),
	aiff("aiff","audio/x-aiff"),
	asc("asc","text/plain"),
	ascii("ascii","text/plain"),
	asf("asf","video/x-ms-asf"),
	asr("asr","video/x-ms-asf"),
	asx("asx","video/x-ms-asf"),
	au("au","audio/basic"),
	avi("avi","video/x-msvideo"),
	axs("axs","application/olescript"),
	bas("bas","text/plain"),
	bcpio("bcpio","application/x-bcpio"),
	bin("bin","application/octet-stream"),
	bmp("bmp","image/bmp"),
	c("c","text/plain"),
	cat("cat","application/vnd.ms-pkiseccat"),
	cdf("cdf","application/x-cdf"),
	cer("cer","application/x-x509-ca-cert"),
	cla("cla","application/octet-stream"),
	classfile("class","application/octet-stream"),
	clp("clp","application/x-msclip"),
	cmvp("cmvp","text/html"),
	cmx("cmx","image/x-cmx"),
	cod("cod","image/cis-cod"),
	cpio("cpio","application/x-cpio"),
	crd("crd","application/x-mscardfile"),
	crl("crl","application/pkix-crl"),
	crt("crt","application/x-x509-ca-cert"),
	csh("csh","application/x-csh"),
	css("css","text/css"),
	dcr("dcr","application/x-director"),
	der("der","application/x-x509-ca-cert"),
	dir("dir","application/x-director"),
	dll("dll","application/x-msdownload"),
	dms("dms","application/octet-stream"),
	doc("doc","application/msword"),
	dot("dot","application/msword"),
	dvi("dvi","application/x-dvi"),
	dxr("dxr","application/x-director"),
	eps("eps","application/postscript"),
	etx("etx","text/x-setext"),
	evy("evy","application/envoy"),
	exe("exe","application/octet-stream"),
	fif("fif","application/fractals"),
	flr("flr","x-world/x-vrml"),
	gif("gif","image/gif"),
	gtar("gtar","application/x-gtar"),
	gz("gz","application/x-gzip"),
	h("h","text/plain"),
	hdf("hdf","application/x-hdf"),
	hlp("hlp","application/winhlp"),
	hqx("hqx","application/mac-binhex40"),
	hta("hta","application/hta"),
	htc("htc","text/x-component"),
	htm("htm","text/html"),
	html("html","text/html"),
	htt("htt","text/webviewhtml"),
	ico("ico","image/x-icon"),
	ief("ief","image/ief"),
	iii("iii","application/x-iphone"),
	ins("ins","application/x-internet-signup"),
	isp("isp","application/x-internet-signup"),
	jfif("jfif","image/pipeg"),
	jpe("jpe","image/jpeg"),
	jpeg("jpeg","image/jpeg"),
	jpg("jpg","image/jpeg"),
	js("js","application/x-javascript"),
	latex("latex","application/x-latex"),
	lha("lha","application/octet-stream"),
	lsf("lsf","video/x-la-asf"),
	lsx("lsx","video/x-la-asf"),
	lzh("lzh","application/octet-stream"),
	m13("m13","application/x-msmediaview"),
	m14("m14","application/x-msmediaview"),
	m3u("m3u","audio/x-mpegurl"),
	man("man","application/x-troff-man"),
	mdb("mdb","application/x-msaccess"),
	me("me","application/x-troff-me"),
	mht("mht","message/rfc822"),
	mhtml("mhtml","message/rfc822"),
	mid("mid","audio/midi"),
	midi("midi","audio/midi"),
	mny("mny","application/x-msmoney"),
	mov("mov","video/quicktime"),
	movie("movie","video/x-sgi-movie"),
	mp2("mp2","video/mpeg"),
	mp3("mp3","audio/mpeg"),
	mpa("mpa","video/mpeg"),
	mpe("mpe","video/mpeg"),
	mpeg("mpeg","video/mpeg"),
	mpg("mpg","video/mpeg"),
	mpp("mpp","application/vnd.ms-project"),
	mpv2("mpv2","video/mpeg"),
	ms("ms","application/x-troff-ms"),
	msg("msg","application/vnd.ms-outlook"),
	mvb("mvb","application/x-msmediaview"),
	mwhtml("mwhtml","text/html"),
	nc("nc","application/x-netcdf"),
	nws("nws","message/rfc822"),
	oda("oda","application/oda"),
	p10("p10","application/pkcs10"),
	p12("p12","application/x-pkcs12"),
	p7b("p7b","application/x-pkcs7-certificates"),
	p7c("p7c","application/x-pkcs7-mime"),
	p7m("p7m","application/x-pkcs7-mime"),
	p7r("p7r","application/x-pkcs7-certreqresp"),
	p7s("p7s","application/x-pkcs7-signature"),
	pbm("pbm","image/x-portable-bitmap"),
	pdf("pdf","application/pdf"),
	pfx("pfx","application/x-pkcs12"),
	pgm("pgm","image/x-portable-graymap"),
	pko("pko","application/ynd.ms-pkipko"),
	pma("pma","application/x-perfmon"),
	pmc("pmc","application/x-perfmon"),
	pml("pml","application/x-perfmon"),
	pmr("pmr","application/x-perfmon"),
	pmw("pmw","application/x-perfmon"),
	png("png","image/png"),
	pnm("pnm","image/x-portable-anymap"),
	pot("pot","application/vnd.ms-powerpoint"),
	ppm("ppm","image/x-portable-pixmap"),
	pps("pps","application/vnd.ms-powerpoint"),
	ppt("ppt","application/vnd.ms-powerpoint"),
	prf("prf","application/pics-rules"),
	ps("ps","application/postscript"),
	pub("pub","application/x-mspublisher"),
	qt("qt","video/quicktime"),
	ra("ra","audio/x-pn-realaudio"),
	ram("ram","audio/x-pn-realaudio"),
	ras("ras","image/x-cmu-raster"),
	rgb("rgb","image/x-rgb"),
	rmi("rmi","audio/mid"),
	roff("roff","application/x-troff"),
	rtf("rtf","application/rtf"),
	rtx("rtx","text/richtext"),
	scd("scd","application/x-msschedule"),
	sct("sct","text/scriptlet"),
	setpay("setpay","application/set-payment-initiation"),
	setreg("setreg","application/set-registration-initiation"),
	sh("sh","application/x-sh"),
	shar("shar","application/x-shar"),
	sit("sit","application/x-stuffit"),
	snd("snd","audio/basic"),
	spc("spc","application/x-pkcs7-certificates"),
	spl("spl","application/futuresplash"),
	src("src","application/x-wais-source"),
	sst("sst","application/vnd.ms-pkicertstore"),
	stl("stl","application/vnd.ms-pkistl"),
	stm("stm","text/html"),
	sv4cpio("sv4cpio","application/x-sv4cpio"),
	sv4crc("sv4crc","application/x-sv4crc"),
	svg("svg","image/svg+xml"),
	swf("swf","application/x-shockwave-flash"),
	t("t","application/x-troff"),
	tar("tar","application/x-tar"),
	tcl("tcl","application/x-tcl"),
	text("text","text/plaim"),
	tex("tex","application/x-tex"),
	texi("texi","application/x-texinfo"),
	texinfo("texinfo","application/x-texinfo"),
	tgz("tgz","application/x-compressed"),
	tif("tif","image/tiff"),
	tiff("tiff","image/tiff"),
	tr("tr","application/x-troff"),
	trm("trm","application/x-msterminal"),
	tsv("tsv","text/tab-separated-values"),
	txt("txt","text/plain"),
	uls("uls","text/iuls"),
	ustar("ustar","application/x-ustar"),
	vcf("vcf","text/x-vcard"),
	vrml("vrml","x-world/x-vrml"),
	wav("wav","audio/x-wav"),
	wcm("wcm","application/vnd.ms-works"),
	wdb("wdb","application/vnd.ms-works"),
	wks("wks","application/vnd.ms-works"),
	wmf("wmf","application/x-msmetafile"),
	wps("wps","application/vnd.ms-works"),
	wri("wri","application/x-mswrite"),
	wrl("wrl","x-world/x-vrml"),
	wrz("wrz","x-world/x-vrml"),
	xaf("xaf","x-world/x-vrml"),
	xbm("xbm","image/x-xbitmap"),
	xla("xla","application/vnd.ms-excel"),
	xlc("xlc","application/vnd.ms-excel"),
	xlm("xlm","application/vnd.ms-excel"),
	xls("xls","application/vnd.ms-excel"),
	xlt("xlt","application/vnd.ms-excel"),
	xlw("xlw","application/vnd.ms-excel"),
	xml("xml","text/xml"),
	xof("xof","x-world/x-vrml"),
	xpm("xpm","image/x-xpixmap"),
	xwd("xwd","image/x-xwindowdump"),
	z("z","application/x-compress"),
	zip("zip","application/zip")
	;
	private final String type;
	private final String ext;
	private final String[] parts;
	
	private MIMEType(String ext, String type)
	{
		this.ext=ext;
		this.type=type;
		this.parts=type.split("/");
	}
	public String[] getParts()
	{
		return parts;
	}
	public String getExt()
	{
		return ext;
	}
	public String getType()
	{
		return type;
	}
	
	// below here are static mime-type utility functions
	
	private static final Hashtable<String,MIMEType> hashedTypes;
	static
	{
		hashedTypes = new Hashtable<String,MIMEType>();
		for(MIMEType type : MIMEType.values())
			hashedTypes.put(type.getExt(), type);
	}
	
	/**
	 * Finds an appropriate mime type given the file path.
	 * It will extract the extension, if any, to find it.
	 * @param filePath a complete or partial file path
	 * @return a matching file type, or the default if not found
	 */
	public static MIMEType getMIMEType(final String filePath)
	{
		String extension;
		int x=filePath.lastIndexOf('.');
		if(x>=0)
			extension=filePath.substring(x+1).toLowerCase();
		else
			extension=filePath.toLowerCase();
		if(hashedTypes.containsKey(extension))
			return hashedTypes.get(extension);
		return MIMEType.DEFAULT;
	}
	
	/**
	 * Returns true if the received content-type map matches this MIMEType.
	 * @param mask a mime-type mask sent as part of a request
	 * @return true if they match, false otherwise
	 */
	public boolean matches(String mask)
	{
		int x=mask.indexOf(';');
		if(x > 0) // eat the quality modifier -- not supported
			mask = mask.substring(0,x).trim();
		else
			mask = mask.trim();
		String[] maskParts=mask.split("/");
		if(maskParts.length != 2)
			return false;
		if(parts.length != 2)
			return true;
		if(((maskParts[0].equals("*")||maskParts[0].equalsIgnoreCase(parts[0])))
		&&((maskParts[1].equals("*")||maskParts[1].equalsIgnoreCase(parts[1]))))
			return true;
		return false;
	}
}
