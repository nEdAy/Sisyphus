[![API](https://img.shields.io/badge/API-11%2B-brightgreen.svg)](https://android-arsenal.com/api?level=1)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/licenses/MIT)

### [English Document](https://github.com/nEdAy/Sisyphus/blob/master/README.md)

# Sisyphus

Sisyphus 是一个方便开发和测试人员在不重新打包的情况下快速切换Android APP环境或配置的工具。

> **如果你觉得这个工具对你有用，随手给个 Star，让我知道它是对你有帮助的，我会继续更新和维护它。**

### 功能

Sisyphus 就是为了解决以上问题而设计的，它具有以下几个特点：

- 配置简单
- 安全，不泄漏测试环境的值
- 不用重新打包即可一键切换环境
- 支持按模块配置与切换环境
- 支持环境切换通知回调
- 自动生成 `切换` `保存` `获取` 环境的逻辑代码
- 与项目解耦
- ......

### 为什么不用 Gradle

看到这里你可能会想，这些功能我用 Gradle 就能搞定了，为什么要用 Sisyphus 呢？别着急，下面我们来比较一下 Sisyphus 和 Gradle。

|比较内容|Sisyphus|Gradle  Application Id 不同| Gradle Application Id 相同 |
|:-:|:--:|:--:|:--:|
|运行时切换环境|✔️|✖️|✖️|
|切换环境回调|✔️|✖️|✖️|
|切换环境逻辑|自动生成|需要自己实现|需要自己实现|
|n 套环境打包数量| 1个 | n个 | n个|
|多套环境同时安装|✔️|✔️|✖️|
|支付等SDK包名校验|✔️|✖️|✔️|
|多模块环境配置|✔️|✔️|✔️|
|测试环境的值不泄露(加密)|✔️|✔️|✔️|
|……|——|——|——|

这里就先列举这么多，仅 `运行时切换环境` 、`打包数量`、`切换环境回调` 这几个特点就比 Gradle 方便很多，而且 Sisyphus 的接入成本也很低。是不是想试一试了？

### 使用方法

1. 配置项目的 build.gradle

	- java 版
	
	    ```
	    dependencies {
	        ...
            implementation 'androidx.security:security-crypto:1.1.0-alpha03'
	        implementation "cn.neday.sisyphus:sisyphus-ui:$version"
	        annotationProcessor "cn.neday.sisyphus:sisyphus-compiler:$version"
	    }
	    ```
   
    - kotlin 版

        ```
        apply plugin: 'kotlin-kapt'
        ...
        dependencies {
            ...
            implementation 'androidx.security:security-crypto:1.1.0-alpha03'
            implementation "cn.neday.sisyphus:sisyphus-ui:$version"
            kapt "cn.neday.sisyphus:Sisyphus-compiler:$version"
        }
        ```

2. 编写 EnvironmentConfig 文件

    **这个类是 Sisyphus 依赖的核心代码，所有获取、修改环境的逻辑代码都会依赖这个类中被 `@Module` 和 `@Environment` 两个注解标记的类和属性自动生成。**

    > 注意：如果你的项目中使用了 Kotlin，请使用 Java 语言编写 EnvironmentConfig，就像在 GreenDao 中必须使用 Java 语言编写 Entity 类一样。

    ```
    /**
     * 环境配置类</br>
     * <p>
     * ⚠ 建议不要引用该类中的任何子类和成员变量️，一但引用了非正式环境的属性，打包时混淆工具就不会移除该类，导致Value泄漏。</br>
     * <p>
     * 建议将该类中所有被 {@link Module} 和 {@link Environment} 修饰的类或成员变量用 private 修饰，</br>
     * Sisyphus 会在编译期间自动生成相应的 Module_XX 和 Environment_XX 静态常量。</br>
     * 例如：通过 Sisyphus.MODULE_APP 就可以获取到 App 模块下相应的所有环境</br>
     */
    public class EnvironmentConfig {
    
        /**
         * App 的环境 Flavor
         */
        @Module(alias = "App 的环境 Flavor")
        private class Flavor {
            @Environment(value = BuildConfig.FLAVOR, isDefault = true, alias = "原型环境")
            private String proto;
            @Environment(value = "dev", alias = "开发环境")
            private String dev;
            @Environment(value = "qa", alias = "测试环境")
            private String qa;
            @Environment(value = "prod", alias = "生产环境")
            private String prod;
        }
    
    
        /**
         * 地址	                             协议	       方法	    QPS限制	     线路
         * v1.hitokoto.cn	                  HTTPS	      Any	    3.5	        全球
         * international.v1.hitokoto.cn	    HTTPS	      Any	    10	        国外
         */
        @Module(alias = "一言服务地址")
        private class Url {
            @Environment(value = "https://v1.hitokoto.cn/", isDefault = true, alias = "全球线路")
            private String global;
            @Environment(value = "https://international.v1.hitokoto.cn/", alias = "国外线路")
            private String foreign;
        }
    
        /**
         * 打印日志开关
         */
        @Module(alias = "打印日志开关")
        private class PrintLog {
            @Environment(value = "false", isDefault = true, alias = "关闭打印日志")
            private String close;
            @Environment(value = "true", alias = "开启打印日志")
            private String open;
        }
    
        /**
         * 网络日志开关
         */
        @Module(alias = "网络日志开关")
        private class NetworkInspector {
            @Environment(value = "false", isDefault = true, alias = "关闭网络日志")
            private String close;
            @Environment(value = "true", alias = "开启网络日志")
            private String open;
        }
    }
    ```

    - @Module

    	被它修饰的类或接口表示一个模块，编译时会自动生成相应模块的 `getXXEnvironment()` 和 `setXXEnvironment()` 方法。一个被 `@Module` 修饰的类中，可以有 n (n>0) 个被 `@Environment` 修饰的属性，表示该模块中有 n 种环境。

      例如：上面的代码中，有四个类被 `@Module` 修饰，意味着有四个模块，其中 Flavor 模块中，有四个属性被 `@Environment` 修饰，表示该模块只有四种环境；而其他模块有 2 种环境。

      此外 `@Module` 还有一个可选属性 `alias` ，用来指定该模块的别名。该值默认为空字符串。这个属性的主要目的是在切换环境 UI 页面显示中文名称。例如：网络日志开关 模块在切换环境页面中就会分别显示 “关闭网络日志” 和 “开启网络日志”。

    - @Environment

    	被它修饰的属性表示一个环境，必须指定 `value` 的值，此外还有两个可选属性：`isDefault` 和 `alias`。

       - isDefault 是一个 boolean 型的属性，默认为 false，当值为 true 时，它就是所在 Module 的默认环境，以及 App 发布时的环境。**一个 Module 中必须有且只有一个 Environment 的 isDefault 的值为 true，否则编译会失败。** 

    		例如：PrintLog 模块中有两种环境分别是 close 和 open，因为 online 的 isDefault = true，所以它就是默认环境和 App 发布时的环境。

      - alias 和 `@Module` 中的 alias 相似，用于在切换环境的UI页面展示该环境的名字，该值默认为空字符串，如果给它指定非空字符串，则环境的名字就被指定为 `alias` 的值。

      > **再次强调**：一个 Module 中必须有且只有一个 Environment 的 isDefault 的值为 true，否则编译会失败。 

3. 点击菜单栏中的 “Build” -> “Run Gen Project”，等待编译完成。

    到这里整个配置就算完成了，接下来就可以在项目中愉快的获取相应模块的环境地址了。

### 添加入口

手动切换环境当然要有一个页面，这个页面 Sisyphus 已经自动集成了，只需要添加一个入口跳转即可（这个入口只在 Debug 测试等内部版显示）。

例如：在“我的”页面中。
    
```
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
	...
	if (!BuildConfig.DEBUG && BuildConfig.FLAVOR === "prod") {
		// not show in relase && prod
		findViewById(R.id.bt_switch_environment).setVisibility(View.GONE);
		return;
	}
        
	findViewById(R.id.bt_switch_environment).setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			// entrance of switch environment
			SisyphusActivity.launch(getContext());
		}
	});
}
```

你可以使用 Sisyphus 已经提供的 `SisyphusActivity.launch(getContext())` 方法启动；当然你也可以通过 `startActivity(new Intent(getContext(), SisyphusActivity.class))` 启动，看个人喜好了。

### 获取相应模块的环境地址：

```
String appEnvironment = Sisyphus.getAppEnvironment(this);
String musicEnvironment = Sisyphus.getMusicEnvironment(this);
String newsEnvironment = Sisyphus.getNewsEnvironment(this);
```

### 获取相应模块的环境实体类：

```
EnvironmentBean appEnvironmentBean = Sisyphus.getAppEnvironmentBean(this);
EnvironmentBean musicEnvironmentBean = Sisyphus.getMusicEnvironmentBean(this;
EnvironmentBean newsEnvironmentBean = Sisyphus.getNewsEnvironmentBean(this);
```

### 添加监听事件

Sisyphus 支持切换环境回调，你可以通过以下方法添加，需要注意的是不要忘记**在不需要监听环境切换事件时移除监听事件**。

```
public class MainActivity extends AppCompatActivity implements OnEnvironmentChangeListener{

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 添加监听事件
        Sisyphus.addOnEnvironmentChangeListener(this);
    }

    @Override
    public void onEnvironmentChanged(ModuleBean module, EnvironmentBean oldEnvironment, EnvironmentBean newEnvironment) {
        Log.e(TAG, module.getName() + "由" + oldEnvironment.getName() + "环境，Url=" + oldEnvironment.getUrl()
                + ",切换为" + newEnvironment.getName() + "环境，Url=" + newEnvironment.getUrl());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 移除监听事件
        Sisyphus.removeOnEnvironmentChangeListener(this);
    }
}
```

### 切换SDK开发环境

我们在项目中一般会依赖第三方提供的SDK，而且这些SDK也会提供测试环境，如果要在App内切换环境，使用上面的方法就不行了。那该怎么办呢？

例如我们的“直播”模块是引用的SDK，我们可以这样做：

1. 首先在 EnvironmentConfig.java 中配置"直播"模块

   ```
   public class EnvironmentConfig {
        @Module(alias = "直播")
        private class Live {
            @Environment(value = "prod", isDefault = true, alias = "正式")
            private String prod;

            @Environment(value = "test", alias = "测试")
            private String test;
        }
   }
   ```
   > value 在这里只是用来区分环境，要保证同一模块中每个环境的 value 不同。

2. 在 Application 中添加监听

   ```
   Sisyphus.addOnEnvironmentChangeListener(new OnEnvironmentChangeListener() {
        @Override
        public void onEnvironmentChanged(ModuleBean module, EnvironmentBean oldEnvironment, EnvironmentBean newEnvironment) {
           if (module.equals(Sisyphus.MODULE_LIVE)) {
               if (newEnvironment.equals(Sisyphus.LIVE_PROD_ENVIRONMENT)) {
                 // 调用 SDK 切换环境的方法，正式环境
               } else if (newEnvironment.equals(Sisyphus.LIVE_TEST_ENVIRONMENT)) {
                 // 调用 SDK 切换环境的方法，测试环境
               }
           }
        }
   });
   ```

	> 利用 Sisyphus 的环境切换回调，实现切换 SDK 环境。

### 可配置开关

Sisyphus 除了可以用来做环境切换工具，还可以做其他的可配置开关，例如：打印日志的开关。

```
@Module(alias = "日志")
private class Log {
    @Environment(value = "false", isDefault = true, alias = "关闭日志")
    private String closeLog;
    @Environment(value = "true", alias = "开启日志")
    private String openLog;
}

public void loge(Context context, String tag, String msg) {
    if (Sisyphus.getLogEnvironmentBean(context)
            .equals(Sisyphus.LOG_OPENLOG_ENVIRONMENT)) {
        android.util.Log.e(tag, msg);
    }
}
```

### MIT License

Copyright (c) 2021 苏晟

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

