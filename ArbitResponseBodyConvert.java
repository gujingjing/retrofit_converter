package ibesteeth.beizhi.lib.retrofit.convert;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ibesteeth.beizhi.lib.tools.LogUtils;
import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * 作者：iBesteeth on 2016/6/22 17:58
 * 邮箱：gujingjing@ibesteeth.com
 */
public class ArbitResponseBodyConvert implements Converter<ResponseBody, ResultJsonModel> {

    @Override
    public ResultJsonModel convert(ResponseBody value) throws IOException {

        ResultJsonModel resultJsonModel = new ResultJsonModel();

        try {
            JSONObject jsonObject = new JSONObject(value.string());
            int errcode = jsonObject.getInt("errcode");
            String errmsg = jsonObject.getString("errmsg");
            String data = jsonObject.get("data").toString();

            resultJsonModel = new ResultJsonModel();
            resultJsonModel.setData(data);
            resultJsonModel.setErrcode(errcode);
            resultJsonModel.setErrmsg(errmsg);

//                LogUtils.e("resultJsonModel==="+resultJsonModel.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e("JSONException===" + e.toString());
        }
        return resultJsonModel;
    }
}
