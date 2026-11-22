package com.gameye98.osint;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ToolAdapter.OnToolClickListener {

    // Preserved fields for backwards compatibility
    GridLayout grid;
    PermissionUtils permissions;
    List<String> sites = new ArrayList<>();
    float density;
    SearchView searchData;
    AlertDialog dialog;

    // Modern UI components
    private final List<OsintTool> allTools = new ArrayList<>();
    private final List<OsintTool> displayedTools = new ArrayList<>();
    private RecyclerView toolRecyclerView;
    private ToolAdapter toolAdapter;
    private GridLayoutManager layoutManager;
    private LinearLayout categoryContainer;
    private View emptyStateView;
    private TextView tvResultsCount;
    private TextView tvToolCountBadge;
    private View btnResetFilter;
    private ImageView btnClearSearch;

    private String currentCategory = "All Tools";
    private String currentQuery = "";
    private FavoritesManager favoritesManager;
    private IconManager iconManager;

    // 30 Comprehensive OSINT Categories with intuitive icons
    public static final String[][] CATEGORIES = new String[][]{
            {"🌐", "All Tools"},
            {"⚡", "Recently Added"},
            {"⭐", "Favorites"},
            {"👥", "People Search"},
            {"📱", "Social Media Intelligence"},
            {"🆔", "Username Investigation"},
            {"✉️", "Email Intelligence"},
            {"📞", "Phone Number Intelligence"},
            {"🔗", "Domain Intelligence"},
            {"📡", "DNS & Subdomain Enumeration"},
            {"💻", "IP Address Intelligence"},
            {"🔌", "Network Reconnaissance"},
            {"📍", "Geolocation Intelligence"},
            {"🗺️", "Maps & Satellite Intelligence"},
            {"📷", "Image Intelligence"},
            {"🔍", "Reverse Image Search"},
            {"🎥", "Video & Media Intelligence"},
            {"🏷️", "Metadata Analysis"},
            {"🕵️", "Dark Web Monitoring Resources"},
            {"🛡️", "Threat Intelligence"},
            {"🦠", "Malware Analysis Resources"},
            {"🔓", "Data Breach Research"},
            {"🔬", "Digital Forensics"},
            {"🗄️", "Archive & Historical Research"},
            {"🔎", "Search Engines & Google Dorks"},
            {"🪙", "Cryptocurrency Intelligence"},
            {"🏢", "Company & Business Intelligence"},
            {"📜", "Public Records Research"},
            {"💻", "Developer & Code Intelligence"},
            {"🧰", "General Investigation Tools"}
    };

    public String readFromAssets(String pathFile) {
        try {
            InputStream stream = getAssets().open(pathFile);
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            return new String(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void toastCenter(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * Preserved filterSites method signature for compatibility.
     */
    public void filterSites(String query) {
        this.currentQuery = (query != null && !query.equals(".")) ? query.trim() : "";
        applyFilter();
    }

    private void applyFilter() {
        displayedTools.clear();
        boolean isAllCategory = currentCategory.equals("All Tools") || currentCategory.equals("All");

        for (OsintTool tool : allTools) {
            // Category check
            if (!isAllCategory && !tool.hasCategory(currentCategory)) {
                continue;
            }

            // Text search check
            if (currentQuery.isEmpty() || tool.matches(currentQuery)) {
                displayedTools.add(tool);
            }
        }

        // Update modern RecyclerView
        if (toolAdapter != null) {
            toolAdapter.updateList(displayedTools);
        }

        // Update count & empty state
        int count = displayedTools.size();
        if (tvResultsCount != null) {
            String countText = "Showing " + count + " of " + allTools.size() + " tools";
            if (!isAllCategory) {
                countText += " in " + currentCategory;
            }
            tvResultsCount.setText(countText);
        }

        boolean isFiltered = !isAllCategory || !currentQuery.isEmpty();
        if (btnResetFilter != null) {
            btnResetFilter.setVisibility(isFiltered ? View.VISIBLE : View.GONE);
        }

        if (btnClearSearch != null) {
            btnClearSearch.setVisibility(currentQuery.isEmpty() ? View.GONE : View.VISIBLE);
        }

        if (emptyStateView != null) {
            emptyStateView.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        }
        if (toolRecyclerView != null) {
            toolRecyclerView.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
        }
    }

    private int getCategoryCount(String catName) {
        if (catName.equals("All Tools") || catName.equals("All")) {
            return allTools.size();
        }
        int count = 0;
        for (OsintTool tool : allTools) {
            if (tool.hasCategory(catName)) {
                count++;
            }
        }
        return count;
    }

    private void setupCategories() {
        if (categoryContainer == null) return;
        categoryContainer.removeAllViews();

        for (final String[] catItem : CATEGORIES) {
            final String icon = catItem[0];
            final String catName = catItem[1];
            final TextView chip = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, (int) (8 * density), 0);
            chip.setLayoutParams(params);

            int count = getCategoryCount(catName);
            chip.setText(icon + " " + catName + " (" + count + ")");
            chip.setTag(catName);
            chip.setTextSize(12);
            chip.setPadding((int) (14 * density), (int) (8 * density), (int) (14 * density), (int) (8 * density));
            chip.setGravity(Gravity.CENTER);

            updateChipVisualState(chip, catName.equals(currentCategory));

            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentCategory = catName;
                    updateAllChips();
                    applyFilter();
                }
            });

            categoryContainer.addView(chip);
        }
    }

    private void updateAllChips() {
        if (categoryContainer == null) return;
        for (int i = 0; i < categoryContainer.getChildCount(); i++) {
            View child = categoryContainer.getChildAt(i);
            if (child instanceof TextView) {
                TextView chip = (TextView) child;
                String catName = (String) chip.getTag();
                if (catName != null) {
                    String icon = "🌐";
                    for (String[] item : CATEGORIES) {
                        if (item[1].equals(catName)) {
                            icon = item[0];
                            break;
                        }
                    }
                    int count = getCategoryCount(catName);
                    chip.setText(icon + " " + catName + " (" + count + ")");
                    updateChipVisualState(chip, catName.equals(currentCategory));
                }
            }
        }
    }

    private void updateChipVisualState(TextView chip, boolean isSelected) {
        if (isSelected) {
            chip.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip_selected));
            chip.setTextColor(Color.WHITE);
            chip.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            chip.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip_unselected));
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            chip.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }

    private void calculateSpanCount() {
        if (layoutManager == null) return;
        int widthDp = getResources().getConfiguration().screenWidthDp;
        // Adaptive grid columns based on device screen width
        int span = Math.max(2, widthDp / 170);
        layoutManager.setSpanCount(span);
    }

    @Override
    public void onToolClick(final OsintTool tool) {
        showToolDetailDialog(tool);
    }

    @Override
    public void onFavoriteClick(OsintTool tool, int position) {
        updateAllChips();
        if (currentCategory.equals("Favorites")) {
            applyFilter();
        }
    }

    private void showToolDetailDialog(final OsintTool tool) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.open, null, false);

        // Bind preserved and modern views
        ImageView openImg = dialogView.findViewById(R.id.openImg);
        TextView openTitle = dialogView.findViewById(R.id.openTitle);
        Button openBrowser = dialogView.findViewById(R.id.openBrowser);
        Button openHere = dialogView.findViewById(R.id.openHere);

        TextView dialogDomain = dialogView.findViewById(R.id.dialogDomain);
        TextView dialogCategory = dialogView.findViewById(R.id.dialogCategory);
        TextView dialogToolType = dialogView.findViewById(R.id.dialogToolType);
        TextView dialogDate = dialogView.findViewById(R.id.dialogDate);
        TextView tvFullUrl = dialogView.findViewById(R.id.tvFullUrl);
        View boxUrlPreview = dialogView.findViewById(R.id.boxUrlPreview);
        ImageView btnQuickCopy = dialogView.findViewById(R.id.btnQuickCopy);
        MaterialButton btnCopyUrl = dialogView.findViewById(R.id.btnCopyUrl);
        MaterialButton btnShareUrl = dialogView.findViewById(R.id.btnShareUrl);
        final MaterialButton btnDialogFavorite = dialogView.findViewById(R.id.btnDialogFavorite);

        // Populate details
        openTitle.setText(tool.getTitle());
        if (dialogDomain != null) dialogDomain.setText(tool.getDomain());
        if (dialogCategory != null) dialogCategory.setText(tool.getCategory());
        if (dialogToolType != null) dialogToolType.setText(tool.getToolType());
        if (dialogDate != null) {
            if (tool.getAddedDate() != null && !tool.getAddedDate().isEmpty()) {
                dialogDate.setText("• Added: " + tool.getAddedDate());
                dialogDate.setVisibility(View.VISIBLE);
            } else {
                dialogDate.setVisibility(View.GONE);
            }
        }
        if (tvFullUrl != null) tvFullUrl.setText(tool.getUrl());

        // Load Icon
        iconManager.loadIcon(this, tool.getIconName(), openImg);

        // Setup Favorite state in dialog
        final boolean isFav = favoritesManager.isFavorite(tool.getUrl());
        if (btnDialogFavorite != null) {
            btnDialogFavorite.setIconResource(isFav ? R.drawable.ic_star_filled : R.drawable.ic_star_border);
            btnDialogFavorite.setText(isFav ? "Favorited" : "Favorite");
            btnDialogFavorite.setIconTint(ContextCompat.getColorStateList(this, isFav ? R.color.color_favorite : R.color.text_secondary));

            btnDialogFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean newFav = favoritesManager.toggleFavorite(tool.getUrl());
                    tool.setFavorite(newFav);
                    btnDialogFavorite.setIconResource(newFav ? R.drawable.ic_star_filled : R.drawable.ic_star_border);
                    btnDialogFavorite.setText(newFav ? "Favorited" : "Favorite");
                    btnDialogFavorite.setIconTint(ContextCompat.getColorStateList(MainActivity.this, newFav ? R.color.color_favorite : R.color.text_secondary));
                    updateAllChips();
                    if (toolAdapter != null) toolAdapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, newFav ? "Saved to Favorites" : "Removed from Favorites", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Copy actions
        View.OnClickListener copyAction = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("OSINT URL", tool.getUrl());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "URL copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        };
        if (btnCopyUrl != null) btnCopyUrl.setOnClickListener(copyAction);
        if (btnQuickCopy != null) btnQuickCopy.setOnClickListener(copyAction);
        if (boxUrlPreview != null) boxUrlPreview.setOnClickListener(copyAction);

        // Share action
        if (btnShareUrl != null) {
            btnShareUrl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, tool.getTitle());
                    sendIntent.putExtra(Intent.EXTRA_TEXT, tool.getTitle() + "\n" + tool.getUrl());
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, "Share OSINT Tool"));
                }
            });
        }

        // Primary: Open in External Browser (openBrowser)
        openBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (dialog != null) dialog.dismiss();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(tool.getUrl()));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Cannot open external browser", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Primary: Open in Internal Browser (openHere)
        openHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (dialog != null) dialog.dismiss();
                    Intent intent = new Intent(MainActivity.this, Browser.class);
                    intent.putExtra("url", tool.getUrl());
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Cannot open internal browser", Toast.LENGTH_SHORT).show();
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
    }

    public void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View parent = inflater.inflate(R.layout.about, null, false);

        TextView title = parent.findViewById(R.id.title);
        title.setText("OSINT Tools v2.0");

        TextView content = parent.findViewById(R.id.content);
        String[] contents = {
                "Curated cyber-intelligence and OSINT directory aggregating 960+ verified security and investigation tools across 30 specialized domains.",
                "\n• Forked & Maintained by: margacis2",
                "• Original Concept & Project by: Gameye98 & BlackHole Security",
                "• License: MIT Open Source License"
        };
        content.setText(String.join("\n", contents));

        final String[] urls = {
                "https://github.com/margacis2/OSINT-Tools",
                "https://github.com/margacis2",
                "https://github.com/Gameye98/OSINT-Tools",
                "https://github.com/Gameye98",
                "https://github.com/BlackHoleSecurity",
                "https://t.me/bhsecr"
        };
        final String[] urlinfo = {
                "Fork Repository (margacis2/OSINT-Tools)",
                "Fork Maintainer Profile (margacis2)",
                "Original Upstream (Gameye98/OSINT-Tools)",
                "Original Author Profile (Gameye98)",
                "BlackHole Security GitHub",
                "BlackHole Security Telegram"
        };

        LinearLayout container = parent.findViewById(R.id.links);
        container.removeAllViews();

        for (int i = 0; i < urls.length; i++) {
            MaterialCardView linkCard = new MaterialCardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, (int) (4 * density), 0, (int) (4 * density));
            linkCard.setLayoutParams(cardParams);
            linkCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_secondary));
            linkCard.setStrokeColor(ContextCompat.getColor(this, R.color.bg_card_stroke));
            linkCard.setStrokeWidth(1);
            linkCard.setRadius(10 * density);
            linkCard.setClickable(true);
            linkCard.setFocusable(true);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding((int) (14 * density), (int) (10 * density), (int) (14 * density), (int) (10 * density));

            ImageView icon = new ImageView(this);
            int iconRes = urls[i].contains("t.me") ? R.drawable.ic_telegram : R.drawable.ic_github;
            icon.setImageResource(iconRes);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams((int) (20 * density), (int) (20 * density));
            iconParams.setMarginEnd((int) (12 * density));
            icon.setLayoutParams(iconParams);

            TextView tv = new TextView(this);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tv.setLayoutParams(textParams);
            tv.setText(urlinfo[i]);
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(13);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);

            ImageView arrow = new ImageView(this);
            arrow.setImageResource(R.drawable.ic_open_in_browser);
            arrow.setColorFilter(ContextCompat.getColor(this, R.color.text_secondary));
            LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams((int) (16 * density), (int) (16 * density));
            arrow.setLayoutParams(arrowParams);

            row.addView(icon);
            row.addView(tv);
            row.addView(arrow);
            linkCard.addView(row);

            final String url = urls[i];
            linkCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            container.addView(linkCard);
        }

        builder.setView(parent);
        final AlertDialog aboutDialog = builder.create();
        if (aboutDialog.getWindow() != null) {
            aboutDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        View btnClose = parent.findViewById(R.id.btnAboutClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    aboutDialog.dismiss();
                }
            });
        }

        aboutDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.information) {
            showAboutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        density = getResources().getDisplayMetrics().density;
        favoritesManager = FavoritesManager.getInstance(this);
        iconManager = IconManager.getInstance();

        permissions = new PermissionUtils(this);
        permissions.checkAllPermissions();

        // Modern status & nav bar colors
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.bg_primary));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.bg_primary));

        // Bind views
        grid = findViewById(R.id.grid);
        searchData = findViewById(R.id.searchData);
        toolRecyclerView = findViewById(R.id.toolRecyclerView);
        categoryContainer = findViewById(R.id.categoryContainer);
        emptyStateView = findViewById(R.id.emptyStateView);
        tvResultsCount = findViewById(R.id.tvResultsCount);
        tvToolCountBadge = findViewById(R.id.tvToolCountBadge);
        btnResetFilter = findViewById(R.id.btnResetFilter);
        btnClearSearch = findViewById(R.id.btnClearSearch);

        ImageButton btnAppInfo = findViewById(R.id.btnAppInfo);
        if (btnAppInfo != null) {
            btnAppInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAboutDialog();
                }
            });
        }

        // Setup Adaptive RecyclerView
        layoutManager = new GridLayoutManager(this, 2);
        calculateSpanCount();
        toolRecyclerView.setLayoutManager(layoutManager);
        toolRecyclerView.setHasFixedSize(true);

        toolAdapter = new ToolAdapter(this, this);
        toolRecyclerView.setAdapter(toolAdapter);

        // Configure SearchView
        setupSearchView();

        // Reset Filter Buttons
        View.OnClickListener resetAction = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCategory = "All Tools";
                currentQuery = "";
                if (searchData != null) {
                    searchData.setQuery("", false);
                    searchData.clearFocus();
                }
                updateAllChips();
                applyFilter();
            }
        };

        if (btnResetFilter != null) btnResetFilter.setOnClickListener(resetAction);
        if (btnClearSearch != null) btnClearSearch.setOnClickListener(resetAction);

        View btnEmptyReset = findViewById(R.id.btnEmptyReset);
        if (btnEmptyReset != null) btnEmptyReset.setOnClickListener(resetAction);

        // Load tools from siteurl.dat and recent_tools.dat
        loadTools();

        // Setup Categories chips with counts
        setupCategories();
    }

    private void setupSearchView() {
        if (searchData == null) return;

        // Customize SearchView text color for readability
        int id = searchData.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView tv = searchData.findViewById(id);
        if (tv != null) {
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            tv.setHintTextColor(ContextCompat.getColor(this, R.color.text_tertiary));
            tv.setTextSize(14);
        }

        searchData.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterSites(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSites(newText);
                return false;
            }
        });
    }

    private void loadTools() {
        try {
            allTools.clear();
            sites.clear();

            // 1. Load original 932 tools from siteurl.dat
            String siteurl = readFromAssets("siteurl.dat");
            if (siteurl != null) {
                String[] lines = siteurl.split("\\r?\\n|\\012");
                for (String line : lines) {
                    if (line == null || line.trim().isEmpty()) continue;
                    sites.add(line);
                    OsintTool tool = OsintTool.parseLine(line);
                    if (tool != null) {
                        tool.setFavorite(favoritesManager.isFavorite(tool.getUrl()));
                        allTools.add(tool);
                    }
                }
            } else {
                toastCenter("File 'siteurl.dat' is missing from assets/");
            }

            // 2. Load 30 new verified tools from recent_tools.dat
            String recentData = readFromAssets("recent_tools.dat");
            if (recentData != null) {
                String[] recentLines = recentData.split("\\r?\\n|\\012");
                for (String line : recentLines) {
                    if (line == null || line.trim().isEmpty()) continue;
                    OsintTool tool = OsintTool.parseRecentLine(line);
                    if (tool != null) {
                        tool.setFavorite(favoritesManager.isFavorite(tool.getUrl()));
                        allTools.add(tool);
                    }
                }
            }

            if (tvToolCountBadge != null) {
                tvToolCountBadge.setText(allTools.size() + " Tools");
            }

            filterSites(".");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        calculateSpanCount();
    }
}
