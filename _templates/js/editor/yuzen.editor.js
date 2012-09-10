!function($) {
	"use strict"; // jshint ;_;
	var transrate = function(value) {
		console.log('transrate ' + new Date);
		// TODO: switch to server side processing.
		var converter = new Markdown.Converter();
		var html = converter.makeHtml(value);
		$('div.preview').html(html);
	};
	var d = $.Deferred().progress(transrate);
	$(function() {
		var cm = CodeMirror.fromTextArea($('textarea.editorMain')[0], {
			mode : 'markdown',
			lineNumbers : true,
			matchBrackets : true,
			tabMode : 'indent',
			onChange : function() {
				d.notify(cm.getValue());
			},
			onCursorActivity : function() {
				cm.setLineClass(lineH);
				lineH = cm.setLineClass(cm.getCursor()['line'], 'activeline');
			}
		});
		transrate(cm.getValue());
		var lineH = cm.getLineHandle(0);
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
					this.component.removeClass('invisible');
				}
			};
			var removeAdd = function(selector, r, a) {
				$(selector).removeClass(r).addClass(a);
			};
			this.expand = function() {
				removeAdd(selector, 'span6', 'span12');
			};
			this.collapse = function() {
				removeAdd(selector, 'span12', 'span6')
			};
		};
		var editorHandler = new StateHandler('div.editor', function(self) {
			$('div.main').prepend(self.component);
		});
		var append = function(self) {
			$('div.main').append(self.component);
		};
		var previewHandler = new StateHandler('div.preview', append);
		var helpHandler = new StateHandler('div.help', append);
		var stateHandlers = {
			eo : function() {
				editorHandler.add();
				editorHandler.expand();
				previewHandler.remove();
				helpHandler.remove();
			},
			ep : function() {
				editorHandler.add();
				editorHandler.collapse();
				previewHandler.add();
				previewHandler.collapse();
				helpHandler.remove();
			},
			po : function() {
				editorHandler.remove();
				previewHandler.add();
				previewHandler.expand();
				helpHandler.remove();
			},
			eh : function() {
				editorHandler.add();
				editorHandler.collapse();
				previewHandler.remove();
				helpHandler.add();
				helpHandler.collapse();
			},
			ho : function() {
				editorHandler.remove();
				previewHandler.remove();
				helpHandler.add();
				helpHandler.expand();
			}
		};
		stateHandlers.ep();
		$(document).on('click', 'div.editor_state button', function() {
			stateHandlers[$(this).data('state')]();
			resize();
		});
		var resize = function() {
			var left = $(window).height() - $('#editor_header').outerHeight()
					- $('div.toolbar').outerHeight();
			if (0 < left) {
				$.each([ editorHandler, previewHandler, helpHandler ],
						function() {
							this.resize(left);
						});
				cm.setSize(null, left);
			}
		};
		resize();
		$(window).resize(resize);
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
						resize();
					}
				});
		// TODO scroll sync
		// TODO search help incrementally
	});
}(window.jQuery);