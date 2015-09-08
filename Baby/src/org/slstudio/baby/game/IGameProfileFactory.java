package org.slstudio.baby.game;

public interface IGameProfileFactory <P extends IGameProfile> {
	public abstract P getProfile(int level);
}
