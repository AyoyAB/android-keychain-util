package se.ayoy.keychainutil;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.security.KeyChain;
import android.security.KeyPairGeneratorSpec;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Calendar;

public class KeyChainDiagActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "KeyChainDiagActivity";

    private static final String KEY_STORE_NAME = "AndroidKeyStore";

    private static final String ALGORITHM_RSA = "RSA";
    private static final String ALGORITHM_DSA = "DSA";
    private static final String ALGORITHM_ECC = "EC";

    private static final String CURVE_NAME_ECC = "prime256v1";

    private static final String KEY_ALIAS_RSA = "RSA Test Key";
    private static final String KEY_ALIAS_DSA = "DSA Test Key";
    private static final String KEY_ALIAS_ECC = "ECC Test Key";

    private static final String SIGNATURE_ALGORITHM_RSA = "SHA1withRSA";
    private static final String SIGNATURE_ALGORITHM_DSA = "SHA1withDSA";
    private static final String SIGNATURE_ALGORITHM_ECC = "SHA1WithECDSA";

    private static final byte[] DATA_TO_BE_SIGNED = new byte[] { 0x00, 0x01, 0x02, 0x03 };

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

    private KeyStore androidKeyStore;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_rsa_generate:
                generateRsaKey();
                break;

            case R.id.btn_dsa_generate:
                generateDsaKey();
                break;

            case R.id.btn_ecc_generate:
                generateEccKey();
                break;

            case R.id.btn_rsa_test:
                testRsaKey();
                break;

            case R.id.btn_dsa_test:
                testDsaKey();
                break;

            case R.id.btn_ecc_test:
                testEccKey();
                break;

            case R.id.btn_rsa_delete:
                deleteRsaKey();
                checkIfRsaKeyExists();
                break;

            case R.id.btn_dsa_delete:
                deleteDsaKey();
                checkIfDsaKeyExists();
                break;

            case R.id.btn_ecc_delete:
                deleteEccKey();
                checkIfEccKeyExists();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_key_chain_diag);

        initializeKeyStore();
        initializeTextViews();
        initializeButtons();

        checkAlgorithmSupport();
        checkIfKeysExist();

        setTitle();
    }

    private void initializeKeyStore() {
        try {
            this.androidKeyStore = KeyStore.getInstance(KEY_STORE_NAME);
            this.androidKeyStore.load(null);
        }
        catch (Exception e) {
            Log.e(TAG, "Failed to initialize KeyStore", e);

            throw new RuntimeException("Failed to initialize KeyStore", e);
        }
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

                existsView.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.checkbox_off_background, 0, 0, 0);

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

    private void generateRsaKey() {
        new GenerateRsaKeyTask().execute();
    }

    private void generateDsaKey() {
        new GenerateDsaKeyTask().execute();
    }

    private void generateEccKey() {
        new GenerateEccKeyTask().execute();
    }

    private void testRsaKey() {
        testKey(KEY_ALIAS_RSA, SIGNATURE_ALGORITHM_RSA);
    }

    private void testDsaKey() {
        testKey(KEY_ALIAS_DSA, SIGNATURE_ALGORITHM_DSA);
    }

    private void testEccKey() {
        testKey(KEY_ALIAS_ECC, SIGNATURE_ALGORITHM_ECC);
    }

    private void testKey(String alias, String algorithm) {
        try {
            PrivateKey key = getPrivateKeyForAlias(alias);

            byte[] signature = signData(algorithm, key, DATA_TO_BE_SIGNED);

            Certificate cert = getCertificateForAlias(alias);

            if (verifyData(algorithm, cert, DATA_TO_BE_SIGNED, signature)) {
                Toast.makeText(this, String.format("%s signature test succeeded.", algorithm), Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, String.format("%s signature test failed!", algorithm), Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e) {
            Log.e(TAG, String.format("Exception caught during %s test.", algorithm), e);

            throw new RuntimeException(String.format("Exception caught during %s test.", algorithm), e);
        }
    }

    private byte[] signData(String algorithmName, PrivateKey key, byte[] data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature s = Signature.getInstance(algorithmName);

        s.initSign(key);

        s.update(data);

        return s.sign();
    }

    private Boolean verifyData(String algorithmName, Certificate cert, byte[] data, byte[] signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature s = Signature.getInstance(algorithmName);

        s.initVerify(cert);

        s.update(data);

        return s.verify(signature);
    }

    private PrivateKey getPrivateKeyForAlias(String alias) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException {
        KeyStore.Entry entry = getEntryForAlias(alias);

        return ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
    }

    private Certificate getCertificateForAlias(String alias) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException {
        KeyStore.Entry entry = getEntryForAlias(alias);

        return ((KeyStore.PrivateKeyEntry) entry).getCertificate();
    }

    private KeyStore.Entry getEntryForAlias(String alias) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException {
        KeyStore.Entry entry = androidKeyStore.getEntry(alias, null);

        if (entry == null) {
            throw new AssertionError(String.format("Key not found: %s", alias));
        }

        if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
            throw new AssertionError(String.format("Entry is not private key: %s", alias));
        }

        return entry;
    }

    private void deleteRsaKey() {
        deleteKey(KEY_ALIAS_RSA);
    }

    private void deleteDsaKey() {
        deleteKey(KEY_ALIAS_DSA);
    }

    private void deleteEccKey() {
        deleteKey(KEY_ALIAS_ECC);
    }

    private void deleteKey(String alias) {
        try {
            androidKeyStore.deleteEntry(alias);

            Toast.makeText(this, String.format("Test key \"%s\" successfully deleted.", alias), Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            Log.e(TAG, String.format("Exception caught deleting test key \"%s\".", alias), e);

            throw new RuntimeException(String.format("Exception caught deleting test key \"%s\".", alias), e);
        }
    }

    private class GenerateRsaKeyTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "GenerateRsaKeyTask";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Calendar certificateStartDate = Calendar.getInstance();
            Calendar certificateEndDate = Calendar.getInstance();
            certificateEndDate.add(Calendar.YEAR, 1);

            KeyPairGeneratorSpec spec =
                    new KeyPairGeneratorSpec.Builder(KeyChainDiagActivity.this).
                            setAlias(KEY_ALIAS_RSA).
                            setSerialNumber(BigInteger.ONE).
                            setSubject(new X500Principal(String.format("CN=%s", KEY_ALIAS_RSA))).
                            setStartDate(certificateStartDate.getTime()).
                            setEndDate(certificateEndDate.getTime()).
                            build();

            try {
                KeyPairGenerator gen = KeyPairGenerator.getInstance(ALGORITHM_RSA, KEY_STORE_NAME);
                gen.initialize(spec);
                gen.generateKeyPair();

                return true;
            }
            catch (Exception e) {
                Log.e(TAG, "Exception caught generating RSA test key.", e);

                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            setProgressBarIndeterminateVisibility(false);

            if (result) {
                Toast.makeText(KeyChainDiagActivity.this, "Test RSA key successfully created.", Toast.LENGTH_SHORT).show();
                checkIfRsaKeyExists();
            }
            else {
                Toast.makeText(KeyChainDiagActivity.this, "Test RSA key generation failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class GenerateDsaKeyTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "GenerateDsaKeyTask";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT)
        protected Boolean doInBackground(Void... params) {
            Calendar certificateStartDate = Calendar.getInstance();
            Calendar certificateEndDate = Calendar.getInstance();
            certificateEndDate.add(Calendar.YEAR, 1);

            try {
                KeyPairGeneratorSpec spec =
                        new KeyPairGeneratorSpec.Builder(KeyChainDiagActivity.this).
                                setAlias(KEY_ALIAS_DSA).
                                setKeySize(1024).
                                setKeyType(ALGORITHM_DSA).
                                setSerialNumber(BigInteger.ONE).
                                setSubject(new X500Principal(String.format("CN=%s", KEY_ALIAS_DSA))).
                                setStartDate(certificateStartDate.getTime()).
                                setEndDate(certificateEndDate.getTime()).
                                build();

                // NB: We have to "masquerade" as an RSA key pair generator here, but the spec will still work.
                KeyPairGenerator gen = KeyPairGenerator.getInstance(ALGORITHM_RSA, KEY_STORE_NAME);
                gen.initialize(spec);
                gen.generateKeyPair();

                return true;
            }
            catch (Exception e) {
                Log.e(TAG, "Exception caught generating DSA test key.", e);

                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            setProgressBarIndeterminateVisibility(false);

            if (result) {
                Toast.makeText(KeyChainDiagActivity.this, "Test DSA key successfully created.", Toast.LENGTH_SHORT).show();
                checkIfDsaKeyExists();
            }
            else {
                Toast.makeText(KeyChainDiagActivity.this, "Test DSA key generation failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class GenerateEccKeyTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "GenerateEccKeyTask";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT)
        protected Boolean doInBackground(Void... params) {
            Calendar certificateStartDate = Calendar.getInstance();
            Calendar certificateEndDate = Calendar.getInstance();
            certificateEndDate.add(Calendar.YEAR, 1);

            try {
                KeyPairGeneratorSpec spec =
                        new KeyPairGeneratorSpec.Builder(KeyChainDiagActivity.this).
                                setAlias(KEY_ALIAS_ECC).
                                setKeyType(ALGORITHM_ECC).
                                setAlgorithmParameterSpec(new ECGenParameterSpec(CURVE_NAME_ECC)).
                                setSerialNumber(BigInteger.ONE).
                                setSubject(new X500Principal(String.format("CN=%s", KEY_ALIAS_ECC))).
                                setStartDate(certificateStartDate.getTime()).
                                setEndDate(certificateEndDate.getTime()).
                                build();

                // NB: We have to "masquerade" as an RSA key pair generator here, but the spec will still work.
                KeyPairGenerator gen = KeyPairGenerator.getInstance(ALGORITHM_RSA, KEY_STORE_NAME);
                gen.initialize(spec);
                gen.generateKeyPair();

                return true;
            }
            catch (Exception e) {
                Log.e(TAG, "Exception caught generating ECC test key.", e);

                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            setProgressBarIndeterminateVisibility(false);

            if (result) {
                Toast.makeText(KeyChainDiagActivity.this, "Test ECC key successfully created.", Toast.LENGTH_SHORT).show();
                checkIfEccKeyExists();
            }
            else {
                Toast.makeText(KeyChainDiagActivity.this, "Test ECC key generation failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
