package org.slstudio.baby.game.lianliankan;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class Path {
private Queue<Block> path;
	
	public Path(){
		path = new LinkedList<Block>();
	}
	
	public Path(Path p){
		path = new LinkedList<Block>();
		path.addAll(p.path);
	}
	
	public boolean contains(Block block){
		for(Block b: path){
			if(b.sameAs(block)){
				return true;
			}
		}
		return false;
	}
	
	public Block pollLastBlock(){
		return path.poll();
	}
	
	public void addToPath(Block block){
		path.add(block);
	}
	
	public int getPathLength(){
		return path.size();
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for(Block b: path){
			sb.append("Path step||");
			sb.append(b.toString());
			sb.append("\r\n");
		}
		return sb.toString();
	}

	public List<Block> getBlockList() {
		List<Block> blocks = new ArrayList<Block>();
		blocks.addAll(path);
		return blocks;
	}
	
	public Block getStartBlock(){
		List<Block> blocks = new ArrayList<Block>();
		blocks.addAll(path);
		return blocks.get(0);
	}
	
	public Block getEndBlock(){
		List<Block> blocks = new ArrayList<Block>();
		blocks.addAll(path);
		return blocks.get(blocks.size()-1);
	}
}
