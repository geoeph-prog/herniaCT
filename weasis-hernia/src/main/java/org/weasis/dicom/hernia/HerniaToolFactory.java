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
 * OSGi factory for the HerniaTool panel.
 * Registers the hernia evaluation panel with the DICOM 2D viewer container.
 */
@org.osgi.service.component.annotations.Component(
    service = InsertableFactory.class,
    property = {"org.weasis.dicom.viewer2d.View2dContainer=true"})
public class HerniaToolFactory implements InsertableFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(HerniaToolFactory.class);

    private HerniaTool toolPane = null;

    @Override
    public Type getType() {
        return Type.TOOL;
    }

    @Override
    public Insertable createInstance(Hashtable<String, Object> properties) {
        if (toolPane == null) {
            toolPane = new HerniaTool(getType());
        }
        return toolPane;
    }

    @Override
    public void dispose(Insertable tool) {
        if (toolPane != null) {
            toolPane = null;
        }
    }

    @Override
    public boolean isComponentCreatedByThisFactory(Insertable tool) {
        return tool instanceof HerniaTool;
    }

    @Activate
    protected void activate(ComponentContext context) {
        LOGGER.info("Activate HerniaCT Evaluation panel");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        LOGGER.info("Deactivate HerniaCT Evaluation panel");
    }
}
