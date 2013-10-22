package ml.core.gui.core;

import java.util.ArrayList;
import java.util.List;

import ml.core.gui.GuiRenderUtils;
import ml.core.gui.core.style.GuiStyle;
import ml.core.gui.event.EventFocusLost;
import ml.core.gui.event.GuiEvent;
import ml.core.vec.Vector2i;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class GuiElement {

	public GuiElement parentObject;
	protected List<GuiElement> childObjects = new ArrayList<GuiElement>();
	private Vector2i position;
	private Vector2i size;
	public GuiStyle style;
	
	public GuiElement(GuiElement parent) {
		parentObject = parent;
		if (parent != null)
			parent.addChild(this);
		setPosition(new Vector2i());
		setSize(new Vector2i());
	}
	
	public Vector2i getPosition() {
		return position;
	}

	public void setPosition(Vector2i position) {
		this.position = position;
	}

	public void clearChildren() {
		childObjects.clear();
	}
	
	public void removeChild(GuiElement chld) {
		if (childObjects.contains(chld))
			childObjects.remove(chld);
	}
	
	public void addChild(GuiElement chld) {
		if (!childObjects.contains(chld))
			childObjects.add(chld);
		chld.parentObject = this;
	}
	
	public GuiElement getParent() {
		return parentObject;
	}
	
	public boolean isTopParentElem() {
		return this instanceof TopParentGuiElement && getParent()==null;
	}
	
	public TopParentGuiElement getTopParent() {
		if (isTopParentElem()) return (TopParentGuiElement)this;
		if (getParent() == null) return null;
		return getParent().getTopParent();
	}
	
	public List<GuiElement> getDescendants() {
		List<GuiElement> lst = new ArrayList<GuiElement>();
		for (GuiElement c : childObjects) {
			lst.add(c);
			lst.addAll(c.getDescendants());
		}
		return lst;
	}
	
	/**
	 * Returns whether the supplied element is an ancestor of this element
	 * @param ans
	 * @return
	 */
	public boolean isAncestor(GuiElement ans) {
		return ans==getParent() || (getParent() != null && getParent().isAncestor(ans));
	}
	
	public boolean isDescendant(GuiElement des) {
		return des.isAncestor(this);
	}
	
	/**
	 * @return The Side the element is instantiated on
	 */
	public Side getSide() {
		return getTopParent().getSide();
	}
	
	public Vector2i getSize() { return size; }
	
	public void setSize(Vector2i size) {
		this.size = size;
	}
	
	public void setSize(int w, int h) {
		setSize(new Vector2i(w, h));
	}
	
	public Vector2i getAbsolutePosition() {
		if (!isTopParentElem()) return getParent().getAbsolutePosition().add(getPosition());
		return getPosition().copy();
	}
	
	public Vector2i getLocalMousePos() {
		return localizeGlobal(getTopParent().gmousePos);
	}
	
	public Vector2i localizeGlobal(Vector2i g) {
		return g.copy().minus(getAbsolutePosition());
	}
	
	/**
	 * Localizes a point that is presently localized to the parent element.
	 */
	public Vector2i localizeParent(Vector2i g) {
		return g.copy().minus(getPosition());
	}
	
	public GuiEvent injectEvent(GuiEvent evt, boolean injectAtTop) {
		if (injectAtTop) {
			return getTopParent().injectEvent(evt, false);
		} else {
			onReceiveEvent(evt);
		}
		return evt;
	}
	
	public GuiEvent injectEvent(GuiEvent evt) { return injectEvent(evt, true); }
	
	/**
	 * First receiver and relayer of events. Should only be overridden in very special cases.
	 * Use {@link #handleEvent(GuiEvent)} instead
	 * @param evt
	 */
	public void onReceiveEvent(GuiEvent evt) {
		handleEvent(evt);
		
		if (evt.propogate) {
			for (GuiElement el : childObjects) {
				el.onReceiveEvent(evt);
			}
		}
	}
	
	public GuiElement findElementAtLocal(Vector2i pos) {
		for (GuiElement el : childObjects) {
			if (el.pointInElement(pos)) {
				GuiElement sel = el.findElementAtLocal(pos.copy().minus(el.getPosition()));
				if (sel != null)
					return sel;
			}
		}
		return this;
	}
	
	/**
	 * Point is localized to the parent element
	 */
	public boolean pointInElement(Vector2i pos) {
		return (pos.x>=getPosition().x && pos.y>=getPosition().y &&
				pos.x<=getPosition().x+getSize().x && pos.y<=getPosition().y+getSize().y);
	}
	
	public void handleEvent(GuiEvent evt) {}
	
	/**
	 * Steal the focus from the currently focused element.
	 * Will fire {@link EventFocusLost} at the currently focused element if it is not null.
	 */
	public void takeFocus() {
		TopParentGuiElement top = getTopParent();
		if (top.focusedElement != null)
			injectEvent(new EventFocusLost(top.focusedElement));
		top.focusedElement = this;
	}
	
	/**
	 * Remove focus from <b>this</b> element, leaving no element in focus
	 */
	public void dropFocus() {
		TopParentGuiElement top = getTopParent();
		if (top.focusedElement == this) {
			injectEvent(new EventFocusLost(top.focusedElement));
			top.focusedElement = null;
		}
	}
	
	public boolean hasFocus() {
		return getTopParent().focusedElement == this;
	}
	
	public boolean hasHover() {
		return getTopParent().hoverElement == this;
	}
	
	public boolean treeHasHover() {
		return hasHover() || (getTopParent().hoverElement != null && getTopParent().hoverElement.isAncestor(this));
	}
	
	/**
	 * The equivalent of {@link GuiScreen#updateScreen()}. Will only be called client-side, once per tick
	 */
	public void guiTick() {
		for (GuiElement el : childObjects) {
			el.guiTick();
		}
	}
	
	protected void bindTexture(ResourceLocation res) {
		getTopParent().getGui().getMinecraft().getTextureManager().bindTexture(res);
	}

	/*
	 * NB: These aren't necessary, but are nice for clean subclass code.
	 */
	
	/**
	 * Your matrix will be localized to the parent element, so you need to shift by your local position.
	 */
	@SideOnly(Side.CLIENT)
	public void drawBackground() {}
	
	/**
	 * Your matrix will be localized to the parent element, so you need to shift by your local position.
	 */
	//@SideOnly(Side.CLIENT)
	//public void drawForeground() {}
	
	/**
	 * Your matrix will be localized to the parent element, so you need to shift by your local position.
	 */
	@SideOnly(Side.CLIENT)
	public void drawOverlay() {}
	
	/**
	 * Always make a super call or a call to drawChilds() as your last call. It will render children.<br/>
	 * Your matrix will be localized to the parent element, so you need to shift by your local position.</br>
	 * You can also just override draw[Background|Foreground|Overlay]() instead
	 */
	@SideOnly(Side.CLIENT)
	public void drawElement(RenderStage stage) {
		switch (stage) {
		case Background:
			drawBackground();
			break;
		//case Foreground:
			//drawForeground();
			//break;
		case Overlay:
			drawOverlay();
			break;
		}
		drawChilds(stage);
	}
	
	/**
	 * Called to draw children of the element. Automatically called if you don't
	 * override {@link #drawElement(RenderStage)} or if you include a super call in your overriding method
	 * @param stage
	 */
	@SideOnly(Side.CLIENT)
	protected void drawChilds(RenderStage stage) {
		Vector2i pos = getPosition();
		for (GuiElement el : childObjects) {
			GL11.glPushMatrix();
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glDisable(GL11.GL_LIGHTING);

			GL11.glTranslatef(pos.x, pos.y, 0.0F);
			el.drawElement(stage);
			GL11.glPopMatrix();
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void bindStyleTexture() {
		GuiStyle stl = getStyle();
		bindTexture(stl.resourceLoc);
	}
	
	@SideOnly(Side.CLIENT)
	public void drawStyledObject(int x, int y, String key, int w, int h) {
		GuiStyle stl = getStyle();
		bindTexture(stl.resourceLoc);
		GuiRenderUtils.drawTexturedModelRectFromIcon(x, y, stl.getIconFor(key), w, h);
	}
	
	@SideOnly(Side.CLIENT)
	public GuiStyle getStyle() {
		if (style == null) return getParent().getStyle();
		return style;
	}
	
	@SideOnly(Side.CLIENT)
	public void setStyle(GuiStyle stl) {
		for (GuiElement element : childObjects) {
			if (element.style == null || element.style == this.style) {
				element.setStyle(stl);
			}
		}
		this.style = stl;
	}
	
	@SideOnly(Side.CLIENT)
	public MLGuiClient getGui() {
		return getParent().getGui();
	}
	
	@SideOnly(Side.CLIENT)
	public static enum RenderStage {
		Background,
		//Foreground,
		Overlay,
		SlotInventory;
	}
	
}