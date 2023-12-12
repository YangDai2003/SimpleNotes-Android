package com.example.notesapp;

import static com.example.notesapp.FileUtils.fileSaveToInside;
import static com.example.notesapp.FileUtils.deleteSingleFile;
import static com.example.notesapp.FileUtils.getRealPathFromURI;
import static com.example.notesapp.FileUtils.readPictureDegree;
import static com.example.notesapp.FileUtils.toTurn;
import static com.example.notesapp.PermissionUtils.verifyStoragePermissions;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityOptionsCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.notesapp.Models.Notes;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author 30415
 */
public class NotesActivity extends AppCompatActivity {
    LinearLayout linearLayout;
    HorizontalScrollView scrollView;
    EditText editTextTitle, editTextNotes;
    TextView textViewDate;
    FloatingActionButton imageButton;
    private Notes notes;
    String images = "", dateStr;
    private List<String> paths = new ArrayList<>();
    private int imageId = 0, getImageId = 0;
    private boolean isOldNote = false;
    private Uri uri;
    ActivityResultLauncher<Intent> intentActivityResultLauncher, photoActivityResultLauncher;

    private void initView() {
        scrollView = findViewById(R.id.horizontalScrollView);
        linearLayout = findViewById(R.id.linear);
        textViewDate = findViewById(R.id.date);
        editTextTitle = findViewById(R.id.edit_text_title);
        editTextNotes = findViewById(R.id.edit_text_notes);
        imageButton = findViewById(R.id.imageButton);
    }

    private void initViewEvents() {
        imageButton.setOnClickListener(v -> {
            editTextTitle.clearFocus();
            editTextNotes.clearFocus();
            Intent intent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
                intent.setType("image/*");
                intentActivityResultLauncher.launch(intent);
            } else {
                intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                intentActivityResultLauncher.launch(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stayout, R.anim.out);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save_note) {
            String title = editTextTitle.getText().toString();
            String description = editTextNotes.getText().toString();
            if (description.isEmpty()) {
                Toast.makeText(NotesActivity.this, getString(R.string.toAdd), Toast.LENGTH_SHORT).show();
                return false;
            }
            notes.setTitle(title);
            notes.setNotes(description);
            notes.setDate(dateStr);
            StringBuilder temp = new StringBuilder();
            for (int i = 0; i < paths.size(); ++i) {
                temp.append(paths.get(i)).append(" ");
            }
            notes.setImage(temp.toString());
            Intent intent = new Intent();
            intent.putExtra("note", notes);
            setResult(Activity.RESULT_OK, intent);
            finish();
        } else if (item.getItemId() == android.R.id.home) {
            if (!isOldNote) {
                for (int i = 0; i < paths.size(); ++i) {
                    deleteSingleFile(paths.get(i));
                }
            }
            finish();
        } else if (item.getItemId() == R.id.time) {
            openCalendar();
        }
        return super.onOptionsItemSelected(item);
    }

    private void openCalendar() {
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(Uri.parse("content://com.android.calendar/events"))
                .putExtra("title", editTextTitle.getText().toString())
                .putExtra("description", editTextNotes.getText().toString());
        startActivity(intent);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getWindow().setStatusBarColor(SurfaceColors.SURFACE_2.getColor(this));
        setContentView(R.layout.activity_notes);
        verifyStoragePermissions(this);
        initView();
        initViewEvents();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        photoActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                findViewById(getImageId).setTransitionName("");
            }
        });

        intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            //此处是跳转的result回调方法
            new Thread(() -> {
                Bitmap bitmap;
                if (result.getData() != null && result.getResultCode() == RESULT_OK) {
                    uri = result.getData().getData();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
                    String realPathFromUri = getRealPathFromURI(uri, this);
                    bitmap = toTurn(decodeSampledBitmap(realPathFromUri), readPictureDegree(realPathFromUri));
                    String path = fileSaveToInside(this, formatter.format(LocalDateTime.now()), bitmap);
                    paths.add(path);
                    runOnUiThread(() -> {
                        if (scrollView.getVisibility() == View.GONE) {
                            scrollView.setVisibility(View.VISIBLE);
                        }
                        ImageView imageView = new ImageView(NotesActivity.this);
                        imageView.setId(imageId++);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(180), ViewGroup.LayoutParams.MATCH_PARENT);
                        params.setMarginEnd(dpToPx(8));
                        imageView.setLayoutParams(params);
                        Glide.with(imageView).asBitmap().load(path).sizeMultiplier(0.8f).into(imageView);
                        imageView.setOnClickListener(v -> {
                            getImageId = imageView.getId();
                            imageView.setTransitionName("testImg");
                            Intent intent = new Intent(this, PhotoActivity.class);
                            intent.putExtra("uri", path);
                            photoActivityResultLauncher.launch(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(NotesActivity.this, imageView, "testImg"));
                        });
                        imageView.setOnLongClickListener(v -> {
                            PopupMenu popupMenu = new PopupMenu(this, imageView);
                            popupMenu.setOnMenuItemClickListener(item -> {
                                if (item.getItemId() == R.id.del) {
                                    int id = imageView.getId();
                                    linearLayout.removeView(imageView);
                                    deleteSingleFile(paths.get(id));
                                    paths.remove(id);
                                    if (paths.isEmpty()) {
                                        scrollView.setVisibility(View.GONE);
                                    }
                                    return true;
                                }
                                return false;
                            });
                            popupMenu.inflate(R.menu.pop_up_delete);
                            popupMenu.show();
                            return false;
                        });
                        linearLayout.addView(imageView, linearLayout.getChildCount());
                    });
                }
            }).start();
        });

        isOldNote = true;
        notes = new Notes();
        if (null != getIntent().getSerializableExtra("old_note")) {
            notes = (Notes) getIntent().getSerializableExtra("old_note");
            editTextTitle.setText(Objects.requireNonNull(notes).getTitle());
            editTextNotes.setText(notes.getNotes());
            dateStr = notes.getDate();
            textViewDate.setText(getString(R.string.create) + dateStr);
            images = notes.getImage();
            if (!images.isEmpty()) {
                List<String> listArr = Arrays.asList(images.trim().split(" "));
                paths = new ArrayList<>(listArr);
                for (int i = 0; i < paths.size(); ++i) {
                    ImageView imageView = new ImageView(NotesActivity.this);
                    imageView.setId(imageId++);
                    Glide.with(imageView).asBitmap().load(paths.get(i)).sizeMultiplier(0.8f).into(imageView);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(180), ViewGroup.LayoutParams.MATCH_PARENT);
                    params.setMarginEnd(dpToPx(8));
                    imageView.setLayoutParams(params);
                    int finalI = i;
                    imageView.setOnClickListener(v -> {
                        getImageId = imageView.getId();
                        imageView.setTransitionName("testImg");
                        Intent intent = new Intent(this, PhotoActivity.class);
                        intent.putExtra("uri", paths.get(finalI));
                        photoActivityResultLauncher.launch(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(NotesActivity.this, imageView, "testImg"));
                    });
                    imageView.setOnLongClickListener(v -> {
                        PopupMenu popupMenu = new PopupMenu(this, imageView);
                        popupMenu.setOnMenuItemClickListener(item -> {
                            if (item.getItemId() == R.id.del) {
                                int id = imageView.getId();
                                linearLayout.removeViewAt(id);
                                deleteSingleFile(paths.get(id));
                                paths.remove(id);
                                if (paths.isEmpty()) {
                                    scrollView.setVisibility(View.GONE);
                                }
                                return true;
                            }
                            return false;
                        });
                        popupMenu.inflate(R.menu.pop_up_delete);
                        popupMenu.show();
                        return false;
                    });
                    linearLayout.addView(imageView, linearLayout.getChildCount());
                }
            } else {
                scrollView.setVisibility(View.GONE);
            }
        } else {
            isOldNote = false;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, yyyy-MM-dd hh:mm a");
            dateStr = formatter.format(LocalDateTime.now());
            textViewDate.setText(getString(R.string.create) + dateStr);
            scrollView.setVisibility(View.GONE);
        }
    }


    private Bitmap decodeSampledBitmap(String path) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        // Calculate inSampleSize
        DisplayMetrics metric = new DisplayMetrics();
        Display display;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            display = this.getDisplay();
            Objects.requireNonNull(display).getRealMetrics(metric);
        } else {
            display = getWindowManager().getDefaultDisplay();
            display.getMetrics(metric);
        }
        int width = metric.widthPixels;     // 屏幕宽度（像素）
        int height = metric.heightPixels;   // 屏幕高度（像素）
        options.inSampleSize = calculateInSampleSize(options, width, height);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            inSampleSize *= 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}