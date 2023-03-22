# DatasetCollection

#### 1.1 需求说明
需要做的事情就是：根据不同的数据集要求录制视频、音频，保存为mp4文件和wav文件，并上传至服务器。

#### 1.2 实现方式
Android音视频采集的方法：预览用SurfaceView，视频采集用Camera类，音频采集用AudioRecord。

#### 1.3 数据处理思路
使用MediaCodec类进行编码压缩，视频压缩为H.264，音频压缩为wav。视频使用MediaMuxer，音频直接
用文件流编码写入



#### test
