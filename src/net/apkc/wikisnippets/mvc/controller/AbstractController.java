package net.apkc.wikisnippets.mvc.controller;

// Beans
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

// Lang
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

// Log4j
import org.apache.log4j.Logger;

// Util
import java.util.ArrayList;

// WikiSnippets
import net.apkc.wikisnippets.mvc.model.AbstractModel;
import net.apkc.wikisnippets.mvc.view.AbstractViewPanel;

public abstract class AbstractController implements PropertyChangeListener {

    private static final Logger LOG = Logger.getLogger(AbstractController.class);
    private ArrayList<AbstractViewPanel> registeredViews;
    private ArrayList<AbstractModel> registeredModels;

    public AbstractController() {
        registeredViews = new ArrayList<>();
        registeredModels = new ArrayList<>();
    }

    public void addModel(AbstractModel model) {
        registeredModels.add(model);
        model.addPropertyChangeListener(this);
    }

    public void removeModel(AbstractModel model) {
        registeredModels.remove(model);
        model.removePropertyChangeListener(this);
    }

    public void addView(AbstractViewPanel view) {
        registeredViews.add(view);
    }

    public void removeView(AbstractViewPanel view) {
        registeredViews.remove(view);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        registeredViews.stream().forEach((view) -> {
            view.modelPropertyChange(evt);
        });
    }

    protected void setModelProperty(String propertyName, Object newValue) {
        registeredModels.stream().forEach((model) -> {
            try {
                Method method = model.getClass().getMethod("set" + propertyName, new Class[]{newValue.getClass()});
                method.invoke(model, newValue);
            }
            catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                LOG.error("Error guardando propiedad en el modelo. Error: " + e.toString(), e);
            }
        });
    }
}
