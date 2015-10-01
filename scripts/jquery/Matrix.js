(function(){
  "use strict";
  /*
   * Original source from this site:
   * http://tech.reboot.pro/showthread.php?tid=2659
   * Demo:
   * https://fb.ltdev.im/examples/matrix.html
   * Modified to fit our needs
   */
  function Matrix (opts) {
     opts = opts||{};
     opts.font = opts.font||{};
     this.cid = opts.cid||'matrix';
     this.font = {};
     this.font.link = opts.font.link||'fonts/mCode15.ttf';
     this.font.format = opts.font.format||'truetype';
     this.font.family = opts.font.family||'matrix';
     this.font.size = opts.font.size||'7px';
     this.genStyle(this.cid,this.font);
     this.preload();
     if ($mat(this.cid)) {
        this.can = $mat(this.cid);
        this.ctx = this.can.getContext('2d');
        this.count = opts.count||300;
        this.interval = false;
        this.paused = false;
        this.setSize();
        this.genStrings();
        if (opts.auto) {
           setTimeout(function(){this.init();}.bind(this),1000);
        }
      } else {
        alert('No canvas available!');
     }
  }
  Matrix.prototype.init = function () {
   if (this.interval) { return false; }
   this.paused = false;
   this.ctx.font = this.font.size+' "'+this.font.family+'"';
   this.interval = setInterval(function() {
    this.ctx.fillStyle = "#000000";
    this.ctx.globalAlpha = 0.4;
    this.ctx.fillRect(0,0,this.can.width,this.can.height);
    this.ctx.globalAlpha = 1;
    for (var i = 0; i < this.count; i++) {
      var string = this.strings[i];
      if (string.c !== undefined) {
       this.ctx.fillStyle = "#e1e1e1";
      }
      this.ctx.fillText(this.randletter(), string.x, string.y);
      this.ctx.fillStyle = "#66FFCC"; //"#00EEEE"; // Was originally `lime`
      for (var x = 1; x < string.t; x++) {
        this.ctx.fillText(this.randletter(), string.x, string.y-(x*20));
      }
      string.y += string.s;
      if (string.y > this.can.height+100) {
       this.strings[i] = this.createString();
      }
    }
   }.bind(this),100);
  };
  Matrix.prototype.genStrings = function () {
    this.strings = [];
    for (var i = 0; i < this.count; i++) {
     this.strings.push(this.createString());
    }
  };
  Matrix.prototype.createString = function () {
           var string = {};
           string.x = Math.floor(Math.random()*this.can.width);
           string.y = Math.floor(Math.random()*this.can.height)-Math.floor(Math.random()*400);
           string.t = Math.floor(Math.random()*10)+4;
           if (Math.random() < 0.2) {
              string.c = true;
           }
           string.s = Math.floor(Math.random()*10)+3;
           return string;
  };
  Matrix.prototype.randletter = function () {
           return String.fromCharCode(47+Math.round(Math.random()*13));
           //String.fromCharCode(94+Math.round(Math.random()*25));
  };
  Matrix.prototype.stop = function () {
           if (this.interval) {
              clearInterval(this.interval);
              this.interval = false;
              this.ctx.fillStyle = "#000000";
              this.ctx.fillRect(0,0,this.can.width,this.can.height);
              this.genStrings();
           }
  };
  Matrix.prototype.pause = function () {
           if (this.interval) {
              clearInterval(this.interval);
              this.interval = false;
              this.paused = true;
           }
  };
  Matrix.prototype.setSize = function () {
           var e = window, a = 'inner';
           if (!('innerWidth' in window)) {
              a = 'client';
              e = document.documentElement || document.body;
           }
           var sizes = {width:e[a+'Width'],height:e[a+'Height']};
           this.can.width = sizes.width;
           this.can.height = sizes.height;
           this.ctx.font = this.font.size+' "'+this.font.family+'"';
           window.onresize = function () {
               this.setSize();
           }.bind(this);

  };
  Matrix.prototype.genStyle = function (cid,font) {
          var style = $mat('style',1);
          style.type = "text/css";
          document.getElementsByTagName('head')[0].appendChild(style);
          var cont = '\n@font-face{font-family:"'+font.family+'";src:url("'+font.link+'") format("'+font.format+'");}\n';
          if (cid !== 'matrix') {
             cont += '#'+cid+'{position:fixed;top:0px;left:0px;z-index:-1;}\n';
          }
          if (!!(window.attachEvent && !window.opera)) {
             style.styleSheet.cssText = cont;
           } else {
             style.appendChild(document.createTextNode(cont));
          }
  };
  Matrix.prototype.preload = function () {
           if (!$mat(this.cid)) {
              var can = $mat('canvas',1);
              can.setAttribute('id',this.cid);
              can.setAttribute('style','position:fixed;top:0px;left:0px;z-index:-1;');
              document.body.appendChild(can);
           }
           if (!$mat('matrix-preload')) {
              var preload = $mat('div',1);
              preload.setAttribute('id','matrix-preload');
              preload.setAttribute('style','font-family:"'+this.font.family+'";font-size:0px;');
              preload.textContent = "preload";
              document.body.appendChild(preload);
           }
  };
  function $mat (id,x) {
    /*jslint eqeqeq:false, eqnull:true */
    return (x==null?document.getElementById(id):document.createElement(id));
  }

  var matrix = false;
  $(function () {
       matrix = new Matrix({count:(screen.width/2),auto:1});
  });
})();
