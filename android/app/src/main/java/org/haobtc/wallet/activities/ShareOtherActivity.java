package org.haobtc.wallet.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yzq.zxinglibrary.encode.CodeCreator;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.entries.FsActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import dr.android.fileselector.FileSelectConstant;


import static org.haobtc.wallet.utils.Daemon.commands;

public class ShareOtherActivity extends BaseActivity {

    @BindView(R.id.img_Orcode)
    ImageView imgOrcode;
    @BindView(R.id.tet_Preservation)
    TextView tetPreservation;
    @BindView(R.id.tet_TrsactionText)
    TextView tetTrsactionText;
    @BindView(R.id.tet_Copy)
    TextView tetCopy;
    @BindView(R.id.lin_downLoad)
    Button lindownLoad;
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_open)
    TextView tetOpen;
    @BindView(R.id.tet_bigMessage)
    TextView tetBigMessage;
    @BindView(R.id.textView24)
    TextView textView24;
    private RxPermissions rxPermissions;
    private Bitmap bitmap;
    private boolean toGallery;
    private String rowTx;

    @Override
    public int getLayoutId() {
        return R.layout.activity_share_other;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        Intent intent = getIntent();
        rowTx = intent.getStringExtra("rowTx");
        rxPermissions = new RxPermissions(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        //synchronize server
        boolean setSynServer = preferences.getBoolean("set_syn_server", false);
        if (setSynServer) {
            textView24.setVisibility(View.VISIBLE);
        } else {
            textView24.setVisibility(View.GONE);
        }

    }

    @Override
    public void initData() {
        //or code
        if (!TextUtils.isEmpty(rowTx)) {
            bitmap = syncEncodeQRCode(rowTx, 268,Color.parseColor("#000000"), Color.parseColor("#ffffff"), null);

            imgOrcode.setImageBitmap(this.bitmap);
        }
        tetTrsactionText.setText(rowTx);
    }

    public static final Map<EncodeHintType, Object> HINTS = new EnumMap<>(EncodeHintType.class);

    static {
        HINTS.put(EncodeHintType.CHARACTER_SET, "utf-8");
        HINTS.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        HINTS.put(EncodeHintType.MARGIN, 0);
    }

    public static Bitmap syncEncodeQRCode(String content, int size, int foregroundColor, int backgroundColor, Bitmap logo) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size, HINTS);
            int[] pixels = new int[size * size];
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (matrix.get(x, y)) {
                        pixels[y * size + x] = foregroundColor;
                    } else {
                        pixels[y * size + x] = backgroundColor;
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SingleClick
    @OnClick({R.id.tet_Preservation, R.id.tet_Copy, R.id.lin_downLoad, R.id.img_back, R.id.tet_open})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tet_Preservation:
                rxPermissions
                        .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) { // Always true pre-M
                                if (bitmap != null) {
                                    toGallery = saveBitmap(bitmap);
                                    if (toGallery) {
                                        mToast(getString(R.string.preservationbitmappic));
                                    } else {
                                        Toast.makeText(this, R.string.preservationfail, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else { // Oups permission denied
                                Toast.makeText(this, R.string.reservatpion_photo, Toast.LENGTH_SHORT).show();
                            }
                        }).dispose();
                break;
            case R.id.tet_Copy:
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // The text is placed on the system clipboard.
                cm.setText(tetTrsactionText.getText());
                Toast.makeText(this, R.string.alredycopy, Toast.LENGTH_SHORT).show();
                break;
            case R.id.lin_downLoad:
                rxPermissions
                        .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) { // Always true pre-M
                                Intent intent = new Intent();
                                intent.setClass(getApplicationContext(), FsActivity.class);
                                intent.putExtra(FileSelectConstant.SELECTOR_REQUEST_CODE_KEY, FileSelectConstant.SELECTOR_MODE_FOLDER);
                                intent.putExtra("keyFile", "2");
                                startActivityForResult(intent, 1);

                            } else { // Oups permission denied
                                Toast.makeText(this, R.string.reservatpion_photo, Toast.LENGTH_SHORT).show();
                            }
                        }).dispose();
                break;
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_open:
                String strOpen = tetOpen.getText().toString();
                if (strOpen.equals(getString(R.string.spin_open))) {
                    LinearLayout.LayoutParams linearParams1 = (LinearLayout.LayoutParams) tetTrsactionText.getLayoutParams();
                    linearParams1.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    tetTrsactionText.setLayoutParams(linearParams1);
                    tetOpen.setText(getString(R.string.retract));

                } else {
                    LinearLayout.LayoutParams linearParams1 = (LinearLayout.LayoutParams) tetTrsactionText.getLayoutParams();
                    linearParams1.height = 200;
                    tetTrsactionText.setLayoutParams(linearParams1);
                    tetOpen.setText(getString(R.string.spin_open));
                }
                break;
            default:
        }
    }

    public boolean saveBitmap(Bitmap bitmap) {
        try {
            File filePic = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + System.currentTimeMillis() + ".jpg");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            boolean success = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return success;

        } catch (IOException ignored) {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent bundle) {
        super.onActivityResult(requestCode, resultCode, bundle);
        if (resultCode == RESULT_OK) {
            ArrayList<String> listExtra = bundle.getStringArrayListExtra(FileSelectConstant.SELECTOR_BUNDLE_PATHS);
            String str = listExtra.toString();
            String substring = str.substring(1);
            String strPath = substring.substring(0, substring.length() - 1);
            showPopupFilename(strPath);

        }
    }

    private void showPopupFilename(String stPath) {
        View view1 = LayoutInflater.from(this).inflate(R.layout.dialog_view, null, false);
        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(view1).create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ImageView imfCancel = view1.findViewById(R.id.img_Cancel);
        String newPath = stPath.replace("内部存储", "/storage/emulated/0");
        view1.findViewById(R.id.btn_Confirm).setOnClickListener(v -> {
            EditText editFilename = view1.findViewById(R.id.edit_Filename);
            String strFilename = editFilename.getText().toString();
            String fullFilename = newPath + "/" + strFilename + ".psbt";

            try {
                commands.callAttr("save_tx_to_file", fullFilename, rowTx);
                mToast(getString(R.string.downloadsuccse));

            } catch (Exception e) {
                e.printStackTrace();
                mToast(getString(R.string.downloadfail));
            }
            alertDialog.dismiss();

        });
        imfCancel.setOnClickListener(v -> {
            alertDialog.dismiss();
        });
        alertDialog.show();
        //show center
        Window dialogWindow = alertDialog.getWindow();
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = dialogWindow.getAttributes();
        p.width = (int) (d.getWidth() * 0.95);
        p.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(p);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

