package org.weasis.dicom.hernia;

import java.util.Hashtable;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.Insertable;
import org.weasis.core.api.gui.Insertable.Type;
import org.weasis.core.api.gui.InsertableFactory;

/**
 * OSGi factory for the HerniaToolBar.
 * Registers the hernia toolbar with the DICOM 2D viewer.
 */
@org.osgi.service.component.annotations.Component(
    service = InsertableFactory.class,
    property = {"org.weasis.dicom.viewer2d.View2dContainer=true"})
public class HerniaToolbarFactory implements InsertableFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(HerniaToolbarFactory.class);

    private HerniaToolBar toolBar = null;

    @Override
    public Type getType() {
        return Type.TOOLBAR;
    }

    @Override
    public Insertable createInstance(Hashtable<String, Object> properties) {
        if (toolBar == null) {
            toolBar = new HerniaToolBar(500);
        }
        return toolBar;
    }

    @Override
    public void dispose(Insertable tool) {
        if (toolBar != null) {
            toolBar = null;
        }
    }

    @Override
    public boolean isComponentCreatedByThisFactory(Insertable tool) {
        return tool instanceof HerniaToolBar;
    }

    @Activate
    protected void activate(ComponentContext context) {
        LOGGER.info("Activate HerniaCT Toolbar");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        LOGGER.info("Deactivate HerniaCT Toolbar");
    }
}
