package cn.neday.app.net;

import cn.neday.app.BuildConfig;
import cn.neday.sisyphus.annotation.Environment;
import cn.neday.sisyphus.annotation.Module;

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
     * 地址	                            协议	        方法	    QPS限制	    线路
     * v1.hitokoto.cn	                HTTPS	    Any	    3.5	        全球
     * international.v1.hitokoto.cn	    HTTPS	    Any	    10	        国外
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
