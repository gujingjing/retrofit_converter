package ibesteeth.beizhi.lib.retrofit;

import java.io.IOException;
import java.lang.reflect.Field;

import ibesteeth.beizhi.lib.tools.LogUtils;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 作者：gjj on 2016/1/18 13:36
 * 邮箱：Gujj512@163.com
 * 网络拦截器-设置请求头
 */
public class NetWorkInterceptor implements Interceptor {
    private String token="";
    public NetWorkInterceptor(){

    }
    public NetWorkInterceptor(String token){
        this.token=token;
    }
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
//        if (!token.equals("")) {
//            request=request.newBuilder().addHeader("Authorization", token).build();
//        }
        //设置请求返回数据类型-addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
//        request=request.newBuilder().addHeader("Content-Type","application/json; charset=UTF-8").build();
//        MediaType mediaType = request.body().contentType();
//        try {
//            Field field = mediaType.getClass().getDeclaredField("mediaType");
//            field.setAccessible(true);
//            field.set(mediaType, "application/json");
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//        Request request = chain.request()
//                .newBuilder()
////                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
//                .addHeader("Content-Type", "application/json; charset=UTF-8")
//                .addHeader("Accept-Encoding", "gzip, deflate")
//                .addHeader("Connection", "keep-alive")
//                .addHeader("Accept", "*/*")
////                .addHeader("Cookie", "add cookies here")
//                .build();

        LogUtils.e("requestUrl==="+request.url().toString());//输出requestUrl
        LogUtils.e("requestHeader==="+request.headers().toString());//输出requestUrl
//        LogUtils.e("requestBody==="+request.body());//输出request请求内容

        Response response=chain.proceed(request);
        return response;
    }
}
