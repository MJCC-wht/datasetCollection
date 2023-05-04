package com.renhui.androidrecorder.muxer;

public interface ProgressListener {
    void onProgress(long bytesWritten, long contentLength);
}
