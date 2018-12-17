package cn.kkmofang.ker;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.lang.*;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.kkmofang.ker.http.Callback;
import cn.kkmofang.ker.http.Request;
import cn.kkmofang.ker.http.Response;
import cn.kkmofang.ker.http.Session;

/**
 * Created by zhanghailong on 2018/12/11.
 */

public final class Native {

    public static String getPrototype(Class<?> isa) {

        if(isa == null) {
            return null;
        }

        if(isa != Object.class) {
            JSPrototype p = isa.getAnnotation(JSPrototype.class);
            if(p != null) {
                if("".equals(p.value())){
                    return isa.getName().replace(".","_");
                } else {
                    return p.value();
                }
            }
        }

        for(Class<?> i : isa.getInterfaces()) {
            JSPrototype p = i.getAnnotation(JSPrototype.class);
            if(p != null) {
                if("".equals(p.value())){
                    return i.getName().replace(".","_");
                } else {
                    return p.value();
                }
            }
        }

        return getPrototype(isa.getSuperclass());
    }

    public static String getPrototype(Object object) {
        if(object == null) {
            return null;
        }
        return getPrototype(object.getClass());
    }

    public static JSObject allocJSObject(long kerObject) {
        return new JSObject(kerObject);
    }

    public static void pushObject(long jsContext,Object object) {

        if(object == null) {
            JSContext.PushUndefined(jsContext);
        } else if(object instanceof Integer || object instanceof Short){
            JSContext.PushInt(jsContext,((Number) object).intValue());
        } else if(object instanceof Double || object instanceof Float){
            JSContext.PushNumber(jsContext,((Number) object).doubleValue());
        } else if(object instanceof Long){
            if(((Number) object).intValue() == ((Number) object).longValue()) {
                JSContext.PushInt(jsContext,((Number) object).intValue());
            } else {
                JSContext.PushString(jsContext,object.toString());
            }
        } else if(object instanceof String) {
            JSContext.PushString(jsContext, (String) object);
        } else if(object instanceof Boolean) {
            JSContext.PushBoolean(jsContext, (boolean) object);
        } else if(object instanceof byte[]) {
            JSContext.PushBytes(jsContext, (byte[]) object);
        } else if(object instanceof Iterable) {
            JSContext.PushArray(jsContext);
            int i = 0;
            for (Object v : (Iterable) object) {
                JSContext.PushInt(jsContext, i);
                pushObject(jsContext,v);
                JSContext.PutProp(jsContext, -3);
                i++;
            }
        } else if(object.getClass().isArray()) {
            JSContext.PushArray(jsContext);
            int n = Array.getLength(object);
            for (int i = 0; i < n; i++) {
                Object v = Array.get(object, i);
                JSContext.PushInt(jsContext, i);
                pushObject(jsContext,v);
                JSContext.PutProp(jsContext, -3);
            }
        } else if(object instanceof Map) {
            JSContext.PushObject(jsContext);
            Map m = (Map) object;
            for (Object key : m.keySet()) {
                JSContext.PushString(jsContext, JSContext.stringValue(key, ""));
                pushObject(jsContext, m.get(key));
                JSContext.PutProp(jsContext, -3);
            }
        } else if(object instanceof JSONString) {
            JSContext.PushJSONString(jsContext,((JSONString)object).string);
        } else {
            JSContext.PushObject(jsContext,object);
        }


    }

    public static Object toObject(long jsContext,int idx) {

        if(idx >= 0) {
            return null;
        }

        switch (JSContext.GetType(jsContext,idx)) {
            case JSContext.TYPE_BOOLEAN:
                return JSContext.ToBoolean(jsContext,idx);
            case JSContext.TYPE_NUMBER:
                return JSContext.ToNumber(jsContext,idx);
            case JSContext.TYPE_STRING:
                return JSContext.ToString(jsContext,idx);
            case JSContext.TYPE_BUFFER:
                return JSContext.ToBytes(jsContext,idx);
            case JSContext.TYPE_OBJECT:
                if(JSContext.IsArray(jsContext,idx)) {
                    List<Object> m = new LinkedList<>();
                    JSContext.EnumArray(jsContext,idx);
                    while(JSContext.Next(jsContext,-1,true)) {
                        Object value = toObject(jsContext,-1);
                        if(value != null) {
                            m.add(value);
                        }
                        JSContext.Pop(jsContext,2);
                    }
                    JSContext.Pop(jsContext);
                    return m;
                } else {
                    Object v = JSContext.ToObject(jsContext,idx);
                    if(v == null) {
                        Map<String,Object> m = new TreeMap<>();
                        JSContext.EnumObject(jsContext,idx);
                        while(JSContext.Next(jsContext,-1,true)) {
                            String key = JSContext.ToString(jsContext,-2);
                            Object value = toObject(jsContext,-1);
                            if(key != null && value != null) {
                                m.put(key,value);
                            }
                            JSContext.Pop(jsContext,2);
                        }
                        JSContext.Pop(jsContext);
                        return m;
                    } else {
                        return v;
                    }
                }
            case JSContext.TYPE_LIGHTFUNC:
                return JSContext.ToJSObject(jsContext,idx);
        }

        return null;
    }

    public static int getImageWidth(Object object) {
        if(object instanceof BitmapDrawable) {
            return ((BitmapDrawable) object).getBitmap().getWidth();
        }
        if(object instanceof Image) {
            return ((Image) object).getBitmap().getWidth();
        }
        return 0;
    }

    public static int getImageHeight(Object object) {
        if(object instanceof BitmapDrawable) {
            return ((BitmapDrawable) object).getBitmap().getHeight();
        }
        if(object instanceof Image) {
            return ((Image) object).getBitmap().getHeight();
        }
        return 0;
    }

    public static Object getImageWithCache(String URI,String basePath) {
        return null;
    }

    public static void getImage(long imageObject,String URI,String basePath) {

    }

    public static void viewObtain(Object view,long viewObject) {

        if(view instanceof IKerView) {
            ((IKerView) view).obtain(viewObject);
        }

    }

    public static void viewRecycle(Object view,long viewObject) {

        if(view instanceof IKerView) {
            ((IKerView) view).recycle(viewObject);
        }

    }

    public static void viewSetAttribute(Object view,long viewObject,String key,String value) {

        if(view instanceof IKerView) {
            ((IKerView) view).setAttributeValue(viewObject,key,value);
        }
    }

    public static void viewSetFrame(Object view,long viewObject,float x,float y,float width,float height) {

        if(view instanceof View) {
            Rect frame = new Rect();
            frame.x = (int) x;
            frame.y = (int) y;
            frame.width = (int) Math.ceil(width);
            frame.height = (int) Math.ceil(height);
            ((View) view).setTag(R.id.ker_frame,frame);
            ViewParent p = ((View) view).getParent();
            if(p != null) {
                p.requestLayout();
            }
        }
    }

    public static void viewSetContentSize(Object view,long viewObject,float width,float height) {

    }

    public static void viewSetContentOffset(Object view,long viewObject,float x,float y,boolean animated) {

        if(view instanceof View) {
            ((View) view).scrollTo((int) x,(int) y);
        }

    }

    public static float viewGetContentOffsetX(Object view,long viewObject) {

        if(view instanceof View) {
            return ((View) view).getScrollX();
        }
        return 0;
    }

    public static float viewGetContentOffsetY(Object view,long viewObject) {

        if(view instanceof View) {
            return ((View) view).getScrollY();
        }

        return 0;
    }

    public static void viewAddSubview(Object view,long viewObject,Object subview,int position) {

        ViewGroup contentView = null;

        if(view instanceof IKerView) {
            contentView = ((IKerView) view).contentView();
        } else if(view instanceof View) {
            contentView = ((View) view).findViewById(R.id.ker_contentView);
        }

        if(contentView == null && view instanceof ViewGroup) {
            contentView = (ViewGroup) view;
        }

        if(subview instanceof View) {
            ViewParent p = ((View) subview).getParent();
            if(p != null) {
                if(p == view) {
                    return ;
                }
                if(p instanceof ViewGroup) {
                    ((ViewGroup) p).removeView((View) subview);
                }
            }
            if(contentView != null) {
                if(position ==1) {
                    contentView.addView((View) subview,0);
                } else  {
                    contentView.addView((View) subview);
                }
            }
        }

    }

    public static void viewRemoveView(Object view,long viewObject) {

        if(view instanceof View) {
            ViewParent p = ((View) view).getParent();
            if(p != null && p instanceof ViewGroup) {
                ((ViewGroup) p).removeView((View) view);
            }
        }
    }

    public static void viewEvaluateJavaScript(Object view,long viewObject,String code) {

        if(view instanceof IKerView) {
            ((IKerView) view).evaluateJavaScript(viewObject,code);
        }
    }

    public static void viewSetAttributedText(Object view,long viewObject, long textObject) {

    }

    public static void viewSetImage(Object view,long viewObject, Object image) {

    }

    public static void viewSetGravity(Object view,long viewObject,String gravity) {

    }

    public static void viewSetContent(Object view,long viewObject,String content,String contentType,String basePath) {

        if(view instanceof IKerView) {
            ((IKerView) view).setContent(viewObject,content,contentType,basePath);
        }
    }

    public static int getViewWidth(Object view) {

        if(view instanceof View) {
            Rect frame = (Rect) ((View) view).getTag(R.id.ker_frame);
            if(frame != null) {
                return frame.width;
            }
        }

        return 0;
    }

    public static int getViewHeight(Object view) {

        if(view instanceof View) {
            Rect frame = (Rect) ((View) view).getTag(R.id.ker_frame);
            if(frame != null) {
                return frame.height;
            }
        }

        return 0;
    }

    public static Object createView(App app,String name,long viewConfiguration) {

        Log.d("ker",name);

        View view = null;

        if(name != null && _viewClasss.containsKey(name)) {

            Class<?> isa = _viewClasss.get(name);

            try {
                Constructor<?> init = isa.getConstructor(Context.class);
                view = (View ) init.newInstance(app.activity());
            } catch (Throwable e) {
                Log.d("ker",Log.getStackTraceString(e));
            }
        }

        if(view == null) {
            view = new KerView(app.activity());
        }

        if(view instanceof IKerView){
            ((IKerView) view).setViewConfiguration(viewConfiguration);
        }

        return view;
    }

    public static void runApp(final App app,String URI,final Object query) {

        Package.getPackage(app.activity(), URI, new Package.Callback() {

            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onLoad(Package pkg) {
                App.open(app.activity(),pkg.basePath,pkg.appkey,query);
            }

            @Override
            public void onProgress(long bytes, long total) {

            }
        });
    }

    private static Map<String,Class<?>> _viewClasss = new TreeMap<>();

    public static void addViewClass(String name,Class<?> viewClass) {
        _viewClasss.put(name,viewClass);
    }

    static  {
        _viewClasss.put("UILabel",KerTextView.class);
        _viewClasss.put("UIView",KerView.class);
        _viewClasss.put("KerButton",KerButton.class);
        _viewClasss.put("WKWebView",KerWebView.class);
    }

    public static void openlib() {

        final Handler v = new Handler();

        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                loop();
                v.postDelayed(this,1000 / 60);
            }
        },1000 / 60);

        loop();
    }

    public static void getPackage(App app, final long ptr, String URI) {

        retain(ptr);

        Package.getPackage(app.activity(), URI, new Package.Callback() {

            @Override
            public void onError(Throwable ex) {
                Map<String,Object> data = new TreeMap<>();
                data.put("error",ex.getLocalizedMessage());
                emit(ptr,"error",data);
                release(ptr);
            }

            @Override
            public void onLoad(Package pkg) {
                emit(ptr,"load",new TreeMap<>());
                release(ptr);
            }

            @Override
            public void onProgress(long bytes, long total) {

            }
        });

    }

    public native static void retain(long kerObject);
    public native static void release(long kerObject);
    public native static void setImage(long imageObject,Object image);
    public native static void loop();
    public native static void emit(long ptr,String name,Object data);
    public native static WebViewConfiguration getWebViewConfiguration(long kerObject);
    public native static String absolutePath(long ptr,String path);
}
