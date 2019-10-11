# opencv_android 

opencv 学习
百度云盘下载链接:https://pan.baidu.com/s/1TaVu5_Vpp2psiXVI9bidgw

1.opencv android 开发环境搭建（No manager）

2.添加了扑克牌检测(见下面效果图)，使用的时候红色框需要框柱数字和花色

![image](https://raw.githubusercontent.com/woshiwzy/opencv_android/master/poker_rec_demo.gif)

测试文件在assets 文件夹下面

3.修复某些设备不支持1920X1080预览bug（详情见：org.opencv.android.CameraBridgeViewBase 155行）
详情见：https://www.jianshu.com/p/610bb95eb389
原始错误：It seems that you device does not support camera (or it is locked). Application will be closed.

4.添加自定义人脸检测

