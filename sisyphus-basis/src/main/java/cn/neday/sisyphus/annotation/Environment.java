package cn.neday.sisyphus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 被 {@link Environment} 标记的属性表示一个环境
 * <p>
 * An attribute marked by {@link Environment} represents an environment
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface Environment {
    /**
     * @return 当前环境的具体的值，必须指定具体的值
     * <p>
     * Specific value of the current environment, you must specify a specific value
     */
    String value();

    /**
     * 一个 {@link Module} 中必须有且只有一个 {@link Environment} 的 isDefault 的值为 true，否则会编译失败。
     * <p>
     * There must be one and only one {@link Environment} of isDefault in a {@link Module} with a value of true,
     * otherwise the compilation will fail.
     *
     * @return 默认返回 false，当返回 true 时，当前 {@link Environment} 就是所属 {@link Module} 的默认环境，以及 App 编译时的环境。
     * <p>
     * By default, false is returned. When true is returned,
     * the current {@link Environment} is the default environment for the {@link Module}
     * and the environment when the app was compiled by default.
     */
    boolean isDefault() default false;

    /**
     * @return 用来指定当前 {@link Environment} 的别名
     * <p>
     * Used to specify the current alias for {@link Environment}
     */
    String alias() default "";
}
