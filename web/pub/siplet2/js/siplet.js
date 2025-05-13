window.currentSiplet = null;
window.nextSipletSpanId = 0;
window.siplets = [];
window.windowArea = null;

var Siplet =
{
	VERSION_MAJOR: 3.0,
	VERSION_MINOR: 0
};

function SipletWindow(windowName)
{
	this.siplet = Siplet;
	this.width = getConfig('window/width',80);
	this.height = getConfig('window/height',25);;
	this.maxLines = getConfig('window/lines',5000);;
	this.MSPsupport = false;
	this.MSDPsupport = false;
	this.GMCPsupport = false;
	this.MXPsupport = false;
	this.MCCPsupport = false;
	this.wsopened = false;
	this.windowName = windowName;
	this.bin = new BPPARSE(false);
	this.ansi = new ANSISTACK();
	this.telnet = new TELNET(this);
	this.msp = new MSP(this);
	this.mxp = new MXP(this);
	this.text = new TEXT([this.mxp,this.msp]);
	this.topWindow = document.getElementById(windowName);
	this.topWindow.onclick = function() { delayhidemenu(); boxFocus(); };
	this.wsocket = null;
	this.gauges=[];
	this.gaugeWindow = null;
	this.textBuffer = '';
	this.textBufferPruneIndex = 0;
	this.globalTriggers = GetGlobalTriggers();

	var me = this;
	
    this.topWindow.style.fontFamily = getConfig('window/fontface', 'monospace');
    this.topWindow.style.fontSize = getConfig('window/fontsize', '16px');
	this.topWindow.style.whiteSpace = 'pre'; // Preserve spaces as-is
    this.topContainer = this.topWindow.firstChild;
	this.window = this.topContainer.firstChild;
	[this.topWindow, this.topContainer, this.window].forEach(function(o)
	{
		o.style.position='absolute';
		o.style.top = '0%';
		o.style.height = '100%';
		o.style.width = '100%';
		o.style.left = '0%';
	});
    this.window.style.overflowY = 'auto';
    this.window.style.overflowX = 'hidden';
	
	this.connect = function(url)
	{
		this.wsocket = new WebSocket(url);
		this.wsocket.binaryType = "arraybuffer";
		this.wsocket.onmessage = this.onReceive;
		this.wsocket.onopen = function(event)  
		{
			me.wsopened=true; 
			me.tab.style.backgroundColor="green";
			me.tab.style.color="white";
			me.tab.innerHTML=(me.tabTitle)?(me.tabTitle):'';
			me.window.style.backgroundColor="black";
			me.window.style.color="white";
		};
		this.wsocket.onclose = function(event)  
		{ 
			me.wsopened=false; 
			me.tab.style.backgroundColor="#FF555B";
			me.tab.style.color="white";
		};
	};

	this.closeSocket = function()
	{
	    if (this.wsocket && this.wsocket.readyState === WebSocket.OPEN) {
	    	this.wsocket.close();
	    }
	}
	
	this.close = function()
	{
		this.closeSocket();
		this.reset();
	}
	
	this.reset = function()
	{
		while (this.topWindow.children.length > 1) {
			this.topWindow.removeChild(this.topWindow.lastChild);
		}
		this.topContainer = this.topWindow.firstChild;
		this.topContainer.style.cssText = 'position:absolute;top:0%;left:0%;width:100%;height:100%;'
		while (this.topContainer.children.length > 1) {
			this.topContainer.removeChild(this.topContainer.lastChild);
		}
		this.window = this.topContainer.firstChild;
		this.window.style.cssText = 'position:absolute;top:0%;left:0%;width:100%;height:100%;'
		this.window.style.overflowY = 'auto';
		this.window.style.overflowX = 'hidden';
  		this.MSPsupport = false;
		this.MSDPsupport = false;
		this.GMCPsupport = false;
		this.MXPsupport = false;
		this.MCCPsupport = false;
		this.wsopened = false;
		this.bin.reset();
		this.ansi.reset();
		this.telnet.reset();
		this.msp.reset();
		this.mxp.reset();
		this.text.reset();
		this.gauges=[];
		this.gaugeWindow = null;
	}
	
	this.htmlBuffer = '';
	this.flushWindow = function() {
		if(this.htmlBuffer.length > 0)
		{
			var span = document.createElement('span');
			span.innerHTML = this.htmlBuffer;
			this.window.appendChild(span);
			this.htmlBuffer='';
			if(window.currentSiplet != me)
			{
				this.tab.style.backgroundColor = "lightgreen";
				this.tab.style.color = "black";
			}
			this.window.scrollTop = this.window.scrollHeight - this.window.clientHeight;
		}
	}
	this.onReceive = function(e)
	{
		var entries = me.bin.parse(e.data);
		while (me.window.childNodes.length > me.maxLines)
			me.window.removeChild(me.window.firstChild);
		me.htmlBuffer = '';
		while(entries.length > 0)
		{
			if((entries.length == 1)
			&&(!entries[0].done))
				break;
			var blk = entries.shift();
			if(blk.data.length==0)
				continue;
			if(blk.type == BPTYPE.TELNET)
			{
				var buffer = me.telnet.process(blk.data);
				if ((buffer.byteLength > 0) && (me.wsopened))
					me.wsocket.send(buffer);
			}
			else
			{
				var newText = '';
				if(blk.type == BPTYPE.ANSI)
				{
					newText += me.ansi.process(blk.data);
					if(!me.mxp.active())
					{
						me.htmlBuffer += newText;
						continue;
					}
					var s = me.mxp.process(blk.data);
					if (s != null)
						newText += s;
					blk.data = newText; // convert to text-like
				}
				else
				{
					//TODO: me is not going to support utf-16 well
					for (var i=0; i < blk.data.length; i++) {
						newText += String.fromCharCode( blk.data[i]);
					}
				}
				newText = me.text.process(newText);
				if(newText.length>0)
				{
					me.htmlBuffer += newText;
					me.textBuffer += stripHtmlTags(newText);
					if(newText.indexOf('<BR>')>=0)
					{
						me.flushWindow();
						me.triggerCheck();
						while(me.textBuffer.length > 1024)
						{
							var x = me.textBuffer.indexOf('\n');
							if(x<0)
							{
								me.textBufferPruneIndex+=me.textBuffer.length;
								me.textBuffer = '';
							}
							else
							{
								me.textBuffer = me.textBuffer.substr(0,x+1);
								me.textBufferPruneIndex+=x;
							}
						}
					}
				}
			}
		}
		me.flushWindow();
		me.triggerCheck();
	};
	
	this.triggerCheck = function()
	{
		var globalTriggers=this.globalTriggers;
		var win = this;
		for(var i=0;i<globalTriggers.length;i++)
		{
			var trig = globalTriggers[i];
			if((!trig.once || trig.prev==0) && eval(trig.allowed) && trig.pattern)
			{
				var prev = trig.prev - this.textBufferPruneIndex;
				if(prev < 0)
					prev = 0;
				if(trig.regex)
				{
					trig.pattern.lastIndex = prev;
					var match = trig.pattern.exec(this.textBuffer);
					while(match !== null)
					{
						trig.prev = this.textBufferPruneIndex + match.index + 1;
						eval(trig.action);
						match = trig.pattern.exec(this.textBuffer);
					}
				}
				else
				{
					var x = this.textBuffer.indexOf(trig.pattern,prev);
					while(x>=0)
					{
						prev += x + trig.pattern.length + 1;
						trig.prev = this.textBufferPruneIndex + prev;
						eval(trig.action);
						x = this.textBuffer.indexOf(trig.pattern,prev);
					}
				}
			}
		}
	}
	
	this.createGauge = function(entity,caption,color,value,max)
	{
		var gaugedata=new Array(5);
		gaugedata[0]=entity;
		gaugedata[1]=caption;
		gaugedata[2]=color;
		gaugedata[3]=value;
		gaugedata[4]=max;
		this.gauges[this.gauges.length]=gaugedata;
		var gaugeHeight = 20;
		if(this.gaugeWindow == null)
		{
			this.gaugeWindow = document.createElement('div');
			this.gaugeWindow.innerHTML = '';
			this.gaugeWindow.style.position = 'absolute';
			this.gaugeWindow.style.top = '0%';
			this.gaugeWindow.style.width = '100%';
			this.gaugeWindow.style.height = gaugeHeight+'px';
			this.gaugeWindow.style.background = 'black';
			this.topWindow.appendChild(this.gaugeWindow);
			var parent = this.window.parentNode;
			parent.style.top = 'calc(' +parent.style.top + ' + '+gaugeHeight + 'px)';
			parent.style.height = 'calc(' +parent.style.height + ' - '+gaugeHeight + 'px)';
		}
		this.modifyGauge(entity,value,max);
	};
	
	this.removeGauge = function(entity)
	{
		var index = this.gauges.indexOf(entity);
		if(index >=0)
		{
			this.gauges.splice(index);
			this.modifyGauge(entity,-1,-1);
			if((this.gauges.length == 0) && (this.topWindow != null))
			{
				
				var mainWindow = this.gaugeWindow.parentNode.firstChild;
				mainWindow.style.top = this.gaugeWindow.top;
				this.gaugeWindow.outerHTML = '';
				this.gaugeWindow = null;
			}
		}
	}
	
	this.modifyGauge = function(entity,value,max)
	{
		var div=this.gaugeWindow;
		if(div == null)
			return;
		if(this.gauges.length==0)
			div.innerHTML='';
		else
		{
			var s='<TABLE WIDTH=100% CELLPADDING=0 CELLSPACING=0 BORDER=1><TR>';
			var i=0;
			var cellwidth=100/this.gauges.length;
			for(i=0;i<this.gauges.length;i++)
			{
				var gaugedata=this.gauges[i];
				if(gaugedata[0]==entity)
				{
					gaugedata[3]=value;
					gaugedata[4]=max;
				}
			}
			for(i=0;i<this.gauges.length;i++)
			{
				var gaugedata=this.gauges[i];
				s+='<TD WIDTH='+cellwidth+'%>';
				s+='<FONT STYLE="color: '+gaugedata[2]+'" SIZE=-2>'+gaugedata[1]+'</FONT><BR>';
				var gaugedata=this.gauges[i];
				var fullwidth=100-gaugedata[3];
				var lesswidth=gaugedata[3];
				s+='<TABLE WIDTH=100% CELLPADDING=0 CELLSPACING=0 BORDER=0 HEIGHT=5><TR HEIGHT=5>';
				s+='<TD STYLE="background-color: '+gaugedata[2]+'" WIDTH='+lesswidth+'%></TD>';
				s+='<TD STYLE="background-color: black" WIDTH='+fullwidth+'%></TD>';
				s+='</TR></TABLE>';
				s+='</TD>';
			}
			s+='</TR></TABLE>'
			div.innerHTML=s;
		}
	};
	
	this.submitInput = function(value)
	{
		var span = document.createElement('span');
		span.innerHTML = value.replaceAll('\n','<BR>') + '<BR>';
		this.window.appendChild(span);
		this.wsocket.send(value+'\n');
	};
}

function AddNewSipletTabByPort(port)
{
	var protocol = (window.location.protocol === 'https:') ? 'wss:' : 'ws:';
	var defaultUrl = protocol + '//' + window.location.host + '/WebSock';
	if(port === 'default')
		return AddNewSipletTab(defaultUrl);
	else
		return AddNewSipletTab(defaultUrl+'?port='+port);
}
	
function AddNewSipletTabByPB(which)
{
	if(which=='')
		return;
	var global = (''+which).startsWith('g');
	var pb;
	if(global)
	{
		which = Number(which.substr(1));
		if((which<0) || (which > window.phonebook.length))
			return;
		pb = window.phonebook[which];
	}
	else
	{
		var phonebook = getConfig('/phonebook/dial',[]);
		which = Number(which);
		if((which<0) || (which > phonebook.length))
			return;
		pb = phonebook[which];
	}
	var port = pb.port;
	var protocol = (window.location.protocol === 'https:') ? 'wss:' : 'ws:';
	var defaultUrl = protocol + '//' + window.location.host + '/WebSock';
	var siplet;
	if(port === 'default')
		siplet = AddNewSipletTab(defaultUrl);
	else
		siplet = AddNewSipletTab(defaultUrl+'?port='+port);
	if(global)
		siplet.tabTitle = pb.name + '('+pb.port+')';
	else
		siplet.tabTitle = pb.user + '@' + pb.name + ' ('+pb.port+')';
	siplet.pbentry = pb;
}

function AddNewSipletTab(url)
{
	var windowName = 'W'+window.siplets.length;
	var newTopElement=document.createElement('DIV');
	var newWinContainer=document.createElement('DIV');
	var newWindow=document.createElement('DIV');
	newTopElement.id = windowName;
	window.windowArea.appendChild(newTopElement);
	newTopElement.appendChild(newWinContainer);
	newWinContainer.appendChild(newWindow);
	newWinContainer.style.cssText = 'position:absolute;top:0%;left:0%;width:100%;height:100%;'
	var siplet = new SipletWindow(windowName); // makes a deep copy
	siplet.tab = AddNewTab(); 
	siplet.url = url;
	window.siplets.push(siplet);
	siplet.connect(url);
	SetCurrentTab(window.siplets.length-1);
	return siplet;
}

function SetCurrentTab(which)
{
	window.currentSiplet=window.siplets[which];
	for(var i=0;i<window.siplets.length;i++)
	{
		var s = window.siplets[i];
		if(i == which)
		{
			s.topWindow.style.visibility = "visible";
			if(s.wsopened)
			{
				s.tab.style.backgroundColor = "green";
				s.tab.style.color = "white";
			}
		}
		else
		{
			s.topWindow.style.visibility = "hidden";
			if(s.wsopened)
			{
				s.tab.style.backgroundColor = "lightgray";
				s.tab.style.color = "black";
			}
		}
	}
}

function CloseAllSiplets()
{
	for(var i=0;i<window.siplets.length;i++)
	{
		var siplet = window.siplets[i];
		siplet.closeSocket();
	}
}

function AutoConnect()
{
	AddNewSipletTabByPB(getConfig('/phonebook/auto',''));
}

setTimeout(function() {
	var updateSipletConfigs = function() {
		for(var i=0;i<window.siplets.length;i++)
		{
			var siplet = window.siplets[i];
			siplet.width = getConfig('window/width',siplet.width);
			siplet.height = getConfig('window/height',siplet.height);
			siplet.maxLines = getConfig('window/lines',siplet.maxLines);
		}
	};
	addConfigListener('window/width', updateSipletConfigs);
	addConfigListener('window/height', updateSipletConfigs);
	addConfigListener('window/lines', updateSipletConfigs);
	var updateSipletWindows = function() {
		for(var i=0;i<window.siplets.length;i++)
		{
			var siplet = window.siplets[i];
		    siplet.topWindow.style.fontFamily = getConfig('window/fontface', siplet.topWindow.style.fontFamily);
		    siplet.topWindow.style.fontSize = getConfig('window/fontsize', siplet.topWindow.style.fontSize);
	    }
	}
	addConfigListener('window/fontface', updateSipletWindows);
	addConfigListener('window/fontsize', updateSipletWindows);
},0);
