[![API](https://img.shields.io/badge/API-11%2B-brightgreen.svg)](https://android-arsenal.com/api?level=11)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/licenses/MIT)

### [中文文档](https://github.com/nEdAy/Sisyphus/blob/master/README_CN.md)

# Sisyphus

Sisyphus is a tool that makes it easy for developers and testers to quickly switch Android APP environments or configurations without repackaging.

> ** If you think this tool is useful for you, send a Star to let me know it is useful for you, I will continue to update and maintain it. **

### Feature

- Easy to configure
- Safe, does not leak test environment value
- Switch environments with one click without repackaging
- Support configuration and switching environment by module
- Support for callbacks when the environment switches
- Automatically generate `toggle` `save` `get` logic for the environment
- Decoupling from the project
- ......

### Sisyphus VS Gradle

Now you might think: "These functions I can do with Gradle, why use Sisyphus?" Let's compare Sisyphus and Gradle.

|Compare content|Sisyphus|Gradle with different Application Id| Gradle with same Application Id|
|:-:|:--:|:--:|:--:|
|Switch environment at runtime|✔️|✖️|✖️|
|Callback when switching environment|✔️|✖️|✖️|
|Switching environment logic|Automatic generated|Need to implement by yourself|Need to implement by yourself|
|Number of n environment packages| 1 | n | n|
|Install multiple sets of environments at the same time|✔️|✔️|✖️|
|Payment and other SDK package name verification|✔️|✖️|✔️|
|Multi-module environment configuration|✔️|✔️|✔️|
|Do not leak test environment value(by encrypting)|✔️|✔️|✔️|
|……|——|——|——|

### Instruction manual

1. Configuring the project's build.gradle

 Latest Version：

 module|sisyphus-ui|sisyphus-compiler
:---:|:---:|:---:
version|[ ![Download](https://api.bintray.com/packages/neday/sisyphus/sisyphus-ui/images/download.svg) ](https://bintray.com/neday/sisyphus/sisyphus-ui/_latestVersion) | [ ![Download](https://api.bintray.com/packages/neday/sisyphus/sisyphus-compiler/images/download.svg) ](https://bintray.com/neday/sisyphus/sisyphus-compiler/_latestVersion)

 - java project

 ```
 dependencies {
         ...
         implementation 'androidx.security:security-crypto:1.1.0-alpha02'
         implementation "com.hccn.sisyphus:sisyphus-ui:$version"
         annotationProcessor "com.hccn.sisyphus:sisyphus-compiler:$version"
 }
 ```

 - kotlin project

 ```
 apply plugin: 'kotlin-kapt'
 ...
 dependencies {
	     ...
            implementation 'androidx.security:security-crypto:1.1.0-alpha02'
	        implementation "com.hccn.sisyphus:sisyphus-ui:$version"
	        kapt "com.hccn.sisyphus:Sisyphus-compiler:$version"
 }
 ```

2. Write the EnvironmentConfig file

    **This class is the core code that the Sisyphus relies on. All the logic that gets and modifies the environment will be automatically generated based on the classes and properties of the class marked with the `@Module` and `@Environment` annotations.**

    > Note: If you are using Kotlin in your project, write EnvironmentConfig in the Java language, just as you must write the Entity class in the Java language in GreenDao.

    ```
    /**
     * Environment configuration class</br>
     *
     * ⚠ It is not recommended to reference any subclasses and member variables in the class. 
     * Once the properties of the informal environment are referenced, 
     * the Proguard tool will not remove the class when it is packaged, 
     * causing the test url to leak.</br>
     *
     * It is recommended that all classes or member variables in this class that are decorated with {@link Module} and {@link Environment} be privately decorated.</br>
     * The Sisyphus automatically generates the corresponding Module_XX and Environment_XX static constants during compilation.</br>
     * For example: Sisyphus.MODULE_APP can get all the corresponding environments under the App module.</br>
     */
    public class EnvironmentConfig {
    
        /**
         * App Environment Flavor
         */
        @Module(alias = "App Environment Flavor")
        private class Flavor {
            @Environment(value = BuildConfig.FLAVOR, isDefault = true, alias = "Prototype Environment")
            private String proto;
            @Environment(value = "dev", alias = "Development Environment")
            private String dev;
            @Environment(value = "qa", alias = "Test Environment")
            private String qa;
            @Environment(value = "prod", alias = "Production Environment")
            private String prod;
        }
    
    
        /**
         * Address                          Protocol    Methods    QPS Limit    Line
         * v1.hitokoto.cn	                  HTTPS	      Any	       3.5	        全球
         * international.v1.hitokoto.cn	    HTTPS	      Any	       10	          国外
         */
        @Module(alias = "Hitokoto Server Host Address")
        private class Url {
            @Environment(value = "https://v1.hitokoto.cn/", isDefault = true, alias = "Global Line")
            private String global;
            @Environment(value = "https://international.v1.hitokoto.cn/", alias = "Foreign Line")
            private String foreign;
        }
    
        /**
         * Print Log Switch
         */
        @Module(alias = "Print Log Switch")
        private class PrintLog {
            @Environment(value = "false", isDefault = true, alias = "Close Print Log")
            private String close;
            @Environment(value = "true", alias = "Open Print Log")
            private String open;
        }
    
        /**
         * Network Log Switch
         */
        @Module(alias = "Network Log Switch")
        private class NetworkInspector {
            @Environment(value = "false", isDefault = true, alias = "Close Network Log")
            private String close;
            @Environment(value = "true", alias = "Open Network Log")
            private String open;
        }
    }
    ```

    - @Module

    	A class or interface decorated with {@link Module} represents a module that automatically generates the getXXEnvironment() and setXXEnvironment() methods of the corresponding module at compile time. A class or interface decorated with {@link Module} can have n (n>0) attributes modified by {@link Environment}, indicating that there are n environments in the module.

      For example, in the above code, there are four classes modified by `@Module`, which means there are four modules, of which only one attribute in the App module is decorated with `@Environment`, indicating that the module has four environments; And there are 2 environments in the others module.

      In addition, `@Module` also has an optional attribute `alias` that specifies the alias of the module. This value defaults to an empty string. The main purpose of this property is to display the Chinese name on the Switch Environment UI page. For example, the NetworkInspector modules will display “Close Network Log” and “Open Network Log” respectively in the Switch Environment page.

    - @Environment

    	An attribute marked by {@link Environment} represents an environment，Specific value of the current environment, you must specify a specific value. There are also two optional attributes: `isDefault` and `alias`.

       - isDefault 

         By default, false is returned. When true is returned, the current {@link Environment} is the default environment for the {@link Module} and the environment when the app was released. **There must be one and only one {@link Environment} of isDefault in a {@link Module} with a value of true,
    otherwise the compilation will fail.** 

         For example, there are two environments in the PrintLog module: close and open, because close isDefault = true, so it is the default environment and the environment when the app is released.

       - alias

         Similar to alias in `@Module`, it is used to display the name of the environment in the UI page of the switching environment. The value defaults to an empty string. If a non-empty string is specified for it, the name of the environment is specified as the value of alias.

     > **Re-emphasize**: There must be one and only one Environment's isDefault value in a Module, otherwise the compilation will fail.

3. Click "Build" -> "Rebuild Project" in the menu bar and wait for the compilation to complete.

    Now that the configuration is complete, you can enjoy it！
    
### Add the entry for EnvrionmentSwtichActivity

The manual switching environment must have an interactive page. The Sisyphus has been automatically integrated. You only need to add an entry (this entry is recommended only for internal versions such as Debug tests).

For example: on the "My" page.

```
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
	...
	if (!BuildConfig.DEBUG && BuildConfig.FLAVOR === "prod") {
		// not show in relase && prod
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

You can use the `SisyphusActivity.launch(getContext())` method already provided by Sisyphus to start it; of course you can also start it with `startActivity(newContext(getContext(), SisyphusActivity.class))`.It depends on you.

### Get the environment url of the corresponding module：

```
String appEnvironment = Sisyphus.getAppEnvironment(this);
String musicEnvironment = Sisyphus.getMusicEnvironment(this);
String newsEnvironment = Sisyphus.getNewsEnvironment(this);
```

### Get the environment entity class of the corresponding module：

```
EnvironmentBean appEnvironmentBean = Sisyphus.getAppEnvironmentBean(this);
EnvironmentBean musicEnvironmentBean = Sisyphus.getMusicEnvironmentBean(this);
EnvironmentBean newsEnvironmentBean = Sisyphus.getNewsEnvironmentBean(this);
```

### AddOnEnvironmentChangeListener

Sisyphus supports switching environment callbacks. You can add them by the following methods. **Note that you should not forget to remove the listener event when you don't need to listen for environment switching events**.

```
public class MainActivity extends AppCompatActivity implements OnEnvironmentChangeListener{

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Sisyphus.addOnEnvironmentChangeListener(this);
    }

    @Override
    public void onEnvironmentChanged(ModuleBean module, EnvironmentBean oldEnvironment, EnvironmentBean newEnvironment) {
        Log.e(TAG, module.getName() + "oleEnvironment=" + oldEnvironment.getName() + "，oldUrl=" + oldEnvironment.getUrl()
         + ",newNevironment=" + newEnvironment.getName() + "，newUrl=" + newEnvironment.getUrl());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Sisyphus.removeOnEnvironmentChangeListener(this);
    }
}
```

### Switch the SDK Environment

We generally rely on SDKs provided by third parties in the project, and these SDKs also provide a test environment. If you want to switch environments in the app, you can't use the above method. What should I do?

For example, our "live" module is a referenced SDK, we can do like this:

1. First configure the "live" module in EnvironmentConfig.java

   ```
   public class EnvironmentConfig {
        private class Live {
            @Environment(value = "prod", isDefault = true)
            private String prod;

            @Environment(value = "test")
            private String test;
        }
   }
   ```
   > The value is only used to distinguish the environment here, to ensure that the value of each environment in the same module is different.

2. Add a listener to the Application

   ```
   Sisyphus.addOnEnvironmentChangeListener(new OnEnvironmentChangeListener() {
        @Override
        public void onEnvironmentChanged(ModuleBean module, EnvironmentBean oldEnvironment, EnvironmentBean newEnvironment) {
           if (module.equals(Sisyphus.MODULE_LIVE)) {
               if (newEnvironment.equals(Sisyphus.LIVE_PROD_ENVIRONMENT)) {
                 // Call the SDK to switch the environment, the formal environment
               } else if (newEnvironment.equals(Sisyphus.LIVE_TEST_ENVIRONMENT)) {
                 // Call the SDK to switch the environment, the test environment
               }
           }
        }
   });
   ```

	> Switch the SDK environment with Sisyphus's environment switch callback.

### Configurable switch

In addition to being used as an environment switching tool, Sisyphus can also be used to make other configurable switches, such as the switch to print logs. 

```
@Module
private class Log {
    @Environment(value = "false", isDefault = true)
    private String closeLog;
    @Environment(value = "true")
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