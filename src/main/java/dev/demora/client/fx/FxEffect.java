package dev.demora.client.fx;

public abstract class FxEffect {
	protected int age;
	protected int life;
	private boolean dead;
	protected FxEffect(int life) {
		this.life = life;
	}
	final boolean tickEffect() {
		if (dead) {
			return false;
		}
		tick();
		age++;
		return !dead && age < life;
	}
	public boolean alive() {
		return !dead && age < life;
	}
	protected void kill() {
		dead = true;
	}
	protected float time(Draw d) {
		return age + d.tickDelta;
	}
	protected void tick() {
	}
	public abstract void render(Draw d);

}
