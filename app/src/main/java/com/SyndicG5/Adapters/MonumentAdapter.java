package com.SyndicG5.Adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.SyndicG5.R;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.syndicg5.networking.models.Monument;
import com.syndicg5.networking.repository.apiRepository;

import java.util.ArrayList;

public class MonumentAdapter extends RecyclerView.Adapter<MonumentAdapter.PitchesAdapterHolder> {

    private ArrayList<Monument> PitchesList = new ArrayList<>();
    private Context context;
    private apiRepository repository;
    private PitcherListener listener;

    public MonumentAdapter(Context context, apiRepository repository, PitcherListener listener) {
        this.context = context;
        this.repository=repository;
        this.listener=listener;
    }

    @NonNull
    @Override
    public PitchesAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PitchesAdapterHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.template_balance_item, parent, false));

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull PitchesAdapterHolder holder, int position) {
        Monument Pitches = PitchesList.get(position);

        holder.PitchesName.setText(Pitches.getNom());
        if(Pitches.getAdresse()!=null)holder.pitch_location.setText(Pitches.getAdresse());
        holder.pitch_description.setText(Pitches.getDescription());
        Glide.with(context)
                .load(Pitches.getImages().get(0).getUrl())
                .placeholder(R.drawable.img_placeholder)
                .into(holder.PitchesImage);
        holder.itemView.setOnClickListener(view -> {
            listener.onPitcherClicked(Pitches);
        });
    }

    @Override
    public int getItemCount() {
        return PitchesList.size();
    }

    public void setList(ArrayList<Monument> PitchesList) {
        this.PitchesList = PitchesList;
        notifyDataSetChanged();
    }

    public class PitchesAdapterHolder extends RecyclerView.ViewHolder {
        public TextView  PitchesName,pitch_location,pitch_description;
        public ShapeableImageView PitchesImage;

        public PitchesAdapterHolder(View itemView) {
            super(itemView);
            PitchesName = itemView.findViewById(R.id.pitch_name);
            pitch_location = itemView.findViewById(R.id.pitch_location);
            pitch_description = itemView.findViewById(R.id.pitch_description);
            PitchesImage = itemView.findViewById(R.id.pitche_img);
        }
    }
    public interface PitcherListener {
        void onPitcherClicked(Monument Pitches);
    }
}
