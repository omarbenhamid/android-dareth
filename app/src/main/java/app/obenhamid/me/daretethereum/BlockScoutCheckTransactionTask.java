package app.obenhamid.me.daretethereum;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class BlockScoutCheckTransactionTask extends AsyncTask<String, Void, JSONObject> {
    public static final String LOG_TAG = "deth.TxStatus";
    /** progress dialog to show user that the backup is processing. */
    private ProgressDialog dialog = null;
    private Context ctx;
    private static final String GET_TX_URL_PREFIX="https://blockscout.com/eth/ropsten/api" +
                                        "?module=transaction&action=gettxinfo&txhash=";
    private long timeOut;
    private final long POLL_DELAY_MS=1000;

    private int minConfirmations;

    public BlockScoutCheckTransactionTask(Context ctx, int minConfirmations, long timeOutMillis) {
        this.ctx = ctx;
        this.timeOut = timeOutMillis;
        this.minConfirmations = minConfirmations;
        if(timeOutMillis < minConfirmations * 10000) {
            Log.w(LOG_TAG,"Timeout for block verification ("+timeOutMillis+" ms) seems too low " +
                    "for required number of confirmations ("+minConfirmations+")");
        }
    }

    /** application context. */

    protected void onPreExecute() {
        if(ctx == null) return;

        this.dialog = new ProgressDialog(ctx);
        this.dialog.setMessage("Checking transaction");
        this.dialog.show();
    }

    @Override
    protected void onPostExecute(final JSONObject success) {
        if(ctx == null) return;
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    protected JSONObject doInBackground(String ... params) {
        String txHash = params[0];

        JSONObject resp=null;
        long start = System.currentTimeMillis();
        OkHttpClient c = new OkHttpClient();
        Request req = new Request.Builder().get().url(GET_TX_URL_PREFIX+txHash).build();
        while((System.currentTimeMillis() - start) < timeOut) {
            try {
                Thread.sleep(POLL_DELAY_MS);
                Response r = c.newCall(req).execute();
                if(r.code() != 200) {
                    Log.w(LOG_TAG, "BlockScout Bad HTTP Status for TX: "+txHash+":"+r.code());
                    continue;
                }
                resp = new JSONObject(r.body().string());
                if(!"1".equals(resp.getString("status"))) {
                    Log.w(LOG_TAG, "BlockScout error for TX: "+txHash+": "+resp.getString("message"));
                    continue;
                }
                Log.w(LOG_TAG, "FIXME: Should check that transaction content is what we asked");
                long conf = Long.parseLong(resp.getJSONObject("result")
                        .getString("confirmations"));
                if(conf >= minConfirmations) break;
            }catch(Exception ex) {
                Log.d(LOG_TAG, "Checking TX status failed for "+txHash,ex);
            }
        }
        return resp == null ? null : resp.optJSONObject("result");
    }
}
