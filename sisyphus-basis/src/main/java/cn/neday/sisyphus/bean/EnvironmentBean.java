package cn.neday.sisyphus.bean;

import java.io.Serializable;
import java.util.Objects;

/**
 * 每个被 {@link cn.neday.sisyphus.annotation.Environment} 标记的属性，在编译时都会在
 * Sisyphus.java 文件中生成与之一一对应的 {@link EnvironmentBean}。
 * <p>
 * Each attribute marked by {@link cn.neday.sisyphus.annotation.Environment}
 * will generate a one-to-one correspondence with {@link EnvironmentBean} in the  Sisyphus.java file at compile time.
 */
public class EnvironmentBean implements Serializable {
    private String name;
    private String alias;
    private String value;
    private ModuleBean module;
    private boolean checked;

    public EnvironmentBean(String name, String value, String alias, ModuleBean module) {
        this(name, value, alias, module, false);
    }

    public EnvironmentBean(String name, String value, String alias, ModuleBean module, boolean checked) {
        this.name = name;
        this.value = value;
        this.alias = alias;
        this.module = module;
        this.checked = checked;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value == null ? "" : value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAlias() {
        return alias == null ? "" : alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public ModuleBean getModule() {
        return module;
    }

    public void setModule(ModuleBean module) {
        this.module = module;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnvironmentBean that = (EnvironmentBean) o;
        if (checked != that.checked) return false;
        if (!Objects.equals(name, that.name)) return false;
        if (!Objects.equals(alias, that.alias)) return false;
        if (!Objects.equals(value, that.value)) return false;
        return Objects.equals(module, that.module);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (alias != null ? alias.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (module != null ? module.hashCode() : 0);
        result = 31 * result + (checked ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EnvironmentBean{" +
                "name='" + name + '\'' +
                ", alias='" + alias + '\'' +
                ", url='" + value + '\'' +
                ", moduleName=" + module.getName() +
                ", moduleAlias=" + module.getAlias() +
                ", checked=" + checked +
                '}';
    }
}

