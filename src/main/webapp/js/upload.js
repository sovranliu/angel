var uploadImg = {};
(function($) {

  uploadImg.UploadPage = function(p){
    this.init(p);
    this.events();
  }

  uploadImg.UploadPage.prototype = {
    constructor: uploadImg.UploadPage,
    init : function (p) {
        this.p = {
            fileInput : '#multi-upload',
            upImgBox : '#up-img-box',
            errorMsg : '#error-message',
            imgMsg : '.imgmsg',
            wrapImg : '#wrap-img',
            fileButton : '.file-button',
            myArea : '#myarea',
            gDoPublish : '#g-do-publish',
            pdCurrent : '#pd_current',
            pTotal : '.p_total',

        };
        this.errorMessage = {
            'repeatUpload' : '请勿重复上传',
            'maxNumber' : '上传图片不能超过8张'
        };
        this.uploadUrl = p.uploadUrl;
        // this.postUrl = p.postUrl;
        // this.defImgSrc = p.defImgSrc;
        // this.dpListUrl = p.dpListUrl;
        // this.loginProcess = p.login_process;
        // this.thirdPartyLogin = p.third_party_login;
        this.pushLock = true;
        // this.swipe = {};
        this.file = [];
    },
    events : function () {
        var self = this;
        //添加多图上传功能
        this.multiImageUpload();
        //文字计算
        this.textAreaCount();
        //上传图片限制
        this.imgController();
        //发布
        //this.ugcPublish();
        //返回机制
        this.goBack();
        //this.deleteImg();
    },
    //获取URL参数值
    GetRequest:function() {
       var url = location.search; //获取url中"?"符后的字串
       var theRequest = new Object();
       if (url.indexOf("?") != -1) {
          var str = url.substr(1);
          strs = str.split("&");
          for(var i = 0; i < strs.length; i ++) {
             theRequest[strs[i].split("=")[0]]=unescape(strs[i].split("=")[1]);
          }
       }
       return theRequest;
    },
    imgController: function(){
        var self = this,
            numClick = true,
            areaClick = true;
        $(self.p.fileButton).on('click',function(){
            if(numClick){
                numClick = false;
            }
        })
        $(self.p.myArea).on('click',function(){
            if(areaClick){
                areaClick = false;
            }
        })
    },
    //字数限制
    textAreaCount: function(){
        var self = this;
        $(self.p.myArea).on('input',function(){
            var myarea = $(this).val(),
                total = 500,
                len,
                message = '真棒，还可写';
            if(myarea.length >= total){ 
              $(this).val(myarea.substr(0,total));
              len = 0;
            }else if(myarea.length < 15 ){
              len = 15 - myarea.length;
              message = '加油，还差';
            }else {
              len = total - myarea.length;
            }
            $('#pinglun').text(message+len+'个字');
        })
    },
    //发布
    // ugcPublish: function() {
    //     var self = this;
    //     $(self.p.gDoPublish).click(function(){
    //         var upImg = $(self.p.upImgBox).find('.up-img'),
    //             array=[],
    //             jsonText='',
    //             textVail = $(self.p.myArea).val();
    //         if( upImg.length > 0){
    //             for(i = 0 ; i < upImg.length; i++){
    //                 var data = {};
    //                 data['image_id'] = upImg.eq(i).attr('data-hash'); 
    //                 data['host_id'] = upImg.eq(i).attr('data-host');
    //                 array.push(data);
    //             }
    //             jsonText = JSON.stringify(array);
    //         }
    //         if(textVail.length < 15){
    //             self.poPrompt('请输入15-200个汉字');
    //         }else if(!self.pushLock){
    //             self.poPrompt('照片还在上传中,请稍后发布');
    //         }else{
    //             $('#images').val(jsonText);
    //             $('#ugcpublish').submit();
    //         }
    //     });
    // },
    //返回
    goBack:function(){
        var self = this;
        $('#g-go-url-back').on('click',function(){
            var imglen = $(self.p.upImgBox).find('img').length;
            if($(self.p.myArea).val() == 0 && imglen == 0){
                history.back();
            }else{
                var t = new window.S({'background-color':'rgba(51,51,51,0.3)'}),
                    html = '<div class="exitEdit"><p id="popu_close"><i class="g-touchicon"></i></p><em>退出此次编辑?</em><span id="popu_link">退出</span></div>';
                t.append(html);
                t.create();
                window.scrollTo(0,50);
                $('#popu_close').on('click',function(){
                    t.remove();
                })
                $('#popu_link').on('click',function(){
                    history.back();
                }) 
            }
        })
    },
    //共用-提示层
    poPrompt: function(text){
        var t = new window.S(),
            html = '<div class="signupopu">'+text+'</div>';
        t.append(html);
        t.create();
        setTimeout(function(){
            t.remove();
        },1500);
        window.scrollTo(0,50);
    },
    //swipe绑定
    // bindSwipe: function(swipe, index, op) {
    //     var self = this,
    //     defaults = {
    //         startSlide: index,
    //         speed: 400,
    //         continuous: false,
    //         disableScroll: false,
    //         stopPropagation: false,
    //         callback: function(index, elem) {},
    //         transitionEnd: function(index, elem) {}
    //     };
    //     self.swipe = new Swipe(swipe, $.extend(defaults, op));
    // },
    //图片删除
    deleteImg:function(){
    },
    //文件上传是否成功
    xmlhttprequest: function(url, success, fail) {
        var xhr = new XMLHttpRequest();
        xhr.open('GET', url, true);
        xhr.send();
        if(typeof success !== 'function') {
            success = function() {}
        }
        if(typeof fail !== 'function') {
            fail = function() {}
        }
        // 文件上传成功或是失败
        xhr.onreadystatechange = function(e) {
           if (xhr.readyState == 4) {
                if (xhr.status == 200) {
                    success(xhr.responseText);
                } else {
                    fail();
                }
            }
        };
    },
    //多图上传
    multiImageUpload : function() {
        var self = this;
        self.CUI = new CUI({
            maxWidth : 640,
            maxHeight : 640,
            uploadUrl : self.uploadUrl,
            inputName : 'file',
            file : $(self.p.fileInput).get(0),
            onSuccess : function(index, file, response) {
                //动态进度条
                var box = $(self.p.upImgBox).find('.upload-img-' + index),
                    data;
                if(box.length < 1) {
                    return false;
                }
                box.find('.i-line').css({'width' : '100%'});
                setTimeout(function() { 
                    box.find('.i-shade,.i-progress').remove();
                }, 1000);

                data = $.parseJSON(response);
                // box.data('hash', data.image.hash);
                // box.data('host', data.image.host);
                box.attr('data-url', data.data);
                $(self.p.imgMsg).hide();
                //绑定删除图片功能
                self.file.push(file);
                // self.viewPicBind(file, index);
                self.pushLock = true;
            },
            onProgress : function(index, file, loaded, total) {
                //动态进度条
                var box = $(self.p.upImgBox).find('.upload-img-' + index),
                    ratio = parseInt(loaded/total*100),
                    k;
                box.find('.i-line').css({'width' : ratio + '%'});
            },
            onSelect : function(index, file) {
                console.log(file);

                //插入缩略图
                var box = $(self.p.upImgBox),
                    boxBig = $(self.p.wrapImg),
                    reader = new FileReader(),
                    imglen = $(self.p.upImgBox).find('img').length,
                    img = document.createElement('img'),
                    img2 = document.createElement('img'),
                    html2,
                    shtml,
                    html;
                reader.readAsDataURL(file);
                reader.onload = function(e) {
                    img.src = this.result;
                    img.setAttribute('data-filename', file.name);
                    img2.src = this.result;
                    img2.setAttribute('data-filename', file.name);
                }
                shtml= '<div class="up-img upload-img-'+index+'" data-index='+ index +'><i class="delete">x</i>'
                html = shtml + '<div class="i-shade"></div><div class="i-progress"><div class="B-60AD00 i-line"></div></div></div>';
                html2 = shtml + '</div>';
                $(self.p.fileButton).before($(html).append($(img)));
                boxBig.append($(html2).append($(img2)));
                setTimeout(function() { 
                    box.find('.upload-img-' + index).find('.i-line').css({'width' : '20%'});
                }, 200);
                $(self.p.imgMsg).show();
                if(imglen == 7){
                    $(self.p.fileButton).hide();
                }
                self.pushLock = false;
            },
            onComplete : function() {
            },
            onCheckFile : function(files) {
                return true;
            },
            onMessage : function(msg) {
            },
            onRepeat : function(file) {
                self.showError(self.errorMessage.repeatUpload);
                self.hideError(2000);
            }
        });
        $(self.p.fileInput).on('change', function(){
            self.CUI.upload();
        });
    },
    //展示错误提示
    showError : function(msg) {
        $(this.p.errorMsg).find('span').html(msg);
        $(this.p.errorMsg).removeClass('msg-hide');
    },
    //隐藏错误提示
    hideError : function(time) {
        var self = this;
        if(time !== 'undefined' && time > 0) {
            setTimeout(function() {
                $(self.p.errorMsg).addClass('msg-hide');
            }, time);
        } else {
            $(self.p.errorMsg).addClass('msg-hide');
        }
    },
  } 
})(jQuery);