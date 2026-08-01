package com.mitra.ai;

import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mitra.ai.databinding.ActivityPreviewBinding;

public class PreviewActivity extends AppCompatActivity {

    private ActivityPreviewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Preview");
        }

        String content = getIntent().getStringExtra("content");
        if (content != null) {
            renderMarkdown(content);
        }
    }

    private void renderMarkdown(String content) {
        // Convert Markdown to HTML
        String html = markdownToHtml(content);
        binding.previewText.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT));
    }

    private String markdownToHtml(String markdown) {
        String html = markdown;

        // Headings
        html = html.replaceAll("(?m)^### (.+)$", "<h3>$1</h3>");
        html = html.replaceAll("(?m)^## (.+)$", "<h2>$1</h2>");
        html = html.replaceAll("(?m)^# (.+)$", "<h1>$1</h1>");

        // Bold and Italic
        html = html.replaceAll("\\*\\*\\*(.+?)\\*\\*\\*", "<b><i>$1</i></b>");
        html = html.replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>");
        html = html.replaceAll("\\*(.+?)\\*", "<i>$1</i>");

        // Strikethrough
        html = html.replaceAll("~~(.+?)~~", "<strike>$1</strike>");

        // Code
        html = html.replaceAll("`(.+?)`", "<code>$1</code>");

        // Blockquote
        html = html.replaceAll("(?m)^> (.+)$", "<blockquote>$1</blockquote>");

        // Unordered List
        html = html.replaceAll("(?m)^- (.+)$", "<li>$1</li>");

        // Ordered List
        html = html.replaceAll("(?m)^\\d+\\. (.+)$", "<li>$1</li>");

        // Horizontal Rule
        html = html.replaceAll("(?m)^---$", "<hr/>");

        // Links
        html = html.replaceAll("\\[(.+?)\\]\\((.+?)\\)", "<a href=\"$2\">$1</a>");

        // Line breaks
        html = html.replaceAll("\n", "<br/>");

        return html;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
