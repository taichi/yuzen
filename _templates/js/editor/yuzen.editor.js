/*jshint browser:true, laxbreak:true, laxcomma:true*/
/*jshint devel:true*/
/*global CodeMirror:false, screenfull:false, marked:false*/
;(function($) {
	"use strict"; // jshint ;_;

	// Utilities
	var eventNames = {
		adjustHeight : 'adjustHeight',
		stateChange : 'stateChange'
	};

	var replaceCss = function($this, r, a) {
		$this.removeClass(r).addClass(a);
	};

	// Outline
	var processOutline = function($parent, prefix) {
		var ot = '<div class="outline span2">';
		var heads = $parent.find('h1,h2,h3,h4,h5,h6');
		heads.each(function(index) {
			$(this).append('<a href name=' + prefix + index + '>&nbsp;</a>');
			ot += '<div class="' + this.localName + '" data-ol="' + prefix
					+ index + '"><a href="#">' + this.innerText + '</a></div>';
		});
		return ot + '</div>';
	};
	var makeOutline = function(html) {
		var prev = $('div.preview');
		if (!prev[0]) {
			prev = $('<div>' + html + '</div>');
		}
		outline.text = processOutline(prev, 'ol');
	};
	var outline = {
		text : false,
		refs : false
	};
	$(document).on('click', 'div.outline div[data-ol]', function() {
		$('a[name="' + $(this).data('ol') + '"]').focus().blur();
	});
	$(document).on(eventNames.adjustHeight, function(e) {
		$('div.outline').height(e.height);
	});
	$(document).on(eventNames.stateChange, function(e) {
		var $ref = $('div.reference');
		var $outline = $('div.outline');
		if ($outline[0]) {
			if ($ref.hasClass('span10')) {
				if (!outline.refs) {
					outline.refs = processOutline($ref, 'ref');
				}
				$outline.replaceWith(outline.refs);
			} else {
				$outline.replaceWith(outline.text);
			}
		}
		$(window).trigger('resize');
	});

	$(document).on('click', 'button.outline', function() {
		var $outline = $('div.outline');
		var $main = $('div.main');
		if ($(this).hasClass('active') && outline.text) {
			$main.find('div.span6').each(function() {
				replaceCss($(this), 'span6', 'span5');
			});
			$main.find('div.span12').each(function() {
				replaceCss($(this), 'span12', 'span10');
			});
			$main.prepend(outline.text);
			$(document).trigger(eventNames.stateChange);
		} else if ($outline[0]) {
			$main.find('div.span5').each(function() {
				replaceCss($(this), 'span5', 'span6');
			});
			$main.find('div.span10').each(function() {
				replaceCss($(this), 'span10', 'span12');
			});
			$outline.remove();
		}
	});

	// Editor views switching
	var StateHandler = function(selector, add) {
		this.component = false;
		var self = this;
		$(document).on(eventNames.adjustHeight, function(e) {
			if (!self.component) {
				$(selector).height(e.height);
			}
		});
		this.add = function() {
			if (this.component) {
				add(this);
				this.component = false;
			}
		};
		this.remove = function() {
			if (!this.component) {
				this.component = $(selector).remove();
				this.component
						.removeClass('invisible span5 span6 span10 span12');
			}
		};
		this.expand = function() {
			if ($('div.outline')[0]) {
				replaceCss($(selector), 'span5', 'span10');
			} else {
				replaceCss($(selector), 'span6', 'span12');
			}
		};
		this.collapse = function() {
			if ($('div.outline')[0]) {
				replaceCss($(selector), 'span10', 'span5');
			} else {
				replaceCss($(selector), 'span12', 'span6');
			}
		};
	};
	var editorHandler = new StateHandler('div.editor', function(self) {
		var $o = $('div.outline');
		if ($o[0]) {
			$o.after(self.component);
		} else {
			$('div.main').prepend(self.component);
		}
	});
	var append = function(self) {
		$('div.main').append(self.component);
	};
	var previewHandler = new StateHandler('div.preview', append);
	var refsHandler = new StateHandler('div.reference', append);
	var stateHandlers = {
		eo : function() {
			editorHandler.add();
			editorHandler.expand();
			previewHandler.remove();
			refsHandler.remove();
		},
		ep : function() {
			editorHandler.add();
			editorHandler.collapse();
			previewHandler.add();
			previewHandler.collapse();
			refsHandler.remove();
		},
		po : function() {
			editorHandler.remove();
			previewHandler.add();
			previewHandler.expand();
			refsHandler.remove();
		},
		er : function() {
			editorHandler.add();
			editorHandler.collapse();
			previewHandler.remove();
			refsHandler.add();
			refsHandler.collapse();
		},
		ro : function() {
			editorHandler.remove();
			previewHandler.remove();
			refsHandler.add();
			refsHandler.expand();
		}
	};
	$(function() {
		refsHandler.remove();
	});
	$(document).on('click', 'div.editor_state button', function() {
		stateHandlers[$(this).data('state')]();
		$(document).trigger(eventNames.stateChange);
	});

	// adjust component height
	$(window).resize(
			function() {
				var left = $(window).height()
						- $('#editor_header').outerHeight()
						- $('div.toolbar').outerHeight();
				if (0 < left) {
					$(document).trigger({
						type : eventNames.adjustHeight,
						height : left
					});
				}
			});

	// Editor
	var aliases = {
		html : "htmlmixed",
		js : "javascript",
		json : "application/json",
		c : "text/x-csrc",
		"c++" : "text/x-c++src",
		java : "text/x-java",
		csharp : "text/x-csharp",
		"c#" : "text/x-csharp",
		scala : "text/x-scala",
		ejs : "application/x-ejs",
		aspx : "application/x-aspx",
		jsp : "application/x-jsp",
		php : "application/x-php",
		pig : "application/x-pig",
		plsql : "application/x-plsql"
	};

	marked.setOptions({
		gfm : true,
		pedantic : false,
		sanitize : true,
		highlight : function(code, lang) {
			var r = $('<div>');
			var mime = aliases[lang];
			CodeMirror.runMode(code, mime ? mime : lang, r[0]);
			return '<span class="cm-s-default">' + r.html() + '</span>';
		}
	});
	var transrate = function(value) {
		$.when(function() {
			console.log('transrate ' + new Date());
			var dfd = $.Deferred();
			setTimeout(function() {
				// TODO: switch to server side processing.
				dfd.resolve(marked(value));
			});
			return dfd.promise();
		}()).then(function(html) {
			$('div.preview').html(html);
			setTimeout(function() {
				makeOutline(html);
			});
		});
	};

	$(function() {
		var cm = CodeMirror.fromTextArea($('textarea.editorMain')[0], {
			mode : 'gfm',
			lineNumbers : true,
			matchBrackets : true,
			tabMode : 'indent',
			onChange : function() {
				transrate(cm.getValue());
			},
			onCursorActivity : function() {
				cm.setLineClass(lineH);
				lineH = cm.setLineClass(cm.getCursor().line, 'activeline');
			}
		});
		$(document).on(eventNames.adjustHeight, function(e) {
			cm.setSize(null, e.height);
		});

		setTimeout(function() {
			transrate(cm.getValue());
			$(window).trigger('resize');
		});
		var lineH = cm.getLineHandle(0);
		// TODO auto save & restore to local storage
		// TODO search reference incrementally
		// TODO save to S3 by CORS

		// TODO FullScreen mode
		// https://wiki.mozilla.org/Gecko:FullScreenAPI
		// http://hacks.mozilla.org/2012/01/using-the-fullscreen-api-in-web-browsers/
		// https://developer.mozilla.org/en-US/docs/DOM/Using_full-screen_mode
		// https://github.com/sindresorhus/screenfull.js/
		// HTML5のFullscreenAPIを使ってFullScreenモードに切り替えるとアルファベット系の入力が全て無効化される。
		// これに対応する為、iframeタグによって領域を分割した上で表示内容の調節を行う必要がある。
	});
}(window.jQuery));
