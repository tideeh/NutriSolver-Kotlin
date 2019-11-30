package br.com.nutrisolver.activitys

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import br.com.nutrisolver.R
import br.com.nutrisolver.utils.UserUtil
import br.com.nutrisolver.utils.UserUtil.isLogged

class RegistrarActivity : AppCompatActivity() {
    private lateinit var editTextInputEmail: EditText
    private lateinit var editTextInputSenha: EditText
    private lateinit var editTextInputSenhaRepetir: EditText
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        editTextInputEmail = findViewById(R.id.register_input_email)
        editTextInputSenha = findViewById(R.id.register_input_senha)
        editTextInputSenhaRepetir = findViewById(R.id.register_input_senha_repetir)
        progressBar = findViewById(R.id.progress_bar)
    }

    override fun onStart() {
        super.onStart()

        if (isLogged()) {
            startActivity(Intent(this, SelecionarFazendaActivity::class.java))
            finish()
        }
    }

    fun registrar(view: View?) {
        if (!validaDados()) {
            return
        }

        val email = editTextInputEmail.text.toString()
        val senha = editTextInputSenha.text.toString()
        UserUtil.createUserWithEmailAndPassword(this, email, senha, progressBar)
    }

    private fun validaDados(): Boolean {
        var valido = true

        val email = editTextInputEmail.text.toString()
        val senha = editTextInputSenha.text.toString()
        val senhaRepetir = editTextInputSenhaRepetir.text.toString()

        editTextInputEmail.error = null
        editTextInputSenha.error = null
        editTextInputSenhaRepetir.error = null

        if (TextUtils.isEmpty(email)) {
            editTextInputEmail.error = getString(R.string.campo_necessario)
            valido = false
        }

        if (TextUtils.isEmpty(senha)) {
            editTextInputSenha.error = getString(R.string.campo_necessario)
            valido = false
        }
        else if (senha.length < 6) {
            editTextInputSenha.error = getString(R.string.necessario_pelo_menos_6_caracteres)
            valido = false
        }
        else if (senha != senhaRepetir) {
            editTextInputSenhaRepetir.error = getString(R.string.senhas_diferentes)
            valido = false
        }

        return valido
    }
}
