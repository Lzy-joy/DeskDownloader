package widget

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.res.ResourceLoader
import androidx.compose.ui.res.loadImageBitmap
import javax.swing.JDialog
import javax.swing.JOptionPane

class DownloadedDialog {
    @OptIn(ExperimentalComposeUiApi::class)
    private val loadImageBitmap = loadImageBitmap(ResourceLoader.Default.load("icon/ic_launcher.png"))
    private val ops = arrayOf("确定")
    private var pane: JOptionPane = JOptionPane(
        "文件全部下载完毕",
        JOptionPane.QUESTION_MESSAGE,
        JOptionPane.YES_NO_OPTION,
        null,
        ops,
        0
    )

    init {
        pane.selectInitialValue()
    }

    fun showDialog(): Int {
        val dialog: JDialog = pane.createDialog("下载完毕")
        dialog.setIconImage(loadImageBitmap.toAwtImage())
        dialog.show()
        dialog.dispose()
        val selectedValue = (pane.value) as? String ?: return JOptionPane.CLOSED_OPTION
        if (selectedValue == "确定") {
            return 0
        }
        return 1
    }
}