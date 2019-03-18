package app.obenhamid.me.daretethereum;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import org.kethereum.erc681.ERC681;
import org.kethereum.erc681.ERC681GeneratorKt;

import java.math.BigInteger;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ERC681 tx = new ERC681();
                tx.setAddress("0x958b91ffa8f005F6Eb70ee917c75AabDD58d06C8");
                tx.setValue(new BigInteger("200000000000000000"));
                tx.setFunction("inscription");

                Snackbar.make(view, "Sending ERC681 URI hoping you have a wallet installed", Snackbar.LENGTH_LONG)
                        .setAction("Open Wallet", v -> {
                            startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse(ERC681GeneratorKt.generateURL(tx))), 1234);
                        }).show();

                //Shall I use EthereumRPC like : https://github.com/walleth/walleth/blob/908b74c51a5eb52d0e3065369f391c568db1b263/app/src/online/java/org/walleth/workers/RelayTransactionWorker.kt
                /*It seems that Wallet does not submit the TX because this is done by the DataProviderService whcih
                        observers livedata of TX database, the PB is that the CreateTransactionActivity is created in
                        current app process and LiveData being single process : DataProviderService does not notice there is any
                        new data... this could be it ...
                */
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
