package org.haobtc.onekey.onekeys.homepage.process;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.core.content.res.ResourcesCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.chaquo.python.Kwarg;
import com.google.gson.Gson;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.math.BigDecimal;
import java.util.Objects;
import org.haobtc.onekey.R;
import org.haobtc.onekey.activities.base.BaseActivity;
import org.haobtc.onekey.activities.transaction.CheckChainDetailWebActivity;
import org.haobtc.onekey.aop.SingleClick;
import org.haobtc.onekey.bean.TransactionETHInfoBean;
import org.haobtc.onekey.bean.TransactionSummaryVo;
import org.haobtc.onekey.business.blockBrowser.BlockBrowserManager;
import org.haobtc.onekey.constant.Vm;
import org.haobtc.onekey.manager.PyEnv;

public class DetailETHTransactionActivity extends BaseActivity {

    private static final String EXT_COIN_TYPE = "coin_type";
    private static final String EXT_TX_ID = "tx_id";
    private static final String EXT_TX_DETAILS = "ext_tx_details";
    private static final String EXT_TX_TIME = "tx_time";

    public static void start(Context context, Vm.CoinType coinType, String txDetails) {
        Intent intent = new Intent(context, DetailETHTransactionActivity.class);
        intent.putExtra(EXT_COIN_TYPE, coinType.name());
        intent.putExtra(EXT_TX_DETAILS, txDetails);
        context.startActivity(intent);
    }

    public static void start(Context context, Vm.CoinType coinType, String txid, String txTime) {
        Intent intent = new Intent(context, DetailETHTransactionActivity.class);
        intent.putExtra(EXT_COIN_TYPE, coinType.name());
        intent.putExtra(EXT_TX_ID, txid);
        intent.putExtra(EXT_TX_TIME, txTime);
        context.startActivity(intent);
    }

    @BindView(R.id.text_tx_amount)
    TextView textTxAmount;

    @BindView(R.id.text_send_address)
    TextView textSendAddress;

    @BindView(R.id.text_receive_address)
    TextView textReceiveAddress;

    @BindView(R.id.text_confirm_num)
    TextView textConfirmNum;

    @BindView(R.id.text_tx_time)
    TextView textTxTime;

    @BindView(R.id.text_tx_status)
    TextView textTxStatus;

    @BindView(R.id.img_tx_status)
    ImageView imgTxStatus;

    @BindView(R.id.text_fee)
    TextView textFee;

    @BindView(R.id.text_remarks)
    TextView textRemarks;

    @BindView(R.id.text_block_high)
    TextView textBlockHigh;

    @BindView(R.id.text_tx_num)
    TextView textTxNum;

    private Vm.CoinType mCoinType;
    private String hashDetail;
    private String txDetail;
    private String txTime;
    private String txid;
    private String txBlockHeight;
    private Disposable mLoadTxDetailDisposable;

    @Override
    public int getLayoutId() {
        return R.layout.activity_detail_eth_transaction;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        mCoinType = Vm.CoinType.convertByCoinName(getIntent().getStringExtra(EXT_COIN_TYPE));
        hashDetail = getIntent().getStringExtra(EXT_TX_ID);
        txDetail = getIntent().getStringExtra(EXT_TX_DETAILS);
        txTime = getIntent().getStringExtra(EXT_TX_TIME);
    }

    @Override
    public void initData() {
        getTxDetail();
    }

    private void getTxDetail() {
        if (mLoadTxDetailDisposable != null && !mLoadTxDetailDisposable.isDisposed()) {
            mLoadTxDetailDisposable.dispose();
        }

        if (!TextUtils.isEmpty(txDetail)) {
            getLocalTxDetail();
        } else {
            getTxDetailByTxId();
        }
    }

    private void getLocalTxDetail() {
        mLoadTxDetailDisposable =
                Single.fromCallable(() -> txDetail)
                        .subscribe(
                                this::localDetailData,
                                e -> {
                                    e.printStackTrace();
                                    if (e.getMessage() != null) {
                                        mToast(
                                                e.getMessage()
                                                        .substring(
                                                                e.getMessage().indexOf(":") + 1));
                                    }
                                });
    }

    private void getTxDetailByTxId() {
        mLoadTxDetailDisposable =
                Single.fromCallable(
                                () ->
                                        PyEnv.sCommands
                                                .callAttr(
                                                        "get_tx_info",
                                                        hashDetail,
                                                        new Kwarg("coin", Vm.CoinType.ETH.callFlag))
                                                .toString())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(
                                txInfo -> {
                                    if (!TextUtils.isEmpty(txInfo)) {
                                        textTxTime.setText(txTime);
                                    }
                                    return txInfo;
                                })
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe(disposable -> showProgress())
                        .doFinally(this::dismissProgress)
                        .subscribe(
                                this::jsonDetailData,
                                e -> {
                                    e.printStackTrace();
                                    if (e.getMessage() != null) {
                                        mToast(
                                                e.getMessage()
                                                        .substring(
                                                                e.getMessage().indexOf(":") + 1));
                                    }
                                });
    }

    private @DrawableRes int getTxStatusImage(int showStatusType) {
        int imageId;
        switch (showStatusType) {
            case 1:
                imageId = R.drawable.ic_tx_unconfirmed;
                break;
            case 2:
                imageId = R.drawable.ic_tx_failure;
                break;
            case 3:
                imageId = R.drawable.ic_tx_confirmed;
                break;
            default:
                imageId = R.drawable.ic_tx_failure;
        }
        return imageId;
    }

    private void localDetailData(String detailMsg) {
        Gson gson = new Gson();
        TransactionSummaryVo listBean = gson.fromJson(detailMsg, TransactionSummaryVo.class);
        String showStatus;
        int showStatusType;
        try {
            showStatus = listBean.getShowStatus().get(1).toString().replace("。", "");
            showStatusType = ((Number) listBean.getShowStatus().get(0)).intValue();
        } catch (Exception e) {
            showStatus = getString(R.string.unknown);
            showStatusType = 1;
        }
        textTxStatus.setText(showStatus.replace("。", ""));
        imgTxStatus.setImageDrawable(
                ResourcesCompat.getDrawable(
                        getResources(), getTxStatusImage(showStatusType), getTheme()));
        String amount = listBean.getAmount();
        if (listBean.getInputAddr() != null && listBean.getInputAddr().size() != 0) {
            String address = listBean.getInputAddr().get(0);
            if (!TextUtils.isEmpty(address)) {
                textSendAddress.setText(address);
            } else {
                textSendAddress.setText(getString(R.string.line));
            }
        }
        if (listBean.getOutputAddr() != null && listBean.getOutputAddr().size() != 0) {
            String outputAddr = listBean.getOutputAddr().get(0);
            textReceiveAddress.setText(outputAddr);
        }
        String txStatus = listBean.getStatus();
        txid = listBean.getTxId();
        textTxAmount.setText(
                String.format(
                        "%s %s",
                        new BigDecimal(listBean.getAmount().trim())
                                .stripTrailingZeros()
                                .toPlainString(),
                        listBean.getAmountUnit()));
        if (txStatus.contains("confirmations")) {
            String confirms = txStatus.substring(0, txStatus.indexOf(" ")).replace("。", "");
            textConfirmNum.setText(confirms);
        } else {
            textConfirmNum.setText(txStatus.replace("。", ""));
        }
        textTxNum.setText(txid);
        String fee = listBean.getFee();
        if (!TextUtils.isEmpty(fee) && fee.contains(" (")) {
            String txFee = fee.substring(0, fee.indexOf(" ("));
            try {
                String[] txFeeSplit = txFee.split(" ");
                textFee.setText(
                        String.format(
                                "%s %s",
                                new BigDecimal(txFeeSplit[0].trim())
                                        .stripTrailingZeros()
                                        .toPlainString(),
                                txFeeSplit[1]));
            } catch (Exception e) {
                textFee.setText("-");
            }
        } else {
            textFee.setText("-");
        }
        String description = "";
        if (!TextUtils.isEmpty(description)) {
            textRemarks.setText(description);
        } else {
            textRemarks.setText("-");
        }
        txBlockHeight = String.valueOf(listBean.getHeight());
        textBlockHigh.setText(txBlockHeight);
        textTxTime.setText(listBean.getDate());
    }

    private void jsonDetailData(String detailMsg) {
        Gson gson = new Gson();
        TransactionETHInfoBean listBean = gson.fromJson(detailMsg, TransactionETHInfoBean.class);
        String showStatus;
        int showStatusType;
        try {
            showStatus = listBean.getShowStatus().get(1).toString().replace("。", "");
            showStatusType = ((Number) listBean.getShowStatus().get(0)).intValue();
        } catch (Exception e) {
            showStatus = getString(R.string.unknown);
            showStatusType = 1;
        }
        textTxStatus.setText(showStatus.replace("。", ""));
        imgTxStatus.setImageDrawable(
                ResourcesCompat.getDrawable(
                        getResources(), getTxStatusImage(showStatusType), getTheme()));
        String amount = listBean.getAmount();
        if (listBean.getInputAddr() != null && listBean.getInputAddr().size() != 0) {
            String address = listBean.getInputAddr().get(0);
            if (!TextUtils.isEmpty(address)) {
                textSendAddress.setText(address);
            } else {
                textSendAddress.setText(getString(R.string.line));
            }
        }
        if (listBean.getOutputAddr() != null && listBean.getOutputAddr().size() != 0) {
            String outputAddr = listBean.getOutputAddr().get(0);
            textReceiveAddress.setText(outputAddr);
        }
        String txStatus = listBean.getTxStatus();
        txid = listBean.getTxid();
        String fee = listBean.getFee();
        String description = listBean.getDescription();
        if (amount.contains(" (")) {
            String txAmount = amount.substring(0, amount.indexOf(" ("));
            try {
                String[] amountSplit = txAmount.split(" ");
                textTxAmount.setText(
                        String.format(
                                "%s %s",
                                new BigDecimal(amountSplit[0].trim())
                                        .stripTrailingZeros()
                                        .toPlainString(),
                                amountSplit[1]));
            } catch (Exception e) {
                textTxAmount.setText(txAmount);
            }
        } else {
            textTxAmount.setText(amount);
        }
        if (txStatus.contains("confirmations")) {
            String confirms = txStatus.substring(0, txStatus.indexOf(" ")).replace("。", "");
            textConfirmNum.setText(confirms);
        } else {
            textConfirmNum.setText(txStatus.replace("。", ""));
        }
        textTxNum.setText(txid);
        if (fee.contains(" (")) {
            String txFee = fee.substring(0, fee.indexOf(" ("));
            try {
                String[] txFeeSplit = txFee.split(" ");
                textFee.setText(
                        String.format(
                                "%s %s",
                                new BigDecimal(txFeeSplit[0].trim())
                                        .stripTrailingZeros()
                                        .toPlainString(),
                                txFeeSplit[1]));
            } catch (Exception e) {
                textFee.setText(txFee);
            }
            textFee.setText(txFee);
        }
        if (!TextUtils.isEmpty(description)) {
            textRemarks.setText(description);
        } else {
            textRemarks.setText("-");
        }
        txBlockHeight = String.valueOf(listBean.getHeight());
        textBlockHigh.setText(txBlockHeight);
    }

    @SingleClick
    @OnClick({
        R.id.img_back,
        R.id.img_send_copy,
        R.id.img_receive_copy,
        R.id.text_block_high,
        R.id.text_tx_num
    })
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.img_send_copy:
                // copy text
                ClipboardManager cm1 =
                        (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // The text is placed on the system clipboard.
                Objects.requireNonNull(cm1, "ClipboardManager not available")
                        .setPrimaryClip(ClipData.newPlainText(null, textSendAddress.getText()));
                mlToast(getString(R.string.copysuccess));
                break;
            case R.id.img_receive_copy:
                // copy text
                ClipboardManager cm2 =
                        (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // The text is placed on the system clipboard.
                Objects.requireNonNull(cm2, "ClipboardManager not available")
                        .setPrimaryClip(ClipData.newPlainText(null, textReceiveAddress.getText()));
                mToast(getString(R.string.copysuccess));
                break;
            case R.id.text_block_high:
                CheckChainDetailWebActivity.startWebUrl(
                        this,
                        getString(R.string.check_trsaction),
                        BlockBrowserManager.INSTANCE.browseBlockUrl(
                                mCoinType, txBlockHeight));
                break;
            case R.id.text_tx_num:
                CheckChainDetailWebActivity.startWebUrl(
                        this,
                        getString(R.string.check_trsaction),
                        BlockBrowserManager.INSTANCE.browseTransactionDetailsUrl(
                                mCoinType, txid));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoadTxDetailDisposable != null && !mLoadTxDetailDisposable.isDisposed()) {
            mLoadTxDetailDisposable.dispose();
        }
    }
}
