package cn.neday.sisyphus.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 每个被 {@link cn.neday.sisyphus.annotation.Module} 标记的属性，在编译时都会在
 * Sisyphus.java 文件中生成与之一一对应的 {@link ModuleBean}。
 * <p>
 * Each attribute marked by {@link cn.neday.sisyphus.annotation.Module}
 * will generate a one-to-one correspondence with {@link ModuleBean} in the  Sisyphus.java file at compile time.
 */
public class ModuleBean implements Serializable {
    private String name;

    private String alias;

    private List<EnvironmentBean> environments;

    public ModuleBean(String name, String alias) {
        this(name, alias, new ArrayList<EnvironmentBean>());
    }

    public ModuleBean(String name, String alias, List<EnvironmentBean> environments) {
        this.name = name;
        this.alias = alias;
        this.environments = environments;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias == null ? "" : alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public List<EnvironmentBean> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<EnvironmentBean> environments) {
        this.environments = environments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleBean that = (ModuleBean) o;
        if (!Objects.equals(name, that.name)) return false;
        if (!Objects.equals(alias, that.alias)) return false;
        return Objects.equals(environments, that.environments);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (alias != null ? alias.hashCode() : 0);
        result = 31 * result + (environments != null ? environments.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ModuleBean{" +
                "name='" + name + '\'' +
                ", alias='" + alias + '\'' +
                ", environments=" + environments +
                '}';
    }
}