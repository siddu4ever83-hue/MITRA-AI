package com.mitra.ai;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.mitra.ai.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FileManager fileManager;
    private boolean isModified = false;
    private static final int OPEN_FILE_REQUEST = 200;
    private static final int SAVE_FILE_REQUEST = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        fileManager = new FileManager(this);

        setupEditor();
        setupFormatButtons();
        updateTitle();
    }

    private void setupEditor() {
        binding.editMarkdown.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isModified) {
                    isModified = true;
                    updateTitle();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFormatButtons() {
        binding.btnBold.setOnClickListener(v ->
                insertMarkdown("**", "**", "bold text"));

        binding.btnItalic.setOnClickListener(v ->
                insertMarkdown("*", "*", "italic text"));

        binding.btnStrike.setOnClickListener(v ->
                insertMarkdown("~~", "~~", "strikethrough"));

        binding.btnHeading.setOnClickListener(v ->
                insertAtLineStart("# "));

        binding.btnH2.setOnClickListener(v ->
                insertAtLineStart("## "));

        binding.btnH3.setOnClickListener(v ->
                insertAtLineStart("### "));

        binding.btnCode.setOnClickListener(v ->
                insertMarkdown("`", "`", "code"));

        binding.btnCodeBlock.setOnClickListener(v ->
                insertMarkdown("```\n", "\n```", "code block"));

        binding.btnLink.setOnClickListener(v ->
                insertMarkdown("[", "](url)", "link text"));

        binding.btnImage.setOnClickListener(v ->
                insertMarkdown("![", "](url)", "alt text"));

        binding.btnList.setOnClickListener(v ->
                insertAtLineStart("- "));

        binding.btnOrderedList.setOnClickListener(v ->
                insertAtLineStart("1. "));

        binding.btnCheckbox.setOnClickListener(v ->
                insertAtLineStart("- [ ] "));

        binding.btnQuote.setOnClickListener(v ->
                insertAtLineStart("> "));

        binding.btnHr.setOnClickListener(v ->
                insertText("\n---\n"));

        binding.btnTable.setOnClickListener(v ->
                insertTable());
    }

    private void insertMarkdown(String prefix, String suffix, String placeholder) {
        int start = binding.editMarkdown.getSelectionStart();
        int end = binding.editMarkdown.getSelectionEnd();
        String selected = binding.editMarkdown.getText().toString().substring(start, end);
        String insert = prefix + (selected.isEmpty() ? placeholder : selected) + suffix;
        binding.editMarkdown.getText().replace(start, end, insert);
        if (selected.isEmpty()) {
            binding.editMarkdown.setSelection(
                    start + prefix.length(),
                    start + prefix.length() + placeholder.length());
        }
    }

    private void insertAtLineStart(String prefix) {
        int start = binding.editMarkdown.getSelectionStart();
        String text = binding.editMarkdown.getText().toString();
        int lineStart = text.lastIndexOf('\n', start - 1) + 1;
        binding.editMarkdown.getText().insert(lineStart, prefix);
        binding.editMarkdown.setSelection(lineStart + prefix.length());
    }

    private void insertText(String text) {
        int start = binding.editMarkdown.getSelectionStart();
        binding.editMarkdown.getText().insert(start, text);
    }

    private void insertTable() {
        String table = "\n| Header 1 | Header 2 | Header 3 |\n" +
                "| -------- | -------- | -------- |\n" +
                "| Cell 1   | Cell 2   | Cell 3   |\n" +
                "| Cell 4   | Cell 5   | Cell 6   |\n";
        insertText(table);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_new) {
            newFile();
        } else if (id == R.id.action_open) {
            openFile();
        } else if (id == R.id.action_save) {
            saveFile();
        } else if (id == R.id.action_save_as) {
            saveFileAs();
        } else if (id == R.id.action_preview) {
            openPreview();
        } else if (id == R.id.action_about) {
            showAbout();
        }
        return super.onOptionsItemSelected(item);
    }

    private void newFile() {
        if (isModified) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.unsaved_changes))
                    .setMessage(getString(R.string.save_changes_message))
                    .setPositiveButton(getString(R.string.save), (d, w) -> {
                        saveFile();
                        clearEditor();
                    })
                    .setNegativeButton(getString(R.string.discard), (d, w) -> clearEditor())
                    .setNeutralButton(getString(R.string.cancel), null)
                    .show();
        } else {
            clearEditor();
        }
    }

    private void clearEditor() {
        binding.editMarkdown.setText("");
        fileManager.setCurrentUri(null);
        isModified = false;
        updateTitle();
    }

    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES,
                new String[]{"text/markdown", "text/plain"});
        startActivityForResult(
                Intent.createChooser(intent, "Open Markdown File"),
                OPEN_FILE_REQUEST);
    }

    private void saveFile() {
        if (fileManager.getCurrentUri() != null) {
            String content = binding.editMarkdown.getText().toString();
            if (fileManager.saveFile(content)) {
                isModified = false;
                updateTitle();
                Toast.makeText(this,
                        getString(R.string.file_saved),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            saveFileAs();
        }
    }

    private void saveFileAs() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/markdown");
        intent.putExtra(Intent.EXTRA_TITLE, "document.md");
        startActivityForResult(intent, SAVE_FILE_REQUEST);
    }

    private void openPreview() {
        String content = binding.editMarkdown.getText().toString();
        Intent intent = new Intent(this, PreviewActivity.class);
        intent.putExtra("content", content);
        startActivity(intent);
    }

    private void showAbout() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.app_name))
                .setMessage(getString(R.string.about_message))
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (requestCode == OPEN_FILE_REQUEST) {
                String content = fileManager.readFile(uri);
                if (content != null) {
                    binding.editMarkdown.setText(content);
                    fileManager.setCurrentUri(uri);
                    isModified = false;
                    updateTitle();
                }
            } else if (requestCode == SAVE_FILE_REQUEST) {
                String content = binding.editMarkdown.getText().toString();
                if (fileManager.saveFileToUri(uri, content)) {
                    fileManager.setCurrentUri(uri);
                    isModified = false;
                    updateTitle();
                    Toast.makeText(this,
                            getString(R.string.file_saved),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void updateTitle() {
        String fileName = fileManager.getCurrentFileName();
        String title = (fileName != null ? fileName : getString(R.string.untitled))
                + (isModified ? " *" : "");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public void onBackPressed() {
        if (isModified) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.unsaved_changes))
                    .setMessage(getString(R.string.exit_message))
                    .setPositiveButton(getString(R.string.exit),
                            (d, w) -> super.onBackPressed())
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}
