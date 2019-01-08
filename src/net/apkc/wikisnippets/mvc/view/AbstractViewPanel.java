package net.apkc.wikisnippets.mvc.view;

// Beans
import java.beans.PropertyChangeEvent;

// Swing
import javax.swing.JPanel;

// WikiSnippets
import net.apkc.wikisnippets.mvc.controller.AbstractController;
import net.apkc.wikisnippets.mvc.model.AbstractModel;

public abstract class AbstractViewPanel extends JPanel {

    /**
     * Creates the component.
     */
    abstract AbstractViewPanel createComponent();

    /**
     * Called from the controller to receive a change notification.
     *
     * @param evt The change event.
     */
    public abstract void modelPropertyChange(PropertyChangeEvent evt);

    /**
     * Configure this Component.
     */
    public abstract void configure();

    /**
     * Makes the GUI visible.
     *
     * @param visible TRUE if the component should be visible, FALSE otherwise.
     *
     * @return The view panel.
     */
    public abstract AbstractViewPanel markVisibility(boolean visible);

    /**
     * Returns a controller associated with this Frame.
     *
     * @param id The unique ID of the controller.
     *
     * @return The controller.
     */
    public abstract AbstractController getController(byte id);

    /**
     * Returns a model associated with this Frame.
     *
     * @param id The unique ID of the model.
     *
     * @return The model.
     */
    public abstract AbstractModel getModel(byte id);

    /**
     * Returns a view associated with this Frame.
     *
     * @param id The unique ID of the view.
     *
     * @return The view.
     */
    public abstract AbstractViewPanel getView(byte id);
}
