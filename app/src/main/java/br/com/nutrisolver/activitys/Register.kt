package br.com.nutrisolver.activitys

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import br.com.nutrisolver.R
import br.com.nutrisolver.tools.UserUtil
import br.com.nutrisolver.tools.UserUtil.isLogged

class Register : AppCompatActivity() {
    private lateinit var input_email: EditText
    private lateinit var input_senha: EditText
    private lateinit var input_senha_repetir: EditText
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        input_email = findViewById(R.id.register_input_email)
        input_senha = findViewById(R.id.register_input_senha)
        input_senha_repetir = findViewById(R.id.register_input_senha_repetir)
        progressBar = findViewById(R.id.progress_bar)
    }

    override fun onStart() {
        super.onStart()

        if (isLogged()) {
            startActivity(Intent(this, SelecionarFazenda::class.java))
            finish()
        }
    }

    fun registrar(view: View?) {
        if (!validaDados()) {
            return
        }
        val email = input_email.text.toString()
        val senha = input_senha.text.toString()
        UserUtil.createUserWithEmailAndPassword(this, email, senha, progressBar)
    }

    private fun validaDados(): Boolean {
        var valido = true
        val email = input_email.text.toString()
        if (TextUtils.isEmpty(email)) {
            input_email.error = "Campo necessário."
            valido = false
        } else {
            input_email.error = null
        }
        val senha = input_senha.text.toString()
        if (TextUtils.isEmpty(senha)) {
            input_senha.error = "Campo necessário."
            valido = false
        } else if (senha.length < 6) {
            input_senha.error = "Necessário pelo menos 6 caracteres."
            valido = false
        } else {
            input_senha.error = null
        }
        val senhaRepetir = input_senha_repetir.text.toString()
        if (senha != senhaRepetir) {
            input_senha_repetir.error = "Senhas diferentes."
            valido = false
        } else {
            input_senha_repetir.error = null
        }
        return valido
    }
}
