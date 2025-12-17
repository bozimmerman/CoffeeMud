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
	open: function(sipwin, msg) 
	{
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
			var width = msg["width"] || "25%";
			var width = msg["width"] || "25%";
			var top = msg["top"] || "25%";
			var left = msg["left"] || "25%";
			if(dock != "")
			{
				var rest=(["top","bottom"].indexOf(dock.toLowerCase())>=0)?("height="+height):("width="+width);
				sipwin.process('<FRAME ACTION=OPEN INTERNAL NAME="'+id+'"  TITLE="WebView" ALIGN='+dock+' '+rest+'>');
			}
			else
				sipwin.process('<FRAME ACTION=OPEN FLOATING NAME="'+id+'" TITLE="WebView" LEFT='+left+' TOP='+top+' HEIGHT='+height+' WIDTH='+width+'>');
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

window.gmcpPackages.push({
	name: "beip.tilemap",
	lname: "beip.tilemap.",
	version: "1",
	info: function(sipwin, msg) 
	{
		if(!isJsonObject(msg))
			return;

		if(!sipwin.tilemaps)
			sipwin.tilemaps = {};

		for(var mapName in msg) 
		{
			var mapInfo = msg[mapName];
			if(!isJsonObject(mapInfo))
				continue;

			var tileSize = mapInfo["tile-size"] || "16,16";
			var tileSizeParts = tileSize.split(',');
			var tileWidth = parseInt(tileSizeParts[0]) || 16;
			var tileHeight = parseInt(tileSizeParts[1]) || 16;

			var mapSize = mapInfo["map-size"] || "10,10";
			var mapSizeParts = mapSize.split(',');
			var mapWidth = parseInt(mapSizeParts[0]) || 10;
			var mapHeight = parseInt(mapSizeParts[1]) || 10;

			var tileUrl = mapInfo["tile-url"] || "";
			var encoding = mapInfo["encoding"] || "Hex_4";

			var mapExists = mapName in sipwin.tilemaps;
			var sizeChanged = false;
			if(mapExists) 
			{
				var oldMap = sipwin.tilemaps[mapName];
				sizeChanged = (oldMap.mapWidth !== mapWidth || oldMap.mapHeight !== mapHeight);
			}

			sipwin.tilemaps[mapName] = 
			{
				tileUrl: tileUrl,
				tileWidth: tileWidth,
				tileHeight: tileHeight,
				mapWidth: mapWidth,
				mapHeight: mapHeight,
				encoding: encoding,
				tileImage: null,
				data: sizeChanged ? null : (sipwin.tilemaps[mapName] ? sipwin.tilemaps[mapName].data : null)
			};

			if(sizeChanged || !sipwin.tilemaps[mapName].data) 
			{
				var dataSize = mapWidth * mapHeight;
				sipwin.tilemaps[mapName].data = new Array(dataSize).fill(0);
			}

			if(tileUrl) 
			{
				var img = new Image();
				img.crossOrigin = "anonymous";
				var self = this;
				img.onload = function(mapName) 
				{
					return function() {
						sipwin.tilemaps[mapName].tileImage = this;
						self._RenderTilemap(sipwin, mapName);
					};
				}(mapName);
				img.src = tileUrl;
			}

			this._CreateTilemapFrame(sipwin, mapName);
		}
	},
	data: function(sipwin, msg) 
	{
		if(!isJsonObject(msg))
			return;
		if(!sipwin.tilemaps)
			sipwin.tilemaps = {};

		// Process each map in the message
		for(var mapName in msg) 
		{
			if(!(mapName in sipwin.tilemaps))
				continue;

			var mapData = msg[mapName];
			var tilemap = sipwin.tilemaps[mapName];

			// Decode the data based on encoding
			var decodedData = this._DecodeTilemapData(mapData, tilemap.encoding, tilemap.mapWidth * tilemap.mapHeight);
			if(decodedData) 
			{
				tilemap.data = decodedData;
				this._RenderTilemap(sipwin, mapName);
			}
		}
	},
	_DecodeTilemapData: function	(dataStr, encoding, expectedSize) 
	{
		if(!dataStr)
			return null;

		var result = [];

		if(encoding === "Hex_4") 
		{
			for(var i = 0; i < dataStr.length; i++) 
			{
				var char = dataStr[i];
				var value = parseInt(char, 16);
				if(!isNaN(value))
					result.push(value);
			}
		} 
		else
		if(encoding === "Hex_8") 
		{
			for(var i = 0; i < dataStr.length; i += 2)
			{
				var byte = dataStr.substr(i, 2);
				var value = parseInt(byte, 16);
				if(!isNaN(value))
					result.push(value);
			}
		}
		else
		if(encoding === "Decimal") 
		{
			var parts = dataStr.split(',');
			for(var i = 0; i < parts.length; i++)
			{
				var value = parseInt(parts[i]);
				if(!isNaN(value))
					result.push(value);
			}
		}

		if(result.length !== expectedSize)
		{
			console.error('Tilemap data size mismatch: expected ' + expectedSize + ', got ' + result.length);
			return null;
		}

		return result;
	},
	_CreateTilemapFrame: function(sipwin, mapName) 
	{
		var frameId = "TILEMAP_" + mapName.replace(/[^a-zA-Z0-9]/g, '_');
		var framechoices = sipwin.mxp.getFrameMap();

		if(!(frameId in framechoices))
		{
			// Create a floating frame for the tilemap
			sipwin.process('<FRAME ACTION=OPEN FLOATING NAME="'+frameId+'" TITLE="Map: '+mapName+'" LEFT=10% TOP=10% HEIGHT=40% WIDTH=40%>');
			framechoices = sipwin.mxp.getFrameMap();
		}

		if(!(frameId in framechoices))
			return;

		var frame = framechoices[frameId];
		if(frame.sprops && frame.firstChild)
			frame = frame.firstChild;

		var canvasId = "tilemap_canvas_" + frameId;
		sipwin.cleanDiv(frame);
		frame.innerHTML = '<canvas id="'+canvasId+'" style="width: 100%; height: 100%; image-rendering: pixelated; image-rendering: crisp-edges;"></canvas>';

		if(sipwin.tilemaps[mapName])
			sipwin.tilemaps[mapName].canvasId = canvasId;
	},
	_RenderTilemap: function(sipwin, mapName) 
	{
		if(!sipwin.tilemaps || !(mapName in sipwin.tilemaps))
			return;

		var tilemap = sipwin.tilemaps[mapName];
		if(!tilemap.data || !tilemap.tileImage)
			return;

		var canvas = document.getElementById(tilemap.canvasId);
		if(!canvas)
			return;

		canvas.width = tilemap.mapWidth * tilemap.tileWidth;
		canvas.height = tilemap.mapHeight * tilemap.tileHeight;

		var ctx = canvas.getContext('2d');
		ctx.imageSmoothingEnabled = false;

		ctx.clearRect(0, 0, canvas.width, canvas.height);

		var tilesPerRow = Math.floor(tilemap.tileImage.width / tilemap.tileWidth);

		for(var y = 0; y < tilemap.mapHeight; y++) 
		{
			for(var x = 0; x < tilemap.mapWidth; x++) 
			{
				var index = y * tilemap.mapWidth + x;
				var tileIndex = tilemap.data[index];
				var srcX = (tileIndex % tilesPerRow) * tilemap.tileWidth;
				var srcY = Math.floor(tileIndex / tilesPerRow) * tilemap.tileHeight;
				var destX = x * tilemap.tileWidth;
				var destY = y * tilemap.tileHeight;

				ctx.drawImage(
					tilemap.tileImage,
					srcX, srcY, tilemap.tileWidth, tilemap.tileHeight,
					destX, destY, tilemap.tileWidth, tilemap.tileHeight
				);
			}
		}
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