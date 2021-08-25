package com.example.uploadimagetoserver.models

import android.os.Handler
import android.os.Looper
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream

class UploadRequestBody(
    private val file: File,
    private val contentType: String,
    private val callback: UploadCallBack
): RequestBody(){

    interface UploadCallBack{
        //This is the function that will update the upload progress of our image
        fun onProgressUpdate(percentage: Int)

    }

    inner class ProgressUpdate (
        private val uploaded: Long,
        private val total: Long
    ): Runnable{
        override fun run() {
            callback.onProgressUpdate((100 * uploaded / total).toInt())
        }
    }

    //This function returns the type of content to be uploaded
    override fun contentType() = MediaType.parse("$contentType/jpg")

    //This function returns the size of the file
    override fun contentLength() = file.length()

    override fun writeTo(sink: BufferedSink) {
        val length = file.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val fileInputStream = FileInputStream(file)
        var uploaded = 0L

        fileInputStream.use { inputStream ->
            var read: Int
            val handler = Handler(Looper.getMainLooper())
            //If it is not equals to -1, that means we read all the file content in the input stream
            while (fileInputStream.read(buffer).also { read = it } != -1) {
                handler.post(ProgressUpdate(uploaded, length))
                uploaded += read
                sink.write(buffer, 0, read)

            }
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 1048
    }

}


