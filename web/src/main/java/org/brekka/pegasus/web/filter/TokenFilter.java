/**
 * 
 */
package org.brekka.pegasus.web.filter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.brekka.pegasus.core.services.AnonymousService;
import org.brekka.pegasus.core.services.DownloadService;
import org.brekka.xml.pegasus.v1.model.BundleType;
import org.brekka.xml.pegasus.v1.model.FileType;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author Andrew Taylor
 *
 */
public class TokenFilter implements Filter {
    
    private static final Pattern ANON_PATTERN = Pattern.compile("^(?:/([0-9]+))?/([B-DF-HJ-NP-TV-Z0-9]{5})(?:\\.zip|/[^/]+)?$");
    
    private AnonymousService anonymousService;
    
    private DownloadService downloadService;

    /* (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
        downloadService = applicationContext.getBean(DownloadService.class);
        anonymousService = applicationContext.getBean(AnonymousService.class);
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        
        String requestURI = req.getRequestURI();
        requestURI = requestURI.substring(req.getContextPath().length());
        Matcher matcher = ANON_PATTERN.matcher(requestURI);
        if (matcher.matches()) {
            String code = matcher.group(1);
            String token = matcher.group(2);
            if (code == null) {
                // Redirect token to unlock.
                resp.sendRedirect(req.getContextPath() + "/unlock/" + token);
                return;
            } else {
                dispatchBundle(token, code, resp);
            }
        } else {
            chain.doFilter(request, response);
        }

    }

    /**
     * @param token
     * @param code
     * @param resp
     */
    private void dispatchBundle(String token, String code, HttpServletResponse resp) throws ServletException, IOException {
        BundleType bundle = anonymousService.unlock(token, code, null);
        List<FileType> fileList = bundle.getFileList();
        if (fileList.size() == 1) {
            // Just one file, return it.
            FileType fileType = fileList.get(0);
            resp.setHeader("Content-Length", String.valueOf(fileType.getLength()));
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileType.getName() + "\"");
            resp.setContentType(fileType.getMimeType());
            try ( InputStream is = downloadService.download(fileType) ) {
                IOUtils.copy(is, resp.getOutputStream());
            }
        } else {
            // Return a zip archive
            resp.setContentType("application/zip");
            resp.setHeader("Content-Disposition", "attachment; filename=\"Bundle_" + token + ".zip\"");
            ZipOutputStream zos = new ZipOutputStream(resp.getOutputStream());
            zos.setLevel(ZipEntry.STORED);
            for (FileType fileType : fileList) {
                ZipEntry ze = new ZipEntry(fileType.getName());
                ze.setSize(fileType.getLength());
                zos.putNextEntry(ze);
                try ( InputStream is = downloadService.download(fileType) ) {
                    IOUtils.copy(is, zos);
                }
                zos.closeEntry();
            }
            zos.finish();
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

}
