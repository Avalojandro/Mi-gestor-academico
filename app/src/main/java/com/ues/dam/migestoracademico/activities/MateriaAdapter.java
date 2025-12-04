package com.ues.dam.migestoracademico.activities;

import android.content.res.ColorStateList;
import android.graphics.Color;
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

    public interface OnMateriaListener {
        void onDeleteClick(Materia materia, int position);
        void onEditClick(Materia materia);
        void onMateriaClick(Materia materia);
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

        // Obtener el promedio
        double promedio = materia.promedioCalculado;

        // Formatear el texto
        holder.tvPromedio.setText(String.format(Locale.getDefault(), "%.1f", promedio));

        // Lógica de Colores
        int colorFondo;

        if (promedio < 6.0) {
            // ROJO (0 - 5.9)
            colorFondo = Color.parseColor("#E53935");
        } else if (promedio < 8.0) {
            // NARANJA (6 - 7.9)
            colorFondo = Color.parseColor("#FB8C00");
        } else {
            // VERDE (8 - 10)
            colorFondo = Color.parseColor("#43A047");
        }

        // Aplicar el color
        holder.tvPromedio.setBackgroundTintList(ColorStateList.valueOf(colorFondo));
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

    class MateriaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCodigo;
        TextView tvPromedio;

        ImageButton btnDeleteMateria;
        ImageButton btnEditMateria;

        public MateriaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvMateriaNombre);
            tvCodigo = itemView.findViewById(R.id.tvMateriaCodigo);
            tvPromedio = itemView.findViewById(R.id.tvMateriaPromedio);

            btnDeleteMateria = itemView.findViewById(R.id.btnDeleteMateria);
            btnEditMateria = itemView.findViewById(R.id.btnEditMateria);

            btnDeleteMateria.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(materias.get(position), position);
                }
            });

            btnEditMateria.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onEditClick(materias.get(position));
                }
            });

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onMateriaClick(materias.get(position));
                }
            });
        }
    }
}