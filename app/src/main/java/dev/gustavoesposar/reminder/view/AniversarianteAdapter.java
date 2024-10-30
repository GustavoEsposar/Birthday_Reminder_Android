package dev.gustavoesposar.reminder.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import dev.gustavoesposar.reminder.R;
import dev.gustavoesposar.reminder.model.Aniversariante;

public class AniversarianteAdapter extends RecyclerView.Adapter<AniversarianteAdapter.AniversarianteViewHolder> {

    private List<Aniversariante> aniversariantes;
    private OnItemClickListener listener;

    // Interface para o clique do botão de deletar
    public interface OnItemClickListener {
        void onDeleteClick(Aniversariante aniversariante);
    }

    public AniversarianteAdapter(List<Aniversariante> aniversariantes, OnItemClickListener listener) {
        this.aniversariantes = aniversariantes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AniversarianteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_aniversariante, parent, false);
        return new AniversarianteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AniversarianteViewHolder holder, int position) {
        Aniversariante aniversariante = aniversariantes.get(position);
        holder.textViewNome.setText(aniversariante.getName());
        holder.textViewData.setText(aniversariante.getFormattedDate());

        // Configura o clique do botão de deletar
        holder.buttonDelete.setOnClickListener(v -> listener.onDeleteClick(aniversariante));
    }

    @Override
    public int getItemCount() {
        return aniversariantes.size();
    }

    static class AniversarianteViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNome;
        TextView textViewData;
        ImageButton buttonDelete;

        public AniversarianteViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNome = itemView.findViewById(R.id.textViewNome);
            textViewData = itemView.findViewById(R.id.textViewData);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
