package mezz.jei.input;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;

import mezz.jei.config.Constants;
import mezz.jei.config.IWorldConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.HoverChecker;
import mezz.jei.gui.elements.DrawableNineSliceTexture;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.overlay.IIngredientGridSource;
import org.lwjgl.glfw.GLFW;

public class GuiTextFieldFilter extends GuiTextField {
	private static final int MAX_HISTORY = 100;
	private static final int maxSearchLength = 128;
	private static final List<String> history = new LinkedList<>();

	private final HoverChecker hoverChecker;
	private final IIngredientGridSource ingredientSource;
	private final IWorldConfig worldConfig;
	private boolean previousKeyboardRepeatEnabled;

	private final DrawableNineSliceTexture background;

	public GuiTextFieldFilter(int componentId, IIngredientGridSource ingredientSource, IWorldConfig worldConfig) {
		super(componentId, Minecraft.getInstance().fontRenderer, 0, 0, 0, 0);
		this.worldConfig = worldConfig;

		setMaxStringLength(maxSearchLength);
		this.hoverChecker = new HoverChecker(0, 0, 0, 0);
		this.ingredientSource = ingredientSource;

		this.background = new DrawableNineSliceTexture(Constants.RECIPE_BACKGROUND, 95, 182, 95, 20, 4, 4, 4, 4);
	}

	public void updateBounds(Rectangle area) {
		this.x = area.x;
		this.y = area.y;
		this.width = area.width;
		this.height = area.height;
		this.background.setWidth(area.width);
		this.background.setHeight(area.height);
		this.hoverChecker.updateBounds(area.y, area.y + area.height, area.x, area.x + area.width);
		setSelectionPos(getCursorPosition());
	}

	public void update() {
		String filterText = worldConfig.getFilterText();
		if (!filterText.equals(getText())) {
			setText(filterText);
		}
		List<IIngredientListElement> ingredientList = ingredientSource.getIngredientList(filterText);
		if (ingredientList.size() == 0) {
			setTextColor(Color.red.getRGB());
		} else {
			setTextColor(Color.white.getRGB());
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
		if (!handled && !history.isEmpty()) {
			if (keyCode == GLFW.GLFW_KEY_UP) {
				String currentText = getText();
				int historyIndex = history.indexOf(currentText);
				if (historyIndex < 0) {
					if (saveHistory()) {
						historyIndex = history.size() - 1;
					} else {
						historyIndex = history.size();
					}
				}
				if (historyIndex > 0) {
					String historyString = history.get(historyIndex - 1);
					setText(historyString);
					handled = true;
				}
			} else if (keyCode == GLFW.GLFW_KEY_DOWN) {
				String currentText = getText();
				int historyIndex = history.indexOf(currentText);
				if (historyIndex >= 0) {
					String historyString;
					if (historyIndex + 1 < history.size()) {
						historyString = history.get(historyIndex + 1);
					} else {
						historyString = "";
					}
					setText(historyString);
					handled = true;
				}
			} else if (KeyBindings.isEnterKey(keyCode)) {
				saveHistory();
			}
		}
		return handled;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return hoverChecker.checkHover(mouseX, mouseY);
	}

	public boolean handleMouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (mouseButton == 1) {
			setText("");
			return worldConfig.setFilterText("");
		} else {
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
		return false;
	}

	@Override
	public void setFocused(boolean keyboardFocus) {
		final boolean previousFocus = isFocused();
		super.setFocused(keyboardFocus);

		if (previousFocus != keyboardFocus) {
			Minecraft minecraft = Minecraft.getInstance();
			if (keyboardFocus) {
				previousKeyboardRepeatEnabled = minecraft.keyboardListener.repeatEventsEnabled;
				minecraft.keyboardListener.enableRepeatEvents(true);
			} else {
				minecraft.keyboardListener.enableRepeatEvents(previousKeyboardRepeatEnabled);
			}

			saveHistory();
		}
	}

	private boolean saveHistory() {
		String text = getText();
		if (text.length() > 0) {
			history.remove(text);
			history.add(text);
			if (history.size() > MAX_HISTORY) {
				history.remove(0);
			}
			return true;
		}
		return false;
	}

	// begin hack to draw our own background texture instead of the ugly default one
	private boolean isDrawing = false;

	@Override
	public boolean getEnableBackgroundDrawing() {
		if (this.isDrawing) {
			GlStateManager.color4f(1, 1, 1, 1);
			background.draw(this.x, this.y);
		}
		return false;
	}

	@Override
	public int getWidth() {
		return this.width - 8;
	}

	@Override
	public void drawTextField(int mouseX, int mouseY, float partialTicks) {
		this.isDrawing = true;
		super.drawTextField(mouseX, mouseY, partialTicks);
		this.isDrawing = false;
	}
	// end background hack
}
