package ibesteeth.beizhi.lib.retrofit.convert;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * 作者：iBesteeth on 2016/6/22 17:57
 * 邮箱：gujingjing@ibesteeth.com
 */
public class ArbitResponseBodyConvertFactory extends Converter.Factory {

    /**
     * Create an instance using a default {@link Gson} instance for conversion. Encoding to JSON and
     * decoding from JSON (when no charset is specified by a header) will use UTF-8.
     */
    public static ArbitResponseBodyConvertFactory create() {
        return create(new Gson());
    }

    /**
     * Create an instance using {@code gson} for conversion. Encoding to JSON and
     * decoding from JSON (when no charset is specified by a header) will use UTF-8.
     */
    public static ArbitResponseBodyConvertFactory create(Gson gson) {
        return new ArbitResponseBodyConvertFactory(gson);
    }

    private final Gson gson;

    private ArbitResponseBodyConvertFactory(Gson gson) {
        if (gson == null) throw new NullPointerException("gson == null");
        this.gson = gson;
    }

    /**
     * 使用这个可以传入对象解析
     */
    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                          Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new ArbitRequestBodyConvert<>(gson, adapter);
    }


//    @Override
//    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
//        return super.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
//    }

    /**
     * 转换服务器数据
     */
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
//        return super.responseBodyConverter(type, annotations, retrofit);
        return new ArbitResponseBodyConvert();
    }

}
