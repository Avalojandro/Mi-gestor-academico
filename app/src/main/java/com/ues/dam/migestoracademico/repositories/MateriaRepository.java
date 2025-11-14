package com.ues.dam.migestoracademico.repositories;


import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.ues.dam.migestoracademico.entities.Materia;

public class MateriaRepository {

    private static final String COLLECTION_NAME = "materias";

    private static CollectionReference getCollection() {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // Usamos .add() para que Firestore genere un ID automatico
    public static Task<DocumentReference> crear(Materia materia) {
        return getCollection().add(materia);
    }

    // Obtener materias por el ID de documento del usuario
    public static Task<QuerySnapshot> obtenerPorUsuario(String userDocId) {
        return getCollection()
                .whereEqualTo("userDocId", userDocId)
                .get();
    }

    // Actualizar una materia
    public static Task<Void> actualizar(String documentId, Materia materia) {
        return getCollection().document(documentId).set(materia);
    }

    // Eliminar una materia
    public static Task<Void> eliminar(String documentId) {
        return getCollection().document(documentId).delete();
    }
}
