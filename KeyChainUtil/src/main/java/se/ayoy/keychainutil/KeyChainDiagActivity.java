package se.ayoy.keychainutil;

import android.app.Activity;
import android.os.Bundle;
import android.security.KeyChain;
import android.util.Log;
import android.widget.TextView;

public class KeyChainDiagActivity extends Activity {
    private static final String TAG = "KeyChainDiagActivity";

    private static final String ALGORITHM_RSA = "RSA";
    private static final String ALGORITHM_DSA = "DSA";
    private static final String ALGORITHM_ECC = "EC";

    private TextView supportedRsa;
    private TextView supportedDsa;
    private TextView supportedEcc;

    private TextView boundRsa;
    private TextView boundDsa;
    private TextView boundEcc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_chain_diag);

        this.supportedRsa = (TextView) this.findViewById(R.id.rsa_supported);
        this.supportedDsa = (TextView) this.findViewById(R.id.dsa_supported);
        this.supportedEcc = (TextView) this.findViewById(R.id.ecc_supported);

        this.boundRsa = (TextView) this.findViewById(R.id.rsa_bound);
        this.boundDsa = (TextView) this.findViewById(R.id.dsa_bound);
        this.boundEcc = (TextView) this.findViewById(R.id.ecc_bound);

        checkAlgorithmSupport();
    }

    private void checkAlgorithmSupport() {
        checkAlgorithmSupport(ALGORITHM_RSA, this.supportedRsa, this.boundRsa);
        checkAlgorithmSupport(ALGORITHM_DSA, this.supportedDsa, this.boundDsa);
        checkAlgorithmSupport(ALGORITHM_ECC, this.supportedEcc, this.boundEcc);
    }

    private Boolean checkAlgorithmSupport(String algorithm, TextView supportedView, TextView boundView) {
        if (isAlgorithmSupported(algorithm)) {
            supportedView.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.checkbox_on_background, 0, 0, 0);

            if (isAlgorithmBound(algorithm)) {
                boundView.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.checkbox_on_background, 0, 0, 0);
            }

            return true;
        }

        return false;
    }

    private Boolean isAlgorithmSupported(String algorithm) {
        Boolean isSupported = KeyChain.isKeyAlgorithmSupported(algorithm);

        Log.d(TAG, String.format("%s %ssupported.", algorithm, isSupported ? "" : "not "));

        return isSupported;
    }

    private Boolean isAlgorithmBound(String algorithm) {
        Boolean isBound = KeyChain.isBoundKeyAlgorithm(algorithm);

        Log.d(TAG, String.format("%s %sbound.", algorithm, isBound ? "" : "not "));

        return isBound;
    }
}
