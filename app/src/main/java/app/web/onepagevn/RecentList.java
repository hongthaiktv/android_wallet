package app.web.onepagevn;
import java.util.*;
import java.util.stream.*;

public class RecentList<E> extends ArrayList<E>
{
	private int position = -1;
	private E element = null;
	private boolean isLatest = true;
	
	public E prev() {
		if (size() > 0) {
			if (position == -1 || position == size()) {
				position = size() - 1;
				element = get(position);
				isLatest = false;
			} else if (position > 0) {
				--position;
				element = get(position);
			}
		}
		return element;
	}
	
	public E next() {
		if (size() > 0) {
			if (position == -1 || position == size() - 1) {
				position = size();
				element = null;
				isLatest = true;
			} else if (position < size() - 1) {
				++position;
				element = get(position);
			}
		}
		return element;
	}
	
	public int getPosition() {
		return position;
	}
	
	public E get() {
		return element;
	}
	
	public boolean isLatest() {
		return isLatest;
	}
	
	public boolean isNull() {
		if (size() > 0) return false;
		return true;
	}
	
	public boolean moveToPrevious() {
		if (position == -1 || isLatest || position > 0) {
			prev();
			return true;
		}
		return false;
	}
	
	public E first() {
		position = 0;
		isLatest = false;
		element = get(position);
		return element;
	}
	
	public E last() {
		position = size();
		isLatest = true;
		element = null;
		return element;
	}
	
	public boolean bringToLast(E object) {
		boolean isMoved = false;
		if (remove(object)) isMoved = true;
		add(object);
		last();
		return isMoved;
	}
	
	@Override
	public static <E extends Object> List<E> of()
	{
		// TODO: Implement this method
		return null;
	}

	@Override
	public static <E extends Object> List<E> of(E e1)
	{
		// TODO: Implement this method
		return null;
	}

	@Override
	public static <E extends Object> List<E> of(E e1, E e2)
	{
		// TODO: Implement this method
		return null;
	}

	@Override
	public static <E extends Object> List<E> of(E e1, E e2, E e3)
	{
		// TODO: Implement this method
		return null;
	}

	@Override
	public static <E extends Object> List<E> of(E e1, E e2, E e3, E e4)
	{
		// TODO: Implement this method
		return null;
	}

	@Override
	public static <E extends Object> List<E> of(E e1, E e2, E e3, E e4, E e5)
	{
		// TODO: Implement this method
		return null;
	}

	@Override
	public static <E extends Object> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6)
	{
		// TODO: Implement this method
		return null;
	}

	@Override
	public static <E extends Object> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7)
	{
		// TODO: Implement this method
		return null;
	}

	@Override
	public static <E extends Object> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8)
	{
		// TODO: Implement this method
		return null;
	}

	@Override
	public static <E extends Object> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9)
	{
		// TODO: Implement this method
		return null;
	}

	@Override
	public static <E extends Object> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9, E e10)
	{
		// TODO: Implement this method
		return null;
	}

	@Override
	public static <E extends Object> List<E> of(E[] elements)
	{
		// TODO: Implement this method
		return null;
	}

	@Override
	public Stream<E> stream()
	{
		// TODO: Implement this method
		return null;
	}

	@Override
	public Stream<E> parallelStream()
	{
		// TODO: Implement this method
		return null;
	}
}
