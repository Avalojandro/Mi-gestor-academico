package com.ues.dam.migestoracademico.repositories;


import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ues.dam.migestoracademico.entities.Usuario;

public class UsuarioRepository {

    private static final String COLLECTION_NAME = "usuarios";

    private static CollectionReference getCollection() {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    public static Task<DocumentSnapshot> getUser(String documentId) {
        return getCollection().document(documentId).get();
    }

    public static Task<Void> updateUser(String documentId, Usuario user) {
        return getCollection().document(documentId).set(user);
    }

    public static Task<Void> deleteUser(String documentId) {
        return getCollection().document(documentId).delete();
    }
}
