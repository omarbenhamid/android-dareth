package app.obenhamid.me.daretethereum;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.kethereum.contract.abi.types.model.AddressABIType;
import org.kethereum.erc681.ERC681;
import org.kethereum.erc681.ERC681GeneratorKt;
import org.kethereum.extensions.BigIntegerKt;
import org.kethereum.methodsignatures.SignatureFunKt;
import org.kethereum.methodsignatures.model.TextMethodSignature;
import org.kethereum.rpc.EthereumRPC;
import org.kethereum.rpc.model.StringResultResponse;
import org.walleth.data.networks.NetworkDefinition;
import org.walleth.data.networks.all.NetworkDefinition3;
import org.walleth.khex.HexFunKt;

import java.math.BigInteger;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {
    private static final int REQ_INSCRIPTION = 1234;
    //private static final String CONTRACT_ADDR = "0xc18a78a7e8dcfd3d640c7a78af95505ecc4fab50";
    private static final NetworkDefinition network = new NetworkDefinition3();
    private static final EthereumRPC rpc = new EthereumRPC(network.getRpcEndpoints().get(0), new OkHttpClient.Builder().build());
    private BigInteger moisCourrent;
    private BigInteger mensualite;
    private BigInteger moisPayes;
    private SharedPreferences settings;

    private String contractAddr = "0xb3d801fb8f7ef40cd6137382032ff8efe0fd22f1";
    private String myAddress = "0xa9f101E35504E1769EB608967C37Fc1f156aB195";

    class TxChecker extends BlockScoutCheckTransactionTask {
        TxChecker() {
            super(MainActivity.this, 1, 120000L);
        }
        @Override
        protected void onPostExecute(JSONObject tx) {
            super.onPostExecute(tx);
            if (tx == null) {
                Toast.makeText(MainActivity.this, "Transaction was not found.", Toast.LENGTH_LONG).show();
                return;
            }

            if (tx.optBoolean("success", false)) {
                Toast.makeText(MainActivity.this, "Request rejected by contract", Toast.LENGTH_LONG).show();
            } else {
                myAddress=tx.optString("from", myAddress);
                settings.edit().putString("MY_ADDRESS", myAddress).commit();
                contractAddr=tx.optString("to", contractAddr);
                settings.edit().putString("CONTRACT_ADDR", contractAddr).commit();

                ((TextView)findViewById(R.id.view_address)).setText(myAddress);
                ((TextView)findViewById(R.id.view_contract)).setText(contractAddr);
                new RefreshInfoTask().execute();
                Toast.makeText(MainActivity.this, "Transaction executed !", Toast.LENGTH_LONG).show();
            }
        }
    };




    class RefreshInfoTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Refreshing data");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String blockNumberString = rpc.blockNumber().getResult();
            long blockNumber = Long.parseLong(blockNumberString.replaceAll("0x", ""), 16);

            /*val parameterContent = functionParams.joinToString("") {
                convertStringToABIType(it.first).apply {
                    parseValueFromString(it.second)
                }.toBytes().toNoPrefixHexString()
            }*/

            ;
            StringResultResponse ret = rpc.call("{\"to\":\"" + contractAddr
                            + "\",\"data\":\"0x" +
                            SignatureFunKt.toHexSignature(new TextMethodSignature("moisCourrent()"))
                            + "\"}",
                    blockNumberString);
            moisCourrent = BigIntegerKt.hexToBigInteger(ret.getResult());

            ret = rpc.call("{\"to\":\"" + contractAddr
                            + "\",\"data\":\"0x" +
                            SignatureFunKt.toHexSignature(new TextMethodSignature("mensualite()"))
                            + "\"}",
                    blockNumberString);
            mensualite = BigIntegerKt.hexToBigInteger(ret.getResult());

            AddressABIType addrABI = new AddressABIType();
            addrABI.parseValueFromString(myAddress);

            ret = rpc.call("{\"to\":\"" + contractAddr
                            + "\",\"data\":\"0x"
                            + SignatureFunKt.toHexSignature(new TextMethodSignature("moisPayes(address)"))
                            + HexFunKt.toNoPrefixHexString(addrABI.toBytes())
                            + "\"}",
                    blockNumberString);

            moisPayes = BigIntegerKt.hexToBigInteger(ret.getResult());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(dialog.isShowing()) dialog.dismiss();
            ((TextView)findViewById(R.id.view_mensualite)).setText(mensualite.toString());
            ((TextView)findViewById(R.id.view_moisPayes)).setText(moisPayes.toString());
            ((TextView)findViewById(R.id.view_moisCourrent)).setText(moisCourrent.toString());
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        settings = getSharedPreferences("data", MODE_PRIVATE);
        contractAddr = settings.getString("CONTRACT_ADDR",contractAddr);
        myAddress = settings.getString("MY_ADDRESS", myAddress);
        ((TextView)findViewById(R.id.view_address)).setText(myAddress);
        ((TextView)findViewById(R.id.view_contract)).setText(contractAddr);

        new RefreshInfoTask().execute();
    }

    public void onClickInscription(android.view.View v) {
        ERC681 tx = new ERC681();
        tx.setAddress(contractAddr);
        tx.setChainId(network.getChain().getId());
        tx.setValue(mensualite);
        tx.setFunction("inscription");
        tx.setGas(new BigInteger("50000"));
        startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse(ERC681GeneratorKt.generateURL(tx))), REQ_INSCRIPTION);
    }

    public void onClickPayerMois(android.view.View v) {
        ERC681 tx = new ERC681();
        tx.setAddress(contractAddr);
        tx.setChainId(network.getChain().getId());
        tx.setValue(mensualite);
        tx.setFunction("payerMois");
        tx.setGas(new BigInteger("50000"));
        startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse(ERC681GeneratorKt.generateURL(tx))), REQ_INSCRIPTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_INSCRIPTION) {

            if (resultCode != RESULT_OK || data == null) {
                Toast.makeText(this, "Transaction was not confirmed by wallet.", Toast.LENGTH_LONG).show();
                return;
            }
            new TxChecker().execute(data.getStringExtra("TXHASH"));
        }
    }




    public void onClickGetInfo(android.view.View v) {
        new RefreshInfoTask().execute();
    }
}