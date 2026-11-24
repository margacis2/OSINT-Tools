package com.gameye98.osint;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private final SiteStatusManager siteStatusManager;

    public ToolAdapter(Context context, OnToolClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.iconManager = IconManager.getInstance();
        this.favoritesManager = FavoritesManager.getInstance(context);
        this.siteStatusManager = SiteStatusManager.getInstance(context);
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
        LinearLayout layoutStatusBadge;
        View dotStatus;
        TextView tvStatusText;
        TextView tvPricingBadge;

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
            layoutStatusBadge = itemView.findViewById(R.id.layoutStatusBadge);
            dotStatus = itemView.findViewById(R.id.dotStatus);
            tvStatusText = itemView.findViewById(R.id.tvStatusText);
            tvPricingBadge = itemView.findViewById(R.id.tvPricingBadge);
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

            // Health Status & Badge
            SiteStatus status = siteStatusManager.getStatus(tool.getUrl());
            bindStatus(status);

            // Pricing Badge
            PricingType pricing = siteStatusManager.getPricing(tool.getUrl());
            bindPricing(pricing);

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

            // Card click with subtle elevation feedback
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

        private void bindStatus(SiteStatus status) {
            if (layoutStatusBadge == null || tvStatusText == null) return;

            if (status == null) status = SiteStatus.PENDING;

            switch (status) {
                case WORKING:
                    layoutStatusBadge.setBackgroundResource(R.drawable.bg_status_working);
                    tvStatusText.setText("Online");
                    tvStatusText.setTextColor(ContextCompat.getColor(context, R.color.color_status_working));
                    if (dotStatus != null) dotStatus.setBackgroundResource(R.drawable.bg_status_dot);
                    break;
                case BLOCKED_OR_LOGIN:
                    layoutStatusBadge.setBackgroundResource(R.drawable.bg_status_blocked);
                    tvStatusText.setText("Protected");
                    tvStatusText.setTextColor(ContextCompat.getColor(context, R.color.color_status_blocked));
                    if (dotStatus != null) dotStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.color_status_blocked));
                    break;
                case DEAD:
                    layoutStatusBadge.setBackgroundResource(R.drawable.bg_status_dead);
                    tvStatusText.setText("Offline");
                    tvStatusText.setTextColor(ContextCompat.getColor(context, R.color.color_status_dead));
                    if (dotStatus != null) dotStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.color_status_dead));
                    break;
                case TIMEOUT:
                    layoutStatusBadge.setBackgroundResource(R.drawable.bg_status_pending);
                    tvStatusText.setText("Timeout");
                    tvStatusText.setTextColor(ContextCompat.getColor(context, R.color.color_status_timeout));
                    if (dotStatus != null) dotStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.color_status_timeout));
                    break;
                case ERROR:
                    layoutStatusBadge.setBackgroundResource(R.drawable.bg_status_dead);
                    tvStatusText.setText("Error");
                    tvStatusText.setTextColor(ContextCompat.getColor(context, R.color.color_status_dead));
                    if (dotStatus != null) dotStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.color_status_dead));
                    break;
                case PENDING:
                default:
                    layoutStatusBadge.setBackgroundResource(R.drawable.bg_status_pending);
                    tvStatusText.setText("Pending");
                    tvStatusText.setTextColor(ContextCompat.getColor(context, R.color.color_status_pending));
                    if (dotStatus != null) dotStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.color_status_pending));
                    break;
            }
        }

        private void bindPricing(PricingType pricing) {
            if (tvPricingBadge == null) return;

            if (pricing == null || pricing == PricingType.UNKNOWN) {
                tvPricingBadge.setVisibility(View.GONE);
                return;
            }

            tvPricingBadge.setVisibility(View.VISIBLE);
            tvPricingBadge.setText(pricing.getDisplayName());

            switch (pricing) {
                case OPEN_SOURCE:
                    tvPricingBadge.setTextColor(ContextCompat.getColor(context, R.color.color_pricing_opensource));
                    break;
                case FREEMIUM:
                    tvPricingBadge.setTextColor(ContextCompat.getColor(context, R.color.color_pricing_freemium));
                    break;
                case FREE:
                    tvPricingBadge.setTextColor(ContextCompat.getColor(context, R.color.color_pricing_free));
                    break;
                case PAID:
                    tvPricingBadge.setTextColor(ContextCompat.getColor(context, R.color.color_warning));
                    break;
                default:
                    tvPricingBadge.setVisibility(View.GONE);
                    break;
            }
        }
    }
}
