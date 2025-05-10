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

	var me = this;
	
    this.topWindow.style.fontFamily = getConfig('window/fontface', 'monospace');
    this.topWindow.style.fontSize = getConfig('window/fontsize', '16px');
    this.topContainer = this.topWindow.firstChild;
	this.topContainer.style.cssText = this.topWindow.style.cssText;
	this.window = this.topContainer.firstChild;
	this.window.style.cssText = this.topContainer.style.cssText;
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
			me.tab.style.foregroundColor="white";
			me.tab.innerHTML='';
			me.window.style.backgroundColor="black";
			me.window.style.foregroundColor="white";
		};
		this.wsocket.onclose = function(event)  
		{ 
			me.wsopened=false; 
			me.tab.style.backgroundColor="#FF555B";
			me.tab.style.foregroundColor="white";
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
			var scroll = this.htmlBuffer.indexOf('<BR>')>=0;
			var span = document.createElement('span');
			span.innerHTML = this.htmlBuffer;
			this.window.appendChild(span);
			this.htmlBuffer='';
			if(scroll)
			{
				if(window.currentSiplet != me)
				{
					this.tab.style.backgroundColor = "lightgreen";
					this.tab.style.foregroundColor = "black";
				}
				this.window.scrollTop = this.window.scrollHeight - this.window.clientHeight;
			}
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
						this.htmlBuffer += newText;
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
					if(newText.indexOf('<BR>')>=0)
						me.flushWindow();
				}
			}
		}
		me.flushWindow();
	};
	
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

window.currentSiplet = null;
window.nextSipletSpanId = 0;
window.siplets = [];
window.windowArea = null;

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
				s.tab.style.foregroundColor = "white";
			}
		}
		else
		{
			s.topWindow.style.visibility = "hidden";
			if(s.wsopened)
			{
				s.tab.style.backgroundColor = "lightgray";
				s.tab.style.foregroundColor = "black";
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