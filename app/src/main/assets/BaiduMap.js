var map;
        var points = []; // 存储轨迹点的数组
        var polyline; // 用于绘制轨迹线的对象
        var mapIsBeingDragged = false; // 追踪地图拖动状态
        var marker;

        function initMap() {
            map = new BMap.Map("map"); // 创建百度地图实例
            var point = new BMap.Point(116.404, 39.915); // 创建一个初始点坐标
            map.centerAndZoom(point, 15); // 初始化地图，设置中心点和缩放级别

            // 添加地图控件
            map.addControl(new BMap.NavigationControl());
            map.addControl(new BMap.ScaleControl());
            map.addControl(new BMap.OverviewMapControl());
            map.addControl(new BMap.MapTypeControl());

            // 添加地图拖动事件监听器
            map.addEventListener("dragstart", function() {
                mapIsBeingDragged = true;
            });

            map.addEventListener("dragend", function() {
                mapIsBeingDragged = false;
            });
        }

        // 调用 initMap 函数初始化地图
        document.addEventListener('DOMContentLoaded', initMap);

        // 创建 WebSocket 连接和自动重连逻辑
        var socket = null;
        var socketUrl = 'ws://47.109.46.41:8001'; // 替换为你的 WebSocket URL

        function connectWebSocket() {
            socket = new WebSocket(socketUrl);

            socket.onopen = function(event) {
                console.log('WebSocket is connected.');
                // 成功连接后可以向服务器发送消息
                socket.send('Hello Server!');
            };

            socket.onmessage = function(event) {
                console.log('Received message from server: ' + event.data);

                    // 解析 JSON 数据
                    var data = JSON.parse(event.data);

                    // 提取 GGA 信息，并去除 $ 符号之前的部分
                    var ggaData = data.GGA.split('$')[1];

                    // 分割 GGA 字符串以提取各部分
                    var ggaParts = ggaData.split(',');

                    // 提取经度信息
                    var rawLon = ggaParts[4]; // 经度 10636.505176
                    var parsedLon = dmsToDecimal(rawLon); // 转换为十进制度数

                    // 提取纬度信息
                    var rawLat = ggaParts[2]; // 纬度 2623.010141
                    var parsedLat = dmsToDecimal(rawLat); // 转换为十进制度数

                    // 获取显示经度的 HTML 元素
                    var lonElement = document.querySelector('.text-lon');
                    // 显示解析后的经度
                    lonElement.textContent = '经度: ' + parsedLon.toFixed(11); // 保留6位小数

                    // 获取显示纬度的 HTML 元素
                    var latElement = document.querySelector('.text-lat');
                    // 显示解析后的纬度
                    latElement.textContent = '纬度: ' + parsedLat.toFixed(11); // 保留6位小数
            };

            socket.onclose = function(event) {
                console.log('WebSocket is closed. Reconnecting...');
                alert('WebSocket连接已断开，正在尝试重新连接...');
                setTimeout(connectWebSocket, 100); // 2秒后尝试重新连接
            };

            socket.onerror = function(error) {
                console.log('WebSocket error: ' + error);
                // 在这里处理错误
            };
        }

        // 初始连接
        connectWebSocket();

        function dmsToDecimal(dms) {
            // 提取度数部分
            let degrees = Math.floor(dms / 100);
            // 计算分钟部分
            let minutes = dms - (degrees * 100);
            // 转换为十进制度数
            let decimalDegrees = degrees + (minutes / 60.0);
            return decimalDegrees;
        }

        // 根据RTK状态值返回对应的文本
        function getRTKStateText(rtkState) {
            switch (rtkState) {
                case 0:
                    return '无定位';
                case 1:
                    return '单点定位';
                case 2:
                    return '亚米级定位';
                case 4:
                    return 'RTK固定解';
                case 5:
                    return 'RTK浮动解';
                default:
                    return '未知';
            }
        }

        function wgs84ToGcj02(lon, lat) {
            var pi = 3.1415926535897932384626;
            var a = 6378245.0;
            var ee = 0.00669342162296594323;

            function transformLat(x, y) {
                var ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
                ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
                ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
                ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
                return ret;
            }

            function transformLon(x, y) {
                var ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
                ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
                ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
                ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
                return ret;
            }

            var dLat = transformLat(lon - 105.0, lat - 35.0);
            var dLon = transformLon(lon - 105.0, lat - 35.0);
            var radLat = lat / 180.0 * pi;
            var magic = Math.sin(radLat);
            magic = 1 - ee * magic * magic;
            var sqrtMagic = Math.sqrt(magic);
            dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
            dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
            var mgLat = lat + dLat;
            var mgLon = lon + dLon;

            return [mgLon, mgLat];
        }

        function gcj02ToBd09(lon, lat) {
            var x_pi = 3.14159265358979324 * 3000.0 / 180.0;
            var z = Math.sqrt(lon * lon + lat * lat) + 0.00002 * Math.sin(lat * x_pi);
            var theta = Math.atan2(lat, lon) + 0.000003 * Math.cos(lon * x_pi);
            var bd_lon = z * Math.cos(theta) + 0.0065;
            var bd_lat = z * Math.sin(theta) + 0.006;

            return [bd_lon, bd_lat];
        }

        function wgs84ToBd09(lon, lat) {
            var gcj02 = wgs84ToGcj02(lon, lat);
            return gcj02ToBd09(gcj02[0], gcj02[1]);
        }

        // 居中地图到初始位置
        document.getElementById('CenterMapButton').addEventListener('click', function() {
            var initialPoint = new BMap.Point(116.404, 39.915); // 初始中心点坐标
            map.centerAndZoom(initialPoint, 15);
        });

        // 清除地图上的轨迹点和标记
        document.getElementById('ClearMapButton').addEventListener('click', function() {
            // 清除轨迹点数组
            points = [];
            // 删除轨迹线
            if (polyline) {
                map.removeOverlay(polyline);
                polyline = null;
            }
            // 删除标记
            if (marker) {
                map.removeOverlay(marker);
                marker = null;
            }
        });