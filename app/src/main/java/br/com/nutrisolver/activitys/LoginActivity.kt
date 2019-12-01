package br.com.nutrisolver.activitys

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import br.com.nutrisolver.R
import br.com.nutrisolver.utils.ACTIVITY_REQUEST_LOGIN_COM_GOOGLE
import br.com.nutrisolver.utils.ACTIVITY_REQUEST_REGISTRAR
import br.com.nutrisolver.utils.ToastUtil
import br.com.nutrisolver.utils.UserUtil
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var progressBar: ProgressBar
    private lateinit var buttonLoginComGoogle: SignInButton
    private lateinit var buttonLoginComSenha: Button
    private lateinit var buttonLoginComFacebook: LoginButton
    private lateinit var buttonRegistrar: TextView
    private lateinit var editTextInputEmail: EditText
    private lateinit var editTextInputSenha: EditText

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mCallbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        progressBar = findViewById(R.id.progress_bar)

        // Configura Google Login
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Configura Facebook Login
        mCallbackManager = CallbackManager.Factory.create()
        buttonLoginComFacebook = findViewById(R.id.btn_login_com_facebook)
        buttonLoginComFacebook.setPermissions("email", "public_profile")
        buttonLoginComFacebook.setOnClickListener(this)
        buttonLoginComFacebook.registerCallback(mCallbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                // sucesso, agora autentica com o firebase
                //Toast.makeText(getApplicationContext(), "Facebook onSuccess", Toast.LENGTH_SHORT).show();
                UserUtil.loginWithFacebook(this@LoginActivity, loginResult.accessToken, progressBar)
            }

            override fun onCancel() {
                //Toast.makeText(getApplicationContext(), "Facebook onCancel", Toast.LENGTH_SHORT).show();
                progressBar.visibility = View.GONE
            }

            override fun onError(error: FacebookException) {
                ToastUtil.show(applicationContext, "Facebook onError: $error", Toast.LENGTH_SHORT)
                progressBar.visibility = View.GONE
            }
        })

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        editTextInputEmail = findViewById(R.id.login_input_email)
        editTextInputSenha = findViewById(R.id.login_input_senha)

        buttonLoginComGoogle = findViewById(R.id.btn_login_com_google)
        setGooglePlusButtonText(buttonLoginComGoogle, getString(R.string.fazer_login_com_google))
        buttonLoginComGoogle.setOnClickListener(this)

        buttonLoginComSenha = findViewById(R.id.btn_login_com_senha)
        buttonLoginComSenha.setOnClickListener(this)

        buttonRegistrar = findViewById(R.id.btn_registrar)
        buttonRegistrar.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()

        if (UserUtil.isLogged()) {
            // ja esta logado, vai para a tela inicial ou selecionar fazenda
            startActivity(Intent(this, SelecionarFazendaActivity::class.java))
            finish()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {

            R.id.btn_login_com_senha -> {
                // login com email e senha
                val email = editTextInputEmail.text.toString()
                val senha = editTextInputSenha.text.toString()

                UserUtil.loginWithEmailAndPassword(this, email, senha, progressBar)
            }

            R.id.btn_login_com_google -> {
                // login com Google
                progressBar.visibility = View.VISIBLE
                val signInIntent = mGoogleSignInClient.signInIntent
                startActivityForResult(signInIntent, ACTIVITY_REQUEST_LOGIN_COM_GOOGLE)
            }

            R.id.btn_login_com_facebook -> progressBar.visibility = View.VISIBLE

            R.id.btn_registrar -> startActivityForResult(Intent(applicationContext, RegistrarActivity::class.java), ACTIVITY_REQUEST_REGISTRAR)

            else -> {
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == ACTIVITY_REQUEST_LOGIN_COM_GOOGLE) { // resposta do login com Google
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    UserUtil.loginWithGoogle(this, account, progressBar)
                }
            } catch (e: ApiException) {
                // Google Sign In failed
                ToastUtil.show(applicationContext, "Autenticação falhou: $e", Toast.LENGTH_SHORT)
                progressBar.visibility = View.GONE
            }

        }

        if(requestCode == ACTIVITY_REQUEST_REGISTRAR){
            if(resultCode == Activity.RESULT_OK){
                startActivity(Intent(this, SelecionarFazendaActivity::class.java))
                finish()
            }
        }
    }

    private fun setGooglePlusButtonText(signInButton: SignInButton, buttonText: String) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (i in 0 until signInButton.childCount) {
            val v = signInButton.getChildAt(i)

            if (v is TextView) {
                v.text = buttonText
                return
            }
        }
    }
}
