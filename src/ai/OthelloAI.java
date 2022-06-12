package ai;

import java.util.Map;

import game.Block;

public interface OthelloAI {
	public Block choice();
	public int getProbability();
	public void stopThinking();
	default Map<String, Object> getDataMap(){
		return null;
	}
}
