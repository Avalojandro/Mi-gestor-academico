package com.ues.dam.migestoracademico.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.ues.dam.migestoracademico.entities.Actividad;

public class ActividadRepository {

    private static final String COLLECTION_NAME = "actividades";


    public static Task<QuerySnapshot> obtenerPorMateria(String materiaFirestoreId) {
        return FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME)
                .whereEqualTo("materiaFirestoreId", materiaFirestoreId)
                .get();
    }


    public static Task<Void> guardar(Actividad actividad) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collection = db.collection(COLLECTION_NAME);

        if (actividad.firestoreId == null || actividad.firestoreId.isEmpty()) {

            DocumentReference newDoc = collection.document();
            actividad.firestoreId = newDoc.getId();
            return newDoc.set(actividad);
        } else {

            return collection.document(actividad.firestoreId).set(actividad);
        }
    }


    public static Task<Void> eliminar(String actividadFirestoreId) {
        return FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME)
                .document(actividadFirestoreId)
                .delete();
    }
}