package mezz.jei.gui.overlay;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.Internal;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.config.IWorldConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.GuiHelper;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.util.Translator;
import org.lwjgl.glfw.GLFW;

public class ConfigButton extends GuiIconToggleButton {
	public static ConfigButton create(IngredientListOverlay parent, IWorldConfig worldConfig) {
		GuiHelper guiHelper = Internal.getHelpers().getGuiHelper();
		return new ConfigButton(guiHelper.getConfigButtonIcon(), guiHelper.getConfigButtonCheatIcon(), parent, worldConfig);
	}

	private final IngredientListOverlay parent;
	private final IWorldConfig worldConfig;

	private ConfigButton(IDrawable disabledIcon, IDrawable enabledIcon, IngredientListOverlay parent, IWorldConfig worldConfig) {
		super(disabledIcon, enabledIcon);
		this.parent = parent;
		this.worldConfig = worldConfig;
	}

	@Override
	protected void getTooltips(List<String> tooltip) {
		tooltip.add(Translator.translateToLocal("jei.tooltip.config"));
		if (!worldConfig.isOverlayEnabled()) {
			tooltip.add(TextFormatting.GOLD + Translator.translateToLocal("jei.tooltip.ingredient.list.disabled"));
			tooltip.add(TextFormatting.GOLD + Translator.translateToLocalFormatted("jei.tooltip.ingredient.list.disabled.how.to.fix", KeyBindings.toggleOverlay.func_197978_k()));
		} else if (!parent.isListDisplayed()) {
			tooltip.add(TextFormatting.GOLD + Translator.translateToLocal("jei.tooltip.not.enough.space"));
		}
		if (worldConfig.isCheatItemsEnabled()) {
			tooltip.add(TextFormatting.RED + Translator.translateToLocal("jei.tooltip.cheat.mode.button.enabled"));
			KeyBinding toggleCheatMode = KeyBindings.toggleCheatMode;
			if (toggleCheatMode.getKey().getKeyCode() != 0) {
				tooltip.add(TextFormatting.RED + Translator.translateToLocalFormatted("jei.tooltip.cheat.mode.how.to.disable.hotkey", toggleCheatMode.func_197978_k()));
			} else {
				String controlKeyLocalization = Translator.translateToLocal(Minecraft.IS_RUNNING_ON_MAC ? "key.jei.ctrl.mac" : "key.jei.ctrl");
				tooltip.add(TextFormatting.RED + Translator.translateToLocalFormatted("jei.tooltip.cheat.mode.how.to.disable.no.hotkey", controlKeyLocalization));
			}
		}
	}

	@Override
	protected boolean isIconToggledOn() {
		return worldConfig.isCheatItemsEnabled();
	}

	@Override
	protected boolean onMouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (worldConfig.isOverlayEnabled()) {

			if (InputMappings.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) || InputMappings.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL)) {
				worldConfig.toggleCheatItemsEnabled();
			} else {
				Minecraft minecraft = Minecraft.getInstance();
				if (minecraft.currentScreen != null) {
//					GuiScreen configScreen = new JEIModConfigGui(minecraft.currentScreen);
//					parent.updateScreen(configScreen, false);
//					minecraft.displayGuiScreen(configScreen);
				}
			}
			return true;
		}
		return false;
	}
}
