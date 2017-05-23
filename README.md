## TestSeafile
### Android应用数据备份和恢复

#### 一.应用介绍
##### 本应用是基于seafile云端的应用程序数据备份恢复工具,通过将应用程序中用户的数据文件进行备份,在用户需要时再同步到本地,实现数据的恢复，特别是在恢复对用户重要的聊天记录或者浏览记录或者媒体文件等的方面有着重要的应用
#### 二.应用程序需要备份的文件列表(包含微信,qq,chrome浏览器)
##### 1.微信列表：
###### 1)"/data/data/com.tencent.mm/MicroMsg"                      //用户数据信息包括动画表情以及文字聊天记录等
###### 2)"/data/data/com.tencent.mm/shared_prefs"                  //用户配置信息包括用户登录名和密码等
###### 3)"/data/media/0/tencent/vusericon"                         //用户图标文件
###### 4)"/data/system/sync"                                       //包含应用账户信息
###### 5)"/data/system/users/0"                                    //应用账户信息

##### 2.qq列表
###### 1)"/data/data/com.tencent.mm/MobileQQ/databases"            //文字信息与静态表情
###### 2)"/data/data/com.tencent.mm/MobileQQ/shared_prefs"         //软件配置信息以及用户登录信息等
###### 3)"/data/media/0/tencent/MobileQQ/diskcache"                //图片信息
###### 4)"/data/media/0/tencent/MobileQQ/shortvideo"                 //视频信息
###### 5)"/data/media/0/tencent/MobileQQ/2107289863(用户账号)/ptt"   //语音信息
###### 6)"/data/data/com.tencent.mobileqq/files/commonusedSystemEmojiInfoFile_v3_3138597361(账号)"   //动画表情信息
###### 7)"/data/data/com.tencent.mobileqq/files/gm_history"        //聊天记录
###### 8)"/data/data/com.tencent.mobileqq/files/ConfigStore2.dat"  //与聊天记录相关的记录信息

###### 3.firefox列表
###### 1)/data/data/org.mozilla.firefox/files/.default            //用户数据信息包括浏览记录以及书签文件
###### 2)/data/data/org.mozilla.firefox/shared_prefs              //软件配置信息以及用户登录信息等
##### 3.初始化需要备份的文件路径类ConfigList
###### 请点击[ConfigList.java](https://github.com/hchong1231/TestSeafile/blob/master/testseafile/src/main/java/com/hersch/testseafile/list/ConfigList.java)
