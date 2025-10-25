package com.ues.dam.migestoracademico.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        // Usamos Locale para formatear el string de UV
        holder.tvUV.setText(String.format(Locale.getDefault(), "%d UV", materia.uv));
    }

    @Override
    public int getItemCount() {
        return materias.size();
    }

    // Método para actualizar la lista de materias en el adapter
    public void setMaterias(List<Materia> nuevasMaterias) {
        this.materias = nuevasMaterias;
        notifyDataSetChanged(); // Notifica al RecyclerView que los datos cambiaron
    }

    // --- ViewHolder Interno ---
    static class MateriaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCodigo, tvUV;

        public MateriaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvMateriaNombre);
            tvCodigo = itemView.findViewById(R.id.tvMateriaCodigo);
            tvUV = itemView.findViewById(R.id.tvMateriaUV);
        }
    }
}