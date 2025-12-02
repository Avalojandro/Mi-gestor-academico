package com.ues.dam.migestoracademico.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
    private final OnMateriaListener listener;

    // --- INTERFAZ ACTUALIZADA ---
    public interface OnMateriaListener {
        void onDeleteClick(Materia materia, int position); // Click en borrar
        void onEditClick(Materia materia);                 // Click en editar (lápiz)
        void onMateriaClick(Materia materia);              // Click en la tarjeta (ver actividades)
    }

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

        // El binding de los listeners se maneja en el ViewHolder
    }

    @Override
    public int getItemCount() {
        return materias.size();
    }

    public void setMaterias(List<Materia> nuevasMaterias) {
        this.materias = nuevasMaterias;
        notifyDataSetChanged();
    }

    public void removerMateria(int position) {
        materias.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, materias.size());
    }

    public Materia getMateriaAt(int position) {
        return materias.get(position);
    }

    // --- VIEWHOLDER COMPLETO ---
    class MateriaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCodigo, tvUV;
        ImageButton btnDeleteMateria;
        ImageButton btnEditMateria; // Referencia al nuevo botón

        public MateriaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvMateriaNombre);
            tvCodigo = itemView.findViewById(R.id.tvMateriaCodigo);
            tvUV = itemView.findViewById(R.id.tvMateriaUV);

            // Botones
            btnDeleteMateria = itemView.findViewById(R.id.btnDeleteMateria);
            btnEditMateria = itemView.findViewById(R.id.btnEditMateria); // Asegúrate que este ID exista en tu XML

            // 1. CLICK EN BORRAR (Basurero)
            btnDeleteMateria.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(materias.get(position), position);
                }
            });

            // 2. CLICK EN EDITAR (Lápiz) -> Abre AddEditMateriaActivity
            btnEditMateria.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onEditClick(materias.get(position));
                }
            });

            // 3. CLICK EN LA TARJETA (Fondo) -> Abre ActividadesActivity
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onMateriaClick(materias.get(position));
                }
            });
        }
    }
}