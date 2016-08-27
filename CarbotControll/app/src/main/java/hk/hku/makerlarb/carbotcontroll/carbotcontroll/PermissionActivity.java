package hk.hku.makerlarb.carbotcontroll.carbotcontroll;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by admin on 8/27/2016.
 */
public class PermissionActivity extends AppCompatActivity{

    public static final int PERMISSIONS_GRANTED = 0;
    public static final int PERMISSIONS_DENIED = 1;

    private static final int PERMISSION_REQUEST_CODE = 0;
    private static final String EXTRA_PERMISSIONS = "hk.hku.makerlab.carbotcontroll.permission";
    private static final String PACKAGE_URL_SCHEME = "package: ";

    private boolean isRequiredCheck;

    public static void startActivityForResult(Activity activity, int requestCode, String... permissions){
        Intent intent = new Intent(activity, PermissionActivity.class);
        intent.putExtra(EXTRA_PERMISSIONS,permissions);
        ActivityCompat.startActivityForResult(activity,intent,requestCode,null);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if(getIntent() == null || !getIntent().hasExtra(EXTRA_PERMISSIONS)){
            throw new RuntimeException("PermissionActivity needs static function startActivityForResult!");
        }
//        setContentView();
    }
}
