package net.huitel.pucapab;

import android.Manifest;

/**
 * Created by root on 1/25/18.
 */

public enum PermissionRequest {

    CAMERA, INTERNET, CALL_PHONE;


    private int requestCode;
    private String manifestPermission;
    private String explanation;

    static {
        CAMERA.manifestPermission = Manifest.permission.CAMERA;
        CAMERA.explanation = "L'accès à la caméra est nécessaire pour pouvoir scanner le code barre des articles à vérifier.";
        CAMERA.requestCode = 0;
        INTERNET.manifestPermission = Manifest.permission.INTERNET;
        INTERNET.explanation = "L'accès à internet est nécessaire pour obtenir les informations sur les produits scannés.";
        INTERNET.requestCode = 1;
        CALL_PHONE.manifestPermission = Manifest.permission.CALL_PHONE;
        CALL_PHONE.explanation = "L'accès à la fonction d'appel sert à appeler le BRO en cas de problème.";
        CALL_PHONE.requestCode = 2;
    }

    public String getManifestPermission() {
        return this.manifestPermission;
    }

    public String getExplanation() {
        return this.explanation;
    }

    public int getRequestCode() {
        return requestCode;
    }


}
