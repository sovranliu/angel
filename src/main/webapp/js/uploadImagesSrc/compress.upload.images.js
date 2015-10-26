/**
 * 多图压缩上传功能，兼容ios&android,同是可以用作多文件上传
 * compress.upload.images
 * @package src/
 * @author rockywu wjl19890427@hotmail.com
 * @created 09-09-2014
 * @site www.rockywu.com
 */
;(function() {
    var prefix = 'debug-';
    function getNowTimeStamp() {
        return +new Date();
    }
    var debugTime = {
        begin : function(name) {
            this.list[prefix + name] = {};
            this.list[prefix + name]['begin'] = getNowTimeStamp();
        },
        end : function(name) {
            if(this.list[prefix + name] === undefined) {
                return;
            }
            this.list[prefix + name]['end'] = getNowTimeStamp();
            this.list[prefix + name]['time'] = this.list[prefix + name].end - this.list[prefix + name].begin;
        },
        all : function() {
            var key,
                result = {};
            for(var k in this.list) {
                key = k.split(prefix)[1];
                if(!key) {
                    continue;
                }
                if(typeof this.list[k].time !== 'undefined') {
                    result[key] = this.list[k].time;
                }
            }
            return result;
        },
        list : {}
    }
    window.debugTime = debugTime;
}).call(this);
;(function(T) {
    "use strict";
    var CUI,
        Tools = {
            extend : function(a,b) {
                var k;
                for(k in b) {
                    a[k] = b[k];
                }
                return a;
            },
            rotate : function (canvasTarget, image, w, h,orientation){
                if(orientation==6 || orientation==8){
                    canvasTarget.width = h;
                    canvasTarget.height = w;
                }else{
                    canvasTarget.width = w;
                    canvasTarget.height = h;
                }
                var ctxtarget = canvasTarget.getContext("2d");
                if(orientation==6){
                    ctxtarget.translate(h, 0);
                    ctxtarget.rotate(Math.PI / 2);
                }else if(orientation==8){
                    ctxtarget.translate(0,w);
                    ctxtarget.rotate(270*Math.PI/180 );
                }else if(orientation==3){
                    ctxtarget.translate(w,h);
                    ctxtarget.rotate(Math.PI );
                }
                ctxtarget.drawImage(image, 0, 0);
            }
        };
    CUI = function(params) {
        this.callbackFuns = [ 
            'onSelect',     //文件选择后
            'onDelete',     //文件删除后
            'onProgress',   //文件上传进度
            'onSuccess',    //文件上传成功时
            'onFailure',    //文件上传失败时,
            'onComplete',   //文件全部上传完毕时
            'onMessage',    //文件上传时出现报错提示
            'onCheckFile',  //自定义验证是否多次上传
            'onRepeat',     //重复上传判断
            'onShowImg'     //展示图片
        ];
        this.defParams = {
            file : null,        //input file dom对象
            uploadUrl : null,   //上传地址
            maxWidth : 0,       //图片压缩最大宽度像素默认为0，不压缩
            maxHeight : 0,      //图片压缩最大高度像素默认为0，不压缩
            inputName : 'file', //设置默认提交的input name 为file
            imageQuality : 100,  //默认图片压缩质量为100%
        };
        this.ListIndex = 0;
        this.params = Tools.extend(this.defParams, params);   //统一参数
        this.params.maxWidth = parseInt(this.params.maxWidth);
        this.params.maxHeight = parseInt(this.params.maxHeight);
        this.filesFilter = [];   //文件过滤器
        this.filesName = [];    //文件名保存器
        this.defBoundary = "--image-someboundary--";
        this.init();            //初始化回调方法
    }
    CUI.prototype = {
        constructor : CUI,
        init : function(p) {
            var k,fun;
            for(k in this.callbackFuns) {
                fun = this.callbackFuns[k];
                if(typeof this.params[fun] === 'function') {
                    this.constructor.prototype[fun] = this.params[fun];
                } else if( typeof this.constructor.prototype[fun] !== 'function') {
                    this.constructor.prototype[fun] = function() {};
                }
            }
        },
        onMessage : function(msg) {
            console.log(msg);
        },
        upload : function() {
            var files, k;
            if(typeof this.params.file === 'string') {
                this.params.file = document.querySelector(this.params.file);
            }
            if(typeof this.params.file.files === 'undefined') {
                this.onMessage('请输入input file对象');
                return false;
            }
            files = this.params.file.files;
            if(files.length < 1 ) {
                this.onMessage('请选择上传的文件');
                return false;
            }
            if(!this.onCheckFile(files)) {
                return false;
            }
            for(k = 0; k < files.length; k++) {
                if(!this.checkFile(files[k])) {
                    continue;
                }
                this.onSelect(this.ListIndex, files[k]);
                if(files[k].type  === "image/jpeg") {
                    if(this.params.maxWidth > 0 && this.params.maxHeight > 0) {
                        this.compressUpload(this.ListIndex, files[k]);
                    } else {
                        this.doUpload(this.ListIndex, files[k]);
                    }
                } else {
                    this.doUpload(this.ListIndex, files[k]);
                }
                this.ListIndex++;
            }
        },
        checkFile : function(file) {
            var k, tmp;
            for(k=0; k <= this.filesName.length; k++) {
                if(this.filesName[k] === file.name + file.size) {
                    this.onRepeat(file);
                    return false;
                }
            }
            this.filesName.push(file.name + file.size);
            this.filesFilter.push(file.name + file.size);
            return true;
        },
        deleteFile : function(index, file) {
            var k, tmp = this.filesFilter;
            for(k=0; k <= tmp.length; k++) {
                if(tmp[k] === file.name + file.size) {
                    this.filesFilter.splice(k, 1);
                    return true;
                }
            }
        },
        deleteFileName : function(file) {
            var k;
            for(k=0; k <= this.filesName.length; k++) {
                if(this.filesName[k] === file.name + file.size) {
                    this.filesName.splice(k, 1);
                }
            }
        },
        onCheckFile :function(file) {
            return true;
        },
        doUpload :function(index, file, data, boundary) {
            var self = this,
                formData,
                xhr = new XMLHttpRequest();    //初始化xhr对象
            if(!xhr.upload) {
                this.onMessage('浏览器无法使用xhr.upload对象');
                return false;
            }
            // 文件上传中
            xhr.upload.addEventListener("progress", function(e) {
                self.onProgress(index, file, e.loaded, e.total);
            });
            // 文件上传成功或是失败
            xhr.onreadystatechange = function(e) {
                if (xhr.readyState == 4) {
                    if (xhr.status == 200) {
                        debugTime.end('uploadTime');
                        debugTime.begin("fileSize:"+file.size);
                        debugTime.end("fileSize:"+file.size);
                        self.onSuccess(index, file, xhr.responseText);
                        self.deleteFile(index, file);
                        if (!self.filesFilter.length) {
                            //全部完毕
                            self.onComplete();
                            var runtime = JSON.stringify(debugTime.all());
                            self.params.file.value='';
                        }
                    } else {
                        self.onFailure(index, file, xhr.responseText);
                    }
                }
            };
            // 开始上传
            xhr.open("POST", this.params.uploadUrl, true);
            if(typeof data === 'undefined' || data === '') {
                formData = new FormData();
                formData.append('file', file);
                xhr.send(formData);
            } else {
                boundary = boundary || this.params.defBoundary;
                if (XMLHttpRequest.prototype.sendAsBinary === undefined) {
                    XMLHttpRequest.prototype.sendAsBinary = function(string) {
                        var bytes = Array.prototype.map.call(string, function(c) {
                            return c.charCodeAt(0) & 0xff;
                        });
                        this.send(new Uint8Array(bytes).buffer);
                    };
                }
                debugTime.begin('base64Time');
                var myEncoder = new JPEGEncoder(this.params.imageQuality),//实例化一个jpeg的转码器 
                    JPEGImage = myEncoder.encode(data, this.params.imageQuality);//将图片位图保存为用JPEG编码的格式的字节数组
                    data = JPEGImage.substr(23);
                      //删除base64头
                debugTime.end('base64Time');
                debugTime.begin('uploadTime');
                xhr.setRequestHeader('Content-Type', 'multipart/form-data; boundary=' + boundary);
                xhr.sendAsBinary(['--' + boundary, 'Content-Disposition: form-data; name="' + this.params.inputName + '"; filename="' + encodeURI(file.name) + '"', 'Content-Type: ' + file.type, '', atob(data), '--' + boundary + '--'].join('\r\n'));
            }
        },
        base64Upload : function(data, boundary) {
            var xhr = new XMLHttpRequest(),
                index = this.ListIndex,
                file = {},
                self = this;    //初始化xhr对象
            file = {
                name : "cordova.jpg",
                size : data.length,
            }
            if(!this.onCheckFile(data)) {
                return false;
            }
            if(!xhr.upload) {
                this.onMessage('浏览器无法使用xhr.upload对象');
                return false;
            }
            if(!this.checkFile(file)) {
                return false;
            }
            this.onSelect(index, data);
            this.onShowImg(index, file, "data:image/jpeg;base64," + data);
            // 文件上传中
            xhr.upload.addEventListener("progress", function(e) {
                self.onProgress(index, e.loaded, e.total);
            });
            // 文件上传成功或是失败
            xhr.onreadystatechange = function(e) {
                if (xhr.readyState == 4) {
                    if (xhr.status == 200) {
                        self.onSuccess(index, file, xhr.responseText);
                        self.onComplete();
                    } else {
                        self.onFailure(index);
                    }
                }
            };
            // 开始上传
            xhr.open("POST", this.params.uploadUrl, true);
            boundary = boundary || this.params.defBoundary;
            if (XMLHttpRequest.prototype.sendAsBinary === undefined) {
                XMLHttpRequest.prototype.sendAsBinary = function(string) {
                    var bytes = Array.prototype.map.call(string, function(c) {
                        return c.charCodeAt(0) & 0xff;
                    });
                    this.send(new Uint8Array(bytes).buffer);
                };
            }
            //删除base64头
            xhr.setRequestHeader('Content-Type', 'multipart/form-data; boundary=' + boundary);
            var ss = [
                '--' + boundary,
                'Content-Disposition: form-data; name="' + this.params.inputName + '"; filename="' + encodeURI(file.name) + '"',
                'Content-Type: image/jpeg',
                '',
                atob(data),
                '--' + boundary + '--'
            ];
            xhr.sendAsBinary(ss.join('\r\n'));
            this.ListIndex++;
        },
        compressUpload : function(index, file) {
            var self = this,
                reader = new FileReader(),
                img = document.createElement('img');
            reader.readAsDataURL(file);
            debugTime.begin('fileReadertime');
            reader.onload = function(e) {
                img.src = this.result;
                self.onShowImg(index, file, this.result);
                debugTime.end('fileReadertime');
            }
            img.onload = function() {
                var width = 0,
                    height = 0,
                    base64 = '',
                    mpImg = new MegaPixImage(file),
                    orientation = 1, //照片方向值
                    tmpImg = document.createElement('img');
                if(img.width < self.params.maxWidth && img.height < self.params.maxHeight) {
                    width = img.width;
                    height = img.height;
                } else {
                    if(img.width / self.params.maxWidth > img.height / self.params.maxHeight ) {
                        width = self.params.maxWidth;
                        height = parseInt(img.height * self.params.maxWidth / img.width);
                    } else {
                        width = parseInt(img.width * self.params.maxHeight / img.height);
                        height = self.params.maxHeight;
                    }
                }
                var isMobile = {
                    Android: function() {
                        return /Android/i.test(navigator.userAgent);
                    },
                    iOS: function() {
                        return /iPhone|iPad|iPod/i.test(navigator.userAgent);
                    },
                    Windows: function() {
                        return /IEMobile/i.test(navigator.userAgent);
                    },
                    any: function() {
                        return (isMobile.Android() || isMobile.BlackBerry() || isMobile.iOS() || isMobile.Windows());
                    }
                };
                if(isMobile.iOS()){
                    debugTime.begin('iosRenderTime');
                    mpImg.render(tmpImg, {maxWidth: width, maxHeight: height });
                    EXIF.getData(file, function() {
                        orientation=EXIF.getTag(this,'Orientation');
                        tmpImg.onload=function(){
                            debugTime.end('iosRenderTime');
                            var tmpCvs = document.createElement("canvas"),
                                tmpCtx = tmpCvs.getContext('2d'),
                                data = '';
                            Tools.rotate(tmpCvs, tmpImg, width, height, orientation);
                            if(orientation == 6 || orientation == 8){
                                data = tmpCtx.getImageData(0, 0, height, width);
                            } else {
                                data = tmpCtx.getImageData(0, 0, width, height);
                            }
                            self.doUpload(index, file, data, "kfstouch-someboundary");
                        }
                    });
                }else{
                    debugTime.begin('androidRenderTime');
                    var cvs = document.createElement("canvas"),
                    ctx = cvs.getContext('2d');
                    cvs.width = width;
                    cvs.height = height;
                    ctx.drawImage(img,0,0,width,height);
                    tmpImg.src = cvs.toDataURL("image/jpeg",0.4);
                    tmpImg.onload = function(){
                        debugTime.end('androidRenderTime');
                        EXIF.getData(file, function() {
                            orientation=EXIF.getTag(this,'Orientation');
                            var tmpCvs = document.createElement("canvas"),
                                tmpCtx = tmpCvs.getContext('2d'),
                                data = '';
                            Tools.rotate(tmpCvs, tmpImg, width, height, orientation);
                            if(orientation == 6 || orientation == 8){
                                data = tmpCtx.getImageData(0, 0, height, width);
                            } else {
                                data = tmpCtx.getImageData(0, 0, width, height);
                            }
                            self.doUpload(index, file, data, "kfstouch-someboundary");
                        });
                    }
                }
            }
        }
    };

    window.CUI = CUI;
})(window.T || {});
