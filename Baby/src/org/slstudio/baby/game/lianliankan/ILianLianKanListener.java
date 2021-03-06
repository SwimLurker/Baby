package org.slstudio.baby.game.lianliankan;

import org.slstudio.baby.game.ICustomizedGameListener;


public interface ILianLianKanListener extends ICustomizedGameListener{
	public static final int BLOCK_SELECTED = 1;
	public static final int BLOCK_UNSELECTED = 2;
	public static final int BLOCK_REMOVED = 3;

	public void onDeadLock(LianLianKan game);
	public void onBlockStateChanged(LianLianKan game, int event, Block block);
	public void onNewHintPathFound(LianLianKan game, Path hintPath);
	public void onGetHintPath(LianLianKan game, Path hintPath);	
	
}
