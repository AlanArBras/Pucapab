package net.huitel.pucapab;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
        implements ActivityCompat.OnRequestPermissionsResultCallback, NavigationView.OnNavigationItemSelectedListener, ScanResultReceiver {

    private static final int PERMISSION_REQUEST_CAMERA = 0;

    EditText barcode;
    TextView result;
    Button checkGluten;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    scanNow(view);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        barcode = (EditText) findViewById(R.id.barcode);
        result = (TextView) findViewById(R.id.result);
        checkGluten = (Button) findViewById(R.id.checkGluten);
        checkGluten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                checkGluten();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            ScanFragment scanFragment = new ScanFragment();
            fragmentTransaction.add(R.id.scan_fragment, scanFragment);
            fragmentTransaction.commit();
        } else {
            // Permission is missing and must be requested.
            requestCameraPermission();
        }
        // END_INCLUDE(scanNow)

    }


    @Override
    public void scanResultData(String codeFormat, String codeContent) {
        if (codeContent != null && !codeContent.isEmpty()) {
            checkGluten();
        }
    }

    @Override
    public void scanResultData(NoScanResultException noScanData) {
        Toast toast = Toast.makeText(this, noScanData.getMessage(), Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     *
     */
    private void checkGluten(){
        HttpUtils.get(barcode.getText().toString(), new RequestParams(), new JsonHttpResponseHandler() {
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
                Snackbar.make(fab, text, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                result.setText(text);
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
                    default:
                        text = getString(R.string.presence_of_gluten_unknown);
                        break;
                }
                Snackbar.make(fab, text, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                result.setText(text);
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanNow(fab);
            } else {
                // Permission request was denied.
                Snackbar.make(fab, "Autorisation d'accès à la caméra refusée, impossible de lancer le scanner",
                        Snackbar.LENGTH_SHORT)
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
            Snackbar.make(fab, "L'accès à la caméra est nécessaire pour pouvoir scanner le code barre des articles à vérifier.",
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

}
