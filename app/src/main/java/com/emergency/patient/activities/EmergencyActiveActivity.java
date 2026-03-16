package com.emergency.patient.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emergency.patient.R;
import com.emergency.patient.network.SocketManager;
import com.emergency.patient.security.TokenManager;
import com.emergency.patient.utils.QrGenerator;
import com.emergency.patient.db.AppDatabaseProvider;
import com.emergency.patient.db.EmergencyContactEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * EmergencyActiveActivity — Screen 3 (UI Spec §15.4).
 *
 * Launched automatically by QuickAccessActivity after emergency dispatch.
 * Shows:
 *  - Emergency banner (dismissible but SOS stays active)
 *  - QR code card (left) + contacts card with CALL ALL (right)
 *  - Live Status Feed (RecyclerView)
 *  - Bottom action bar: CANCEL SOS + 102 POLICE
 */
public class EmergencyActiveActivity extends AppCompatActivity {

    // Intent extras from the SOS initiator
    public static final String EXTRA_LAT     = "extra_lat";
    public static final String EXTRA_LNG     = "extra_lng";
    public static final String EXTRA_CHANNEL = "extra_channel";

    // ─── Views ────────────────────────────────────────────────────────────────
    private LinearLayout bannerLayout, llContactsList;
    private TextView tvBannerText, tvChannelInfo;
    private Button btnCancelSos, btn102, btnCallAll;
    private RecyclerView rvStatusFeed;
    private ImageView ivQrEmergency, ivQrFullscreen;
    private View vBannerDot, vFeedDot, qrFullscreenOverlay;

    // ─── Data ─────────────────────────────────────────────────────────────────
    private double lat, lng;
    private String dispatchChannel;
    private final List<StatusFeedItem> feedItems = new ArrayList<>();
    // Use the real entity to hold fetched contacts
    private final List<EmergencyContactEntity> emergencyContacts = new ArrayList<>();
    private android.graphics.Bitmap qrBitmap;

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep the emergency active screen visible over the lock screen
        getWindow().addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        setContentView(R.layout.activity_emergency_active);

        // Read dispatch extras
        lat             = getIntent().getDoubleExtra(EXTRA_LAT, 0.0);
        lng             = getIntent().getDoubleExtra(EXTRA_LNG, 0.0);
        dispatchChannel = getIntent().getStringExtra(EXTRA_CHANNEL);

        bindViews();
        setupBanner();
        setupStatusFeed();
        setupContactList();
        setupQrCode();
        setupBottomBar();
        startPulsingAnimations();
    }

    // ─── Binding ──────────────────────────────────────────────────────────────

    private void bindViews() {
        bannerLayout   = findViewById(R.id.layout_emergency_banner);
        tvBannerText   = findViewById(R.id.tv_banner_text);
        tvChannelInfo  = findViewById(R.id.tv_channel_info);
        btnCancelSos   = findViewById(R.id.btn_cancel_sos);
        btn102         = findViewById(R.id.btn_102_police);
        rvStatusFeed   = findViewById(R.id.rv_status_feed);
        llContactsList = findViewById(R.id.ll_contacts);
        btnCallAll     = findViewById(R.id.btn_call_all);
        ivQrEmergency  = findViewById(R.id.iv_qr_emergency);
        vFeedDot       = findViewById(R.id.v_feed_dot);
        qrFullscreenOverlay = findViewById(R.id.fl_qr_fullscreen_emergency);
        ivQrFullscreen      = findViewById(R.id.iv_qr_fullscreen_emergency);
    }

    // ─── Emergency Banner ─────────────────────────────────────────────────────

    private void setupBanner() {
        tvBannerText.setText(R.string.emergency_activated);
        if (dispatchChannel != null) {
            tvChannelInfo.setText(getString(R.string.sent_via, dispatchChannel));
            tvChannelInfo.setVisibility(View.VISIBLE);
        }
    }

    private void startPulsingAnimations() {
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse_alpha);
        if (vFeedDot != null) vFeedDot.startAnimation(pulse);
    }

    // ─── Live Status Feed ─────────────────────────────────────────────────────

    private void setupStatusFeed() {
        // Pre-populate feed based on dispatch results
        feedItems.add(new StatusFeedItem("Location captured", StatusFeedItem.State.COMPLETED, "Just now"));
        feedItems.add(new StatusFeedItem("Medical ID pushed to lock screen", StatusFeedItem.State.COMPLETED, "Just now"));
        feedItems.add(new StatusFeedItem("Alerting nearby hospitals", StatusFeedItem.State.IN_PROGRESS, null));
        feedItems.add(new StatusFeedItem("Notifying emergency services", StatusFeedItem.State.QUEUED, null));

        rvStatusFeed.setLayoutManager(new LinearLayoutManager(this));
        rvStatusFeed.setAdapter(new StatusFeedAdapter(feedItems));
    }

    // ─── Contact List ─────────────────────────────────────────────────────────

    private void setupContactList() {
        String uuid = TokenManager.getUUID(this);
        
        // Fetch real contacts from database on background thread
        new Thread(() -> {
            List<EmergencyContactEntity> dbContacts = AppDatabaseProvider.getInstance(this)
                    .emergencyContactDao().getContactsForPatient(uuid);
                    
            runOnUiThread(() -> {
                emergencyContacts.clear();
                if (dbContacts != null && !dbContacts.isEmpty()) {
                    emergencyContacts.addAll(dbContacts);
                }
                populateContactsList();
            });
        }).start();

        btnCallAll.setOnClickListener(v -> callAllContacts());
    }

    private void populateContactsList() {
        llContactsList.removeAllViews();
        
        if (emergencyContacts.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No emergency contacts saved.");
            tvEmpty.setTextColor(getResources().getColor(R.color.color_text_secondary));
            tvEmpty.setTextSize(14);
            llContactsList.addView(tvEmpty);
            return;
        }

        for (EmergencyContactEntity contact : emergencyContacts) {
            View itemView = getLayoutInflater().inflate(R.layout.item_contact_row, llContactsList, false);
            TextView tvName = itemView.findViewById(R.id.tv_contact_name);
            TextView tvTag  = itemView.findViewById(R.id.tv_relationship_tag);
            View btnCall    = itemView.findViewById(R.id.btn_call_contact);

            tvName.setText(contact.name);
            tvTag.setText("Emergency Contact"); // Using default tag since relationship isn't stored
            btnCall.setOnClickListener(v -> triggerCall(contact.phoneNumber));

            llContactsList.addView(itemView);
        }
    }

    private void triggerCall(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) return;
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    private void callAllContacts() {
        if (emergencyContacts.isEmpty()) return;
        // Mock sequential dialer — in a real app this would monitor call state
        // but for MVP we pre-dial the first one
        triggerCall(emergencyContacts.get(0).phoneNumber);
    }

    // ─── QR Code ──────────────────────────────────────────────────────────────

    private void setupQrCode() {
        try {
            String uuid = TokenManager.getUUID(this);
            qrBitmap = QrGenerator.generate(uuid, 512);
            ivQrEmergency.setImageBitmap(qrBitmap);

            ivQrEmergency.setOnClickListener(v -> {
                if (qrBitmap != null) {
                    ivQrFullscreen.setImageBitmap(qrBitmap);
                    qrFullscreenOverlay.setVisibility(View.VISIBLE);
                }
            });

            qrFullscreenOverlay.setOnClickListener(v -> qrFullscreenOverlay.setVisibility(View.GONE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─── Bottom Bar ───────────────────────────────────────────────────────────

    private void setupBottomBar() {
        // CANCEL SOS — requires confirmation
        btnCancelSos.setOnClickListener(v -> showCancelConfirmation());

        // 102 POLICE — pre-dials without auto-calling
        btn102.setOnClickListener(v -> {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:102"));
            startActivity(dialIntent);
        });
    }

    private void showCancelConfirmation() {
        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.cancel_emergency_title)
                .setMessage(R.string.cancel_emergency_message)
                .setPositiveButton(R.string.yes_cancel, (dialog, which) -> {
                    SocketManager.getInstance(this).emitCancelEmergency();
                    finish();
                })
                .setNegativeButton(R.string.keep_active, null)
                .show();
    }

    // ─── Status Feed Item Model ────────────────────────────────────────────────

    public static class StatusFeedItem {
        public enum State { COMPLETED, IN_PROGRESS, QUEUED }
        public final String label;
        public final State  state;
        public final String timestamp;

        public StatusFeedItem(String label, State state, String timestamp) {
            this.label     = label;
            this.state     = state;
            this.timestamp = timestamp;
        }
    }


    // ─── Status Feed Adapter ───────────────────────────────────────────────────

    static class StatusFeedAdapter extends RecyclerView.Adapter<StatusFeedAdapter.ViewHolder> {
        private final List<StatusFeedItem> items;

        StatusFeedAdapter(List<StatusFeedItem> items) { this.items = items; }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View v = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_status_feed, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            StatusFeedItem item = items.get(position);
            holder.tvLabel.setText(item.label);

            if (item.timestamp != null) {
                holder.tvTimestamp.setVisibility(View.VISIBLE);
                holder.tvTimestamp.setText(item.timestamp);
            } else {
                holder.tvTimestamp.setVisibility(View.GONE);
            }

            switch (item.state) {
                case COMPLETED:
                    holder.tvIcon.setText("✅");
                    holder.tvLabel.setAlpha(1.0f);
                    break;
                case IN_PROGRESS:
                    holder.tvIcon.setText("⏳");
                    holder.tvLabel.setAlpha(1.0f);
                    break;
                case QUEUED:
                    holder.tvIcon.setText("◯");
                    holder.tvLabel.setAlpha(0.5f);
                    break;
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvIcon, tvLabel, tvTimestamp;
            ViewHolder(View v) {
                super(v);
                tvIcon      = v.findViewById(R.id.tv_feed_icon);
                tvLabel     = v.findViewById(R.id.tv_feed_label);
                tvTimestamp = v.findViewById(R.id.tv_feed_timestamp);
            }
        }
    }
}
