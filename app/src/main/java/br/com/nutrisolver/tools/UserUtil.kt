package br.com.nutrisolver.tools

import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.nutrisolver.activitys.SelecionarFazenda
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*

object UserUtil {
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    public fun getCurrentUser() = mAuth.currentUser

    public fun isLogged(): Boolean {
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
                    ctx.startActivity(Intent(ctx, SelecionarFazenda::class.java))
                    ctx.finish()
                } else {
                    ToastUtil.show(
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
            ToastUtil.show(ctx, "Os campos Email e Senha não podem ser vazios!", Toast.LENGTH_SHORT)
            return
        }

        progressBar.visibility = View.VISIBLE

        mAuth.signInWithEmailAndPassword(email, senha).addOnCompleteListener(
            ctx
        ) { task ->
            if (task.isSuccessful) {
                // login realizado com sucesso, vai para a tela selecionar fazenda
                ctx.startActivity(Intent(ctx, SelecionarFazenda::class.java))
                ctx.finish()
            } else {
                ToastUtil.show(ctx, "Autenticação falhou: " + task.exception!!, Toast.LENGTH_SHORT)
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
                    ctx.startActivity(Intent(ctx, SelecionarFazenda::class.java))
                    ctx.finish()
                } else {
                    ToastUtil.show(
                        ctx,
                        "Autenticação falhou: " + task.exception!!,
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