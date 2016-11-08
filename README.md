从刚进这家公司开始，搭建项目的时候，就开始使用retrofit网络框架了,虽然有考虑过volley,和或者将okhttp封装一层，但是最后还是被retrofit注解式的网络请求方式打败了，代码简介，加上retrofit又完美结合了rxjava非常的方便,好了，让我们来认真的了解一下它吧。


####什么是retrofit?
首先retrofit是一个开源网络库，目前比较火的开源库有volley、okhttp、retrofit,其中 okhttp和retrofit是有square团队开发的,volley是谷歌退出的一款网络库。
补充一下，retrofit中使用的是okhttp,对于retrofit个人理解其实就是对okhttp进行了再次封装，提供了更方便好用，代码更简洁的一个开源库。

####retrofit的使用
 项目的配置
配置依赖: 
* Lambda表达式
* Retrofit2
* GsonConverter
* RxJavaAdapter
* RxAndroid
* Logging
		plugins {
			id "me.tatarka.retrolambda" version "3.2.5"
		}
		// ...
		android {
			// ...
			compileOptions {
				sourceCompatibility JavaVersion.VERSION_1_8;
				targetCompatibility JavaVersion.VERSION_1_8;
			}
		}
		dependencies {
			// ...
			compile 'com.squareup.retrofit2:retrofit:2.0.2'
			compile 'com.squareup.retrofit2:converter-gson:2.0.2'
			compile 'com.squareup.retrofit2:adapter-rxjava:2.0.2'
			compile 'io.reactivex:rxandroid:1.1.0'
			compile 'com.squareup.okhttp3:logging-interceptor:3.0.0-RC1'
		}



首先需要创建一个REST API接口，我们所有的请求都是在借口中写的，因为项目中会有很多类型的借口请求，所以封装了一层。

		public static <T> T creeatApis(Class<T> clazz, String baseUrl, String token) {
		//        Gson gson = new GsonBuilder()
		//                .registerTypeAdapter(clazz, new MyDeserializer<T>())
		//                .registerTypeAdapter(clazz, new MyDeserializer<T>())
		//                .create();
				Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl)//- Base URL: 总是以 /结尾- @Url: 不要以 / 开头
						//你也可以通过实现Converter.Factory接口来创建自己的转换器
						.addConverterFactory(ArbitResponseBodyConvertFactory.create())//添加自定义的解析器
						.client(initClient(token))//Retrofit 2.0上，OKHttp是必须
						.addCallAdapterFactory(RxJavaCallAdapterFactory.create())//Service接口现在可以作为Observable返回了
						.build();
				return retrofit.create(clazz);
			}


每个步骤都加入了相应的注解，相信应该看得懂的哈。
由于项目中的借口返回参数是固定的，所以对解析器也做了一个自定义。关于自定义解析器，大家可以看看[这篇文章](http://www.wangchenlong.org/2016/03/16/1602/use-retrofit-first/)呢，写的很详细呢

公司请求借口分会参数类型都是这样的

		{
			errcode: 0,
			errmsg:"返回提示信息",
			data:object
		}
只有data参数是不确定的，所以做了一层封装,最主要的代码如下

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


我将返回的结果，用一个自定义的model接收了，不确定的data参数，保留了原来json的字符串，更方便灵活
，关于这几个封装的文件已经上传到[github上了](https://github.com/gujingjing/retrofit_converter.git)



		/**
			 * 设置okHttp的初始化
			 */
			public static OkHttpClient initClient(final String token) {

				//用于输出网络请求和结果的 Log，可以配置 level 为 BASIC / HEADERS / BODY
		//        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
		//        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
				OkHttpClient client = new OkHttpClient.Builder()
		//                .addInterceptor(interceptor)
						.retryOnConnectionFailure(true)
						.connectTimeout(15, TimeUnit.SECONDS)//设置超时时间
						.addNetworkInterceptor(new NetWorkInterceptor(token))
						.build();
				//   设置cookie     http://blog.csdn.net/sbsujjbcy/article/details/46895039
				//        client.setCookieHandler()
				return client;
			}


在设置okhttp的时候，可以设置超时时间，请求头等设置,请求头设置有很多种方式，可以参考这几种
[retrofit header通用设置](http://www.jianshu.com/p/aaaa3a9bd46f)
[retrofit header设置](https://futurestud.io/tutorials/retrofit-add-custom-request-header)


需要用的时候直接

    //请求api
    public PlatformApi platformApi = RetrofitMaker.creeatApis(PlatformApi.class, HttpConfig.URL_HOST, "");

有人要问了，你把结果的data还原成json格式还是要解析，是不是很麻烦，哈哈，不要着急，在这里，我又做了一层封装，加上rxJava的使用，在你的present拿到数据之前，我就已经把所有的数据都解析分析完成了，等于是扒了一层皮，把有用的和没有的进行分类，先看看，我的分类处理呢


		/**
			 * 请求网络返回模版-返回固定的model
			 */
			public <T> void linkModel(final Activity context, final boolean showLoading, Observable<ResultJsonModel> observable, final Class<T> tClass, final RetrofitLisener<T> lisener) {
				if (checkNetWork(context, showLoading)) {
					lisener.onfinished(showLoading, false, context);
					return;
				}
				
				observable
						// Subscriber前面执行的代码都是在I/O线程中运行
						.subscribeOn(Schedulers.io())
						// 操作observeOn之后操作主线程中运行.
						.observeOn(AndroidSchedulers.mainThread())
						.unsubscribeOn(Schedulers.io())
						//onNext,onError,onCompleted
						.subscribe(resultJsonModel -> {//onNext
							LogUtils.e("onNext===" + resultJsonModel.toString());

							switch (resultJsonModel.getErrcode()) {
								case  错误码:
									break;
								case 错误码:// 缺少登录Token
									tokenUnuse(getMyApplication);
									break;
								case  错误码://请求成功
									T loginModel1 = new Gson().fromJson(resultJsonModel.toString(), tClass);
									lisener.onSucceed(loginModel1);
									break;
								default://一般的错误
									lisener.onfailed(resultJsonModel);
									break;
							}
						}, throwable -> {//onError
							lisener.onfinished(showLoading,true, getMyApplication);
							LogUtils.e("onError===" + throwable.toString());

						}, () -> {//onCompleted
							lisener.onfinished(showLoading,true, getMyApplication);
						});
			}


* . 在请求数据之前一般是一些网络情况的判断
* . 在拿到数据之后，将数据进行过滤，没用的或者错误的进行处理(拿到的数据又分为三个部分)
       1. 请求成功的数据
       2. 请求失败的数据
       3. 请求结束，最后finally需要做的回调
* . 将拿到的正确数据返回给界面中

哈哈,这样我每个需要请求的借口只需要一行代码就可以搞定了，是不是很方便呢

    linkModel(context, showLoading, platformCApi.creatediary(map), ResultModel.class, lisener);


在此对retrofit的简单封装就完成了呢，以上有什么错误，或者对retrofit更好的处理，可以告知我哦。
