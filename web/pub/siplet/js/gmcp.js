window.gmcpPackages = [];

window.gmcpPackages.push({
	name: "Core",
	lname: "core.",
	version: "1",
	hello: function(sipwin, msg) { 
		/* uh, ok */
		if(!sipwin.gmcpSupported)
			sipwin.gmcpSupported = {};
	},
	supports_set: function(sipwin, msg) {
		UpdateGMCPSupports(sipwin, msg, 'set');
	},
	supports_add: function(sipwin, msg) {
		UpdateGMCPSupports(sipwin, msg, 'add');
	},
	supports_remove: function(sipwin, msg) {
		UpdateGMCPSupports(sipwin, msg, 'remove');
	}
});

window.gmcpPackages.push({
	name: "Client.Media",
	lname: "client.media.",
	version: "1",
	load: function(sipwin, msg) {
		if(!isJsonObject(msg))
			return;
		if((msg.name)&&(!msg.file))
			msg.file = msg.name;
		if((!msg.file) || (!msg.url))
			return;
		sipwin.msp.LoadSound(msg.file, msg.url, msg.tag, false);
	},
	play: function(sipwin, msg) {
		if(!isJsonObject(msg))
			return;
		if((msg.name)&&(!msg.file))
			msg.file = msg.name;
		if((!msg.file) || (!msg.type))
			return;
		if (msg.type !== 'sound' && msg.type !== 'music') {
			console.warn(`Invalid type in Client.Media.Play: ${msg.type}`);
			return;
		}
		msg.volume = isNumber(msg.volume)?Number(msg.volume):50;
		msg.url = msg.url || '';
		msg.tag = msg.tag || '';
		msg.loops = isNumber(msg.loops)?Number(msg.loops):1;
		msg.loops = (msg.loops <= 0 ? (msg.loops < 0 ? -1 : 1) : msg.loops);
		msg.priority = isNumber(msg.priority)?Number(msg.priority):50;
		if(msg.type == 'sound')
			sipwin.msp.PlaySound(msg.file, msg.url, msg.loops, msg.volume, msg.priority, false, msg.tag)
		else
		if(msg.type == 'music')
			sipwin.msp.PlaySound(msg.file, msg.url, msg.loops, msg.volume, msg.priority, true, msg.tag)
	},
	stop: function(sipwin, msg) {
		if(!isJsonObject(msg))
			return;
		if((msg.name)&&(!msg.file))
			msg.file = msg.name;
		if(msg.file || msg.type)
			sipwin.msp.StopSound(msg.file, msg.type);
		else
			sipwin.msp.StopSound();
	}
});

window.gmcpPackages.push({
	name: "Char.Login",
	lname: "char.login.",
	version: "1",
	default: function(sipwin, msg) {
		if(!isJsonObject(msg))
			return;
		if(typeof msg.type === 'string')
		{
			if("password-credentials" !== msg.type)
				return;
		}
		else
		if(Array.isArray(msg.type))
		{
			if(msg.type.indexOf("password-credentials")<0)
				return;
		}
		else
			return;
		if(sipwin.pb && sipwin.pb.user && sipwin.pb.password)
		{
			var loginObj = {
				account: sipwin.pb.accountName || sipwin.pb.user,
				password: sipwin.pb.password
			};
			sipwin.sendGMCP('Char.Login.Credentials', loginObj);
		}
		
	},
	response: function(sipwin, msg) {
		// good for us?
	},
	credentials: function(sipwin, msg) {
		// we will never get this one
	}
});

window.gmcpPackages.push({
	name: "Siplet",
	lname: "siplet.",
	version: "1",
	input: function(sipwin, msg) {
		if(!isJsonObject(msg))
			return;
		window.sipletInputTitle = msg["title"];
		window.sipletInputText = msg["text"];
		var content = getOptionWindow("Siplet.Input",60,40);
		populateDivFromUrl(content, 'dialogs/editor.htm');
		window.SubmitSipletInputEntry = function()
		{
			var textarea = content.getElementsByTagName('textarea')[0];
			if(textarea)
			{
				sendOneLine(textarea.value);
				hideOptionWindow();
				setTimeout(setInputBoxFocus,500);
			}
		};
		var SipletInputEntryFocus = function()
		{
			var textarea = content.getElementsByTagName('textarea')[0];
			if(textarea)
				textarea.focus();
		};
		setTimeout(SipletInputEntryFocus,1000);
	}
});

window.gmcpPackages.push({
	name: "IRE.Composer.Edit",
	lname: "ire.composer.",
	version: "1",
	edit: function(sipwin, msg) {
		if(!isJsonObject(msg))
			return;
		window.sipletInputTitle = msg["title"];
		window.sipletInputText = msg["text"];
		var content = getOptionWindow("Siplet.Input",60,40);
		populateDivFromUrl(content, 'dialogs/editor.htm');
		window.SubmitSipletInputEntry = function()
		{
			var textarea = content.getElementsByTagName('textarea')[0];
			if(textarea)
			{
				var txt = textarea.value;
				txt = txt.replaceAll('\r','\\r')
						.replaceAll('\n','\\n');
				window.currWin.submitInput(txt);
				hideOptionWindow();
				setTimeout(setInputBoxFocus,500);
			}
		};
		var SipletInputEntryFocus = function()
		{
			var textarea = content.getElementsByTagName('textarea')[0];
			if(textarea)
				textarea.focus();
		};
		setTimeout(SipletInputEntryFocus,1000);
	}
});

window.gmcpPackages.push({
	name: "WebView",
	lname: "webview.",
	version: "1",
	open: function(sipwin, msg) {
		if(!isJsonObject(msg))
			return;
		if(!msg["url"])
			return;
		var url = msg["url"];
		var id = msg["id"];
		if(!id)
			id="WEBVIEW";
		var dock = msg["dock"];
		if(!dock)
			dock = "";
		if(["","top","bottom","left","right"].indexOf(dock.toLowerCase())<0)
			dock="";
		var headerObj = msg["http-request-headers"]
		if(!isJsonObject(headerObj))
			headerObj = {};
		var framechoices = sipwin.mxp.getFrameMap();
		if(!(id in framechoices))
		{
			var rest="width=25%";
			if(["top","bottom"].indexOf(dock)>=0)
				rest="height=25%";
			if(dock != "")
				sipwin.process('<FRAME ACTION=OPEN INTERNAL NAME="'+id+'"  TITLE="WebView" ALIGN='+dock+' '+rest+'>');
			else
				sipwin.process('<FRAME ACTION=OPEN FLOATING NAME="'+id+'" TITLE="WebView" LEFT=25% TOP=25% HEIGHT=50% WIDTH=50%>');
			framechoices = sipwin.mxp.getFrameMap();
		}
		if(!(id in framechoices))
			return;
		var frame = framechoices[id];
		if(frame.sprops && frame.firstChild)
			frame = frame.firstChild;
		sipwin.cleanDiv(frame);
		var iframeId = "webview_iframe_"+id.replace(' ','_');
		frame.innerHTML = '<iframe id="'+iframeId+'"style="width: 100%; height: 100%; border: none; background-color: white;"></iframe>';
		fetch(url, {
			method: 'GET',
			headers: headerObj
		}).then(function(response) {
			if (!response.ok) throw new Error('Fetch failed');
			return response.text();
		}).then(function(html) {
			const iframe = document.getElementById(iframeId);
			iframe.srcdoc = html;
		}).catch(function(error) {
			console.error('Error loading iframe:', error);
		});
	}
});

function ParseGMCPPkg(pkg)
{
	if(!pkg)
		return null;
	var x = pkg.lastIndexOf(' ');
	var pkgObj = {
		name: pkg,
		ver: 1
	};
	if(x>0)
	{
		pkgObj.name = pkg.substr(0,x);
		pkgObj.ver = isNumber(pkg.substr(x+1)) ? Math.max(1, Number(pkg.substr(x+1))) : 1;
	}
	return pkg;
}

function UpdateGMCPSupports(sipwin, msg, action)
{
	if(!sipwin.gmcpSupported)
		sipwin.gmcpSupported = {};
	if(Array.isArray(msg))
	{
		for(var i=0;i<msg.length;i++)
		{
			var p = ParseGMCPPkg(msg[i]);
			if(p)
			{
				if(action === 'remove')
					delete sipwin.gmcpSupported[p.name];
				else
					sipwin.gmcpSupported[p.name] = p;
			}
		}
	}
}

function BuildGMCPHello(sipwin)
{
	var response = [];
	response = response.concat([TELOPT.IAC, TELOPT.SB,TELOPT.GMCP]);
	var gmcpMsg = "Core.Hello {\"client\":\""+sipwin.siplet.NAME+"\",\"version\":"
				+ sipwin.siplet.VERSION_MAJOR + "}";
	response = response.concat(StringToAsciiArray(gmcpMsg));
	response = response.concat([TELOPT.IAC, TELOPT.SE]);
	response = response.concat([TELOPT.IAC, TELOPT.SB,TELOPT.GMCP]);
	var arr = [];
	for(var k in window.gmcpPackages)
	{
		var pkg = window.gmcpPackages[k];
		arr.push(pkg.name+' '+pkg.version);
	}
	var suppStr = 'Core.Supports.Set ' + JSON.stringify(arr);
	response = response.concat(StringToAsciiArray(suppStr));
	response = response.concat([TELOPT.IAC, TELOPT.SE]);
	return response;
}

function InvokeGMCP(sipwin, cmd, json)
{
	var lcmd = cmd.toLowerCase();
	for(var i=0;i<window.gmcpPackages.length;i++)
	{
		var gmcpPkg = window.gmcpPackages[i];
		if(lcmd.startsWith(gmcpPkg.lname))
		{
			var cmd = cmd.substr(gmcpPkg.lname.length);
			cmd = cmd.replaceAll('.','_').toLowerCase();
			if((cmd in gmcpPkg)
			&&(typeof gmcpPkg[cmd] === 'function'))
				try { gmcpPkg[cmd](sipwin, json); } catch(ee) {console.log(ee);}
		}
	}
}