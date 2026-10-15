package top.earthstudio.nextgenbedwars.api.config;

import java.util.List;

public interface IConfig<T> {
    void save(List<T> values);
    List<T> getValues();
    void release();
}
