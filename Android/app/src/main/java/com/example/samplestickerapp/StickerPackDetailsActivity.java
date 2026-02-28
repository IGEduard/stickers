/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.example.samplestickerapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StickerPackDetailsActivity extends AddStickerPackActivity {

    /**
     * Do not change below values of below 3 lines as this is also used by WhatsApp
     */
    public static final String EXTRA_STICKER_PACK_ID = "sticker_pack_id";
    public static final String EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority";
    public static final String EXTRA_STICKER_PACK_NAME = "sticker_pack_name";

    public static final String EXTRA_STICKER_PACK_WEBSITE = "sticker_pack_website";
    public static final String EXTRA_STICKER_PACK_EMAIL = "sticker_pack_email";
    public static final String EXTRA_STICKER_PACK_PRIVACY_POLICY = "sticker_pack_privacy_policy";
    public static final String EXTRA_STICKER_PACK_LICENSE_AGREEMENT = "sticker_pack_license_agreement";
    public static final String EXTRA_STICKER_PACK_TRAY_ICON = "sticker_pack_tray_icon";
    public static final String EXTRA_SHOW_UP_BUTTON = "show_up_button";
    public static final String EXTRA_STICKER_PACK_DATA = "sticker_pack";


    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private StickerPreviewAdapter stickerPreviewAdapter;
    private int numColumns;
    private View addButton;
    private View alreadyAddedText;
    private StickerPack stickerPack;
    private View divider;

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_sticker_pack_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sticker_list), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

        boolean showUpButton = getIntent().getBooleanExtra(EXTRA_SHOW_UP_BUTTON, false);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            stickerPack = getIntent().getParcelableExtra(EXTRA_STICKER_PACK_DATA, StickerPack.class);
        } else {
            stickerPack = getIntent().getParcelableExtra(EXTRA_STICKER_PACK_DATA);
        }
        TextView packNameTextView = findViewById(R.id.pack_name);
        TextView packPublisherTextView = findViewById(R.id.author);
        ImageView packTrayIcon = findViewById(R.id.tray_image);
        TextView packSizeTextView = findViewById(R.id.pack_size);
        SimpleDraweeView expandedStickerView = findViewById(R.id.sticker_details_expanded_sticker);

        addButton = findViewById(R.id.add_to_whatsapp_button);
        alreadyAddedText = findViewById(R.id.already_added_text);
        layoutManager = new GridLayoutManager(this, 1);
        recyclerView = findViewById(R.id.sticker_list);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setNumColumns(recyclerView.getWidth() / recyclerView.getContext().getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_size));
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        recyclerView.addOnScrollListener(dividerScrollListener);
        divider = findViewById(R.id.divider);
        if (stickerPreviewAdapter == null) {
            stickerPreviewAdapter = new StickerPreviewAdapter(getLayoutInflater(), R.drawable.sticker_error, getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_size), getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_padding), stickerPack, expandedStickerView);
            recyclerView.setAdapter(stickerPreviewAdapter);
        }
        packNameTextView.setText(stickerPack.name);
        packPublisherTextView.setText(stickerPack.publisher);
        packTrayIcon.setImageURI(StickerPackLoader.getStickerAssetUri(stickerPack.identifier, stickerPack.trayImageFile));
        packSizeTextView.setText(Formatter.formatFileSize(this, stickerPack.getTotalSize()));
        addButton.setOnClickListener(v -> addStickerPackToWhatsApp(stickerPack.identifier, stickerPack.name));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(showUpButton);
            getSupportActionBar().setTitle(showUpButton ? getResources().getString(R.string.title_activity_sticker_pack_details_multiple_pack) : getResources().getQuantityString(R.plurals.title_activity_sticker_packs_list, 1));
        }
        findViewById(R.id.sticker_pack_animation_indicator).setVisibility(stickerPack.animatedStickerPack ? View.VISIBLE : View.GONE);
    }

    private void setNumColumns(int numColumns) {
        if (this.numColumns != numColumns) {
            layoutManager.setSpanCount(numColumns);
            this.numColumns = numColumns;
            if (stickerPreviewAdapter != null) {
                stickerPreviewAdapter.notifyDataSetChanged();
            }
        }
    }

    private final RecyclerView.OnScrollListener dividerScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull final RecyclerView recyclerView, final int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            updateDivider(recyclerView);
        }

        @Override
        public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy) {
            super.onScrolled(recyclerView, dx, dy);
            updateDivider(recyclerView);
        }

        private void updateDivider(RecyclerView recyclerView) {
            boolean showDivider = recyclerView.computeVerticalScrollOffset() > 0;
            if (divider != null) {
                divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        checkIfPackIsAdded(stickerPack);
    }

    private void checkIfPackIsAdded(StickerPack stickerPack) {
        executor.execute(() -> {
            final boolean isWhitelisted = WhitelistCheck.isWhitelisted(StickerPackDetailsActivity.this, stickerPack.identifier);
            handler.post(() -> updateAddUI(isWhitelisted));
        });
    }

    private void updateAddUI(Boolean isWhitelisted) {
        if (isWhitelisted) {
            addButton.setVisibility(View.GONE);
            alreadyAddedText.setVisibility(View.VISIBLE);
            findViewById(R.id.sticker_pack_details_tap_to_preview).setVisibility(View.GONE);
        } else {
            addButton.setVisibility(View.VISIBLE);
            alreadyAddedText.setVisibility(View.GONE);
            findViewById(R.id.sticker_pack_details_tap_to_preview).setVisibility(View.VISIBLE);
        }
    }
}
