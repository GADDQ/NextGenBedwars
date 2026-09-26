package top.earthstudio.nextgenbedwars.api.game;

/**
 * 子系统构造模板（实例化配方）
 * 接收当前房间的 GameInstance 上下文，返回该房间专属的全新子系统实例
 */
@FunctionalInterface
public interface SubSystemConstructor<T extends IGameSubSystem> {
    T construct(GameInstance gameInstance);
}