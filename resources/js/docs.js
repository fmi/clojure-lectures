$(function() {
  SyntaxHighlighter.highlight({gutter: false, toolbar: false});

  $('code.clojure.functions,clojure.code.symbol').each(function () {
    var code = $(this),
        key = code.html(),
        doc = docs[key];

    if (doc) {
      code.data('docKey', key);
      code.addClass('js-doc');
    }
  });

  var tooltip = $('#doc-tooltip');

  $(document).delegate('.js-doc', {
    mouseenter: function() {
      var code = $(this),
          offset = code.offset().top + code.height(),
          doc = window.docs[code.data('docKey')].replace(/^(.*)\n/, "<strong>$1</strong>\n");

      tooltip.show();
      tooltip.css('top', offset + 'px');
      tooltip.html(doc);
    },
    mouseleave: function() {
      tooltip.hide();
    },
  });
});
