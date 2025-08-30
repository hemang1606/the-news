package com.example.thenews;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    private final Context context;
    private final ArrayList<newsitem> newsList;

    public NewsAdapter(Context context, ArrayList<newsitem> newsList) {
        this.context = context; 
        this.newsList = newsList;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.news_recycler_viewrow, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        newsitem news = newsList.get(position);
        holder.title.setText(news.getTitle());
        holder.description.setText(news.getDescription());
        holder.source.setText("Source: " + news.getSource());

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            holder.publishedAt.setText("Published: " + outputFormat.format(inputFormat.parse(news.getPublishedAt())));
        } catch (ParseException e) {
            holder.publishedAt.setText("Published: " + news.getPublishedAt());
        }

        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.getUrl()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, source, publishedAt;
        CardView cardView;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            source = itemView.findViewById(R.id.source);
            publishedAt = itemView.findViewById(R.id.publishedAt);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}