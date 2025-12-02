package com.ues.dam.migestoracademico.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ues.dam.migestoracademico.R;
import com.ues.dam.migestoracademico.entities.Actividad;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActividadAdapter extends RecyclerView.Adapter<ActividadAdapter.ActividadViewHolder> {

    private List<Actividad> actividades = new ArrayList<>();
    private final OnActividadListener listener;

    public interface OnActividadListener {
        void onEditClick(Actividad actividad);
        void onDeleteClick(Actividad actividad, int position);
    }

    public ActividadAdapter(OnActividadListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActividadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_actividad, parent, false);
        return new ActividadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActividadViewHolder holder, int position) {
        Actividad actividad = actividades.get(position);
        holder.tvNombre.setText(actividad.nombre);
        holder.tvFecha.setText(actividad.fecha != null ? actividad.fecha : "--/--");
        holder.tvPorcentaje.setText(String.format(Locale.getDefault(), "%.0f%%", actividad.porcentaje));
    }

    @Override
    public int getItemCount() {
        return actividades.size();
    }

    public void setActividades(List<Actividad> nuevasActividades) {
        this.actividades = nuevasActividades;
        notifyDataSetChanged();
    }

    public void removerActividad(int position) {
        actividades.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, actividades.size());
    }

    class ActividadViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvFecha, tvPorcentaje;
        ImageButton btnEdit, btnDelete;

        public ActividadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvActividadNombre);
            tvFecha = itemView.findViewById(R.id.tvActividadFecha);
            tvPorcentaje = itemView.findViewById(R.id.tvActividadPorcentaje);
            btnEdit = itemView.findViewById(R.id.btnEditActividad);
            btnDelete = itemView.findViewById(R.id.btnDeleteActividad);

            btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onEditClick(actividades.get(position));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(actividades.get(position), position);
                }
            });
        }
    }
}