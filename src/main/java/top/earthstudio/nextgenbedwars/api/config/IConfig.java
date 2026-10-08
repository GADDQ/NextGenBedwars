package top.earthstudio.nextgenbedwars.api.config;

import java.util.List;

public interface IConfig<T> {
    List<T> getValues();
    void release();
    default void save(List<T> values) {}
}