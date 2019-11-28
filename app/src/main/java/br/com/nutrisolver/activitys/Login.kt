package br.com.nutrisolver.activitys

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import br.com.nutrisolver.R
import br.com.nutrisolver.tools.ToastUtil
import br.com.nutrisolver.tools.UserUtil
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

class Login : AppCompatActivity(), View.OnClickListener  {
    private val RC_SIGN_IN_GOOGLE = 9001

    private lateinit var progressBar: ProgressBar
    private lateinit var bt_login_com_google: SignInButton
    private lateinit var bt_login_com_senha: Button
    private lateinit var bt_login_com_facebook: LoginButton
    private lateinit var bt_registrar: TextView
    private lateinit var input_email: EditText
    private lateinit var input_senha: EditText

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
        bt_login_com_facebook = findViewById(R.id.btn_login_com_facebook)
        bt_login_com_facebook.setPermissions("email", "public_profile")
        bt_login_com_facebook.setOnClickListener(this)
        bt_login_com_facebook.registerCallback(mCallbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                // sucesso, agora autentica com o firebase
                //Toast.makeText(getApplicationContext(), "Facebook onSuccess", Toast.LENGTH_SHORT).show();
                UserUtil.loginWithFacebook(this@Login, loginResult.accessToken, progressBar)
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

        input_email = findViewById(R.id.login_input_email)
        input_senha = findViewById(R.id.login_input_senha)

        bt_login_com_google = findViewById(R.id.btn_login_com_google)
        setGooglePlusButtonText(bt_login_com_google, getString(R.string.fazer_login_com_google))
        bt_login_com_google.setOnClickListener(this)

        bt_login_com_senha = findViewById(R.id.btn_login_com_senha)
        bt_login_com_senha.setOnClickListener(this)

        bt_registrar = findViewById(R.id.btn_registrar)
        bt_registrar.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()

        if (UserUtil.isLogged()) {
            // ja esta logado, vai para a tela inicial ou selecionar fazenda
            startActivity(Intent(this, SelecionarFazenda::class.java))
            finish()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {

            R.id.btn_login_com_senha -> {
                // login com email e senha
                val email = input_email.text.toString()
                val senha = input_senha.text.toString()

                UserUtil.loginWithEmailAndPassword(this, email, senha, progressBar)
            }

            R.id.btn_login_com_google -> {
                // login com Google
                progressBar.visibility = View.VISIBLE
                val signInIntent = mGoogleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE)
            }

            R.id.btn_login_com_facebook -> progressBar.visibility = View.VISIBLE

            R.id.btn_registrar -> startActivity(Intent(applicationContext, Register::class.java))

            else -> {
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN_GOOGLE) { // resposta do login com Google
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
    }

    protected fun setGooglePlusButtonText(signInButton: SignInButton, buttonText: String) {
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
