var angel = {};
(function($,Angel) {
    var getDevicePixelRatio = function() {
        var dpr = Math.floor(window.devicePixelRatio);
        dpr = dpr >= 2 ? 2 : 1;
        getDevicePixelRatio = function() {
            return dpr;
        };
        return dpr;
    };

    Angel.Index = function(op) {
        this.circle = op.circle;
        this.isCookie = op.isCookie;
        this.actCookie = op.actCookie;
        this.getCity = op.getCity;
        this.isPhoto = op.isPhoto;
        this.activity = op.activity;
        this.comment = op.comment;
        this.bid_id = op.bid_id;
        this.isMsg = op.isMsg;
        this.swipe = op.swipe;
        this.qrcode = op.qrcode;
        this.needphoto = op.needphoto;
        this.dom = {};
        this.dom.menu = $("#selectMenu");
        this.dom.user = $("#mine");
        this.dom.list = $(".list");
        this.ajaxlock = true;
        this.qrcodelock = true;
        this.init();
    };
    Angel.Index.prototype = {
        constructor: Angel.Index,
        init:function() {
            var self = this;
            $(document).on('touchmove', function(){
                var height = $(document).scrollTop();
                if(height > 40) {
                    $('.pay').addClass('puttop');
                }else {
                    $('.pay').removeClass('puttop');
                }
            });
            if(self.swipe){
                self.sliderImg();
            }
            $('.msg-dot').hide();
            $('.msgnub').hide();
            if(self.circle) {
                self.openUserMenu();
            }
            if(self.isCookie) {
                self.showCity();
                self.searchCity('#forselect');
                $(window).scroll($.proxy(this.scroll, this));
            };
            if(self.actCookie) {
                self.historyCity();
                self.searchCity('#citylist');
                self.actSelectCity();
            };
            if(self.qrcode){
                $('#btnQucode').click(function (){
                    if(self.qrcodelock){
                        self.getQrcode();
                    }
                })
            }
            $('.share').click(function () {
                if(self.qrcodelock){
                    self.getActQrcode();
                }
            })
            if(self.getCity) {
                self.getCityName();
                $('#actdate').date({theme:"datetime"});
                $('#acttime1').date({theme:"datetime"});
            }
            if(self.isMsg) {
                self.getMessage();
                setInterval(self.getMessage,10000);
            }
            if(self.isPhoto) {
                setInterval(self.checkPhoto,200);
                $('#jptime').date({theme:"datetime"});
            }
            if(self.isWX) {
                self.WXconfig();
            };
            $('.dmoney').blur(function() {
                self.regMoney();
            })
            $('#actarea').blur(function(){
                var c = $.trim($(this).val());
                if(c != ''){
                    $('#actaddress').val(c);
                }
            })
            $('#act-submit').click(function() {
                var pageType = $('body').data('page');
                if(pageType == 'JPpage'){
                    self.setJPDialog();//竞拍弹层
                }else{
                    self.setDialog();//发起活动弹层
                }
            })
            $('#cancel-pay').click(function() {
                var r = confirm('放弃本次支付，将被扣取一定的信誉积分');
                if(r == true){
                    self.cancelPay();
                }
            })
            
            self.giveUpAct();
            self.selectPhotos();
            self.partakeList();
            self.selectMenu();
            self.sortMenu();
            self.aucTab();
            $('.actcity').click(function() {
                window.location.href = '/mp/user/selectcity'
            });
            $('.msg-list').each(function() {
                var hasRead = $(this).data('read');
                if(hasRead == 0) {
                    $(this).find('.red-dot').css('display','block');
                }
            })
            $('.msg-list').click(function() {
                var _this = $(this);
                self.msgClick(_this);
            })
            //留言板相关
            $('.js_arr').click(function () {
                var className = $(this).parent().attr('class');
                if(className.indexOf('drop-tree') == -1){
                    $(this).parent().addClass('drop-tree');
                }else{
                    $(this).parent().removeClass('drop-tree');
                }
            })
            $('.dpsubmit').click(function(){
                var con = $('#myarea').val(),
                    rcon = $.trim(con);
                if(rcon.length < 15){
                    $('#pinglun').html('还不满15个字，请继续填写哦~');
                    return false;
                }
                else{
                    $('#ugcpublish').submit();
                }
            })
            self.deleteDP();
        },
        scroll: function() {
            var scrollTop = $(window).scrollTop();
            if (scrollTop > 0) {
                $('#goTop').show();
            } else {
                $('#goTop').hide();
            }
            $('#goTop').click(function () {
                $(this).hide();
                $(window).scrollTop(0);
            })
        },
        // 抽取URL中的参数
        fetchParameters:function() {
            var result = {};
            var path = window.location.search;
            if('?' == path.substring(0, 1)) {
                path = path.substring(1);
            }
            path = path.replace("#", "");
            var parameterStringArray = path.split('&');
            for(var i = 0; i < parameterStringArray.length; i++) {
                var j = parameterStringArray[i].indexOf('=');
                if(-1 == j) {
                    continue;
                }
                result[parameterStringArray[i].substring(0, j)] = decodeURI(parameterStringArray[i].substring(j + 1));
            }
            return result;
        },
        //切换导航菜单
        selectMenu:function() {
            var self = this,
                select = this.dom.menu,
                list = this.dom.list;
            select.click(function() {
                list.toggle();
                $('.bg-wrap').toggle();
                var isUp = select.attr('class').indexOf('down');
                if(isUp != -1) {
                    select.removeClass("down").addClass("up");
                    list.removeClass("animated slideInUp").addClass("animated");
                }else {
                    select.removeClass("up").addClass("down");
                    list.removeClass("animated slideInDown").addClass("animated");
                }
                $(".minelist").hide();
            })
        },
        //获取新消息
        getMessage:function() {
            $.ajax({
                    type:'get',
                    url:'/mp/user/hasmessage',
                    dataType:'json',
                    success:function(d) {
                        if(d.code < 0) {
                            return;
                        }
                        if(d.data != 0) {
                            $('.msg-dot').show();
                            $('#mymsg').append('<i class="msgnub">' + d.data + '</i>');
                        }
                    }
                })
        },
        actSelectCity:function() {
            $('#citylist').find('li').click(function() {
                var cid = $(this).data('code'),
                    cname = $(this).text();
                window.location.href = '/mp/user/organize?cid=' + cid + '&cname=' +cname; 
            });
            $('.current-city').find('a').click(function() {
                 var cid = $(this).data('code'),
                     cname = $(this).text();
                window.location.href = '/mp/user/organize?cid=' + cid + '&cname=' +cname; 
            })
        },
        getCityName:function() {
            var url = window.location.search;
            if('?' == url.substring(0, 1)) {
                url = url.substring(1);
            }
            var path = url.split('&');
            for(var i = 0; i < path.length; i++) {
                var j = path[i].indexOf('=');
                if(-1 == j) {
                    continue;
                }
                if('cid' == path[i].substring(0, j)) {
                    $('.actcity').attr('data-code', path[i].substring(j + 1));
                }
                else if('cname' == path[i].substring(0, j)) {
                    $('.actcity').html(decodeURI(path[i].substring(j + 1)));
                }
            }
        },
        openUserMenu:function() {
            var self = this,
                select = this.dom.user;
            select.click(function() {
                $(".minelist").fadeToggle();
                $(".bg-mast").toggle();
                $(".list").hide();
            });
            $(".bg-wrap,.bg-mast").click(function() {
                $('.bg-wrap').fadeOut();
                $('.bg-mast').fadeOut();
                $(".minelist").fadeOut();
                $(".list").fadeOut();
                $("#selectMenu").removeClass('up').addClass('down');
            })
        },
        //点击我的头像显示遮罩框
        /*
            活动详情页焦点图轮播
            图片大小不一致居中补黑色背景
        */
        sliderImg:function() {
            var self = this;
            var len = $('.swipe-wrap').find('.wrap').length;
            var bullets = $('#position');
            var html = '';
            for(var j = 0;j < len; j++) {
                html += '<li class=""></li>';
            }
            bullets.html(html);
            bullets.find('li').eq(0).addClass('on');
            var slider = Swipe(document.getElementById('slider'), {
                startSlide: 0,  //起始图片切换的索引位置
                speed:500,
                continuous: false,  //无限循环的图片切换效果
                disableScroll: true,  //阻止由于触摸而滚动屏幕
                stopPropagation: false,  //停止滑动事件
                callback: function(pos) {
                    var bulletsli = bullets.find('li');
                    var i = len;
                      while (i--) {
                        bulletsli[i].className = ' ';
                      }
                      bulletsli[pos].className = 'on';
                },  //回调函数，切换时触发
                transitionEnd: function(index, element) {}  //回调函数，切换结束调用该函数
            });
            self.imgZoom('.actimg');
        },
        imgZoom:function(className){
            var boxWidth = document.body.offsetWidth,
                img = $(className);
            img.each(function() {
                var rimg = $(this);
                $("<img/>").attr("src", $(rimg).attr("src")).load(function() {
                    realWidth = this.width;
                    realHeight = this.height;
                    var ppt = 320/boxWidth,
                        newppt = realHeight/realWidth;
                    if(newppt > ppt) {
                        //图片比较高，左右居中
                        var mleft = (boxWidth - 320*(realWidth/realHeight))/2;
                        $(rimg).css("width",320*(realWidth/realHeight) + "px").css("height","320px").css("margin-left",mleft + "px");
                    }else{
                        //图片比较宽，上下居中
                        var mtop = (320 - (boxWidth*newppt))/2;
                        $(rimg).css("width",boxWidth + 'px').css("height", boxWidth*newppt + "px").css("margin-top",mtop + "px");
                    }
                })
            })
        },
        showCity:function() {
            var self = this;
            var historyCitys = $.cookie("cityhistory");
            if(undefined == historyCitys || '' == historyCitys) {
                historyCitys = [];
                $('.current-p').hide();
            }
            else {
                $('.current-p').show();
                historyCitys = eval(historyCitys);
            }
            $('#forselect').on('click','.row',function() {
                var currentCityId = parseInt($(this).attr('data-code'));
                var currentCityName = $(this).html();
                for(i = historyCitys.length - 1; i >= 0; i--) {
                    if(currentCityId == historyCitys[i].id) {
                        historyCitys.splice(i, 1);
                    }
                }
                var newCity = {"id": currentCityId, "name":currentCityName};
                historyCitys.unshift(newCity);
                if(4 == historyCitys.length) {
                    historyCitys.pop();
                }
                $.cookie("cityhistory", JSON.stringify(historyCitys), {expires: 30});
                $.cookie("city", JSON.stringify(newCity), {expires: 30});
                window.location.href="/mp/user/stage";
            });
            $('.current-city').on('click','a',function() {
                var cookie_name = $(this).html(),
                    cookie_id = $(this).attr('data-code'),
                    row = {"name":cookie_name, "id": parseInt(cookie_id)};
                $.cookie("city", JSON.stringify(row), {expires: 30});
                window.location.href="/mp/user/stage";
            });
            //读取cookie中的城市
            if(historyCitys) {
                for(i = 0;i < historyCitys.length;i++) {
                    cont = '<a data-code="'+ historyCitys[i].id + '">' + historyCitys[i].name + '</a>';
                    $('.current-city').append(cont);
                }
            }else{
                $('.current-p').hide();
                $('.current-city').hide();
            }
        },
        historyCity:function() {
            var historyCitys = $.cookie("cityhistory");
            if(undefined == historyCitys || '' == historyCitys) {
                historyCitys = [];
                $('.current-p').hide();
            }
            else {
                $('.current-p').show();
                historyCitys = eval(historyCitys);
            }
            if(historyCitys) {
                for(i = 0;i < historyCitys.length;i++) {
                    cont = '<a data-code="'+ historyCitys[i].id + '">' + historyCitys[i].name + '</a>';
                    $('.current-city').append(cont);
                }
            }else{
                $('.current-p').hide();
                $('.current-city').hide();
            }
        },
        searchCity:function(id) {
            $('#putin').change(function() {
                var content = $(this).val();
                $.ajax({
                    type: "post",
                    url: "/mp/user/searchcity?keyword="+content,
                    data: content,
                    success: function(d) {
                        if(d.code >= 0) {
                            var html = '';
                            $(id).html('');
                            for(var i = 0;i < d.data.length;i++) {
                                html += '<li class="row"  data-code="'+ d.data[i].id;
                                html += '">'+ d.data[i].name +'</li>';
                            }
                            $(id).append(html);
                        }
                    }
                });
            })
        },
        //验证正整数
        regMoney:function() {
            var reg = /^[0-9]\d*$/;
            var value = $('.dmoney').val();
            if(!reg.test(value)) {
                $('.dmoney').val('');
            }
        },
        //验证空
        regEmpty:function(target) {
            var val = $(target).val();
            if($.trim(val) == ''){
                $(target).siblings('.error').css('display','block');
                return false;
            }else{
                $(target).siblings('.error').hide();
                return true;
            }
        },
        
        msgClick:function(target) {
            var url = target.data('url'),
                hasRead = target.data('read'),
                msgid = target.data('id');
            if(url.length > 0) {
                window.location.href = url;
            }
            if(hasRead == 0) {
                $.ajax({
                    type:'get',
                    url:'/mp/user/readmessage',
                    data:{
                        id:msgid
                    },
                    dataType:'text',
                    success:function(d) {
                    }
                })
            }
        },
        aucTab:function() {
            var d = $('.auc-tab-list').find('.on').index();
            $('.auction-list').eq(d).show().siblings().hide();
            $('.auc-tab-list').find('li').click(function() {
                var index = $(this).index();
                $(this).addClass('on');
                $(this).siblings('li').removeClass('on');
                $('.auction-list').eq(index).show().siblings().hide();
            })
        },
        openDialog:function(title,msg) {
            var self = this;
            $('#commonLayer').find('.cui-text-center').html(title);
            $('#commonLayer').find('.cui-select-view').html(msg);
            var htmlHeight = $(window).height();
            var height = $(document).height();
            var dialogWidth= $('#commonLayer').width()/2;
            var dialogHeight= $('#commonLayer').height()/2;
            $('.bg-mast').css('height',height+'px');
            $('.bg-mast').show();
            $('#commonLayer').css('margin-left',-dialogWidth+'px');
            $('#commonLayer').css('margin-top',-dialogHeight+'px');
            $('#commonLayer').show();
            $('.cui-top-close').click(function() {
                self.ajaxlock = true;
                if($('.confirm-btn').length > 0){
                    $('.confirm-btn').remove();
                }
                $('#commonLayer').hide();
                $('.bg-mast').hide();
            })
        },
        openQrcodeDialog:function () {
            var self = this;
            var htmlHeight = $(window).height();
            var height = $(document).height();
            var dialogWidth= $('#QrcodeLayer').width()/2;
            var dialogHeight= $('#QrcodeLayer').height()/2;
            self.qrcodelock = false;
            $('.bg-mast').css('height',height+'px');
            $('.bg-mast').show();
            $('#QrcodeLayer').css('margin-left',-dialogWidth+'px');
            $('#QrcodeLayer').css('margin-top',-dialogHeight+'px');
            $('#QrcodeLayer').show();
            $('.cui-top-close').click(function() {
                self.qrcodelock = true;
                $('#QrcodeLayer').hide();
                $('#code').html('');
                $('.bg-mast').hide();
            })
        },
        getQrcode:function (){
            var self = this;
            var id = parseInt(self.activity);
            var str = 'http://www.angeldinner.com/mp/user/deal?activity=' + id;
            $('#QrcodeLayer').find('.title').html('请对方扫描二维码');
            $("#code").qrcode({
                render: "canvas",
                width: 200,
                height:200,
                text: str
            });
            self.openQrcodeDialog();
        },
        getActQrcode:function (){
            var self = this;
            var id = parseInt(self.activity);
            var str = 'http://www.angeldinner.com/mp/user/activity?id=' + id + '&source=act-' + id;
            $('#QrcodeLayer').find('.title').html('长按保存二维码图片');
            $("#code").qrcode({
                render: "canvas",
                width: 200,
                height:200,
                text: str
            });
            self.openQrcodeDialog();
        },
        setDialog:function() {
            var self = this,
                a = $('#actcity').attr('data-code'),
                b = $('#actname').val(),
                c = $('#actarea').val(),
                d = $('#acttime1').val(),
                f = $('#actdate').val(),
                g = $('#actmoney').val(),
                h = $('#huodong').val(),
                i = $('#nickname').val(),
                j = $('#realname').val(),
                k = $('.up-img').attr('data-url');
            var cishan = $('#cishan').find("option:selected").val();
            var html = '';
            if(!self.checkEmpty(a,'城市') ||!self.checkEmpty(b,'活动名称') || !self.checkEmpty(c,'活动区域') || !self.checkEmpty(j,'真实信息') ){
                return false;
            }
            if(!self.checkEmpty(d,'竞拍截止时间')|| !self.checkEmpty(f,'活动截止时间') || !self.checkEmpty(g,'活动金额') ){
                return false;
            }
            if(!self.checkEmpty(h,'活动内容') || !self.checkEmpty(i,'昵称') || !self.checkEmpty(k,'活动图片')){
                return false;
            }
            html = '<li><label>活动时间：</label><span id="act-date">'+ f +'</span></li>';
            html += '<li style="height:42px;"><label>活动底价：</label><span id="act-money">'+ g +'</span>元<span class="hasmsg">平台将收取最终出价10%的运营费用</span></li>';
            html += '<li><label>慈善比例：</label><span id="act-cishan">'+ cishan +'</span></li>';
            html += '<a id="confirm-submit" class="confirm-btn">确定</a>';
            self.openDialog('信息确认',html);
            $('.cui-layer').on('click','#confirm-submit',function() {
                if(self.ajaxlock){
                    self.submitAct();
                }
            })
        },
        setJPDialog:function() {
            var self = this,
                html = '';
            var m = $('#jpmoney').val();
            var d = $('#jptime').val();
            var k = $('.up-img').attr('data-url');
            if(!self.checkEmpty(m,'竞拍价格')){
                return false;
            }
            if(self.needphoto){
                if(!self.checkEmpty(k,'活动图片')) {
                    return false;
                }
            }
            html += '<li><label>我的出价：</label><span id="act-money">'+ m +'</span> 元</li>';
            html += '<a id="confirm-submit-2" class="confirm-btn">确定</a>';
            self.openDialog('信息确认',html);
            $('.cui-layer').on('click','#confirm-submit-2',function() {
                if(self.ajaxlock){
                    self.submitJP();
                }
            })
        },
        submitAct:function() {
            var self = this;
            self.ajaxlock = false;
            var actaddress = $('#actaddress').val(),
                requirement = $('#requirement').val(),
                career = $('#career').val(),
                beizhu = $('#beizhu').val(),
                needphoto = $('#actneed').find("option:selected").val(),
                acttype = $('#acttype').find("option:selected").val(),
                cishan = $('#cishan').find("option:selected").val(),
                a = $('#actcity').attr('data-code'),
                b = $('#actname').val(),
                c = $('#actarea').val(),
                d = $('#acttime1').val(),
                f = $('#actdate').val(),
                g = $('#actmoney').val(),
                h = $('#huodong').val(),
                i = $('#nickname').val(),
                j = $('#realname').val(),
                photos = '';
                actaddress = actaddress ? actaddress : '';
                requirement = requirement ? requirement : '';
                career = career ? career : '';
                beizhu = beizhu ? beizhu : '';
                beizhu = beizhu ? beizhu : '';
                var z = 0;
                $('.up-img').each(function() {
                    if(0 == z) {
                        photos = $(this).data('url');
                    }
                    else {
                        photos += ',' + $(this).data('url');
                    }
                    z++;
                })
            $.ajax({
                type:'post',
                url:'/mp/user/launch',
                data:{
                    title:b,
                    name:j,
                    cityid:a,
                    type:acttype,
                    region:c,
                    address:actaddress,
                    preparetime:d,
                    starttime:f,
                    basicprice:g,
                    donate:cishan,
                    content:h,
                    rules:requirement,
                    mockname:i,
                    career:career,
                    memo:beizhu,
                    needphoto:needphoto,
                    photos:photos
                },
                dataType:'json',
                success:function(d) {
                    if(d.code < 0) {
                        alert(d.msg);
                    }else{
                        alert('提交成功，请稍后查看哦...');
                        window.location.href = '/mp/user/myactivity';
                    }
                    self.ajaxlock = true;
                }
            })
        },
        submitJP:function() {
            var self = this,
                jpactivity = self.fetchParameters().activity,
                jpdate = $('#jptime').val(),
                jpmoney = $('#jpmoney').val(),
                photo = $('.up-img').attr('data-url');
                jpdate = jpdate ? jpdate : '';
                photo = photo ? photo : '';
                self.ajaxlock = false;
            $.ajax({
                type:'post',
                url:'/mp/user/bid',
                data:{
                    activityid:jpactivity,
                    amount:jpmoney,
                    expiretime:jpdate,
                    photo:photo
                },
                dataType:'json',
                success:function(d) {
                    if(d.code < 0) {
                        alert(d.msg);
                    }else{
                        alert('提交成功，请稍后查看哦...');
                        window.location.href = '/mp/user/mybid';
                    }
                    self.ajaxlock = true;
                }
            })
        },
        //只能上传一张头像
        checkPhoto:function() {
            var isUpload = $('#up-img-box').find('.up-img').length;
            if(isUpload > 0) {
                $('.file-button').hide();
            }
            else{
                $('.file-button').show();
            }
        },
        //选择相册中已有的图片
        selectPhotos:function() {
            var self = this,
                onephoto = true;
            var arr = [];
            $('.form-item').on('click','.photos',function() {
                var url = $(this).attr('src'),
                    index = $(this).data('index');
                var img = '<div class="up-img" data-url="'+ url +'" data-index="'+ index +'"><i class="delete">x</i><img src="' + url + '"></div>';
                if(-1 == arr.indexOf(index) && onephoto) {
                    arr.push(index);
                    $('.file-button').before(img);
                }
                if (self.isPhoto) {
                    onephoto = false;
                };
            })
            $('.form-item').on('click','.delete',function() {
                var index = $(this).parent('.up-img').data('index');
                if (self.isPhoto) {
                    onephoto = true;
                };
                for(i = 0; i < arr.length; i++) {
                    if(index == arr[i]) {
                        arr.splice(i,1);
                    }
                }
                $(this).parent().remove();
            })
        },
        //候选人列表接受、屏蔽、解除屏蔽
        partakeList:function() {
            var self = this;
            $('.detail').on('click','.accept',function() {
                var bid_id = $(this).parent().siblings('.ptname').data('bid');
                var mythis = $(this);
                $.ajax({
                    type:'get',
                    url:'/mp/user/choose?bid=' + bid_id,
                    dataType:'json',
                    success:function(d) {
                        if(d.code >= 0) {
                            mythis.hide();
                            //mythis.parent().siblings('.ptname').find('.accepted').show();
                        }
                        else{
                            if(d.msg != '') {
                                alert(d.msg);
                            }else {
                                alert('网络异常，请稍后重试。');
                            };
                        }
                    }
                })
            });
            $('.detail').on('click','.shield',function() {
                var bid_id = $(this).parent().siblings('.ptname').data('bid');
                var mythis = $(this);
                $.ajax({
                    type:'get',
                    url:'/mp/user/black?bid=' + bid_id + '&op=true',
                    dataType:'json',
                    success:function(d) {
                        if(d.code >= 0) {
                            mythis.parent('.pt-btn-f').hide();
                            mythis.parent().siblings('.pt-btn-s').show();
                        }
                        else{
                            if(d.msg != '') {
                                alert(d.msg);
                            }else {
                                alert('网络异常，请稍后重试。');
                            };
                        }
                    }
                })
            });
            $('.detail').on('click','.release',function() {
                var bid_id = $(this).parent().siblings('.ptname').data('bid');
                var mythis = $(this);
                $.ajax({
                    type:'get',
                    url:'/mp/user/black?bid=' + bid_id + '&op=false',
                    dataType:'json',
                    success:function(d) {
                        if(d.code >= 0) {
                            mythis.parent().siblings('.pt-btn-f').show();
                            mythis.parent('.pt-btn-s').hide();
                        }
                        else{
                            if(d.msg != '') {
                                alert(d.msg);
                            }else {
                                alert('网络异常，请稍后重试。');
                            };
                        }
                    }
                })
            });
        },
        //取消申请，放弃约会，取消活动
        giveUpAct:function() {
            var self = this,
                url = '',
                data = {};
            //竞拍者撤销竞拍-女神没确认
            $('#giveup-apply').click(function() {
                data = {
                    id:self.bid_id
                };
                url = '/mp/user/giveupbid';
                var r = confirm('撤销竞拍，是否确定？（撤销后将无法再参与本活动的竞拍）');
                if(r == true){
                    self.giveUpAjax(url,data);
                }
            });
            //竞拍者取消活动-女神确认之后
            $('#cancel-apply').click(function() {
                data = {
                    id:self.bid_id
                };
                url = '/mp/user/cancelbid';
                var r = confirm('竞拍已中标，放弃本次资格，是否确定？（您将会被扣取一定的信誉值）');
                if(r == true){
                    self.giveUpAjax(url,data);
                }
            });
            //竞拍者违约-竞拍人已付款
            $('#interrupt-apply').click(function() {
                var html = '';
                html += '<p class="title">请输入放弃原因：</p><textarea id="giveup-date-input" type="text"></textarea><span class="error">不能为空</span>';
                html += '<p class="tipmsg">放弃会扣取您大量信誉值，竞拍者会收到完整退款</p>';
                html += '<button id="confirm-interrupt-apply" class="confirm-btn">确定</button>';
                self.openDialog('违约信息',html);
            });
            //竞拍人填写违约理由
            $('.cui-select-view').on('click','#confirm-interrupt-apply',function() {
                var val = '';
                if(self.regEmpty('#giveup-date-input')){
                    val = $('#giveup-date-input').val();
                    data = {
                        id:self.bid_id,
                        reason:val
                    };
                    url = '/mp/user/interruptbid';
                    self.giveUpAjax(url,data);
                }
            });
            //发起者取消活动-活动没上线之前
            $('#giveup-activity').click(function() {
                data = {
                    id:self.activity
                };
                url = '/mp/user/giveupactivity';
                var r = confirm('撤销申请发起活动，是否确定？');
                if(r == true){
                    self.giveUpAjax(url,data);
                }
            });
            //发起者违约-确认竞拍人
            $('#cancel-activity').click(function() {
                data = {
                    id:self.activity
                };
                url = '/mp/user/cancelactivity';
                var r = confirm('放弃本次支付，将被扣取一定的信誉积分');
                if(r == true){
                    self.giveUpAjax(url,data);
                }
            });
            //发起者违约-竞拍人已付款
            $('#interrupt-activity').click(function() {
                var html = '';
                html += '<p class="title">请输入放弃原因：</p><textarea id="giveup-date-input" type="text"></textarea><span class="error">不能为空</span>';
                html += '<p class="tipmsg">放弃会扣取您大量信誉值，竞拍者会收到完整退款</p>';
                html += '<button id="confirm-interrupt-activity" class="confirm-btn">确定</button>';
                self.openDialog('违约信息',html);
            });
            //发起人填写违约理由
            $('.cui-select-view').on('click','#confirm-interrupt-activity',function() {
                var val = '';
                if(self.regEmpty('#giveup-date-input')){
                    val = $('#giveup-date-input').val();
                    data = {
                        id:self.activity,
                        reason:val
                    };
                    url = '/mp/user/interruptactivity';
                    self.giveUpAjax(url,data);
                }
            });
            
            
        },
        giveUpAjax:function(url,data) {
            $.ajax({
                type:'get',
                url:url,
                dataType:'json',
                data:data,
                success:function(d) {
                    if(d.msg != '' && d.msg != null && d.msg != undefined) {
                        alert(d.msg);
                    }
                    if(d.redirect != '' && d.redirect != null && d.redirect != undefined) {
                        if(d.redirect == '#') {
                            window.location.reload();
                        }else{
                            window.location.href = d.redirect;
                        }
                    }
                }
            })
        },
        //排序
        sortMenu:function() {
            $('.sort').click(function() {
                $('.sort-list').toggle();
                $('.bg-wrap').toggle();
            })
            $(".bg-wrap").click(function() {
                $(this).fadeOut();
                $(".sort-list").fadeOut();
            })
        },
        //取消支付
        cancelPay:function() {
            $.ajax({
                type:'get',
                url:'/mp/user/cancelbid',
                data:{
                    bid:this.bid_id
                },
                dataType:'json',
                success:function(d) {
                    if(d.code >= 0) {
                        alert('取消成功');
                    }
                    else{
                        alert(d.msg)
                    }
                }
            })
        },
        checkEmpty:function(val,msg){
            var self = this,
                html = '';
                val = $.trim(val);
            if("" == val || null == val || undefined == val){
                html = '<li>请填写'+ msg +'信息</li>';
                self.openDialog('提示',html);
                return false;
            }
            else{
                return true;
            }
        },
        deleteDP:function(){
            $('.dplist').on('click','#del-dp',function() {
                var div = $(this).parent().parent('.dpitem'),
                    dpid = div.attr('data-dpid');
                $.ajax({
                    type:'get',
                    url:'/mp/user/dplist',
                    data:{
                        dpid:dpid
                    },
                    dataType:'json',
                    success:function(d) {
                        if(d.code >= 0) {
                            div.remove();
                        }
                        else{
                            alert(d.msg)
                        }
                    }
                })
            })
            $('.dplist').on('click','#replay-dp',function() {
                var self = this;
                var dpid = $(this).parent().parent('.dpitem').attr('data-dpid');
                window.location.href = '/mp/user/comment/?activity='+ self.activity +'&comment='+dpid;
            })
        }
    };
    Angel.LazyLoadImg = function(opt) {
        var defaults = {
            min: 0,
            max: -1,
            select: 'img',
            attr: 'data-src',
            ratioAttr: 'origin',
            isClip : false,
        };
        this.ops = {};
        $.extend(this.ops, defaults, opt);
        this.init();
    };
    Angel.LazyLoadImg.prototype = {
        constructor: Angel.LazyLoadImg,
        init: function() {
            var scrolltop = $(window).scrollTop(),
                min,
                wheight,
                max;
            $(function() {
                setTimeout(function() {
                    $(window).scroll();
                }, 200);
            });
            $(window).scroll($.proxy(this.scroll, this));
            $(document).on('touchmove', $.proxy(this.scroll, this));
            min = this.ops.min > scrolltop ? this.ops.min : scrolltop;
            wheight = $(window).height();
            if (this.ops.max === -1) {
                max = wheight + scrolltop;
            } else {
                max = wheight + scrolltop >= this.ops.max ? this.ops.max : wheight + scrolltop;
            }
            this.refreshImg(min, max);
        },
        scroll: function() {
            var max = this.ops.max === -1 ? $(window).scrollTop() +
                $(window).height() : this.ops.max;
            this.refreshImg(this.ops.min, max);
        },

        refreshImg: function(min, max) {
            var _this = this,
                style,
                top;
            style = this.ops.select.replace('.', '');
            $(this.ops.select).each(function(index, el) {
                top = $(el).offset().top;
                if (top >= min && top <= max) {
                    _this.imgReplace($(el), _this.ops.attr, _this.ops.ratioAttr);
                    $(el).removeClass(style);
                }
            });
        },
        imgReplace: function(dom, attr, ratioAttr) {
            var attrName = attr || 'data-src',
                url = dom.attr(attrName),
                ext,
                w,
                h,
                r,
                ratio,
                arr,
                imgWidth,
                imgHeigh,
                support = ratioAttr ? dom.data(ratioAttr) : false,
                imgext,
                routeArr,
                img;
            if (!url) {
                return;
            }
            ext = url.split('/').pop().match(/\d+x\d+/g);
            if (ext) {
                if (support) {
                    w = parseInt(dom.parent().width(), 10); // 获取父元素的宽
                    h = parseInt(dom.parent().height(), 10); // 获取父元素的高
                    arr = ext[0].split('x');
                    imgWidth = parseInt(arr[0], 10);
                    imgHeigh = parseInt(arr[1], 10);
                    ratio = imgHeigh / imgWidth;
                    if (ratio > h / w) {
                        w = parseInt(h / ratio, 10);
                    } else {
                        h = parseInt(w * ratio, 10);
                    }

                } else {
                    w = parseInt(dom.width(), 10);
                    h = parseInt(dom.height(), 10);
                }
                if(w < 1 || h < 1) {
                    return;
                }
                r = getDevicePixelRatio();
                var size = w + 'x' + h;
                if(this.ops && this.ops.isClip == true) {
                    size += "c";                }
                url = url.replace(ext, size);
                if (r !== 1) {
                    routeArr = url.split('.');
                    imgext = routeArr.pop();
                    url = routeArr.join('.') + '@2x.' + imgext;
                }
            }
            if (url) {
                img = new Image();
                img.onerror = function() {
                    // dom.removeAttr('data-src');
                    return false;
                };
                img.onload = function() {
                    // dom.removeAttr('data-src');
                    dom.attr('src', url);
                };
                img.src = url;
            }
        }
    };
    Angel.LoadMorePage = function(opt) {
        this.page = 2;
        this.morelink = $(".morelink");
        this.ajaxLock = true;
        this.type = opt.type;
        this.pagetype = opt.pagetype;
        this.commentpage = 1;
        this.activity = opt.activity;
        this.init();
    };
    Angel.LoadMorePage.prototype = {
        constructor: Angel.LoadMorePage,
        init: function() {
            var _this = this;
            $(window).scroll($.proxy(this.scroll, this));
            _this.Circle();
            _this.getComments(_this.commentpage);
        },
        scroll: function() {
            var _this = this, 
                scrollTop = $(window).scrollTop(),
                wHeight = $(window).height(),
                bodyHeight = $(document).height();
                if (_this.ajaxLock && bodyHeight - scrollTop - wHeight < 40 ) {
                    _this.ajaxLock = false;
                    _this.morelink.show();
                    if(_this.pagetype){
                        _this.getComments(_this.commentpage);
                    }else{
                        _this.loadMoreact();
                    }
                }
            },
        loadMoreact: function() {
            var _this=this,
            ajax_data = {
                page: _this.commentpage,
                type:_this.type
            };
            $.ajax({
                url: '/mp/user/loadactivity',
                type: 'get',
                dataType: 'text',
                data: ajax_data,
                success: function(data) {
                    if(data.indexOf("a") == -1){
                        _this.morelink.hide();
                        _this.ajaxLock = false;
                    }else{
                        _this.ajaxLock = true;
                        _this.renderAct(data,_this.page);
                    }
                },
                error: function() {
                    _this.morelink.hide();
                }
            });     
        },
        renderAct: function(data,page){
            var _this = this;
            _this.morelink.hide();
            _this.page = page + 1;
            $('.amf-content').append(data);
            _this.Circle();
        },
        getComments:function (page) {
            var _this = this;
            $.ajax({
                type:'get',
                url:'/mp/user/fetchcomments',
                data:{
                    page:page,
                    activity:_this.activity
                },
                dataType:'json',
                success:function(d) {
                    if(d.code >= 0) {
                        var data = d.data;
                        _this.renderComments(data);
                        _this.commentpage = page + 1;
                        _this.ajaxLock = true;
                    }
                    else{
                        alert(d.msg);
                    }
                }
            })
        },
        renderComments:function (data) {
            for(var i = 0;i < data.length;i++){
                var html = '';
                html += '<div class="dpitem" data-dpid="' + data[i].id +'">';
                html += '<div class="dpname"><img class="userheart" src="' + data[i].photo + '" alt="">';
                html += '<span>' + data[i].nickName + '</span>';
                html += '<div class="date">' + data[i].submitTime + '</div></div>';
                html += '<div class="dpcontent drop-tree"><p class="dp-hidden line-clamp-4">'+data[i].content+'</p>';
                html += '<span class="arr js_arr"></span></div>';
                if(data[i].response != undefined){
                    html += '<div class="dpreplay dp-hidden">';
                    html += '<b>活动主回复：</b>' + data[i].response + '</div>';
                }
                html += '<div class="dpedit-btn-wrap">';
                html += '<button id="del-dp" class="light-btn">删除</button>';
                if(data[i].response != ''){
                    html += '<button id="replay-dp" class="light-btn">回复</button>';
                }
                html += '</div></div>';
                $('.dplist').append(html);
            }
        },
        Circle:function() {
            $('.circle-progress').each(function (index, el) {
                var num = parseFloat($(this).find('.circle-progress-text label').text());
                if (num <= 50) {
                    var right = num * 3.6 + 135;
                    $(this).find('.circle-left').css('-webkit-transform', "rotate(-225deg)");
                    $(this).find('.circle-left').css('-moz-transform', "rotate(-225)");
                    $(this).find('.circle-left').css('-ms-transform', "rotate(-225)");
                    $(this).find('.circle-left').css('transform', "rotate(-225deg)");
                    $(this).find('.circle-right').css('-webkit-transform', "rotate(" + right + "deg)");
                    $(this).find('.circle-right').css('-moz-transform', "rotate(" + right + "deg)");
                    $(this).find('.circle-right').css('-ms-transform', "rotate(" + right + "deg)");
                    $(this).find('.circle-right').css('transform', "rotate(" + right + "deg)");
                }
                else {
                    var left = num * 3.6 - 45;
                    $(this).find('.circle-right').css('-webkit-transform', "rotate(-45deg)");
                    $(this).find('.circle-right').css('-moz-transform', "rotate(-45deg)");
                    $(this).find('.circle-right').css('-ms-transform', "rotate(-45deg)");
                    $(this).find('.circle-right').css('transform', "rotate(-45deg)");
                    $(this).find('.circle-left').css('-webkit-transform', "rotate(" + left + "deg)");
                    $(this).find('.circle-left').css('-moz-transform', "rotate(" + left + "deg)");
                    $(this).find('.circle-left').css('-ms-transform', "rotate(" + left + "deg)");
                    $(this).find('.circle-left').css('transform', "rotate(" + left + "deg)");
                }
            });
        },
    };
    Angel.Chart = function(op) { 
        this.activity = $('#chat-messages').data('activity');
        this.chart_id = 0;
        this.init();
    };
    Angel.Chart.prototype = {
        constructor: Angel.Chart,
        init:function() {
            var self = this,
                height = $('#chat-messages').height();
            $(window).scrollTop(height);
            setInterval(function() {
                self.getHistory();
            },2000);
            document.onkeydown = function(e) { 
                var ev = document.all ? window.event : e;
                if(ev.keyCode==13) {
                    self.sendMsg();
                 }
            }
            $('#send').click(function() {
                self.sendMsg();
            })
        },
        /**
        进入页面先拉取历史对话，获得最后一个消息id
        点击发送-贴input内容到聊天界面，input清空内容，ajax传参
        获得返回成功将消息放到聊天界面，之前input内容删除
        **/
        getHistory:function() {
            var self = this,
                html = '';
            $.ajax({
                type:'post',
                url:'/mp/user/talk/pull',
                data:{
                    id:this.chart_id,
                    activity:this.activity
                },
                dataType:'json',
                success:function(d) {
                    var data = eval(d.data);
                    if(d.code >= 0) {
                        self.renderMsg(data);
                    }
                }
            });
        },
        sendMsg:function() {
            var self = this,
                content = $('#msgcontent').val(),
                html = '',
                height = $('#chat-messages').height();
            $(window).scrollTop(height);
            if($.trim(content) == ''){
                return false;
            }
            $.ajax({
                type:'post',
                url:'/mp/user/talk/push',
                data:{
                    activity:this.activity,
                    id:this.chart_id,
                    message:content
                },
                dataType:'json',
                success:function(d) {
                    var data = eval(d.data);
                    if(d.code >= 0) {
                        self.renderMsg(data);
                    }
                    $('#msgcontent').val('');
                }
            });
        },
        renderMsg:function(data) {
            for(i = 0;i < data.length;i++) {
                if(data[i].id > this.chart_id) {
                    if(data[i].speaker == 'other') {
                        html = '<div class="message" data-chart="' + data[i].id + '">';
                    }else{
                        html = '<div class="message right" data-chart="' + data[i].id + '">';
                    }
                    html += '<img src="'+ data[i].photo +'">';
                    html += '<div class="bubble">' + data[i].content + '<div class="corner"></div>';
                    html += '</div><span>' + data[i].time + '</span></div>';
                    $('#chat-messages').append(html);
                    this.chart_id = data[i].id;
                    window.scrollTo(0,document.body.scrollHeight)
                }
            }
        },
    };
    Angel.WXconfig = function(op) { 
        this.ajaxlock = true;
        this.bid_id = op.bid_id;
        this.activity_id = op.activity_id;
        this.init();
    };
    Angel.WXconfig.prototype = {
        constructor: Angel.WXconfig,
        init:function() {
            var self = this;
            if(self.ajaxlock){
                $('#chooseWXPay').click(function() {
                    self.ajaxlock = false;
                    self.getWXpath();
                    self.WXpay();
                })
            }
            $('#scanQRCode1').click(function() {
                self.getWXpath();
                self.WXcode();
            })
        },
        getWXpath:function(){
            var url = encodeURIComponent(window.location.href);
            var self = this,
                appId,
                timestamp,
                nonceStr,
                signature;
            $.ajax({
                type:'post',
                url:'/mp/user/preparewechart',
                data:{
                    url:url
                },
                dataType:'json',
                success:function(d) {
                    var data = eval(d.data);
                    if(d.code >= 0) {
                        appId = data.appId;
                        timestamp = data.timestamp;
                        nonceStr = data.nonceStr;
                        signature = data.signature;
                        self.WXConfig(appId,timestamp,nonceStr,signature);
                    }else{
                        alert('企鹅君不给力呀');
                    }
                }
            });
        },
        WXConfig:function(appId,timestamp,nonceStr,signature){
            wx.config({
                debug: false, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
                appId: appId, // 必填，公众号的唯一标识
                timestamp: timestamp, // 必填，生成签名的时间戳
                nonceStr: nonceStr, // 必填，生成签名的随机串
                signature: signature,// 必填，签名，见附录1
                jsApiList: ['chooseWXPay','scanQRCode'] // 必填，需要使用的JS接口列表，所有JS接口列表见附录2
            });
        },
        WXpay:function() {
            var self = this;
            $.ajax({
                type:'post',
                url:'/mp/user/order',
                data:{
                    bid:this.bid_id
                },
                dataType:'json',
                success:function(d) {
                    var data = eval(d.data);
                    if(d.code >= 0) {
                        wx.chooseWXPay({
                            timestamp: data.timestamp, // 支付签名时间戳，注意微信jssdk中的所有使用timestamp字段均为小写。但最新版的支付后台生成签名使用的timeStamp字段名需大写其中的S字符
                            nonceStr: data.nonceStr, // 支付签名随机串，不长于 32 位
                            package: data.package, // 统一支付接口返回的prepay_id参数值，提交格式如：prepay_id=***）
                            signType: 'MD5', // 签名方式，默认为'SHA1'，使用新版支付需传入'MD5'
                            paySign: data.paySign, // 支付签名
                            success: function (res) {
                                // 支付成功后的回调函数
                                window.location.href = '/mp/user/activity?id='+ self.activity_id;
                            },
                            error: function() {
                            }
                        });
                    }
                    else if(d.code < 0) {
                        if(d.msg) {
                            alert(d.msg);
                        }
                    }
                    if(d.redirect) {
                        if(d.redirect == '#') {
                            window.location.reload();
                        }else{
                            window.location.href = d.redirect;
                        }
                    }
                    self.ajaxlock = true;
                },
                error:function(){
                    self.ajaxlock = true;
                }
            })
        },
        WXcode:function() {
            wx.scanQRCode({                 
                needResult: 0,
                scanType: ["qrCode"], // 可以指定扫二维码还是一维码，默认二者都有
                success: function (res) {
                    var result = res.resultStr; // 当needResult 为 1 时，扫码返回的结果
                }
            });
        }
    };
})(jQuery,angel);
