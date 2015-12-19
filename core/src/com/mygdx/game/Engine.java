package com.mygdx.game;


import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.LongMap;

public class Engine {

	private Array<Entity> entities;
	private LongMap<Entity> entitiesById;

	private long nextEntityId = 1;

	public Engine(){
		entities = new Array<Entity>(false, 16);
		entitiesById = new LongMap<Entity>();
	}

	private long obtainEntityId() {
		return nextEntityId++;
	}

	/**
	 * Adds an entity to this Engine.
	 * This will throw an IllegalArgumentException if the given entity
	 * was already registered with an engine.
	 */
	public void addEntity(Entity entity){
		if (entity.uuid != 0L) {
			throw new IllegalArgumentException("Entity is already registered with an Engine id = " + entity.uuid);
		}

		entity.uuid = obtainEntityId();

		entities.add(entity);
		entitiesById.put(entity.getId(), entity);
	}

	/**
	 * Removes an entity from this Engine.
	 */
	public void removeEntity(Entity entity){
		boolean removed = false;

		entities.removeValue(entity, true);

		if (entitiesById.remove(entity.getId()) == entity) {
			removed = true;
		}

		if (removed) {
			entity.uuid = 0L;
		}
	}

	public Entity getEntity(long id) {
		return entitiesById.get(id);
	}

}
