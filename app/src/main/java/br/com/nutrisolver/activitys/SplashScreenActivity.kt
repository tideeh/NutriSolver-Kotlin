package br.com.nutrisolver.activitys

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import br.com.nutrisolver.BuildConfig
import br.com.nutrisolver.R
import br.com.nutrisolver.models.Fazenda
import br.com.nutrisolver.utils.*
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var sharedPreferences : SharedPreferences

    private var fazendaCorrenteId : String = DEFAULT_STRING_VALUE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        sharedPreferences = getSharedPreferences(SP_NOME, Context.MODE_PRIVATE)

        Fabric.with(this, Crashlytics())

        //val versionCode = BuildConfig.VERSION_CODE
        val versionName = BuildConfig.VERSION_NAME
        //Log.i("MY_VERSION_CONTROL", "versionCode: $versionCode")
        //Log.i("MY_VERSION_CONTROL", "versionName: $versionName")

        findViewById<TextView>(R.id.splash_version_name).text = versionName

        // verifica é o primeiro run, se for e estiver logado, tem algo errado (nao deveria estar logado)
        checkFirstRun()

        Handler().postDelayed( {verificaLogin()}, 500)
    }

    private fun checkFirstRun() {
        val currentVersionCode : Int = BuildConfig.VERSION_CODE
        val savedVersionCode : Int = sharedPreferences.getInt(SP_KEY_VERSION_CODE, DEFAULT_INT_VALUE)

        val editor = sharedPreferences.edit()
        editor.putInt(SP_KEY_VERSION_CODE, currentVersionCode)
        editor.apply()

        when {
            currentVersionCode == savedVersionCode -> {
                // this is just a normal run
                return
            }
            savedVersionCode == DEFAULT_INT_VALUE -> {
                // this is a new install (or the user cleared the preferences)
                // primeira execução e ja esta logado???

                logout()
                return
            }
            currentVersionCode > savedVersionCode -> {
                // this is an upgrade
                return
            }
        }
    }

    private fun verificaLogin(){

        if(!UserUtil.isLogged()){
            val it = Intent(this, LoginActivity::class.java)
            startActivity(it)
            finish()
        }
        else{

            fazendaCorrenteId = sharedPreferences.getString(SP_KEY_FAZENDA_CORRENTE_ID, DEFAULT_STRING_VALUE) ?: DEFAULT_STRING_VALUE

            DataBaseUtil.getDocument(DB_COLLECTION_FAZENDAS, fazendaCorrenteId)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        val document = task.result
                        if(document != null){
                            val fazenda = document.toObject(Fazenda::class.java)
                            if(fazenda != null) {
                                if (fazenda.donoUid == UserUtil.getCurrentUser()?.uid) {
                                    val editor = sharedPreferences.edit()
                                    editor.putString(SP_KEY_FAZENDA_CORRENTE_ID, fazenda.id)
                                    editor.putString(SP_KEY_FAZENDA_CORRENTE_NOME, fazenda.nome)
                                    editor.apply()

                                    startActivity(Intent(applicationContext, PrincipalActivity::class.java))
                                    finish()
                                    return@addOnCompleteListener
                                }
                            }
                        }
                    }

                    startActivity(Intent(applicationContext, SelecionarFazendaActivity::class.java))
                    finish()
                }
        }
    }

    private fun logout() {
        UserUtil.logOut()

        val editor = sharedPreferences.edit()
        editor.remove(SP_KEY_FAZENDA_CORRENTE_ID)
        editor.remove(SP_KEY_FAZENDA_CORRENTE_NOME)
        editor.apply()
    }

}
