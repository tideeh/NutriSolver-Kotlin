package br.com.nutrisolver.utils

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.nutrisolver.R
import br.com.nutrisolver.activitys.SelecionarFazendaActivity
import br.com.nutrisolver.utils.ToastUtil.show
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

object UserUtil {
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUser() = mAuth.currentUser

    fun isLogged(): Boolean {
        return mAuth.currentUser != null
    }

    fun loginWithFacebook(ctx: AppCompatActivity, token: AccessToken, progressBar: ProgressBar) {

        progressBar.visibility = View.VISIBLE

        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(
                ctx
            ) { task ->
                if (task.isSuccessful) {
                    // login realizado com sucesso, vai para tela inicial ou selecionar fazenda
                    ctx.startActivity(Intent(ctx, SelecionarFazendaActivity::class.java))
                    ctx.finish()
                } else {
                    show(
                        ctx,
                        "Autenticação falhou: " + task.exception,
                        Toast.LENGTH_SHORT
                    )
                }
                progressBar.visibility = View.GONE
            }
    }

    fun loginWithEmailAndPassword(
        ctx: AppCompatActivity,
        email: String,
        senha: String,
        progressBar: ProgressBar
    ) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) {
            show(ctx, ctx.getString(R.string.campos_email_senha_nao_podem_ser_vazios), Toast.LENGTH_SHORT)
            return
        }

        progressBar.visibility = View.VISIBLE

        mAuth.signInWithEmailAndPassword(email, senha).addOnCompleteListener(
            ctx
        ) { task ->
            if (task.isSuccessful) {
                // login realizado com sucesso, vai para a tela selecionar fazenda
                ctx.startActivity(Intent(ctx, SelecionarFazendaActivity::class.java))
                ctx.finish()
            } else {
                show(ctx, "Autenticação falhou: " + task.exception!!, Toast.LENGTH_SHORT)
            }
            progressBar.visibility = View.GONE
        }
    }

    fun loginWithGoogle(
        ctx: AppCompatActivity,
        acct: GoogleSignInAccount,
        progressBar: ProgressBar
    ) {

        progressBar.visibility = View.VISIBLE

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(
                ctx
            ) { task ->
                if (task.isSuccessful) {
                    // login realizado com sucesso, vai para tela inicial ou selecionar fazenda
                    ctx.startActivity(Intent(ctx, SelecionarFazendaActivity::class.java))
                    ctx.finish()
                } else {
                    show(
                        ctx,
                        "Autenticação falhou: " + task.exception!!,
                        Toast.LENGTH_SHORT
                    )
                }
                progressBar.visibility = View.GONE
            }
    }

    fun createUserWithEmailAndPassword(
        ctx: AppCompatActivity,
        email: String,
        senha: String,
        progressBar: ProgressBar
    ) {

        progressBar.visibility = View.VISIBLE
        mAuth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener(
                ctx
            ) { task ->
                if (task.isSuccessful) { // registrado com sucesso, vai para a tela selecionar fazenda
                    val it = Intent()
                    ctx.setResult(RESULT_OK, it)
                    //ctx.startActivity(Intent(ctx, SelecionarFazendaActivity::class.java))
                    ctx.finish()
                } else { // registro falhou
                    show(
                        ctx,
                        "Registro falhou: " + task.exception,
                        Toast.LENGTH_SHORT
                    )
                }
                progressBar.visibility = View.GONE
            }
    }

    fun logOut() {
        mAuth.signOut()
        LoginManager.getInstance().logOut()
    }
}