import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import download.DownloadUtil
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import utils.FileUtils
import widget.DownloadBreakWarningDialog
import widget.DownloadedDialog
import widget.ErrorDialog
import java.awt.FileDialog
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

@Composable
@Preview
fun App(isDownloadingFunc: ((Boolean) -> Unit)?) {
    var urlFilePath by remember { mutableStateOf("请选择url配置文件") }
    val urlList: MutableList<String> by remember { mutableStateOf(mutableListOf()) }
    var tips by remember { mutableStateOf("") }
    var chooseDownloadDir by remember { mutableStateOf<File>(File(FileSystemView.getFileSystemView().defaultDirectory.absolutePath)) }
    var progress by remember { mutableStateOf(0.0f) }
    var progressTxt by remember { mutableStateOf("0/0") }
    var totalMount by remember { mutableStateOf(0) }
    val errorUrlList by remember { mutableStateOf<MutableList<String>>(mutableListOf()) }
    var downloading by remember { mutableStateOf(false) }
    var downloadingTxt by remember { mutableStateOf("点击下载") }

    MaterialTheme {
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.padding(12.dp).background(Color.White)
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
                       codeLines.filter { url ->
                            !url.startsWith("created time")
                        }.forEach { code ->
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
            Button(
                enabled = !downloading,
                onClick = {
                    if (urlList.isEmpty()) {
                        tips = "url配置文件无效"
                        return@Button
                    }
                    if (!chooseDownloadDir.isDirectory) {
                        tips = "下载目录无效"
                        return@Button
                    }
                    errorUrlList.clear()
                    totalMount = urlList.size
                    downloadingTxt = "下载中..."
                    startDownload(urlList, chooseDownloadDir.absolutePath) { _, errorUrl, restMount ->
                        downloading = restMount != 0
                        isDownloadingFunc?.invoke(downloading)
                        progressTxt = "${totalMount - restMount}/${totalMount}"
                        progress = 1.0f * (totalMount - restMount) / totalMount
                        val errorUrlTemp = errorUrl ?: return@startDownload
                        if (errorUrlTemp.isNotEmpty()) {
                            errorUrlList.add(errorUrlTemp)
                        }
                        if (restMount == 0) {
                            downloadingTxt = "重新下载"
                            if (errorUrlList.isNotEmpty()) {
                                FileUtils.saveErrorLog(chooseDownloadDir.absolutePath, errorUrlList) { errorLogPath ->
                                    ErrorDialog().showDialog(errorLogPath)
                                }
                            } else {
                                DownloadedDialog().showDialog()
                            }
                        }
                    }
                }) {
                Text(downloadingTxt)
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
    var isDownloading by remember { mutableStateOf(false) }
    val downloadingDialog = DownloadBreakWarningDialog()

    val handleExitApp: (() -> Unit) = {
        if (isDownloading) {
            val selectedValue = downloadingDialog.showDialog()
            if (selectedValue == 0) {
                exitApplication()
            }
        } else {
            exitApplication()
        }
    }

    Window(
        title = "绘本下载器",
        onCloseRequest = handleExitApp,
        undecorated = false,
        transparent = false,
        icon = ColorPainter(Color.Red),
        state = rememberWindowState(
            size = DpSize(800.dp, 600.dp),
            position = WindowPosition.Aligned(Alignment.Center)
        ),
        resizable = false
    ) {
        this.window.background = java.awt.Color.white
        App {
            isDownloading = it
        }
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
            if (url.startsWith("created time")) {
                result.invoke(null, null, urlList.size)
            } else {
                result.invoke(null, url, urlList.size)
            }
            startDownload(urlList, saveDir, result)
        }
    })
}
