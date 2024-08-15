var map;
var points = []; // 存储轨迹点的数组
var polyline; // 用于绘制轨迹线的对象
var mapIsBeingDragged = false; // 追踪地图拖动状态
var marker;

var last_lon;
var last_lat;

var new_lon;
var new_lat;

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

        // 尝试解析 JSON 数据
        var data;
        try {
            data = JSON.parse(event.data);
        } catch (e) {
            console.error("Invalid JSON data:", e);
            return;
        }

        // 解析 GGA 数据
        if (data.GGA) {
            var ggaData = parseGGA(data.GGA);
            if (ggaData) {
                // 更新 HTML 显示
                document.querySelector('.text-lon').textContent = '经度: ' + ggaData.longitude.toFixed(11);
                document.querySelector('.text-lat').textContent = '纬度: ' + ggaData.latitude.toFixed(11);
                document.querySelector('.text-status').textContent = 'RTK状态: ' + ggaData.rtkState;
                document.querySelector('.text-HCSDS').textContent = '卫星数量: ' + ggaData.satelliteCount;
                document.querySelector('.text-altitude').textContent = '海拔高度: ' + ggaData.altitude + ' m';
                document.querySelector('.text-HDOP').textContent = '水平精度因子: ' + ggaData.HDOP;

                new_lat = ggaData.latitude;
                new_lon = ggaData.longitude;

                const distance = linearDistance(last_lat, last_lon, new_lat, new_lon);
                document.querySelector('.text-Distance').textContent = '距离: ' + (1000 * distance).toFixed(10) + ' m';
            }
        }

        // 解析 RMC 数据
        if (data.RMC) {
            var rmcData = parseRMC(data.RMC);
            if (rmcData) {
                // 更新 HTML 显示
                document.querySelector('.text-speedms').textContent = '速度(m/s): ' + rmcData.speedms.toFixed(4);
                document.querySelector('.text-speedkmh').textContent = '速度(km/h): ' + (rmcData.speedms.toFixed(4) * 3.6).toFixed(3);
            }
        }
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

// 解析 GGA 数据
function parseGGA(ggaSentence) {
    try {
        // 去除 $ 符号
        var ggaParts = ggaSentence.split('$')[1].split(',');

//    $GNGGA,020523.000,2623.013809,N,10636.512344,E,5,33,1.26,1199.9,M,-26.1,M,1.0,0451*4A
//    $GNRMC,020523.000,A,2623.013809,N,10636.512344,E,0.001,208.53,150824,,,F,V*3D

        // 提取经纬度和方向
        var latitude = dmsToDecimal(ggaParts[2]);
        if (ggaParts[3] === 'S') latitude = -latitude;

        var longitude = dmsToDecimal(ggaParts[4]);
        if (ggaParts[5] === 'W') longitude = -longitude;

        // 提取 RTK 状态，卫星数量，海拔高度
        var rtkState = getRTKStateText(Number(ggaParts[6]));
        var satelliteCount = ggaParts[7];
        var HDOP = ggaParts[8];//水平精度因子
        var altitude = ggaParts[9];

        return {
            latitude: latitude,
            longitude: longitude,
            rtkState: rtkState,
            satelliteCount: satelliteCount,
            altitude: altitude,
            HDOP: HDOP
        };
    } catch (e) {
        console.error("Failed to parse GGA:", e);
        return null;
    }
}

// 解析 RMC 数据
function parseRMC(rmcSentence) {
    try {
        // 去除 $ 符号
        var rmcParts = rmcSentence.split('$')[1].split(',');

        // 提取地面速度信息（单位为节，需转换为米/秒）
        var speedInKnots = parseFloat(rmcParts[7]);
        var speedmsInMetersPerSecond = speedInKnots * 0.51444;

        return {
            speedms: speedmsInMetersPerSecond
        };
    } catch (e) {
        console.error("Failed to parse RMC:", e);
        return null;
    }
}

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

// 辅助函数：将角度转换为弧度
function toRadians(degrees) {
    return degrees * Math.PI / 180.0;
}

// 使用 Haversine 公式计算地球上两点之间的距离
function linearDistance(lat1, lon1, lat2, lon2) {
    // 将纬度和经度差转换为弧度
    const dlat = toRadians(lat2 - lat1);
    const dlon = toRadians(lon2 - lon1);

    // 使用 Haversine 公式计算距离
    const a = Math.sin(dlat / 2) * Math.sin(dlat / 2) +
              Math.cos(toRadians(lat1)) * Math.cos(toRadians(lat2)) *
              Math.sin(dlon / 2) * Math.sin(dlon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distance = 6371.0 * c; // 地球半径为 6371 公里

    return distance;
}

document.getElementById('StartRangingButton').addEventListener('click', function() {
    last_lat = new_lat;
    last_lon = new_lon;
});

