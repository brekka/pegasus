package org.brekka.pegasus.web.services;

import java.io.File;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.services.PageResponseRenderer;
import org.apache.tapestry5.internal.services.RequestPageCache;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.ioc.services.ServiceOverride;
import org.apache.tapestry5.services.RequestExceptionHandler;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.upload.internal.services.MultipartDecoderImpl;
import org.apache.tapestry5.upload.services.MultipartDecoder;
import org.apache.tapestry5.upload.services.UploadSymbols;
import org.brekka.commons.maven.ModuleVersion;
import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.paveway.core.services.PavewayService;
import org.brekka.paveway.web.upload.EncryptedFileItemFactory;
import org.brekka.pegasus.core.services.UploadPolicyService;
import org.brekka.pegasus.web.services.impl.PegasusExceptionHandler;
import org.got5.tapestry5.jquery.services.AjaxUploadDecoder;
import org.got5.tapestry5.jquery.services.AjaxUploadDecoderImpl;
import org.slf4j.Logger;

/**
 * 
 * @author Andrew Taylor
 *
 */
public class PegasusModule {

    public static final ModuleVersion APP_VERSION = ModuleVersion.getVersion(
            "org.brekka.pegasus", "pegasus-web", PegasusModule.class.getClassLoader(), ModuleVersion.TIMESTAMP);
    

    public static void contributeFactoryDefaults(MappedConfiguration<String, Object> configuration) {
        // The application version number is incorprated into URLs for some
        // assets. Web browsers will cache assets because of the far future expires
        // header. If existing assets are changed, the version number should also
        // change, to force the browser to download new versions. This overrides Tapesty's default
        // (a random hexadecimal number), but may be further overriden by DevelopmentModule or
        // QaModule.
        configuration.override(SymbolConstants.APPLICATION_VERSION, APP_VERSION.getVersion());
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
        configuration.add(SymbolConstants.PRODUCTION_MODE, "true"); 
        configuration.add(SymbolConstants.COMPACT_JSON, "false"); 
        configuration.add(SymbolConstants.COMBINE_SCRIPTS, "false"); 
        configuration.add(SymbolConstants.MINIFICATION_ENABLED, "false"); 
    }
    

    /**
     * The default FileItemFactory used by the MultipartDecoder is
     * {@link org.apache.commons.fileupload.disk.DiskFileItemFactory}.
     */
    public static EncryptedFileItemFactory buildEncryptedFileItemFactory(
            @Symbol(UploadSymbols.REPOSITORY_THRESHOLD) int repositoryThreshold,
            @Symbol(UploadSymbols.REPOSITORY_LOCATION) String repositoryLocation,
            UploadPolicyService uploadPolicyService, PavewayService pavewayService) {
        return new EncryptedFileItemFactory(repositoryThreshold, 
                new File(repositoryLocation), pavewayService, uploadPolicyService.identifyUploadPolicy());
    }

    @Scope(ScopeConstants.PERTHREAD)
    public static MultipartDecoder buildOverrideMultipartDecoder(PerthreadManager perthreadManager,
            RegistryShutdownHub shutdownHub, UploadPolicyService uploadPolicyService,
            @Symbol(SymbolConstants.CHARSET) String requestEncoding, @Local FileItemFactory fileItemFactory) {
        UploadPolicy policy = uploadPolicyService.identifyUploadPolicy();
        MultipartDecoderImpl multipartDecoder = new MultipartDecoderImpl(fileItemFactory, policy.getMaxSize(), 
                policy.getMaxFileSize(), requestEncoding);
        perthreadManager.addThreadCleanupListener(multipartDecoder);
        return multipartDecoder;
    }

    public static AjaxUploadDecoder buildOverrideAjaxUploadDecoder(@Local FileItemFactory fileItemFactory) {
        return new AjaxUploadDecoderImpl(fileItemFactory);
    }

    @Contribute(ServiceOverride.class)
    public static void setupServiceOverride(MappedConfiguration<Class<?>, Object> configuration,
            @Local MultipartDecoder multipartDecoder, @Local AjaxUploadDecoder ajaxUploadDecoder) {
        configuration.add(MultipartDecoder.class, multipartDecoder);
        configuration.add(AjaxUploadDecoder.class, ajaxUploadDecoder);
    }


    public void contributeIgnoredPathsFilter(Configuration<String> conf) {
        conf.add("/upload.*");
    }
    
    public RequestExceptionHandler buildAppExceptionHandler(
            RequestPageCache pageCache,
            PageResponseRenderer pageResponseRenderer,
            Logger logger,
            @Inject @Symbol(SymbolConstants.EXCEPTION_REPORT_PAGE) String pageName,
            RequestGlobals globals,
            Response response) {
        return new PegasusExceptionHandler(pageCache, pageResponseRenderer, logger, pageName, globals, response);
    }
    
    
    public void contributeServiceOverride(
            MappedConfiguration<Class<?>, Object> configuration,
            @Local
            RequestExceptionHandler handler) {
        configuration.add(RequestExceptionHandler.class, handler);
        
    }

}
