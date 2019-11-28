package br.com.nutrisolver.tools

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

object DataBaseUtil {
    private val db : FirebaseFirestore = FirebaseFirestore.getInstance()

    fun insertDocument(
        collection: String,
        documentID: String,
        `object`: Any
    ): Task<*>? {
        return db.collection(collection).document(documentID).set(`object`)
    }

    fun getDocument(collection: String, documentID: String) = db.collection(collection).document(documentID).get()

    fun getDocumentsWhereEqualTo(
        collection: String,
        whereKey: String,
        whereValue: Any
    ): Task<QuerySnapshot> {
        return db.collection(collection).whereEqualTo(whereKey, whereValue).get()
    }

    fun getDocumentsWhereEqualTo(
        collection: String,
        whereKey: String,
        whereValue: Any,
        limit: Long
    ): Task<QuerySnapshot> {
        return db.collection(collection).whereEqualTo(whereKey, whereValue).limit(limit).get()
    }

    fun getDocumentsWhereEqualTo(
        collection: String,
        whereKey: Array<String>,
        whereValue: Array<Any>
    ): Task<QuerySnapshot> {
        var query: Query
        query = db.collection(collection).whereEqualTo(whereKey[0], whereValue[0])

        for (i in 1 until whereKey.size) {
            query = query.whereEqualTo(whereKey[i], whereValue[i])
        }

        return query.get()
    }

    fun getDocumentsWhereEqualTo(
        collection: String,
        whereKey: Array<String>,
        whereValue: Array<Any>,
        limit: Long
    ): Task<QuerySnapshot> {
        var query: Query
        query = db.collection(collection).whereEqualTo(whereKey[0], whereValue[0])

        for (i in 1 until whereKey.size) {
            query = query.whereEqualTo(whereKey[i], whereValue[i])
        }

        return query.limit(limit).get()
    }

    fun updateDocument(
        collection: String,
        documentID: String,
        updateKey: String,
        updateValue: Any
    ): Task<*> {
        return db.collection(collection).document(documentID).update(updateKey, updateValue)
    }
}