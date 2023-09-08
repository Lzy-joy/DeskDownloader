package widget

import javax.swing.JDialog
import javax.swing.JOptionPane

class ErrorDialog {
    fun showDialog(path: String): Int {
        val ops = arrayOf("确定")
        val pane = JOptionPane(
            "有部分文件未下载成功，具体请看日志文件:\n${path}",
            JOptionPane.QUESTION_MESSAGE,
            JOptionPane.YES_OPTION,
            null,
            ops,
            0
        )
        pane.selectInitialValue()
        val dialog: JDialog = pane.createDialog("⚠️警告")
        dialog.show()
        dialog.dispose()
        val selectedValue = (pane.value) as? String ?: return JOptionPane.CLOSED_OPTION
        if (selectedValue == "确定") {
            return 0
        }
        return 1;
    }
}