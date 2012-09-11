!function($) {
	"use strict"; // jshint ;_;
	
	// Utilities
	var replaceCss = function($this, r, a) {
		$this.removeClass(r).addClass(a);
	};

	// Outline
	var makeOutline = function(html) {
		var ot = '<div class="outline span2">';
		var prev = $('div.preview');
		if (!prev[0]) {
			prev = $('<div>' + html + '</div>');
		}
		var heads = prev.find('h1,h2,h3,h4,h5,h6');
		heads.each(function(index) {
			$(this).append('<a href name=ol' + index + '>&nbsp;</a>');
			ot += '<div class="' + this.localName + '" data-ol="' + index
					+ '"><a href="#">' + this.innerText + '</a></div>';
		});
		outline.text = ot + '</div>';
		var $outline = $('div.outline');
		if ($outline[0]) {
			$outline.replaceWith(outline.text);
			$(window).trigger('resize');
		}
	};
	var outline = {
		text : false
	};
	$(document).on('click', 'div.outline div[data-ol]', function() {
		$('a[name="ol' + $(this).data('ol') + '"]').focus().blur();
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
			$(window).trigger('resize');
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
		this.resize = function(height) {
			if (!this.component) {
				$(selector).height(height);
			}
		};
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
	var refsHandler = new StateHandler('div.help', append);
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
		$(window).trigger('resize');
	});

	// FullScreen
	var header, footer;
	$(document).on(
			'keyup',
			'body',
			function(event) {
				if (event.keyCode === 122/* F11 */) {
					event.preventDefault();
					var body = $('body');
					if (body.hasClass('fullscreen')) {
						screenfull.exit();
						$('body').removeClass('fullscreen').prepend(header)
								.append(footer);
						$('div.app').addClass('container-fluid');
					} else {
						screenfull.request();
						$('body').addClass('fullscreen');
						$('div.app').removeClass('container-fluid');
						header = $('#editor_header').remove();
						footer = $('#editor_footer').remove();
					}
					$(window).trigger('resize');
				}
			});

	// Editor
	var transrate = function(value) {
		$.when(function() {
			console.log('transrate ' + new Date);
			var dfd = $.Deferred();
			setTimeout(function() {
				// TODO: switch to server side processing.
				var converter = new Markdown.Converter();
				var html = converter.makeHtml(value);
				dfd.resolve(html);
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
			mode : 'markdown',
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
		setTimeout(function() {
			transrate(cm.getValue());
		});
		var lineH = cm.getLineHandle(0);

		var resize = function() {
			var left = $(window).height() - $('#editor_header').outerHeight()
					- $('div.toolbar').outerHeight();
			if (0 < left) {
				$.each([ editorHandler, previewHandler, refsHandler ],
						function() {
							this.resize(left);
						});
				$('div.outline').height(left);
				cm.setSize(null, left);
			}
		};
		resize();
		$(window).resize(resize);
		// TODO auto save to local storage
		// TODO drag & drop file from desktop
		// TODO search help incrementally
	});
}(window.jQuery);
