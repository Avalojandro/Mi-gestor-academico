package com.ues.dam.migestoracademico.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // IMPORTAR
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ues.dam.migestoracademico.R;
import com.ues.dam.migestoracademico.entities.Materia;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MateriaAdapter extends RecyclerView.Adapter<MateriaAdapter.MateriaViewHolder> {

    private List<Materia> materias = new ArrayList<>();

    // --- AÑADIR LISTENER ---
    private final OnMateriaListener listener;

    // --- INTERFAZ PARA EL LISTENER ---
    public interface OnMateriaListener {
        void onDeleteClick(Materia materia, int position);
        void onEditClick(Materia materia);
    }

    // --- MODIFICAR CONSTRUCTOR ---
    public MateriaAdapter(OnMateriaListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MateriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_materia, parent, false);
        return new MateriaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MateriaViewHolder holder, int position) {
        Materia materia = materias.get(position);
        holder.tvNombre.setText(materia.nombre);
        holder.tvCodigo.setText(materia.codigo);
        holder.tvUV.setText(String.format(Locale.getDefault(), "%d UV", materia.uv));
    }

    @Override
    public int getItemCount() {
        return materias.size();
    }

    public void setMaterias(List<Materia> nuevasMaterias) {
        this.materias = nuevasMaterias;
        notifyDataSetChanged();
    }

    // --- AÑADIR MÉTODO PARA QUITAR ITEM ---
    public void removerMateria(int position) {
        materias.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, materias.size()); // Actualiza las posiciones
    }

    // --- AÑADIR MÉTODO PARA OBTENER ITEM ---
    public Materia getMateriaAt(int position) {
        return materias.get(position);
    }

    // --- MODIFICAR VIEWHOLDER ---
    class MateriaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCodigo, tvUV;
        ImageButton btnDeleteMateria; // AÑADIR

        public MateriaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvMateriaNombre);
            tvCodigo = itemView.findViewById(R.id.tvMateriaCodigo);
            tvUV = itemView.findViewById(R.id.tvMateriaUV);
            btnDeleteMateria = itemView.findViewById(R.id.btnDeleteMateria); // AÑADIR

            // --- AÑADIR ONCLICK LISTENER PARA BORRAR ---
            btnDeleteMateria.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(materias.get(position), position);
                }
            });

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onEditClick(materias.get(position));
                }
            });
        }
    }
}