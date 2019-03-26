package op27no2.comsta;

import android.content.Context;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class MyApplication extends android.app.Application {
    private static Context context;

    public MyApplication() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MyApplication.context = op27no2.comsta.MyApplication.getAppContext();
        System.out.println("APPLICATION CALLED");
        Fabric.with(this, new Crashlytics());

    }
    public static Context getAppContext() {
        return MyApplication.context;
    }
}
