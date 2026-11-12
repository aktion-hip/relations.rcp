/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2018, Benno Luthiger
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 ***************************************************************************/
package org.elbe.relations.internal.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.actions.RelationsPreferences;
import org.elbe.relations.services.ICloudProviderConfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jakarta.inject.Inject;

/**
 * The preference page to configure the active cloud provider.
 * <p>
 * The page can always be left: an incomplete configuration of the active provider
 * is reported as error message only. On save, a provider is activated only if its
 * configuration is valid.
 * </p>
 *
 * @author lbenno
 */
public class CloudConfigPrefPage extends AbstractPreferencePage {
    private static final String TMPL_KEY = "cloud_%s_pref"; //$NON-NLS-1$

    private final List<ConfigWrapper> mappings = new ArrayList<>(5);
    private Button noneRadio;

    @Inject
    private IEclipseContext context;

    @Override
    protected Control createContents(final Composite parent) {
        final IEclipsePreferences store = RelationsPreferences.getPreferences();
        final ActiveWrapper activePref = createActive(store);

        this.noneRadio = new Button(parent, SWT.RADIO);
        this.noneRadio.setText(RelationsMessages.getString("CloudConfigPrefPage.btn.none.lbl")); //$NON-NLS-1$
        this.noneRadio.addSelectionListener(
                SelectionListener.widgetSelectedAdapter(
                        this::handleActivationChange));

        final CloudConfigRegistry configRegistry = this.context
                .get(CloudConfigRegistry.class);
        final List<ICloudProviderConfig> configurations = configRegistry
                .getConfigurations();
        for (final ICloudProviderConfig configuration : configurations) {
            createGroup(configuration, parent, activePref);
        }

        // 'None' is selected if no (registered) provider is stored as active
        this.noneRadio.setSelection(getSelected().isEmpty());
        refreshMessage();
        return parent;
    }

    private void createGroup(final ICloudProviderConfig configuration,
            final Composite parent, final ActiveWrapper activePref) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText(configuration.getName());
        group.setLayoutData(
                GridDataFactory.swtDefaults().grab(true, true)
                .align(SWT.FILL, SWT.BEGINNING).create());
        group.setLayout(
                GridLayoutFactory.swtDefaults().numColumns(2).create());
        // each provider gets its own callback; the message is always evaluated
        // from the selected provider, so signals of inactive providers have no effect
        configuration.createConfigContents(group, isValid -> refreshMessage());

        new Label(group, SWT.NONE).setText(RelationsMessages.getString("CloudConfigPrefPage.btn.active.lbl")); //$NON-NLS-1$
        final Button radioBtn = new Button(group, SWT.RADIO);
        radioBtn.setData(configuration.getName());
        radioBtn.addSelectionListener(
                SelectionListener.widgetSelectedAdapter(
                        this::handleActivationChange));

        final ConfigWrapper config = new ConfigWrapper(configuration)
                .setRadio(radioBtn);
        this.mappings.add(config);
        configuration.initialize(activePref.getValues(getKey(config)));

        // if active group, initialize widgets with values from preferences
        if (activePref.isActive(configuration.getName())) {
            radioBtn.setSelection(true);
        } else {
            configuration.setEnabled(false);
        }
    }

    private void handleActivationChange(final SelectionEvent event) {
        final Button source = (Button) event.widget;
        if (!source.getSelection()) {
            // the deselected radio of a pair, nothing to do
            return;
        }
        final boolean isNone = source == this.noneRadio;
        this.noneRadio.setSelection(isNone);
        for (final ConfigWrapper config : this.mappings) {
            config.setEnabled(!isNone && config.hasRadio(source));
        }
        refreshMessage();
    }

    /**
     * Sets or clears the error message, evaluating the selected provider only.
     */
    private void refreshMessage() {
        setErrorMessage(getSelected().filter(config -> !config.isValid())
                .map(config -> String.format(
                        RelationsMessages.getString("CloudConfigPrefPage.msg.incomplete"), //$NON-NLS-1$
                        config.getName()))
                .orElse(null));
    }

    private Optional<ConfigWrapper> getSelected() {
        return this.mappings.stream().filter(ConfigWrapper::isActive).findFirst();
    }

    /**
     * The page can always be left, whatever its content.
     */
    @Override
    public boolean okToLeave() {
        return true;
    }

    @Override
    public boolean performOk() {
        savePreferences();
        return super.performOk();
    }

    @Override
    protected void performApply() {
        savePreferences();
        super.performApply();
    }

    private boolean savePreferences() {
        final IEclipsePreferences store = RelationsPreferences.getPreferences();
        boolean hasActive = false;
        for (final ConfigWrapper config : this.mappings) {
            hasActive |= store(store, config);
        }
        if (!hasActive) {
            store.remove(RelationsConstants.PREFS_CLOUD_ACTIVE);
        }
        return true;
    }

    // methods for interacting with the preference store

    /**
     * Stores the provider's configuration (or removes it, if empty) and stores
     * the provider as active if it is selected and its configuration is valid.
     *
     * @return boolean <code>true</code> if the provider has been stored as active
     */
    protected boolean store(final IEclipsePreferences store,
            final ConfigWrapper config) {
        final Optional<JsonObject> json = config.getConfig();
        if (json.isPresent()) {
            if (isEmpty(json.get())) {
                store.remove(getKey(config));
            } else {
                store.put(getKey(config), new GsonBuilder().create().toJson(json.get()));
            }
        }
        if (config.isActive() && config.isValid()) {
            store.put(RelationsConstants.PREFS_CLOUD_ACTIVE, config.getName());
            return true;
        }
        return false;
    }

    /**
     * A configuration is empty if it has no properties or every property is
     * <code>null</code> or an empty string.
     */
    private static boolean isEmpty(final JsonObject json) {
        for (final Map.Entry<String, JsonElement> entry : json.entrySet()) {
            final JsonElement value = entry.getValue();
            if (value == null || value.isJsonNull()) {
                continue;
            }
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
                    && value.getAsString().isEmpty()) {
                continue;
            }
            return false;
        }
        return true;
    }

    protected ActiveWrapper createActive(final IEclipsePreferences store) {
        return new ActiveWrapper(store,
                store.get(RelationsConstants.PREFS_CLOUD_ACTIVE, "")); //$NON-NLS-1$
    }

    private String getKey(final ConfigWrapper config) {
        return getKey(config.getName());
    }

    /**
     * Convenience method: converts the cloud provider's name to a key used in
     * the preferences store.
     *
     * @param name
     *            String the cloud provider's name
     * @return String the generated key
     */
    public static String getKey(final String name) {
        return String.format(TMPL_KEY, name.replace(" ", "").toLowerCase()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    // ---

    /**
     * Helper object to interact with the <code>ICloudProviderConfig</code>
     */
    private static class ConfigWrapper {
        private final ICloudProviderConfig configuration;
        private Button isActiveRadio;

        protected ConfigWrapper(final ICloudProviderConfig configuration) {
            this.configuration = configuration;
        }

        /**
         * @return boolean <code>true</code> if the configuration is valid; a
         *         provider failing to evaluate its input counts as invalid
         */
        protected boolean isValid() {
            try {
                return this.configuration.isValid();
            } catch (final RuntimeException exc) {
                return false;
            }
        }

        protected ConfigWrapper setRadio(final Button isActiveRadio) {
            this.isActiveRadio = isActiveRadio;
            return this;
        }

        protected boolean hasRadio(final Button radio) {
            return this.isActiveRadio == radio;
        }

        protected boolean isActive() {
            return this.isActiveRadio != null && this.isActiveRadio.getSelection();
        }

        protected String getName() {
            return this.configuration.getName();
        }

        /**
         * @return {@link Optional} the provider's configuration, empty if the
         *         provider cannot convert its input to JSON
         */
        protected Optional<JsonObject> getConfig() {
            try {
                return Optional.ofNullable(this.configuration.getConfig());
            } catch (final RuntimeException exc) {
                return Optional.empty();
            }
        }

        protected void setEnabled(final boolean enabled) {
            this.isActiveRadio.setSelection(enabled);
            this.configuration.setEnabled(enabled);
        }

    }

    /**
     * Helper object to interact with the preference store.
     */
    private static class ActiveWrapper {
        private final IEclipsePreferences store;
        private final String nameOfActive;

        protected ActiveWrapper(final IEclipsePreferences store,
                final String nameOfActive) {
            this.store = store;
            this.nameOfActive = nameOfActive;
        }

        protected boolean isActive(final String name) {
            return this.nameOfActive.equals(name);
        }

        protected JsonObject getValues(final String key) {
            final String jsonOfValues = this.store.get(key, "{}"); //$NON-NLS-1$
            return new Gson().fromJson(
                    jsonOfValues.isEmpty() ? "{}" : jsonOfValues, //$NON-NLS-1$
                            JsonObject.class);
        }

    }

}
