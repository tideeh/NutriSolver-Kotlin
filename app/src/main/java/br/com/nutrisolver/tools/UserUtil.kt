package br.com.nutrisolver.tools

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object UserUtil {
    private val mAuth : FirebaseAuth = FirebaseAuth.getInstance()

    public fun getCurrentUser() = mAuth.currentUser

    public fun isLogged(): Boolean {
        return mAuth.currentUser != null
    }
}