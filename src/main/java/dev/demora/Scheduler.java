package dev.demora;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import java.util.ArrayList;
import java.util.List;
public final class Scheduler {
	@FunctionalInterface
	public interface Task {
		boolean tick(int tick);
	}
	private static final class Entry {
		final Task task;
		int age;
		int delay;

		Entry(Task task, int delay) {
			this.task = task;
			this.delay = delay;
		}
	}

	private static final List<Entry> TASKS = new ArrayList<>();
	private static final List<Entry> PENDING = new ArrayList<>();
	public static void run(Task task) {
		PENDING.add(new Entry(task, 0));
	}
	public static void later(int delay, Runnable action) {
		PENDING.add(new Entry(t -> {
			action.run();
			return false;
		}, delay));
	}
	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			TASKS.addAll(PENDING);
			PENDING.clear();
			TASKS.removeIf(e -> {
				if (e.delay > 0) {
					e.delay--;
					return false;
				}
				return !e.task.tick(e.age++);
			});
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			TASKS.clear();
			PENDING.clear();
		});
	}

	private Scheduler() {
	}
}
