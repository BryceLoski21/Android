# MockLoc.apk

- Author: BryceLoski21
- Time: 2021/11/27

## 项目介绍

- 一个平平无奇的模拟定位软件
- 班级魔方亲测可以使用(就是为了这玩意儿搞得,这玩意儿能用不就行了吗 :) )
- 使用高德地图,百度地图,腾讯地图无法使用,使用这3家地图API的应用也可能无法使用,原因在其会检查手机是否开启了开发者模式以及是否允许模拟定位权限isEnable() :fu:

> 手机定位通常采用GPS定位以及网络定位, 此项目通过获取系统服务(SYSTEM_SERVICE)中的定位服务(LOCATION_SERVICE), 更改GPS模块(GPS_PROVIDER)以及NETWORK_PROVIDER的位置信息,达到GPS欺骗网络位置欺骗的效果

## 运行环境

- 适用于装配Android 6.0以上版本的安卓手机
- 需要获取GPS权限,联网权限(用于获取网络位置),允许模拟位置权限

## 使用说明

***apk已签名,&ensp;可直接安装,&ensp;已签名安装包在&ensp;ApkSigned&ensp;目录下***

1. 手机需要打开开发者权限(设置中点击7次版本号:warning:)
2. 在开发者选项中打开"允许模拟定位"选项(Android6.0以上版本删除了此选项,需要将本应用选择为模拟定位应用:warning:)
3. 在version 1.0版本中,由于监听问题,暂时无法获取当前定位(GET LOCATION),后续会修复:running:
4. 点击"MOCK LOCATION":hankey:
5. ok,拿捏了:ok_hand:
