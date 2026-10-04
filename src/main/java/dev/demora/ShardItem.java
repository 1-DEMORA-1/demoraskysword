package dev.demora;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public class ShardItem extends Item {
	public ShardItem(Settings settings) {
		super(settings);
	}
	@Override
	public Text getName(ItemStack stack) {
		return super.getName(stack).copy().formatted(Formatting.YELLOW, Formatting.BOLD);
	}
}
