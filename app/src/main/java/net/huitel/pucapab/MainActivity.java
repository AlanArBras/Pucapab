package net.huitel.pucapab;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ErrorCallback;
import com.google.zxing.Result;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.huitel.pucapab.network.GlutenChecker;
import net.huitel.pucapab.network.HttpUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * https://android-arsenal.com/details/1/6510
 */
public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback, NavigationView.OnNavigationItemSelectedListener {

    private static final int PERMISSION_REQUEST_CAMERA = 0;
    private static final int PERMISSION_REQUEST_CALL_PHONE = 1;

    private CodeScanner mCodeScanner;
    CodeScannerView scannerView;
    ImageView mainButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findViewById(R.id.textView4).setVisibility(View.GONE);

        scannerView = findViewById(R.id.scanner_view);
        scannerView.setAutoFocusButtonVisible(false);
        scannerView.setVisibility(View.GONE);
        // Use builder
        mCodeScanner = CodeScanner.builder()
                /*camera can be specified by calling .camera(cameraId),
                first back-facing camera on the device by default*/
                /*code formats*/
                .formats(CodeScanner.ALL_FORMATS)/*List<BarcodeFormat>*/
                /*or .formats(BarcodeFormat.QR_CODE, BarcodeFormat.DATA_MATRIX, ...)*/
                /*or .format(BarcodeFormat.QR_CODE) - only one format*/
                /*auto focus*/
                .autoFocus(true).autoFocusMode(AutoFocusMode.SAFE).autoFocusInterval(2000L)
                /*flash*/
                .flash(false)
                /*decode callback*/
                .onDecoded(new DecodeCallback() {
                    @Override
                    public void onDecoded(@NonNull final Result result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String barcode = result.getText();
                                if (barcode != null && !barcode.isEmpty()) {
                                    checkGluten(barcode);
                                }
                                mCodeScanner.stopPreview();
                                scannerView.setVisibility(View.GONE);
                            }
                        });
                    }
                })
                /*error callback*/
                .onError(new ErrorCallback() {
                    @Override
                    public void onError(@NonNull final Exception error) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, error.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).build(this, scannerView);
        // Or use constructor to create scanner with default parameters
        // All parameters can be changed after scanner created
        // mCodeScanner = new CodeScanner(this, scannerView);
        // mCodeScanner.setDecodeCallback(...);
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });

        mainButton = findViewById(R.id.rotating_image);
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanNow(view);
            }
        });

        mainButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (scannerView.getVisibility() == View.VISIBLE) {
            mCodeScanner.stopPreview();
            scannerView.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.stopPreview();
        mCodeScanner.releaseResources();
        super.onPause();
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_local_list) {
            Snackbar.make(scannerView, "Fonctionnalité de liste locale à venir.", Snackbar.LENGTH_LONG).show();
        } else if (id == R.id.nav_manage) {
            Snackbar.make(scannerView, "Outils à venir", Snackbar.LENGTH_LONG).show();
        } else if (id == R.id.nav_send) {
            Snackbar.make(scannerView, "Fonctionnalité d'envoi de logs à venir", Snackbar.LENGTH_LONG).show();
        } else if (id == R.id.call_bro) {
            callBro(scannerView);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /**
     * event handler for scan button
     *
     * @param view view of the activity
     */
    public void scanNow(View view) {
        // BEGIN_INCLUDE(scanNow)
        // Check if the Camera permission has been granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available, start camera preview
            mCodeScanner.setFlashEnabled(false);
            scannerView.setVisibility(View.VISIBLE);
            mCodeScanner.startPreview();
        } else {
            // Permission is missing and must be requested.
            requestCameraPermission();
        }
        // END_INCLUDE(scanNow)

    }


    /**
     *
     */
    private void checkGluten(String barcode) {
        HttpUtils.get(barcode, new RequestParams(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String text;
                switch (GlutenChecker.getGlutenPresence(response)) {
                    case GLUTEN:
                        text = getString(R.string.contains_gluten);
                        break;
                    case TRACES:
                        text = getString(R.string.traces_of_gluten);
                        break;
                    case GLUTEN_FREE:
                        text = getString(R.string.gluten_free);
                        break;
                    case UNKNOWN:
                        text = getString(R.string.presence_of_gluten_unknown);
                        break;
                    case NOT_FOUND:
                        //TODO check another database
                        text = getString(R.string.product_not_found);
                        break;
                    default:
                        text = getString(R.string.presence_of_gluten_unknown);
                        break;
                }
                findViewById(R.id.textView4).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.product_name)).setText(GlutenChecker.getProductName(response));
                ((TextView) findViewById(R.id.gluten_state)).setText(text);
                Snackbar.make(scannerView, text, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                // Pull out the first event on the public timeline
                String text;
                switch (GlutenChecker.getGlutenPresence(timeline)) {
                    case GLUTEN:
                        text = getString(R.string.contains_gluten);
                        break;
                    case TRACES:
                        text = getString(R.string.traces_of_gluten);
                        break;
                    case GLUTEN_FREE:
                        text = getString(R.string.gluten_free);
                        break;
                    case UNKNOWN:
                        text = getString(R.string.presence_of_gluten_unknown);
                        break;
                    case ERROR:
                        text = getString(R.string.parsing_error);
                        break;
                    case NOT_FOUND:
                        text = getString(R.string.product_not_found);
                        break;
                    default:
                        text = getString(R.string.presence_of_gluten_unknown);
                        break;
                }
                Snackbar.make(scannerView, text, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }


    private void callBro(View view) {
        Snackbar.make(view, getResources().getString(R.string.call_snackbar_text), Snackbar.LENGTH_SHORT)
                .setActionTextColor(getResources().getColor(R.color.white))
                .setAction(getResources().getString(R.string.cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        call(true);
                    }
                })
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);
                        if (event != DISMISS_EVENT_ACTION && event != DISMISS_EVENT_CONSECUTIVE)
                            call(false);
                    }
                }).show();
    }

    /**
     * If canceled is not true, then a CALL Intent is launched,
     * nothing is done otherwise.
     *
     * @param canceled boolean, the call is canceled if true.
     */
    private void call(boolean canceled) {
        if (!canceled) {
            // Check if the CALL_PHONE permission has been granted
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {
                // Permission is already available, start call
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + "+33606421679"));
                startActivity(callIntent);
            } else {
                // Permission is missing and must be requested.
                requestCallPhonePermission();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanNow(scannerView);
            } else {
                // Permission request was denied.
                Snackbar.make(scannerView, "Autorisation d'accès à la caméra refusée, impossible de lancer le scanner",
                        Snackbar.LENGTH_LONG)
                        .show();
            }
        } else if (requestCode == PERMISSION_REQUEST_CALL_PHONE) {
            // Request for CALL_PHONE permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callBro(scannerView);
            } else {
                // Permission request was denied.
                Snackbar.make(scannerView, "Autorisation d'accès à la fonction d'appel refusée, impossible d'appeler le BRO",
                        Snackbar.LENGTH_LONG)
                        .show();
            }
        }
        // END_INCLUDE(onRequestPermissionsResult)
    }

    /**
     * Requests the {@link android.Manifest.permission#CAMERA} permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a SnackBar that includes additional information.
     */
    private void requestCameraPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            Snackbar.make(scannerView, "L'accès à la caméra est nécessaire pour pouvoir scanner le code barre des articles à vérifier.",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            PERMISSION_REQUEST_CAMERA);
                }
            }).show();

        } else {
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        }
    }

    /**
     * Requests the {@link android.Manifest.permission#CAMERA} permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a SnackBar that includes additional information.
     */
    private void requestCallPhonePermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CALL_PHONE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            Snackbar.make(scannerView, "L'accès à la fonction d'appel sert à appeler le BRO en cas de problème.",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            PERMISSION_REQUEST_CALL_PHONE);
                }
            }).show();

        } else {
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},
                    PERMISSION_REQUEST_CALL_PHONE);
        }
    }


}
