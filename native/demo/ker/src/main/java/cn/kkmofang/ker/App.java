package cn.kkmofang.ker;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webkit.WebSettings;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by zhanghailong on 2018/12/12.
 */

public class App {

    static {

        System.loadLibrary("ker");

        _openlibs = new ArrayList<>();

        addOpenlib(new Openlib() {
            @Override
            public void open(long jsContext, App app) {

                DisplayMetrics metrics = new DisplayMetrics();
                app.activity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

                JSContext.PushObject(jsContext);

                JSContext.PushString(jsContext,"width");
                JSContext.PushInt(jsContext, Math.min(metrics.widthPixels,metrics.heightPixels));
                JSContext.PutProp(jsContext,-3);

                JSContext.PushString(jsContext,"height");
                JSContext.PushInt(jsContext, Math.max(metrics.widthPixels,metrics.heightPixels));
                JSContext.PutProp(jsContext,-3);

                JSContext.PushString(jsContext,"density");
                JSContext.PushNumber(jsContext, metrics.density);
                JSContext.PutProp(jsContext,-3);

                JSContext.PutGlobalString(jsContext,"screen");

            }
        });

        openlib();

    }

    public interface Openlib {
        void open(long jsContext,App app);
    }

    private final static List<Openlib> _openlibs;

    public static void addOpenlib(Openlib openlib){
        _openlibs.add(openlib);
    }

    public static void openlib(long jsContext,App app) {
        for(Openlib v : _openlibs) {
            v.open(jsContext,app);
        }
    }

    private final String _basePath;
    private final String _appkey;
    private final WeakReference<Activity> _activity;
    private final long _jsContext;
    private long _ptr;

    public long ptr() {
        return _ptr;
    }

    public App(Activity activity , String basePath, String appkey) {
        _ptr = alloc(this,basePath,appkey,WebSettings.getDefaultUserAgent(activity));
        _activity = new WeakReference<>(activity);
        _basePath = basePath;
        _appkey = appkey;
        _jsContext = jsContext(_ptr);
        JSContext.openlib(_jsContext);
        openlib(_jsContext,this);
    }

    protected void finalize() throws Throwable {
        if(_ptr != 0) {
            dealloc(_ptr);
        }
        super.finalize();
    }

    public void run(Object query) {
        if(_ptr != 0) {
            run(_ptr,query);
        }
    }

    public void open(String uri,boolean animated) {

        if(uri == null) {
            return;
        }

        if(uri.contains("://")) {

            if(uri.startsWith("window://")) {

                URI u = URI.create("app:///" + uri.substring(9));

                openPageFragment(PageFragment.TYPE_WINDOW, u.getPath().substring(1),decodeQuery(u.getQuery()),animated);

            } else if(uri.startsWith("ker://")) {
                URI u = URI.create(uri);
                App.open(activity(),"http://" + u.getHost() + u.getPath(),decodeQuery(u.getQuery()));
            } else if(uri.startsWith("kers://")) {
                URI u = URI.create(uri);
                App.open(activity(),"https://" + u.getHost() + u.getPath(),decodeQuery(u.getQuery()));
            }

        } else {

            URI u = URI.create("app:///" + uri);

            openPageFragment(PageFragment.TYPE_PAGE, u.getPath().substring(1),decodeQuery(u.getQuery()),animated);

        }
    }


    protected void openPageFragment(String type,String path,Map<String,String> query,boolean animated) {

        PageFragment fragment = new PageFragment();

        fragment.path = path;
        fragment.type = type;
        fragment.query = query;

        if(PageFragment.TYPE_WINDOW.equals(type)) {
            Activity a = activity();
            if(a != null && a instanceof IAppActivity) {
                ((IAppActivity) a).show(fragment, animated);
            }
        } else {
            Activity a = activity();
            if(a != null && a instanceof IAppActivity) {
                ((IAppActivity) a).open(fragment, animated);
            }
        }
    }

    public void back(int delta,boolean animated) {
        Activity a = activity();
        if(a != null && a instanceof IAppActivity) {
            ((IAppActivity) a).back(delta, animated);
        }
    }

    public void recycle() {
        if(_ptr != 0) {
            dealloc(_ptr);
            _ptr = 0;
        }
    }

    public Activity activity() {
        return _activity.get();
    }


    public static String stringValue(Object v,String defaultValue) {

        if(v != null) {
            if(v instanceof String ){
                return (String) v;
            }
            return v.toString();
        }
        return defaultValue;
    }

    public static String encodeQuery(Object query){
        StringBuffer sb = new StringBuffer();

        if(query != null) {
            String dot = "";
            if(query instanceof Map) {
                Map<?,?> m = (Map<?,?>) query;
                for(Object key : m.keySet()) {
                    sb.append(dot);
                    dot = "&";
                    sb.append(stringValue(key,""));
                    sb.append("=");
                    try {
                        sb.append(URLEncoder.encode(stringValue(m.get(key),""),"UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                    }
                }
            }

        }

        return sb.toString();
    }

    public static Map<String,String> decodeQuery(String queryString) {
        Map<String,String> m = new TreeMap<>();
        if(queryString != null) {
            String[] vs = queryString.split("&");
            for(String v : vs) {
                String[] kv = v.split("=");
                if(kv.length > 1) {
                    try {
                        m.put(kv[0], URLDecoder.decode(kv[1],"UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                    }
                }
            }
        }
        return m;
    }

    public static Class<?> AppActivityClass = AppActivity.class;

    public static void open(Activity activity,String basePath,String appkey,Object query) {

        Intent i = new Intent(activity,AppActivityClass);

        i.putExtra("basePath",basePath);
        i.putExtra("appkey",appkey);
        i.putExtra("query",encodeQuery(query));

        activity.startActivity(i);
    }

    public static void open(final Activity activity, String URI, final Object query) {

        Package.getPackage(activity, URI, new Package.Callback() {

            @Override
            public void onError(Throwable ex) {
                Log.e("ker",Log.getStackTraceString(ex));
            }

            @Override
            public void onLoad(Package pkg) {
                App.open(activity,pkg.basePath,pkg.appkey,query);
            }

            @Override
            public void onProgress(long bytes, long total) {

            }
        });
    }

    private static native long alloc(App object,String basePath,String appkey,String userAgent);
    private static native void dealloc(long ptr);
    private static native long jsContext(long ptr);
    private static native void run(long ptr,Object query);
    private static native void openlib();
}
