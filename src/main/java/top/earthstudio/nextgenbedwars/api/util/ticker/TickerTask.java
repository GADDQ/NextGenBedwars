package top.earthstudio.nextgenbedwars.api.util.ticker;

public interface TickerTask extends Runnable{
    default void shutdown() {}
}
