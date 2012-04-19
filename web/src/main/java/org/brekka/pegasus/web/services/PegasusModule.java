package org.brekka.pegasus.web.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;

/**
 * 
 * @author Andrew Taylor
 *
 */
public class PegasusModule {


    public static void contributeFactoryDefaults(MappedConfiguration<String, Object> configuration) {
        // The application version number is incorprated into URLs for some
        // assets. Web browsers will cache assets because of the far future expires
        // header. If existing assets are changed, the version number should also
        // change, to force the browser to download new versions. This overrides Tapesty's default
        // (a random hexadecimal number), but may be further overriden by DevelopmentModule or
        // QaModule.
        configuration.override(SymbolConstants.APPLICATION_VERSION, "1.0.0");
    }

    public static void contributeApplicationDefaults(MappedConfiguration<String, Object> configuration) {
        // Contributions to ApplicationDefaults will override any contributions to
        // FactoryDefaults (with the same key). Here we're restricting the supported
        // locales to just "en" (English). As you add localised message catalogs and other assets,
        // you can extend this list of locales (it's a comma separated series of locale names;
        // the first locale name is the default when there's no reasonable match).
        configuration.add(SymbolConstants.SUPPORTED_LOCALES, "en");
        // Disable Tapestry's secure handling in favour of Spring's.
        configuration.add(SymbolConstants.SECURE_ENABLED, "false"); 
    }


    public void contributeIgnoredPathsFilter(Configuration<String> conf) {
        conf.add("/report/download/.+");
    }

}
