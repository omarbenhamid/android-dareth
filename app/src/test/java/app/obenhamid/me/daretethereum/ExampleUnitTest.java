package app.obenhamid.me.daretethereum;

import org.json.JSONObject;
import org.junit.Test;

import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        //JSONObject status = new BlockScoutCheckTransactionTask(null).doInBackground("0xc94c7e0b63efa93d795c8faae8856928facfee088d6483b848e473d7fdab9d2d");
        //new URL("https://blockscout.com/eth/ropsten/api?module=transaction&action=gettxinfo&txhash=0xc94c7e0b63efa93d795c8faae8856928facfee088d6483b848e473d7fdab9d2d")
        //        .openStream();
        OkHttpClient c = new OkHttpClient();
        //Request req = new Request.Builder().get().url("https://blockscout.com/eth/ropsten/api?module=transaction&action=gettxinfo&txhash=0xc94c7e0b63efa93d795c8faae8856928facfee088d6483b848e473d7fdab9d2d")
        //        .build();
        Request req = new Request.Builder().get().url("https://blockscout.com/eth/ropsten/tx/0xc94c7e0b63efa93d795c8faae8856928facfee088d6483b848e473d7fdab9d2d")
                .build();
        String s = c.newCall(req).execute().body().string();

        assertEquals(4, 2 + 2);
    }
}