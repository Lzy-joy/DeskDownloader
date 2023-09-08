package utils

import java.io.File
import java.text.SimpleDateFormat

object FileUtils {

    fun saveErrorLog(dir: String, urlList: MutableList<String>, filePath: ((String) -> Unit)?) {
        val file = File(dir, "000-error-log-${System.currentTimeMillis()}.txt")
        val lineFirst = "created time:${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis())}"
        file.printWriter(Charsets.UTF_8).use { pw ->
            pw.println(lineFirst)
            urlList.forEach {
                pw.println(it)
            }
        }
        filePath?.invoke(file.absolutePath)
    }

}