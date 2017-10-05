package com.haulmont.addon.admintools.script_generator;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.OptionsGroup;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

public class GenerateScripts extends AbstractWindow {

    private static final String PARAMETER_SELECTED_ENTITIES = "selectedEntities";
    private static final String GENERATION_MODE = "generationMode";

    @WindowParam(name = PARAMETER_SELECTED_ENTITIES)
    private Collection selectedEntities;

    @Inject
    private OptionsGroup generationMode;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        generationMode.setOptionsEnum(GenerationMode.class);
        generationMode.setValue(GenerationMode.SELECTED_ENTITIES);
    }

    public void windowCommit(){
        openWindow("admintools$generateScriptsResult",
                WindowManager.OpenType.DIALOG,
                ParamsMap.of(
                        PARAMETER_SELECTED_ENTITIES, selectedEntities,
                        GENERATION_MODE, generationMode.getValue()
                )
        );
        windowClose();
    }

    public void windowClose(){
        this.close("windowClose");
    }
}