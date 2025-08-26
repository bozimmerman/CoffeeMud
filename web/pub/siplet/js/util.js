window.mimeTypes = 
{
	jpg: 'image/jpeg',
	jpeg: 'image/jpeg',
	png: 'image/png',
	gif: 'image/gif',
	bmp: 'image/bmp',
	webp: 'image/webp',
	mp3: 'audio/mpeg', 
	ogg: 'audio/ogg', 
	wav: 'audio/wav'
};

window.imgMimeTypes = 
{
	jpg: 'image/jpeg',
	jpeg: 'image/jpeg',
	png: 'image/png',
	gif: 'image/gif',
	bmp: 'image/bmp',
	webp: 'image/webp'
};

function isLetter(c)
{
	if ((typeof c === 'string')&&(c.length>0))
		c = c.charCodeAt(0);
	return ((((c>=97)&&(c<=122))
			||((c>=65)&&(c<=90))));
}

function isLowerCase(c)
{
	if ((typeof c === 'string')&&(c.length>0))
		c = c.charCodeAt(0);
	return (c>=97)&&(c<=122);
}

function isUpperCase(c)
{
	if ((typeof c === 'string')&&(c.length>0))
		c = c.charCodeAt(0);
	return (c>=65)&&(c<=90);
}

function isLetterOrDigit(c)
{
	return isLetter(c) || isDigit(c);
}

function isDigit(c)
{
	if ((typeof c === 'string')&&(c.length>0))
		c = c.charCodeAt(0);
	return (c>=48)&&(c<=90);
}

function isNumber(c)
{
	if((c == null)||(c === undefined))
		return false;
	if(typeof c === 'number')
		return true;
	if ((typeof c === 'string')&&(c.length>0))
		return (!isNaN(c)) && (!isNaN(parseFloat(c)));
	return false;
}

function isJsonObject(variable) {
	return variable !== null 
		&& typeof variable === 'object' 
		&& !Array.isArray(variable) 
		&& Object.prototype.toString.call(variable) === '[object Object]';
}

function SortArray(arr) 
{
	return arr.sort(function(a, b) 
	{
		if (a == null && b == null) return 0;
		if (a == null) return 1;
		if (b == null) return -1;
		const isANumber = typeof a === 'number' && !isNaN(a);
		const isBNumber = typeof b === 'number' && !isNaN(b);
		if (isANumber && !isBNumber) return -1;
		if (!isANumber && isBNumber) return 1;
		if (isANumber && isBNumber) return a - b;
		return String(a).localeCompare(String(b));
	});
}

function stripHtmlTags(htmlString) 
{
	var tempDiv = document.createElement('div');
	tempDiv.innerHTML = htmlString;
	return tempDiv.textContent || tempDiv.innerText || '';
}

function setSelectByValue(select, value) 
{
	for (let i = 0; i < select.options.length; i++) 
	{
		if (select.options[i].value == value) 
		{
			select.selectedIndex = i;
			break;
		}
	}
}

function isValidAction(s)
{
	if(!s) 
		return false;
	var x = s.indexOf('(');
	if(x<0) return false;
	if((!s.endsWith(')'))&&(!s.endsWith(');'))) 
		return false;
	var cmd = s.substr(0,x);
	var arg = s.substring(x+1, s.lastIndexOf(')'));
	var action = SipletActions[cmd];
	if(action == null)
		return false;
	if((action != SipletActions['win.submitInput'])
	&&(arg.indexOf('pb.password')>=0))
		return false;
	return IsQuotedStringArgument(arg,action.args,Siplet.R);
};

function isValidExpression(exp) {
	try 
	{
		if (exp.trim().length === 0)
			return false;
		var depth = 0;
		var inString = false;
		var stringChar = null;
		var inComment = false;
		for (var i = 0; i < exp.length; i++) 
		{
			if (inComment) {
				inComment =  (exp[i] !== '\n');
				continue;
			}
			if (inString) {
				inString =  ! (exp[i] === stringChar && exp[i - 1] !== '\\');
				continue;
			}
			if (exp[i] === '"' || exp[i] === "'") 
			{
				inString = true;
				stringChar = exp[i];
			} 
			else 
			if (exp[i] === '/' && exp[i + 1] === '/')
				inComment = true;
			else 
			if (exp[i] === '{')
				depth++;
			else 
			if (exp[i] === '}') 
			{
				depth--;
				if (depth < 0)
					return false;
			}
		}
		if (inString)
			return false;
		if (depth !== 0)
			return false;
		if (exp.trim().startsWith('}'))
			return false;
		new Function(exp);
		return true;
	} 
	catch (e) 
	{
		return false;
	}
}

function In(element,inlist)
{
	if(Array.isArray(inlist))
		return inlist.indexOf(element)>=0;
	return element in inlist;
}

function getRadioValue(radio)
{
	for(var i=0;i<radio.length;i++)
		if(radio[i].checked)
			return radio[i].value;
	return '';
}

function setRadioValue(radio, value)
{
	for(var i=0;i<radio.length;i++)
		radio[i].checked = radio[i].value == value;
}

function escapeHTML(s)
{
	return s
		.replace(/&/g, '&amp;')
		.replace(/</g, '&lt;')
		.replace(/>/g, '&gt;')
		.replace(/"/g, '&quot;')
		.replace(/'/g, '&#39;');
}

function brCount(s)
{
	var matches = s.match(/<br\s*\/?>/gi) || [];
	return matches.length;
}

function butt(t,js)
{
	return "<span style=\"display: inline-block; background-image: url('images/lilButt.gif'); background-position: center;"
		+"background-repeat: no-repeat; background-size: 100% 100%; padding: 5px 10px; white-space: nowrap; cursor: pointer; line-height: 10px;\"" 
		+"onclick=\""+js+"\"><font face=\"Arial\" color=\"purple\" size=\"-2\"><b>"+t+"</b></font></span>";
}

function extractUnclosedFontTags(span, htmlBuffer) 
{
	if(!htmlBuffer)
		return '';
	var w = '';
	var x = htmlBuffer.lastIndexOf("<FONT");
	var y = htmlBuffer.lastIndexOf("</FONT");
	if(y<x)
		w += htmlBuffer.substr(x,htmlBuffer.indexOf('>',x)-x+1);
	var x1 = htmlBuffer.lastIndexOf("<A ");
	var y1 = htmlBuffer.lastIndexOf("</A>");
	if(y1<x1)
	{
		if(x>=0)
		{
			if(x<x1)
				w += htmlBuffer.substr(x1,htmlBuffer.indexOf('>',x1)-x1+1);
			else
				w = htmlBuffer.substr(x1,htmlBuffer.indexOf('>',x1)-x1+1) + w;
		}
		else
			w = htmlBuffer.substr(x1,htmlBuffer.indexOf('>',x1)-x1+1);
		
	}
	return w;
}

function SiPrompt(text, callback) 
{
	var overlay = document.createElement("div");
	overlay.style = "position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.5);z-index:9999";
	overlay.onkeydown = function(e) { e.stopPropagation(); };
	var dialog = document.createElement("div");
	dialog.style = "position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);background:#000;color:#fff;border:1px solid #fff;padding:10px";
	var label = document.createElement("div");
	label.innerHTML = text;
	var input = document.createElement("input");
	input.style = "width:200px;border:1px solid #fff;background:#fff;color:#000;font-size:16px;padding:2px";
	input.maxLength = 30;
	input.onkeydown = function(e) {
		e.stopPropagation();
		if (e.key == "Enter") button.click();
		if (e.key == "Escape") overlay.remove();
	};
	var button = document.createElement("button");
	button.textContent = "OK";
	button.onclick = function() {
		overlay.remove();
		callback(input.value);
	};
	dialog.append(label, input, button);
	overlay.append(dialog);
	document.body.append(overlay);
	setTimeout(function() { input.focus() }, 0);
}

function SiFontPicker(labelText, callback) 
{
	var overlay = document.createElement("div");
	overlay.style = "position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.5);z-index:9999";
	overlay.onkeydown = function(e) { e.stopPropagation(); };
	var dialog = document.createElement("div");
	dialog.style = "position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);background:#000;color:#fff;border:1px solid #fff;padding:10px";
	var label = document.createElement("div");
	label.innerHTML = labelText;
	var fontSelect = document.createElement("select");
	fontSelect.style = "width:200px;border:1px solid #fff;background:#fff;color:#000;font-size:16px;padding:2px;margin:5px 0";
	var fonts = ["Arial", "Helvetica", "Times New Roman", "Courier New", "Verdana", "Georgia"];
	fonts.forEach(function(font) 
	{
		var option = document.createElement("option");
		option.value = font;
		option.textContent = font;
		fontSelect.append(option);
	});
	
	var sizeSelect = document.createElement("select");
	sizeSelect.style = "width:200px;border:1px solid #fff;background:#fff;color:#000;font-size:16px;padding:2px;margin:5px 0";
	var sizes = [12, 14, 16, 18, 20, 24, 28, 32];
	sizes.forEach(function(size) 
	{
		var option = document.createElement("option");
		option.value = size;
		option.textContent = size + "px";
		sizeSelect.append(option);
	});
	
	var button = document.createElement("button");
	button.textContent = "OK";
	button.onclick = function() 
	{
		overlay.remove();
		callback(fontSelect.value, parseInt(sizeSelect.value));
	};
	
	fontSelect.onkeydown = sizeSelect.onkeydown = function(e) 
	{
		e.stopPropagation();
		if (e.key == "Enter") 
			button.click();
		if (e.key == "Escape")
			overlay.remove();
	};
	dialog.append(label, fontSelect, sizeSelect, button);
	overlay.append(dialog);
	document.body.append(overlay);
	setTimeout(function() { fontSelect.focus() }, 0);
}

function SiFilePicker(labelText, callback, multiple = false) 
{
	var overlay = document.createElement("div");
	overlay.style = "position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.5);z-index:9999";
	overlay.onkeydown = function(e) { e.stopPropagation(); };
	var dialog = document.createElement("div");
	dialog.style = "position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);background:#000;color:#fff;border:1px solid #fff;padding:10px;width:400px;max-height:80%;overflow:auto";
	var label = document.createElement("div");
	label.innerHTML = labelText;
	var previewSpan = document.createElement("span");
	previewSpan.style = "visibility:hidden;float:right";
	var previewImg = document.createElement("img");
	previewImg.style = "max-width:100px;max-height:100px;object-fit:contain";
	previewSpan.appendChild(previewImg);
	var treeSpan = document.createElement("div");
	var rootNode = document.createElement("ul");
	treeSpan.appendChild(rootNode);
	var button = document.createElement("button");
	button.textContent = "OK";
	button.onclick = function() 
	{
		overlay.remove();
		if(multiple)
			callback(root.selected.map(n => 'media://' + n.path));
		else
			callback(root.selected ? 'media://' + root.selected.path : null);
	};
	var cancel = document.createElement("button");
	cancel.textContent = "Cancel";
	cancel.onclick = function() { overlay.remove(); };
	treeSpan.onkeydown = function(e) 
	{
		e.stopPropagation();
		if (e.key == "Enter") 
			button.click();
		if (e.key == "Escape")
			overlay.remove();
	};
	dialog.append(label, previewSpan, treeSpan, button, cancel);
	overlay.append(dialog);
	document.body.append(overlay);
	setTimeout(function() { treeSpan.focus() }, 0);
	var root = {};
	root.selected = multiple ? [] : null;
	root.tree = {};
	root.rootNode = rootNode;
	window.sipfs.getMediaTree(function(err,newtree){
		if(err)
		{
			SiAlert(err);
			return;
		}
		root.tree = newtree;
		root.tree.opened = true;
		root.tree.name = '/';
		root.rootNode.innerHTML = '';
		root.renderNode(root.rootNode, [root.tree]);
	});
	root.selectNode = function(nr)
	{
		previewSpan.style.visibility='hidden';
		if(nr.isFolder)
			nr.opened = !nr.opened;
		else
		{
			if(multiple)
			{
				var idx = root.selected.indexOf(nr);
				if(idx >= 0)
					root.selected.splice(idx,1);
				else
					root.selected.push(nr);
			}
			else
			{
				if(root.selected)
					root.selected.selected = false;
				root.selected = nr;
				nr.selected = true;
			}
			var x = nr.name.lastIndexOf('.');
			if((x>0)
			&&(nr.name.substr(x+1).toLowerCase() in window.imgMimeTypes))
			{
				previewSpan.style.visibility='visible';
				previewImg.src = 'media://'+nr.path;
				updateMediaImagesInSpan(window.sipfs, previewSpan);
			}
		}
		root.rootNode.innerHTML = '';
		root.renderNode(root.rootNode, [root.tree]);
	};
	root.renderNode = function(parent, nodes)
	{
		for(var i=0;i<nodes.length;i++)
		{
			var n = nodes[i];
			var nr = document.createElement('li');
			var tn;
			var isSelected = multiple ? root.selected.includes(n) : (n == root.selected);
			if(isSelected)
			{
				tn = document.createElement('b');
				tn.style.fontSize = 16;
				if (n.isFolder)
					tn.style.color = 'blue';
				else
					tn.style.color = 'green';
				tn.appendChild(document.createTextNode(n.name));
			}
			else
			{
				tn = document.createElement('span');
				if (n.isFolder)
					tn.style.color = 'blue';
				else
					tn.style.color = 'green';
				tn.appendChild(document.createTextNode(n.name));
			}
			nr.appendChild(tn);
			nr.node = n;
			parent.appendChild(nr);
			nr.onclick = function(event) {
				event.stopPropagation();
				root.selectNode(this.node); 
			};
			if(n.opened && n.isFolder)
			{
				if((n.children && n.children.length)
				||(n.entries && n.entries.length))
				{
					var nc = document.createElement('ul');
					nr.appendChild(nc);
					root.renderNode(nc,n.children);
					root.renderNode(nc,n.entries);
				}
			}
		}
	};
}

function SiAlert(text) 
{
	var overlay = document.createElement("div");
	overlay.style = "position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.5);z-index:9999";
	overlay.onkeydown = function(e) { e.stopPropagation(); };
	var dialog = document.createElement("div");
	dialog.style = "position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);background:#000;color:#fff;border:1px solid #fff;padding:10px";
	var label = document.createElement("div");
	label.textContent = text;
	var button = document.createElement("button");
	button.textContent = "OK";
	button.onclick = function() {
		overlay.remove();
	};
	overlay.onkeydown = function(e) {
		e.stopPropagation();
		if (e.key == "Enter") button.click();
		if (e.key == "Escape") overlay.remove();
	};
	dialog.append(label, button);
	overlay.append(dialog);
	document.body.append(overlay);
}

function SiColorPicker(text, callback, includeAlpha = false)
{
	var overlay = document.createElement("div");
	overlay.style = "position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.5);z-index:9999";
	overlay.onkeydown = function(e) { e.stopPropagation(); };
	var dialog = document.createElement("div");
	dialog.style = "position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);background:#000;color:#fff;border:1px solid #fff;padding:10px";
	var label = document.createElement("div");
	label.innerHTML = text;
	label.style = "margin-bottom:10px";
	var canvas = document.createElement("canvas");
	canvas.width = 200;
	canvas.height = 150;
	canvas.style = "border:1px solid #fff;cursor:crosshair";
	var ctx = canvas.getContext("2d");
	var valueLabel = document.createElement('div');
	valueLabel.textContent = 'color level';
	valueLabel.style = "position:absolute;color:#fff;font-size:16px;margin:0;padding:0;top:2px;left:46px;z-index:2;";
	var valueSlider = document.createElement("input");
	valueSlider.type = "range";
	valueSlider.min = 0;
	valueSlider.max = 100;
	valueSlider.value = 100;
	valueSlider.style = "width:200px;margin:5px 0;position:relative;z-index:1;";
	var valueWrapper = document.createElement("div");
	valueWrapper.style = "position:relative;";
	valueWrapper.append(valueLabel, valueSlider);
	var alphaSlider = includeAlpha ? document.createElement("input") : null;
	var alphaWrapper = null;
	if (includeAlpha) {
		var alphaLabel = document.createElement('div');
		alphaLabel.textContent = 'transparency';
		alphaLabel.style = "position:absolute;color:#fff;font-size:16px;margin:0;padding:0;top:2px;left:50px;z-index:2;";
		alphaSlider.type = "range";
		alphaSlider.min = 0;
		alphaSlider.max = 100;
		alphaSlider.value = 100;
		alphaSlider.style = "width:200px;margin:5px 0;position:relative;z-index:1;";
		alphaWrapper = document.createElement("div");
		alphaWrapper.style = "position:relative;";
		alphaWrapper.append(alphaLabel, alphaSlider);
	}
	var button = document.createElement("button");
	button.textContent = "OK";
	button.style = "margin-top:10px";
	function hsvToRgb(h, s, v) 
	{
		var r, g, b;
		var i = Math.floor(h * 6);
		var f = h * 6 - i;
		var p = v * (1 - s);
		var q = v * (1 - f * s);
		var t = v * (1 - (1 - f) * s);
		switch (i % 6) 
		{
			case 0: r = v, g = t, b = p; break;
			case 1: r = q, g = v, b = p; break;
			case 2: r = p, g = v, b = t; break;
			case 3: r = p, g = q, b = v; break;
			case 4: r = t, g = p, b = v; break;
			case 5: r = v, g = p, b = q; break;
		}
		return [Math.round(r * 255), Math.round(g * 255), Math.round(b * 255)];
	}
	function drawPicker() 
	{
		var gradient = ctx.createLinearGradient(0, 0, canvas.width, 0);
		gradient.addColorStop(0, "#f00");
		gradient.addColorStop(0.166, "#ff0");
		gradient.addColorStop(0.333, "#0f0");
		gradient.addColorStop(0.5, "#0ff");
		gradient.addColorStop(0.666, "#00f");
		gradient.addColorStop(0.833, "#f0f");
		gradient.addColorStop(1, "#f00");
		ctx.fillStyle = gradient;
		ctx.fillRect(0, 0, canvas.width, canvas.height);
		gradient = ctx.createLinearGradient(0, 0, 0, canvas.height);
		gradient.addColorStop(0, "rgba(255,255,255,1)");
		gradient.addColorStop(1, "rgba(255,255,255,0)");
		ctx.fillStyle = gradient;
		ctx.fillRect(0, 0, canvas.width, canvas.height);
		gradient = ctx.createLinearGradient(0, 0, 0, canvas.height);
		gradient.addColorStop(0, "rgba(0,0,0,0)");
		gradient.addColorStop(1, "rgba(0,0,0,1)");
		ctx.fillStyle = gradient;
		ctx.fillRect(0, 0, canvas.width, canvas.height);
	}
	var h = 0, s = 1, v = 1, a = 1;
	canvas.onmousedown = function(e) 
	{
		var rect = canvas.getBoundingClientRect();
		var x = e.clientX - rect.left;
		var y = e.clientY - rect.top;
		h = x / canvas.width;
		s = 1 - y / canvas.height;
		updatePreview();
	};
	valueSlider.oninput = function() {
		v = valueSlider.value / 100;
		updatePreview();
	};
	if (includeAlpha) {
		alphaSlider.oninput = function() {
			a = alphaSlider.value / 100;
			updatePreview();
		};
	}
	var preview = document.createElement("div");
	preview.style = "width:200px;height:20px;border:1px solid #fff;margin:10px 0";
	function updatePreview() {
		var rgb = hsvToRgb(h, s, v);
		var color = includeAlpha ? `rgba(${rgb[0]},${rgb[1]},${rgb[2]},${a})` : `rgb(${rgb[0]},${rgb[1]},${rgb[2]})`;
		preview.style.backgroundColor = color;
	}
	button.onclick = function() {
		overlay.remove();
		var rgb = hsvToRgb(h, s, v);
		var result = includeAlpha ? [...rgb, a] : rgb;
		callback(result);
	};
	overlay.onkeydown = function(e) {
		if (e.key == "Escape") overlay.remove();
	};
	dialog.append(label, canvas, valueWrapper);
	dialog.append(preview);
	if (includeAlpha) dialog.append(alphaWrapper);
	dialog.append(button);
	overlay.append(dialog);
	document.body.append(overlay);
	drawPicker();
	updatePreview();
}

function SiSwatchPicker(text, colors, callback) 
{
	if (!Array.isArray(colors) 
	|| colors.length === 0 
	|| !colors.every(c => Array.isArray(c) && c.length === 3 && c.every(n => Number.isInteger(n) && n >= 0 && n <= 255))) 
		return;
	var overlay = document.createElement("div");
	overlay.style = "position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.5);z-index:9999";
	overlay.onkeydown = function(e) { e.stopPropagation(); };
	var dialog = document.createElement("div");
	dialog.style = "position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);background:#000;color:#fff;border:1px solid #fff;padding:10px";
	var label = document.createElement("div");
	label.innerHTML = text;
	label.style = "margin-bottom:10px";
	var swatchContainer = document.createElement("div");
	swatchContainer.style = "display:grid;grid-template-columns:repeat(5,40px);gap:5px;max-width:230px;";
	var selectedColor = colors[0];
	colors.forEach(function(color) {
		var swatch = document.createElement("div");
		swatch.style = `width:40px;height:40px;background-color:rgb(${color[0]},${color[1]},${color[2]});border:2px solid #fff;cursor:pointer;`;
		swatch.onclick = function() 
		{
			selectedColor = color;
			updatePreview();
			swatchContainer.querySelectorAll("div").forEach(s => s.style.border = "2px solid #fff");
			swatch.style.border = "2px solid #ff0";
		};
		swatchContainer.append(swatch);
	});
	var preview = document.createElement("div");
	preview.style = "width:200px;height:20px;border:1px solid #fff;margin:10px 0";
	function updatePreview() {
		preview.style.backgroundColor = `rgb(${selectedColor[0]},${selectedColor[1]},${selectedColor[2]})`;
	}
	var button = document.createElement("button");
	button.textContent = "OK";
	button.style = "margin-top:10px";
	button.onclick = function() {
		overlay.remove();
		callback([...selectedColor]);
	};
	overlay.onkeydown = function(e) {
		if (e.key === "Escape") overlay.remove();
	};
	dialog.append(label, swatchContainer, preview, button);
	overlay.append(dialog);
	document.body.append(overlay);
	updatePreview();
	swatchContainer.children[0].style.border = "2px solid #ff0";
}

function logDat(typ, dat, opts)
{
	var s = typ + ': ';
	if(dat.length)
		for(var i=0;i<dat.length;i++)
		{
			if(i<3 && opts)
			{
				var o='';
				for(var k in opts)
				{
					if(opts[k] == dat[i])
					{
						o = k;
						break;
						
					}
				}
				if(o)
				{
					s += o + ' ';
					continue;
					
				}
			}
			var h = dat[i].toString(16).toUpperCase();
			while(h.length < 2)
				h = '0' + h;
			s += h + ' ';
		}
	console.log(s);
}

function SiButtons(text, buttons, callback) 
{
	var overlay = document.createElement("div");
	overlay.style = "position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.5);z-index:9999";
	overlay.onkeydown = function(e) {
		e.stopPropagation();
		if (e.key == "Escape")
			overlay.remove();
	};
	var dialog = document.createElement("div");
	dialog.style = "position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);background:#000;color:#fff;border:1px solid #fff;padding:10px";
	var label = document.createElement("div");
	label.textContent = text;
	var buttonContainer = document.createElement("div");
	buttonContainer.style = "display: flex; justify-content: space-around; margin-top: 10px;";
	var buttonElems = [];
	for(var i=0;i<buttons.length;i++)
	{
		function addButton(buttonName)
		{
			var button = document.createElement("button");
			button.textContent = buttonName;
			button.onclick = function() 
			{
				overlay.remove();
				callback(buttonName);
			};
			buttonElems.push(button);
		}
		addButton(buttons[i]);
	}
	for(var i=0;i<buttonElems.length;i++)
		buttonContainer.append(buttonElems[i]);
	dialog.append(label, buttonContainer);
	overlay.append(dialog);
	document.body.append(overlay);
	setTimeout(function() { buttonElems[0].focus() }, 0);
}

function SiConfirm(text, callback) 
{
	var overlay = document.createElement("div");
	overlay.style = "position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.5);z-index:9999";
	overlay.onkeydown = function(e) {
		e.stopPropagation();
		if (e.key == "Enter")
			okButton.click();
		else 
		if (e.key == "Escape")
			overlay.remove();
	};
	var dialog = document.createElement("div");
	dialog.style = "position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);background:#000;color:#fff;border:1px solid #fff;padding:10px";
	var label = document.createElement("div");
	label.textContent = text;
	var buttonContainer = document.createElement("div");
	buttonContainer.style = "display: flex; justify-content: space-around; margin-top: 10px;";
	var okButton = document.createElement("button");
	okButton.textContent = "OK";
	okButton.onclick = function() {
		overlay.remove();
		callback(true);
	};
	var cancelButton = document.createElement("button");
	cancelButton.textContent = "Cancel";
	cancelButton.onclick = function() {
		overlay.remove();
	};
	buttonContainer.append(okButton, cancelButton);
	dialog.append(label, buttonContainer);
	overlay.append(dialog);
	document.body.append(overlay);

	// Focus the OK button by default
	setTimeout(function() { okButton.focus() }, 0);
}

function updateMediaImagesInSpan(sipfs, span)
{
	var images = span.querySelectorAll('img[src^="media://"]');
	images.forEach(function(img) {
		var path = img.getAttribute('src').substr(8);
		sipfs.load(path, function(err, dataUrl) 
		{
			if (err) 
			{
				console.error('Error loading ' + path + ':', err);
				return;
			}
			if(dataUrl)
				img.setAttribute('src', dataUrl);
		});
	});
	
	var bgImage = span.style.backgroundImage || '';
	if (bgImage.includes('media://'))
	{
  		var regex = /(?:url\(['"]?(media:\/\/[^)'"]+)['"]?\)|(media:\/\/[^'\s)]+))/;
  		var match = regex.exec(bgImage);
  		if(match)
  		{
			var mediaUrl = match[1] || match[2];
			mediaUrl = mediaUrl.replace(/^media:\/\//, '').trim();
			sipfs.load(mediaUrl, function(err, dataUrl) 
			{
				if (err) 
				{
					console.error('Error loading ' + mediaUrl + ':', err);
					return;
				}
				if(dataUrl)
					span.style.backgroundImage = 'url('+dataUrl+')';
			});
		}
	}
};

function SplitQuotedStringArguments(str,ignoreQuotes,okRegex)
{
	var parts = [];
	if(ignoreQuotes)
		parts.remainder='';
	var start=0;
	for(var i=0;i<str.length;i++)
	{
		if(str[i]=='\\')
			i++;
		else
		if(str[i]==',')
		{
			if((start<i)
			&&(ignoreQuotes||IsQuotedStringArgument(str.substring(start,i),1,okRegex)))
			{
				parts.push(str.substring(start,i));
				start=i+1;
			}
		}
	}
	if(start<str.length)
	{
		if((ignoreQuotes||IsQuotedStringArgument(str.substring(start,str.length),1,okRegex)))
			parts.push(str.substring(start,str.length));
		else
			parts.remainder = str.substring(start,str.length).trim();
	}
	return parts;
}

function IsQuotedStringArgument(str, args, okRegex)
{
	if((str == null)||(str === undefined))
		return false;
	if((args !== undefined)&&(Number(args)>1))
	{
		var split = SplitQuotedStringArguments(str,false,okRegex);
		return split.length == args;
	}
	str = str.trim();
	if(str.length<2)
		return false;
	if(str.startsWith('`')&&str.endsWith('`'))
	{
		if(okRegex)
		{
			var variableRegex = /\${([^}]+)}/g;
			var match;
			while((match = variableRegex.exec(str)) !== null) {
				var variable = match[1];
				if(!variable.trim().match(okRegex))
					return false;
			}
		}
		return /^(?!.*(([^\\]|^)(?:\\\\)*`)).*$/.test(str.substr(1,str.length-2));
	}
	if(str.startsWith('"')&&str.endsWith('"'))
		return /^(?!.*(([^\\]|^)(?:\\\\)*")).*$/.test(str.substr(1,str.length-2));
	if(str.startsWith("'")&&str.endsWith("'"))
		return /^(?!.*(([^\\]|^)(?:\\\\)*')).*$/.test(str.substr(1,str.length-2));
	return (okRegex && str.match(okRegex));
}

function EnsureQuotedStringArguments(str, args, okRegex)
{
	var nargs = (args==undefined)?0:Number(args);
	if((str == null)
	||(str === undefined)
	||(!str.trim()))
	{
		var s = "''";
		for(var i=1;i<nargs;i++)
			s += ",''";
		return s;
	}
	str = str.trim();
	if(nargs>1)
	{
		// qparts is guaranteed to consume entire str and return either array entries or remainder
		var qparts = SplitQuotedStringArguments(str,false,okRegex);
		if((qparts.length == nargs)&&(!qparts.remainder))
			return str;
		// drop remainder
		if(qparts.length == nargs)
			return qparts.join(',');
		if(qparts.length>nargs) //more legit args than needed? why?
			return qparts.splice(nargs).join(',');
		var nparts = SplitQuotedStringArguments(str,true,okRegex); // parts w or w/o quotes
		// below is a classic use case we must support, and there's never remainder
		if(qparts.length==0)
		{
			while(nparts.length>nargs)
			{
				nparts[nparts.length-2]+=nparts[nparts.length-1];
				nparts=nparts.splice(nparts.length-1,1);
			}
			for(var i=0;i<nparts.length;i++)
				nparts[i]=EnsureQuotedStringArguments(nparts[i],1,okRegex);
			while(nparts.length<nargs)
				nparts.push("''");
			return nparts.join(',');
		}
		// not enough qparts, but more than 0, and nargs always >1
		var isInside = nparts.indexOf(qparts[0]);
		if(isInside == 0)
		{
			nparts.splice(0,1); // eats top of qparts, re-parses rest
			return qparts[0]+','+EnsureQuotedStringArguments(nparts.join(','),nargs-1,okRegex);
		}
		else // qparts not the first, but theres only two
		if((isInside == 1)&&(nargs==2))
			return EnsureQuotedStringArguments(nparts[0],1,okRegex)+','+qparts[0];
		//  just eat the rest and screw qparts
		for (var i = 0; i < nparts.length; i++)
			nparts[i] = EnsureQuotedStringArguments(nparts[i],1,okRegex);
		while (nparts.length < nargs)
			nparts.push("''");
		if (nparts.length > nargs)
			nparts = nparts.slice(0, nargs);
		return nparts.join(',');
	}
	var q;
	if(str.startsWith('"')||str.endsWith('"'))
		q='"';
	else
	if(str.startsWith("'")||str.endsWith("'"))
		q="'";
	else
	if(str.startsWith("`")||str.endsWith("`"))
		q="`";
	else
	if(str.indexOf("'")>=0)
		q='"';
	else
	if(okRegex && (str.match(okRegex)))
		return true;
	else
		q="'";
	if(!str.startsWith(q))
		str = q + str;
	if(!str.endsWith(q))
		str = str + q;
	for(var i=1;i<str.length-1;i++)
	{
		if(str[i] == '\\')
			i++;
		else
		if(str[i] == q)
		{
			str = str.substr(0,i)+'\\'+str.substr(i);
			i++;
		}
	}
	if(q == '`' && okRegex)
	{
		var variableRegex = /\${([^}]+)}/g;
		str = str.replace(variableRegex, function(match, variable) {
			var trimmedVar = variable.trim();
			if(trimmedVar.match(okRegex))
				return match;
			else
				return trimmedVar;
		});
	}
	return str;
}

function isValidJavaScriptLine(str) 
{
	if (!str)
		return false;
	str = str.toLowerCase().trim();
	if (!((str.startsWith('window.currwin.'))
	|| (str.startsWith('addtoprompt'))
	|| (str.startsWith('contextdelayhide'))
	|| (str.startsWith('return mxpcontextmenu'))
	|| (str.startsWith('win.'))))
		return false;

	if(str.startsWith('return '))
		str = str.substr(7).trim();
	const prefixRegex = /^\s*(?:[a-za-z_$][a-za-z_$0-9]*\s*\.\s*)*[a-za-z_$][a-za-z_$0-9]*\s*\(/;
	const prefixMatch = str.match(prefixRegex);
	if (!prefixMatch) 
		return false;

	var extractInnerArgs = function(str)
	{
		var openPos=str.indexOf('(');
		if(openPos===-1) 
			return null;
		var start=openPos+1;
		var count=1;
		var i=start;
		var inQuote=false;
		var quoteChar='';
		var escape=false;
		while(i<str.length) 
		{
			var c=str[i];
			if(escape) 
			{
				escape=false;
				i++;
				continue;
			}
			if(c==='\\') 
			{
				escape=true;
				i++;
				continue;
			}
			if(inQuote) 
			{
				if(c===quoteChar)
					inQuote=false;
				i++;
				continue;
			}
			if((c==='"')||(c==="'")) 
			{
				inQuote=true;
				quoteChar=c;
				i++;
				continue;
			}
			if(c==='(') 
				count++;
			else 
			if(c===')') 
			{
				count--;
				if(count===0)
					return str.slice(start, i).trim();
			}
			i++;
		}
		return null;
	};
	
	var inner=extractInnerArgs(str);
	if(inner===null)
		return false;
	var closingPos=str.indexOf('(', prefixMatch[0].length-1)+1+inner.length;
	var suffix=str.slice(closingPos+1).trim();
	if (!/^(;)?$/.test(suffix)) 
		return false;
	while(inner.startsWith('this,')||inner.startsWith('event,'))
		inner = inner.substr(inner.indexOf(',')+1).trim();
	var sanitized='['+inner+']';
	sanitized = sanitized
		.replace(/\'(.*?)\'/g,'"$1"')
		.replace(/([\{ ]*?)([a-z0-9_]*?)(\:)/gi,'$1"$2"$3')
		.replace(/,\s*([\]\}])/g,'$1');
	try 
	{
		JSON.parse(sanitized);
		return true;
	} 
	catch (e) 
	{
		return false;
	}
}

function SafeEval(str, context) 
{
	if (!str || !str.trim())
		return true;
	if (str.includes('isConnected()'))
		str = str.replace(/isConnected\(\)/g, 'wsopened');
	var tokens =  str.match(/[\w.]+/g)?.map(t => t.trim()) || [];
	var operators = str.match(/\s*(&&|\|\|)\s*/g)?.map(op => op.trim()) || [];
	var values = tokens.map(function(token) {
		if (token.includes('('))
			return false;
		try {
			var value = context;
			for (var key of token.split('.')) 
			{
				value = value[key];
				if (value === undefined || value === null) 
					return false;
			}
			return value;
		} catch (e) {
			return false;
		}
	});
	var result = values[0];
	for (var i = 0; i < operators.length; i++) 
	{
		if (operators[i] === '&&')
			result = result && values[i + 1];
		else if (operators[i] === '||')
			result = result || values[i + 1];
	}
	return !!result;
}

function SipWin(elem)
{
	while(elem && !elem.sipwin && elem.parentNode)
		elem = elem.parentNode;
	if(elem)
		return elem.sipwin;
	return undefined;
}

function populateDivFromUrl(div, url, callback) 
{
	var xhr = new XMLHttpRequest();
	xhr.open('GET', url, true);
	xhr.onreadystatechange = function() {
		if((xhr.readyState === 4)&&(xhr.status === 200)) 
		{
			var txt = xhr.responseText; // Populate div
			var x = txt.indexOf('${');
			while(x>=0) {
				var y = x+2;
				var depth = 0;
				while((y<txt.length) && ((txt[y]!='}')||(depth>0)))
				{
					var c = txt[y++];
					if(c=='{')
						depth++;
					else
					if(c=='}')
						depth--;
				}
				if((y>x)&&(y<txt.length))
				{
					var js = txt.substr(x+2,y-(x+2));
					txt = txt.substr(0,x) + eval(js) + txt.substr(y+1);
				}
				x = txt.indexOf('${',x+1);
			}
			div.innerHTML = txt;
			var scripts = div.getElementsByTagName("script");
			for (var script of scripts)
				if (script.textContent)
					eval(script.textContent);
			if(callback !== undefined && callback)
				callback();
		}
	};
	xhr.onerror = function() { div.innerHTML = 'Failed'; };
	xhr.send();
}
