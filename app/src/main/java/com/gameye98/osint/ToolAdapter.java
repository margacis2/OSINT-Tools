package com.gameye98.osint;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class ToolAdapter extends RecyclerView.Adapter<ToolAdapter.ToolViewHolder> {

    public interface OnToolClickListener {
        void onToolClick(OsintTool tool);
        void onFavoriteClick(OsintTool tool, int position);
    }

    private final Context context;
    private List<OsintTool> toolList = new ArrayList<>();
    private final OnToolClickListener listener;
    private final IconManager iconManager;
    private final FavoritesManager favoritesManager;

    public ToolAdapter(Context context, OnToolClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.iconManager = IconManager.getInstance();
        this.favoritesManager = FavoritesManager.getInstance(context);
    }

    public void updateList(List<OsintTool> newList) {
        this.toolList = newList != null ? new ArrayList<>(newList) : new ArrayList<OsintTool>();
        notifyDataSetChanged();
    }

    public List<OsintTool> getItems() {
        return toolList;
    }

    @NonNull
    @Override
    public ToolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tool_card, parent, false);
        return new ToolViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToolViewHolder holder, int position) {
        OsintTool tool = toolList.get(position);
        holder.bind(tool, position);
    }

    @Override
    public int getItemCount() {
        return toolList.size();
    }

    class ToolViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        ImageView imgIcon;
        ImageView btnFavorite;
        TextView tvRecentBadge;
        TextView tvTitle;
        TextView tvDomain;
        TextView tvCategory;
        TextView tvToolTypeBadge;

        ToolViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardTool);
            imgIcon = itemView.findViewById(R.id.imgToolIcon);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            tvRecentBadge = itemView.findViewById(R.id.tvRecentBadge);
            tvTitle = itemView.findViewById(R.id.tvToolTitle);
            tvDomain = itemView.findViewById(R.id.tvToolDomain);
            tvCategory = itemView.findViewById(R.id.tvToolCategory);
            tvToolTypeBadge = itemView.findViewById(R.id.tvToolTypeBadge);
        }

        void bind(final OsintTool tool, final int position) {
            tvTitle.setText(tool.getTitle());
            tvDomain.setText(tool.getDomain());
            tvCategory.setText(tool.getCategory());

            if (tool.isRecent()) {
                if (tvRecentBadge != null) tvRecentBadge.setVisibility(View.VISIBLE);
                if (tvToolTypeBadge != null) {
                    tvToolTypeBadge.setVisibility(View.VISIBLE);
                    tvToolTypeBadge.setText(tool.getToolType());
                }
            } else {
                if (tvRecentBadge != null) tvRecentBadge.setVisibility(View.GONE);
                if (tvToolTypeBadge != null) tvToolTypeBadge.setVisibility(View.GONE);
            }

            // Load favicon with memory cache
            iconManager.loadIcon(context, tool.getIconName(), imgIcon);

            // Set favorite state
            boolean isFav = favoritesManager.isFavorite(tool.getUrl());
            tool.setFavorite(isFav);
            if (isFav) {
                btnFavorite.setImageResource(R.drawable.ic_star_filled);
                btnFavorite.setImageTintList(ContextCompat.getColorStateList(context, R.color.color_favorite));
            } else {
                btnFavorite.setImageResource(R.drawable.ic_star_border);
                btnFavorite.setImageTintList(ContextCompat.getColorStateList(context, R.color.text_secondary));
            }

            // Card click
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onToolClick(tool);
                    }
                }
            });

            // Favorite button click
            btnFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean newFav = favoritesManager.toggleFavorite(tool.getUrl());
                    tool.setFavorite(newFav);
                    if (newFav) {
                        btnFavorite.setImageResource(R.drawable.ic_star_filled);
                        btnFavorite.setImageTintList(ContextCompat.getColorStateList(context, R.color.color_favorite));
                    } else {
                        btnFavorite.setImageResource(R.drawable.ic_star_border);
                        btnFavorite.setImageTintList(ContextCompat.getColorStateList(context, R.color.text_secondary));
                    }
                    if (listener != null) {
                        listener.onFavoriteClick(tool, position);
                    }
                }
            });
        }
    }
}
