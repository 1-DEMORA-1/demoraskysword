package dev.demora.mixin;

import dev.demora.DemoraSkySword;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(CraftingScreenHandler.class)
public abstract class CraftingScreenHandlerMixin {
	@Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
	private void demora$ritualOnShiftClick(PlayerEntity player, int index, CallbackInfoReturnable<ItemStack> cir) {
		if (index != 0) {
			return;
		}
		Slot slot = ((CraftingScreenHandler) (Object) this).slots.get(0);
		if (!slot.getStack().isOf(DemoraSkySword.SKY_SWORD)) {
			return;
		}
		ItemStack taken = slot.takeStack(slot.getStack().getCount());
		slot.onTakeItem(player, taken);
		cir.setReturnValue(ItemStack.EMPTY);
	}
}
