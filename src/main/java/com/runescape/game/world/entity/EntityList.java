package com.runescape.game.world.entity;

import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class EntityList<T extends Entity> implements Iterable<T> {

	public T[] entities;

	public int lowestFreeIndex;

	private int size;

	@SuppressWarnings("unchecked")
	public EntityList(int capacity, boolean player) {
		entities = (T[]) (player ? new Player[capacity] : new NPC[capacity]);
	}

	public boolean add(T entity) {
		synchronized (this) {
			entity.setIndex(lowestFreeIndex + 1);
			entities[lowestFreeIndex] = entity;
			size++;
			for (int i = lowestFreeIndex + 1; i < entities.length; i++) {
				if (entities[i] == null) {
					lowestFreeIndex = i;
					break;
				}
			}
			return true;
		}
	}

	public void remove(T entity) {
		synchronized (this) {
			int listIndex = entity.getIndex() - 1;
			entities[listIndex] = null;
			size--;
			if (listIndex < lowestFreeIndex) { lowestFreeIndex = listIndex; }
		}
	}

	public T get(int index) {
		if (index >= entities.length || index == 0) { return null; }
		return entities[index - 1];
	}

	public boolean contains(T entity) {
		return entity.getIndex() != 0 && entities[entity.getIndex() - 1] == entity;
	}

	public int size() {
		return size;
	}

	@Override
	public Iterator<T> iterator() {
		return new EntityIterator();
	}

	public Stream<T> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	private final class EntityIterator implements Iterator<T> {

		/**
		 * The previous index of this iterator.
		 */
		private int previousIndex = -1;

		/**
		 * The current index of this iterator.
		 */
		private int index = 0;

		@Override
		public boolean hasNext() {
			for (int i = index; i < entities.length; i++) {
				if (entities[i] != null) {
					index = i;
					return true;
				}
			}
			return false;
		}

		@Override
		public T next() {
			T entity = null;
			for (int i = index; i < entities.length; i++) {
				if (entities[i] != null) {
					entity = (T) entities[i];
					index = i;
					break;
				}
			}
			if (entity == null) { throw new NoSuchElementException(); }
			previousIndex = index;
			index++;
			return entity;
		}

		@Override
		public void remove() {
			if (previousIndex == -1) {
				throw new IllegalStateException();
			}
			EntityList.this.remove((T) entities[previousIndex]);
			previousIndex = -1;
		}

	}

}
