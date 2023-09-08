package download

import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


object DownloadUtil {

    private var okHttpClient: OkHttpClient = OkHttpClient()

    fun download(url: String, saveDir: String, listener: OnDownloadListener) {
        if (!url.startsWith("http") && url.startsWith("https")) {
            listener.onDownloadFailed()
            return
        }
        val request: Request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                listener.onDownloadFailed()
            }

            override fun onResponse(call: Call, response: Response) {
                var ips: InputStream? = null
                val buf = ByteArray(2048)
                var len = 0
                var fos: FileOutputStream? = null
                try {
                    response.body?.let { resBody ->
                        ips = resBody.byteStream()
                        val total: Long = resBody.contentLength()
                        val file = File(saveDir, getNameFromUrl(url))
                        fos = FileOutputStream(file)
                        var sum: Long = 0
                        while (ips!!.read(buf).also { len = it } != -1) {
                            fos!!.write(buf, 0, len)
                            sum += len.toLong()
                            val progress = (sum * 1.0f / total * 100).toInt()
                            // 下载中
                            listener.onDownloading(progress)
                        }
                        fos!!.flush()
                        // 下载完成
                        listener.onDownloadSuccess()
                    }

                } catch (e: Exception) {
                    listener.onDownloadFailed()
                } finally {
                    try {
                        ips?.close()
                        fos?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    /**
     * @param url
     * @return
     * 从下载连接中解析出文件名
     */
    fun getNameFromUrl(url: String): String {
        return url.substring(url.lastIndexOf("/") + 1)
    }


    interface OnDownloadListener {
        /**
         * 下载成功
         */
        fun onDownloadSuccess()

        /**
         * @param progress
         * 下载进度
         */
        fun onDownloading(progress: Int)

        /**
         * 下载失败
         */
        fun onDownloadFailed()
    }

}