package br.com.nutrisolver.tools

import android.content.Context
import android.widget.Toast

object ToastUtil {
    private var toast: Toast? = null

    fun show(context: Context, text: String, duration: Int) {

        if (toast != null)
            toast!!.cancel()

        toast = Toast.makeText(context.applicationContext, text, duration)
        toast!!.show()
    }
}