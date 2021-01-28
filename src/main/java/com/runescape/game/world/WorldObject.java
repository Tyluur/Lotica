package com.runescape.game.world;

import com.runescape.cache.loaders.ObjectDefinitions;

@SuppressWarnings("serial")
public class WorldObject extends WorldTile {

	private final int originalId;
	private int id;
	private int type;
	private int rotation;
	private int life;

	private boolean spawned;

	public WorldObject(int id, int type, int rotation, WorldTile tile) {
		super(tile.getX(), tile.getY(), tile.getPlane());
		this.originalId = this.id = id;
		this.type = type;
		this.rotation = rotation;
		this.life = 1;
		this.spawned = false;
	}

	public WorldObject(int id, int type, int rotation, int x, int y, int plane) {
		super(x, y, plane);
		this.originalId = this.id = id;
		this.type = type;
		this.rotation = rotation;
		this.life = 1;
		this.spawned = false;
	}

	public WorldObject(int id, int type, int rotation, int x, int y, int plane, int life) {
		super(x, y, plane);
		this.originalId = this.id = id;
		this.type = type;
		this.rotation = rotation;
		this.life = life;
		this.spawned = false;
	}

	public WorldObject(WorldObject object) {
		super(object.getX(), object.getY(), object.getPlane());
		this.originalId = this.id = object.id;
		this.type = object.type;
		this.rotation = object.rotation;
		this.life = object.life;
		this.spawned = false;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public int getRotation() {
		return rotation;
	}

	public void setRotation(int rotation) {
		this.rotation = rotation;
	}

	public int getLife() {
		return life;
	}

	public void setLife(int life) {
		this.life = life;
	}

	public void decrementObjectLife() {
		this.life--;
	}

	public ObjectDefinitions getDefinitions() {
		return ObjectDefinitions.getObjectDefinitions(id);
	}

	@Override
	public String toString() {
		return "[id=" + id + ", type=" + type + ", rotation=" + rotation + ", name=" + getDefinitions().name + ", location=" + getWorldTile() + "]";
	}

    public int getOriginalId() {
        return this.originalId;
    }

    public boolean isSpawned() {
        return this.spawned;
    }

    public void setSpawned(boolean spawned) {
        this.spawned = spawned;
    }
}
