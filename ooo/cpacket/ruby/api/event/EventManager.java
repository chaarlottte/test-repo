package ooo.cpacket.ruby.api.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventManager {
	private static final Map<Class<? extends IEvent>, List<MethodData>> REGISTRY_MAP;

	static {
		REGISTRY_MAP = new HashMap<Class<? extends IEvent>, List<MethodData>>();
	}

	public static void register(final Object object) {
		Method[] declaredMethods;
		for (int length = (declaredMethods = object.getClass().getDeclaredMethods()).length, i = 0; i < length; ++i) {
			final Method method = declaredMethods[i];
			if (!isMethodBad(method)) {
				register(method, object);
			}
		}
	}

	public static void register(final Object object, final Class<? extends IEvent> IEventClass) {
		Method[] declaredMethods;
		for (int length = (declaredMethods = object.getClass().getDeclaredMethods()).length, i = 0; i < length; ++i) {
			final Method method = declaredMethods[i];
			if (!isMethodBad(method, IEventClass)) {
				register(method, object);
			}
		}
	}

	public static void unregister(final Object object) {
		for (final List<MethodData> dataList : EventManager.REGISTRY_MAP.values()) {
			for (final MethodData data : dataList) {
				if (data.getSource().equals(object)) {
					dataList.remove(data);
				}
			}
		}
		cleanMap(true);
	}

	public static void unregister(final Object object, final Class<? extends IEvent> IEventClass) {
		if (EventManager.REGISTRY_MAP.containsKey(IEventClass)) {
			for (final MethodData data : EventManager.REGISTRY_MAP.get(IEventClass)) {
				if (data.getSource().equals(object)) {
					EventManager.REGISTRY_MAP.get(IEventClass).remove(data);
				}
			}
			cleanMap(true);
		}
	}

	private static void register(final Method method, final Object object) {
		final Class<? extends IEvent> indexClass = (Class<? extends IEvent>) method.getParameterTypes()[0];
		final MethodData data = new MethodData(object, method, method.getAnnotation(EventImpl.class).value());
		if (!data.getTarget().isAccessible()) {
			data.getTarget().setAccessible(true);
		}
		if (EventManager.REGISTRY_MAP.containsKey(indexClass)) {
			if (!EventManager.REGISTRY_MAP.get(indexClass).contains(data)) {
				EventManager.REGISTRY_MAP.get(indexClass).add(data);
				sortListValue(indexClass);
			}
		} else {
			EventManager.REGISTRY_MAP.put(indexClass, new CopyOnWriteArrayList<MethodData>() {
				private static final long serialVersionUID = 666L;

				{
					this.add(data);
				}
			});
		}
	}

	public static void removeEntry(final Class<? extends IEvent> indexClass) {
		final Iterator<Map.Entry<Class<? extends IEvent>, List<MethodData>>> mapIterator = EventManager.REGISTRY_MAP
				.entrySet().iterator();
		while (mapIterator.hasNext()) {
			if (mapIterator.next().getKey().equals(indexClass)) {
				mapIterator.remove();
				break;
			}
		}
	}

	public static void cleanMap(final boolean onlyEmptyEntries) {
		final Iterator<Map.Entry<Class<? extends IEvent>, List<MethodData>>> mapIterator = EventManager.REGISTRY_MAP
				.entrySet().iterator();
		while (mapIterator.hasNext()) {
			if (!onlyEmptyEntries || mapIterator.next().getValue().isEmpty()) {
				mapIterator.remove();
			}
		}
	}

	private static void sortListValue(final Class<? extends IEvent> indexClass) {
		final List<MethodData> sortedList = new CopyOnWriteArrayList<MethodData>();
		byte[] value_ARRAY;
		for (int length = (value_ARRAY = Priority.VALUE_ARRAY).length, i = 0; i < length; ++i) {
			final byte priority = value_ARRAY[i];
			for (final MethodData data : EventManager.REGISTRY_MAP.get(indexClass)) {
				if (data.getPriority() == priority) {
					sortedList.add(data);
				}
			}
		}
		EventManager.REGISTRY_MAP.put(indexClass, sortedList);
	}

	private static boolean isMethodBad(final Method method) {
		return method.getParameterTypes().length != 1 || !method.isAnnotationPresent(EventImpl.class);
	}

	private static boolean isMethodBad(final Method method, final Class<? extends IEvent> IEventClass) {
		return isMethodBad(method) || !method.getParameterTypes()[0].equals(IEventClass);
	}

	public static final IEvent call(final IEvent IEvent) {
		final List<MethodData> dataList = EventManager.REGISTRY_MAP.get(IEvent.getClass());
		if (dataList != null) {
			{
				for (final MethodData data2 : dataList) {
					invoke(data2, IEvent);
				}
			}
		}
		return IEvent;
	}

	private static void invoke(final MethodData data, final IEvent argument) {
		try {
			data.getTarget().invoke(data.getSource(), argument);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e2) {
			e2.printStackTrace();
		} catch (InvocationTargetException e3) {
			e3.printStackTrace();
		}
	}

	private static final class MethodData {
		private final Object source;
		private final Method target;
		private final byte priority;

		public MethodData(final Object source, final Method target, final byte priority) {
			this.source = source;
			this.target = target;
			this.priority = priority;
		}

		public Object getSource() {
			return this.source;
		}

		public Method getTarget() {
			return this.target;
		}

		public byte getPriority() {
			return this.priority;
		}
	}
}
