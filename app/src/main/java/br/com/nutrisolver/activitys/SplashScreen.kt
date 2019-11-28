package br.com.nutrisolver.activitys

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import br.com.nutrisolver.BuildConfig
import br.com.nutrisolver.R
import br.com.nutrisolver.objects.Fazenda
import br.com.nutrisolver.tools.DataBaseUtil
import br.com.nutrisolver.tools.UserUtil
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric

class SplashScreen : AppCompatActivity() {

    private val sharedpreferences : SharedPreferences by lazy {
        getSharedPreferences("MyPref", Context.MODE_PRIVATE)
    }

    private var fazenda_corrente_id : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Fabric.with(this, Crashlytics())

        val versionCode = BuildConfig.VERSION_CODE
        val versionName = BuildConfig.VERSION_NAME
        Log.i("MY_VERSION_CONTROL", "versionCode: $versionCode")
        Log.i("MY_VERSION_CONTROL", "versionName: $versionName")

        findViewById<TextView>(R.id.splash_version_name).text = versionName

        Handler().postDelayed( {verificaLogin()}, 500)
    }

    private fun verificaLogin(){

        if(!UserUtil.isLogged()){
            val it = Intent(this, Login::class.java)
            startActivity(it)
            finish()
        }
        else{
            fazenda_corrente_id = sharedpreferences.getString("fazenda_corrente_id", "-1") ?: "-1"

            DataBaseUtil.getDocument("fazendas", fazenda_corrente_id)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        val document = task.result
                        if(document != null){
                            val fazenda = document.toObject(Fazenda::class.java)
                            if(fazenda != null) {
                                if (fazenda.dono_uid == UserUtil.getCurrentUser()?.uid) {
                                    val editor = sharedpreferences.edit()
                                    editor.putString("fazenda_corrente_id", fazenda.id)
                                    editor.putString("fazenda_corrente_nome", fazenda.nome)
                                    editor.apply()

                                    startActivity(Intent(applicationContext, Principal::class.java))
                                    finish()
                                    return@addOnCompleteListener
                                }
                            }
                        }
                    }
                    startActivity(Intent(applicationContext, SelecionarFazenda::class.java))
                    finish()
                }
        }
    }
}
