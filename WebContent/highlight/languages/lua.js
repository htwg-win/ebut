/*
Language: Lua
Author: Andrew Fedorov <dmmdrs@mail.ru>
*/

hljs.LANGUAGES.lua = function(){
  var OPENING_LONG_BRACKET = '\\[=*\\[', CLOSING_LONG_BRACKET = '\\]=*\\]';
  return {
    defaultMode: {
      lexems: [hljs.UNDERSCORE_IDENT_RE],
      keywords: {
        'keyword': {
          'and': 1, 'break': 1, 'do': 1, 'else': 1, 'elseif': 1, 'end': 1,
          'false': 1, 'for': 1, 'if': 1, 'in': 1, 'local': 1, 'nil': 1,
          'not': 1, 'or': 1, 'repeat': 1, 'return': 1, 'then': 1, 'true': 1,
          'until': 1, 'while': 1
        },
        'built_in': {
          '_G': 1, '_VERSION': 1, 'assert': 1, 'collectgarbage': 1, 'dofile': 1,
          'error': 1, 'getfenv': 1, 'getmetatable': 1, 'ipairs': 1, 'load': 1,
          'loadfile': 1, 'loadstring': 1, 'module': 1, 'next': 1, 'pairs': 1,
          'pcall': 1, 'print': 1, 'rawequal': 1, 'rawget': 1, 'rawset': 1,
          'require': 1, 'select': 1, 'setfenv': 1, 'setmetatable': 1,
          'tonumber': 1, 'tostring': 1, 'type': 1, 'unpack': 1, 'xpcall': 1,
          'coroutine': 1, 'debug': 1, 'io': 1, 'math': 1, 'os': 1, 'package': 1,
          'string': 1, 'table': 1
        }
      },
      contains: ['comment', 'function', 'number', 'string']
    },
    modes: [
      // comment
      {
        className: 'comment',
        begin: '--(?!' + OPENING_LONG_BRACKET + ')', end: '$'
      },
      {
        className: 'comment',
        begin: '--' + OPENING_LONG_BRACKET, end: CLOSING_LONG_BRACKET,
        contains: ['long_brackets'],
        relevance: 10
      },
      // long_brackets
      {
        className: 'long_brackets',
        begin: OPENING_LONG_BRACKET, end: CLOSING_LONG_BRACKET,
        contains: ['long_brackets'],
        noMarkup: true
      },
      // function
      {
        className: 'function',
        begin: '\\bfunction\\b', end: '\\)',
        lexems: [hljs.UNDERSCORE_IDENT_RE],
        keywords: {'function': 1},
        contains: [
          {
            className: 'title',
            begin: '([_a-zA-Z]\\w*\\.)*([_a-zA-Z]\\w*:)?[_a-zA-Z]\\w*', end: hljs.IMMEDIATE_RE
          },
          {
            className: 'params',
            begin: '\\(', endsWithParent: true,
            contains: ['comment']
          },
          'comment'
        ]
      },
      // number
      hljs.C_NUMBER_MODE,
      // string
      hljs.APOS_STRING_MODE,
      hljs.QUOTE_STRING_MODE,
      {
        className: 'string',
        begin: OPENING_LONG_BRACKET, end: CLOSING_LONG_BRACKET,
        contains: ['long_brackets'],
        relevance: 10
      },
      hljs.BACKSLASH_ESCAPE
    ]
  };
}();
