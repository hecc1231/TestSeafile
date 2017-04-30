# TestSeafile
## Android应用数据备份和恢复
### 一.应用介绍
#### 本应用是基于seafile的应用程序用户数据备份恢复工具,通过将应用程序中用户的数据文件进行备份,在用户需要时再同步到本地,实现数据的恢复，特别是在恢复对用户重要的聊天记录或者浏览记录或者媒体文件等的方面有着重要的应用
### 二.应用程序需要备份的文件列表(包含微信,qq,chrome浏览器)
#### 1.微信列表：
##### 1)"/data/data/com.tencent.mm/MicroMsg"
##### 2)"/data/data/com.tencent.mm/shared_prefs" 
##### 3)"/storage/emulated/0/tencent/MicroMsg"
#### 2.qq列表
##### 1)"data/data/com.tencent.mm/MobileQQ/databases"              //文字信息与静态表情
##### 2)"data/media/0/tencent/MobileQQ/diskcache"                  //图片信息
##### 3)"data/media/0/tencent/MobileQQ/shortvideo"                 //视频信息
##### 4)"data/media/0/tencent/MobileQQ/2107289863(用户账号)/ptt"   //语音信息
##### 5)"data/data/com.tencent.mobileqq/files/commonusedSystemEmojiInfoFile_v3_3138597361(账号)"   //动画表情信息
#### 3.配置列表类ConfigList
##### 请点击[链接](https://github.com/hchong1231/TestSeafile/blob/master/testseafile/src/main/java/com/hersch/testseafile/files/ConfigList.java)
