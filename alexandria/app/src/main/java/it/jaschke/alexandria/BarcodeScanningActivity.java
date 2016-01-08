package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.Collections;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class BarcodeScanningActivity extends ActionBarActivity implements ZXingScannerView.ResultHandler
{
    ZXingScannerView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_scanning);

        scannerView = (ZXingScannerView) findViewById(R.id.scanner_view);
        scannerView.setFormats(new ArrayList<>(Collections.singletonList(BarcodeFormat.EAN_13)));
        scannerView.startCamera();
        scannerView.setResultHandler(this);
    }

    @Override
    public void handleResult(Result result) {
        Intent returnIntent = new Intent();
        // TODO Extra name as constant
        returnIntent.putExtra("EAN", result.getText());
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    protected void onResume() {
        super.onResume();
        scannerView.startCamera();
    }

    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    protected void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();
    }
}
