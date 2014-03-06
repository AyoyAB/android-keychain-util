package se.ayoy.keychainutil;

import android.app.Activity;
import android.os.Binder;
import android.os.Bundle;
import android.security.KeyChain;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.security.KeyStore;

public class KeyChainDiagActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "KeyChainDiagActivity";

    private static final String KEY_STORE_NAME = "AndroidKeyStore";

    private static final String ALGORITHM_RSA = "RSA";
    private static final String ALGORITHM_DSA = "DSA";
    private static final String ALGORITHM_ECC = "EC";

    private static final String KEY_ALIAS_RSA = "RSA Test Key";
    private static final String KEY_ALIAS_DSA = "DSA Test Key";
    private static final String KEY_ALIAS_ECC = "ECC Test Key";

    private TextView supportedRsa;
    private TextView supportedDsa;
    private TextView supportedEcc;

    private TextView boundRsa;
    private TextView boundDsa;
    private TextView boundEcc;

    private TextView keyExistsRsa;
    private TextView keyExistsDsa;
    private TextView keyExistsEcc;

    private Button btnGenerateRsa;
    private Button btnGenerateDsa;
    private Button btnGenerateEcc;

    private Button btnTestRsa;
    private Button btnTestDsa;
    private Button btnTestEcc;

    private Button btnDeleteRsa;
    private Button btnDeleteDsa;
    private Button btnDeleteEcc;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_rsa_generate:
            case R.id.btn_dsa_generate:
            case R.id.btn_ecc_generate:
            case R.id.btn_rsa_test:
            case R.id.btn_dsa_test:
            case R.id.btn_ecc_test:
            case R.id.btn_rsa_delete:
            case R.id.btn_dsa_delete:
            case R.id.btn_ecc_delete:
            default:
                // TODO: Implement.
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_chain_diag);

        initializeTextViews();
        initializeButtons();

        checkAlgorithmSupport();
        checkIfKeysExist();

        setTitle();
    }

    private void initializeTextViews() {
        this.supportedRsa = (TextView) this.findViewById(R.id.rsa_supported);
        this.supportedDsa = (TextView) this.findViewById(R.id.dsa_supported);
        this.supportedEcc = (TextView) this.findViewById(R.id.ecc_supported);

        this.boundRsa = (TextView) this.findViewById(R.id.rsa_bound);
        this.boundDsa = (TextView) this.findViewById(R.id.dsa_bound);
        this.boundEcc = (TextView) this.findViewById(R.id.ecc_bound);

        this.keyExistsRsa = (TextView) this.findViewById(R.id.rsa_key_exists);
        this.keyExistsDsa = (TextView) this.findViewById(R.id.dsa_key_exists);
        this.keyExistsEcc = (TextView) this.findViewById(R.id.ecc_key_exists);
    }

    private void initializeButtons() {
        this.btnGenerateRsa = (Button) this.findViewById(R.id.btn_rsa_generate);
        this.btnGenerateDsa = (Button) this.findViewById(R.id.btn_dsa_generate);
        this.btnGenerateEcc = (Button) this.findViewById(R.id.btn_ecc_generate);

        this.btnTestRsa = (Button) this.findViewById(R.id.btn_rsa_test);
        this.btnTestDsa = (Button) this.findViewById(R.id.btn_dsa_test);
        this.btnTestEcc = (Button) this.findViewById(R.id.btn_ecc_test);

        this.btnDeleteRsa = (Button) this.findViewById(R.id.btn_rsa_delete);
        this.btnDeleteDsa = (Button) this.findViewById(R.id.btn_dsa_delete);
        this.btnDeleteEcc = (Button) this.findViewById(R.id.btn_ecc_delete);

        this.btnGenerateRsa.setOnClickListener(this);
        this.btnGenerateDsa.setOnClickListener(this);
        this.btnGenerateEcc.setOnClickListener(this);

        this.btnTestRsa.setOnClickListener(this);
        this.btnTestDsa.setOnClickListener(this);
        this.btnTestEcc.setOnClickListener(this);

        this.btnDeleteRsa.setOnClickListener(this);
        this.btnDeleteDsa.setOnClickListener(this);
        this.btnDeleteEcc.setOnClickListener(this);
    }

    private void checkAlgorithmSupport() {
        this.btnGenerateRsa.setEnabled(checkAlgorithmSupport(ALGORITHM_RSA, this.supportedRsa, this.boundRsa));
        this.btnGenerateDsa.setEnabled(checkAlgorithmSupport(ALGORITHM_DSA, this.supportedDsa, this.boundDsa));
        this.btnGenerateEcc.setEnabled(checkAlgorithmSupport(ALGORITHM_ECC, this.supportedEcc, this.boundEcc));
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

    private void checkIfKeysExist() {
        checkIfRsaKeyExists();
        checkIfDsaKeyExists();
        checkIfEccKeyExists();
    }

    private void checkIfRsaKeyExists() {
        Log.d(TAG, "Checking RSA test key.");

        checkIfKeyExists(KEY_ALIAS_RSA, this.keyExistsRsa, this.btnGenerateRsa, this.btnTestRsa, this.btnDeleteRsa);
    }

    private void checkIfDsaKeyExists() {
        Log.d(TAG, "Checking DSA test key.");

        checkIfKeyExists(KEY_ALIAS_DSA, this.keyExistsDsa, this.btnGenerateDsa, this.btnTestDsa, this.btnDeleteDsa);
    }

    private void checkIfEccKeyExists() {
        Log.d(TAG, "Checking ECC test key.");

        checkIfKeyExists(KEY_ALIAS_ECC, this.keyExistsEcc, this.btnGenerateEcc, this.btnTestEcc, this.btnDeleteEcc);
    }

    private void checkIfKeyExists(String alias, TextView existsView, Button generateButton, Button testButton, Button deleteButton) {
        try {
            KeyStore ks = KeyStore.getInstance(KEY_STORE_NAME);
            ks.load(null);

            if (ks.containsAlias(alias)) {
                Log.d(TAG, String.format("Test key \"%s\" exists.", alias));

                existsView.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.checkbox_on_background, 0, 0, 0);

                generateButton.setEnabled(false);
                testButton.setEnabled(true);
                deleteButton.setEnabled(true);
            }
            else {
                Log.d(TAG, String.format("Test key \"%s\" does not exist.", alias));

                generateButton.setEnabled(true);
                testButton.setEnabled(false);
                deleteButton.setEnabled(false);
            }
        }
        catch (Exception e) {
            Log.e(TAG, String.format("Exception caught checking test key \"%s\".", alias), e);

            generateButton.setEnabled(false);
            testButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }

    private void setTitle() {
        getActionBar().setTitle(String.format("%s (Uid: %d)", getResources().getText(R.string.app_name), Binder.getCallingUid()));
    }
}
