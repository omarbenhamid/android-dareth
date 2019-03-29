package app.obenhamid.me.daretethereum;

import android.os.AsyncTask;

import org.kethereum.methodsignatures.SignatureFunKt;
import org.kethereum.methodsignatures.model.TextMethodSignature;
import org.kethereum.rpc.EthereumRPC;
import org.kethereum.rpc.model.StringResultResponse;
import org.walleth.data.networks.NetworkDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import okhttp3.OkHttpClient;

public class CallContractTask extends AsyncTask<CallContractTask.JobInput, Void, CallContractTask.JobInput[]> {
    private String contractAddress;
    private EthereumRPC rpc;

    public interface Callback {
        void onFinish(Map<String, StringResultResponse> p);
    }
    public static class JobInput {
        String[] calls;
        CallContractTask.Callback onFinish;
        Map<String,StringResultResponse> res = new HashMap<>();

        public JobInput(String[] calls, Callback onFinish) {
            this.calls = calls;
            this.onFinish = onFinish;
        }
    }

    public CallContractTask(NetworkDefinition network, String contractAddress) {
        this.contractAddress = contractAddress;
        rpc = new EthereumRPC(network.getRpcEndpoints().get(0), new OkHttpClient.Builder().build());
    }


    @Override
    protected JobInput[] doInBackground(JobInput... requests) {
        String blockNumberString = rpc.blockNumber().getResult();
        long blockNumber = Long.parseLong(blockNumberString.replaceAll("0x", ""), 16);

        for(JobInput j : requests) {
            for (String call : j.calls) {
                TextMethodSignature functionSignature = new TextMethodSignature(call);

            /*val parameterContent = functionParams.joinToString("") {
                convertStringToABIType(it.first).apply {
                    parseValueFromString(it.second)
                }.toBytes().toNoPrefixHexString()
            }*/
                String data = SignatureFunKt.toHexSignature(functionSignature);
                final String json = "{\"to\":\"" + contractAddress + "\",\"data\":\"0x" + data + "\"}";

                j.res.put(call, rpc.call(json, blockNumberString));
            }
        }
        return requests;
    }

    @Override
    protected void onPostExecute(JobInput[] requests) {
        for(JobInput j : requests) {
            j.onFinish.onFinish(j.res);
        }
    }
}
