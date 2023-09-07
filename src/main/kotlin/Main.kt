import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import download.DownloadUtil
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import java.awt.FileDialog
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView


@Composable
@Preview
fun App() {
    rememberWindowState()
    var urlFilePath by remember { mutableStateOf("请选择url配置文件") }
    var saveFilePath by remember { mutableStateOf("请选保存下载文件目录") }
    var urlList: MutableList<String> by remember { mutableStateOf(mutableListOf()) }
    var tips by remember { mutableStateOf("") }
    var chooseDownloadDir by remember { mutableStateOf<File>(File(FileSystemView.getFileSystemView().defaultDirectory.absolutePath)) }
    var progress by remember { mutableStateOf(0.0f) }
    var progressTxt by remember { mutableStateOf("0/0") }
    var totalMount by remember { mutableStateOf(0) }

    MaterialTheme {
        Column(
            verticalArrangement = Arrangement.Top, modifier = Modifier.padding(12.dp)
        ) {
            Row {
                Box(
                    modifier = Modifier.height(50.dp).width(400.dp)
                        .border(width = 2.dp, color = Color.Gray, shape = RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier.padding(10.dp, 0.dp, 0.dp, 10.dp).width(380.dp),
                        color = Color.Gray,
                        fontSize = 14.sp,
                        text = urlFilePath
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(modifier = Modifier.padding(Dp(8.0f), Dp(3.0f), Dp(8.0f), Dp(3.0f)), onClick = {
                    val window = ComposeWindow()
                    val list = mutableListOf(".txt")
                    val fileSet = openFileDialog(window, "请选择书单配置文件", list, false, FileDialog.LOAD)
                    if (fileSet.isNotEmpty()) {
                        urlList.clear()
                        val first = fileSet.first()
                        urlFilePath = first.absolutePath
                        val codeLines = fileSet.first().readLines(Charsets.UTF_8)
                        codeLines.forEach { code ->
                            println("文件内容:${code}")
                            urlList.add(code)
                        }
                    } else {
                        println("未能读取到文件")
                    }
                    progressTxt = "0/${urlList.size}"
                    tips = if (urlList.isEmpty()) "url配置文件无效" else ""
                }) {
                    Text("选择配置文件")
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Row {
                Box(
                    modifier = Modifier.height(50.dp).width(400.dp)
                        .border(width = 2.dp, color = Color.Gray, shape = RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier.padding(10.dp, 0.dp, 0.dp, 10.dp).width(380.dp),
                        color = Color.Gray,
                        fontSize = 14.sp,
                        text = chooseDownloadDir.absolutePath
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(modifier = Modifier.padding(Dp(8.0f), Dp(3.0f), Dp(8.0f), Dp(3.0f)), onClick = {
                    chooseDownloadDir = chooseDownloadDir() ?: return@Button
                }) {
                    Text("更改下载目录")
                }
            }
            Spacer(modifier = Modifier.width(30.dp))
            Text(
                text = tips, modifier = Modifier.height(40.dp), color = Color.Red, fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(30.dp))
            Button(onClick = {
                if (urlList.isEmpty()) {
                    tips = "url配置文件无效"
                    return@Button
                }
                if (!chooseDownloadDir.isDirectory) {
                    tips = "下载目录无效"
                    return@Button
                }
                totalMount = urlList.size
                startDownload(urlList, chooseDownloadDir.absolutePath) { successUrl, errorUrl, restMount ->
                    progressTxt = "${totalMount - restMount}/${totalMount}"
                    progress = 1.0f * (totalMount - restMount) / totalMount
                }
            }) {
                Text("开始下载")
            }
            Spacer(modifier = Modifier.height(30.dp))
            Text(progressTxt)
            Spacer(modifier = Modifier.height(5.dp))
            LinearProgressIndicator(
                modifier = Modifier.height(12.dp).width(400.dp),
                progress = progress,
                strokeCap = StrokeCap.Round,
                color = Color.Green,
                backgroundColor = Color.Red
            )
        }
    }

}


fun main() = application {
    Window(
        title = "绘本下载器", onCloseRequest = ::exitApplication
    ) {
        App()
    }
}

fun openFileDialog(
    window: ComposeWindow,
    title: String,
    allowedExtensions: List<String> = mutableListOf(),
    allowMultiSelection: Boolean = true,
    mode: Int = FileDialog.LOAD
): Set<File> {
    return FileDialog(window, title, mode).apply {
        isMultipleMode = allowMultiSelection
        directory = FileSystemView.getFileSystemView().defaultDirectory.absolutePath
        if (allowedExtensions.isNotEmpty()) {
            if (hostOs == OS.Windows) {
                file = allowedExtensions.joinToString(";") { "*$it" } // e.g. '*.jpg'
            } else {
                setFilenameFilter { _, name ->
                    allowedExtensions.any {
                        name.endsWith(it)
                    }
                }
            }
        }
        isVisible = true
    }.files.toSet()
}

fun chooseDownloadDir(): File? {
    val jf = JFileChooser()
    jf.setFileSelectionMode(JFileChooser.SAVE_DIALOG or JFileChooser.DIRECTORIES_ONLY)
    jf.showDialog(null, null)
    return jf.selectedFile
}

fun startDownload(urlList: MutableList<String>, saveDir: String, result: ((String?, String?, Int) -> Unit)) {
    if (urlList.isEmpty()) {
        return
    }
    val url = urlList.removeAt(0)
    DownloadUtil.download(url, saveDir, object : DownloadUtil.OnDownloadListener {
        override fun onDownloadSuccess() {
            result.invoke(url, null, urlList.size)
            startDownload(urlList, saveDir, result)
        }

        override fun onDownloading(progress: Int) {

        }

        override fun onDownloadFailed() {
            result.invoke(null, url, urlList.size)
            startDownload(urlList, saveDir, result)
        }
    })
}
