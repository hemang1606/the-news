package com.example.thenews;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class CategoryFragment extends Fragment {

    private static final String TAG = "NewsDataAPI";
    private static final String API_KEY = "pub_57cee51e99fc4d1d8c04541a6ab4cd64"; // ///<-- Put your key here
    private static final String BASE_URL = "https://newsdata.io/api/1/latest";

    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private ArrayList<newsitem> newsList;
    private ProgressBar loading;
    private TextView errorText;
    private TabLayout tabLayout;
    private Button fetchButton;
    private ImageView searchIcon;

    private String selectedCategory = "";
    private String currentQuery = "";
    private String nextPage = null; // <-- For infinite news
    private boolean isLoading = false; // Prevent multiple calls at same time

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        loading = view.findViewById(R.id.loading);
        errorText = view.findViewById(R.id.errorText);
        tabLayout = view.findViewById(R.id.tabLayout);
        fetchButton = view.findViewById(R.id.fetchButton);
        searchIcon = view.findViewById(R.id.searchIcon);

        newsList = new ArrayList<>();
        newsAdapter = new NewsAdapter(requireContext(), newsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(newsAdapter);

        // Set up tabs for categories
        String[] categories = {"All Categories", "business", "technology", "entertainment", "sports", "health"  , "science" , "politics", "world"};
        for (String category : categories) {
            tabLayout.addTab(tabLayout.newTab().setText(category));
        }

        // Select the first tab by default
        tabLayout.selectTab(tabLayout.getTabAt(0));
        selectedCategory = tabLayout.getTabAt(0).getText().toString();
        if (selectedCategory.equals("All Categories")) selectedCategory = "";

        // Tab selection listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedCategory = tab.getText().toString();
                if (selectedCategory.equals("All Categories")) selectedCategory = "";
                nextPage = null;
                newsList.clear();
                newsAdapter.notifyDataSetChanged();
                fetchNews();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Optional: Refresh on reselect
            }
        });

        // Search icon click listener
        searchIcon.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Search News");

            final EditText input = new EditText(requireContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(currentQuery); // Pre-fill with current query
            builder.setView(input);

            builder.setPositiveButton("Search", (dialog, which) -> {
                currentQuery = input.getText().toString().trim();
                nextPage = null;
                newsList.clear();
                newsAdapter.notifyDataSetChanged();
                fetchNews();
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        });

        fetchButton.setOnClickListener(v -> {
            nextPage = null; // reset
            newsList.clear();
            newsAdapter.notifyDataSetChanged();
            fetchNews();
        });

        // Infinite scroll listener
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) rv.getLayoutManager();
                if (!isLoading && nextPage != null &&
                        layoutManager != null &&
                        layoutManager.findLastVisibleItemPosition() >= newsList.size() - 3) {
                    fetchNews();
                }
            }
        });

        // Load default news
        fetchNews();
        return view;
    }

    // Fetch news from API (search the news)
    private void fetchNews() {
        if (API_KEY.isEmpty()) {
            errorText.setText("⚠ Please set your NewsData.io API key in the code.");
            errorText.setVisibility(View.VISIBLE);
            return;
        }

        try {
            String query = currentQuery;

            StringBuilder urlBuilder = new StringBuilder(BASE_URL);
            urlBuilder.append("?apikey=").append(API_KEY);
            urlBuilder.append("&language=en");
            if (!query.isEmpty()) urlBuilder.append("&q=").append(URLEncoder.encode(query, "UTF-8"));
            if (!selectedCategory.isEmpty()) urlBuilder.append("&category=").append(URLEncoder.encode(selectedCategory, "UTF-8"));
            if (nextPage != null) urlBuilder.append("&page=").append(nextPage); // <-- Pagination

            Log.i(TAG, "Fetching URL: " + urlBuilder);
            new FetchNewsTask().execute(urlBuilder.toString());

        } catch (Exception e) {
            errorText.setText("URL Error: " + e.getMessage());
            errorText.setVisibility(View.VISIBLE);
        }
    }

    private class FetchNewsTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            isLoading = true;
            loading.setVisibility(View.VISIBLE);
            errorText.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                Log.i(TAG, "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    connection.disconnect();
                    return response.toString();
                } else {
                    return "{\"error\":\"HTTP Error: " + responseCode + "\"}";
                }

            } catch (Exception e) {
                Log.e(TAG, "Network Error", e);
                return "{\"error\":\"" + e.getMessage() + "\"}";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            loading.setVisibility(View.GONE);
            isLoading = false;

            try {
                JSONObject jsonResponse = new JSONObject(result);

                if (jsonResponse.has("error")) {
                    errorText.setText(jsonResponse.getString("error"));
                    errorText.setVisibility(View.VISIBLE);
                    return;
                }

                if ("success".equalsIgnoreCase(jsonResponse.optString("status"))) {
                    JSONArray articles = jsonResponse.getJSONArray("results");
                    for (int i = 0; i < articles.length(); i++) {
                        JSONObject article = articles.getJSONObject(i);
                        newsList.add(new newsitem(
                                article.optString("title", "No Title"),
                                article.optString("description", "No Description"),
                                article.optString("source_id", "Unknown"),
                                article.optString("pubDate", ""),
                                article.optString("link", "")
                        ));
                    }

                    Collections.sort(newsList, (a, b) -> {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                            return sdf.parse(b.getPublishedAt()).compareTo(sdf.parse(a.getPublishedAt()));
                        } catch (Exception e) {
                            return b.getPublishedAt().compareTo(a.getPublishedAt());
                        }
                    });

                    newsAdapter.notifyDataSetChanged();
                    nextPage = jsonResponse.optString("nextPage", null); // <-- Save for next load

                } else {
                    errorText.setText("API Error: " + jsonResponse.optString("message", "Unknown error"));
                    errorText.setVisibility(View.VISIBLE);
                }

            } catch (Exception e) {
                errorText.setText("Parsing Error: " + e.getMessage());
                errorText.setVisibility(View.VISIBLE);
            }
        }
    }
}