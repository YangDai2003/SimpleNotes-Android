package com.example.notesapp.Adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.notesapp.Models.Notes;
import com.example.notesapp.NotesClick;
import com.example.notesapp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author 30415
 */
public class NotesListAdapter extends RecyclerView.Adapter<NotesListAdapter.NotesViewHolder> {
    private List<Notes> list;
    NotesClick notesClick;
    boolean scrollUp = false;

    public NotesListAdapter(List<Notes> list, NotesClick notesClick) {
        this.list = list;
        this.notesClick = notesClick;
    }

    public void setScrollUp(boolean scroll) {
        scrollUp = scroll;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull NotesViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (scrollUp) {
            addAnimation(holder);
        }
    }

    private void addAnimation(NotesViewHolder holder) {
        holder.itemView.setAnimation(AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.item_anim));
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_list, parent, false);
        NotesViewHolder notesViewHolder = new NotesViewHolder(view);
        notesViewHolder.notesContainer.setCardBackgroundColor(notesViewHolder.itemView.getResources().getColor(getRandomColr(), null));

        view.setOnClickListener(v -> {
            int position1 = notesViewHolder.getAdapterPosition();
            notesClick.onClick(list.get(position1), position1);
        });

        view.setOnLongClickListener(v -> {
            int position1 = notesViewHolder.getAdapterPosition();
            notesClick.onLongClick(list.get(position1), notesViewHolder.notesContainer, position1);
            return true;
        });
        return notesViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {

        holder.textViewTitle.setText(list.get(position).getTitle());
        holder.textViewNotes.setText(list.get(position).getNotes());
        holder.textViewDate.setText(list.get(position).getDate());
        String images = list.get(holder.getAdapterPosition()).getImage();
        if (!images.isEmpty()) {
            List<String> paths = Arrays.asList(images.trim().split(" "));
            Glide.with(holder.itemView.getContext()).load(paths.get(0)).sizeMultiplier(0.65f).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.imageView);
        } else {
            Glide.with(holder.itemView.getContext()).clear(holder.imageView);
        }

        if (list.get(position).isPinned()) {
            holder.pinImage.setImageResource(R.drawable.ic_baseline_push_pin_24);
        } else {
            holder.pinImage.setImageDrawable(null);
        }

    }

    private int getRandomColr() {
        List<Integer> colorCode = new ArrayList<>();
        colorCode.add(R.color.color1);
        colorCode.add(R.color.color2);
        colorCode.add(R.color.color3);
        colorCode.add(R.color.color4);
        colorCode.add(R.color.color5);
        colorCode.add(R.color.color6);
        colorCode.add(R.color.color7);
        colorCode.add(R.color.color8);
        colorCode.add(R.color.color9);
        colorCode.add(R.color.color10);
        colorCode.add(R.color.color11);
        colorCode.add(R.color.color12);
        colorCode.add(R.color.color13);
        colorCode.add(R.color.color14);
        colorCode.add(R.color.color15);
        colorCode.add(R.color.color16);
        Random random = new Random();
        return colorCode.get(random.nextInt(colorCode.size()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filteredList(List<Notes> filterList) {
        list = filterList;
        notifyDataSetChanged();
    }


    static class NotesViewHolder extends RecyclerView.ViewHolder {
        CardView notesContainer;
        TextView textViewTitle, textViewNotes, textViewDate;
        ImageView pinImage, imageView;

        public NotesViewHolder(@NonNull View itemView) {
            super(itemView);

            notesContainer = itemView.findViewById(R.id.notes_card_container);
            textViewTitle = itemView.findViewById(R.id.title_text);
            textViewNotes = itemView.findViewById(R.id.textview_notes);
            textViewDate = itemView.findViewById(R.id.textview_date);
            pinImage = itemView.findViewById(R.id.image_view_pin);
            imageView = itemView.findViewById(R.id.imageShow);
        }
    }
}
