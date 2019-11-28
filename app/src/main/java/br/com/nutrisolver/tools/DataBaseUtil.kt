package br.com.nutrisolver.tools

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

object DataBaseUtil {
    private val db : FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getDocument(collection: String, documentID: String) = db.collection(collection).document(documentID).get()
}