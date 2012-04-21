/**
 * 
 */
package org.brekka.pegasus.web.support;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.brekka.paveway.core.model.FileBuilder;

/**
 * @author Andrew Taylor
 *
 */
public class CompletedFileBuilders {
    public static final String SESSION_KEY = "COMPLETED_FILE_BUILDERS";
    

    private transient List<FileBuilder> fileBuilderList;

    /**
     * @param fileBuilder
     */
    public synchronized void add(FileBuilder fileBuilder) {
        list().add(fileBuilder);
    }
    
    public synchronized List<FileBuilder> retrieveAll() {
        List<FileBuilder> list = list();
        this.fileBuilderList = null;
        return list;
    }
    
    protected List<FileBuilder> list() {
        if (this.fileBuilderList == null) {
            this.fileBuilderList = new ArrayList<>();
        }
        return this.fileBuilderList;
    }
    

    /**
     * @param req
     * @return
     */
    public static CompletedFileBuilders getCompletedFileBuilders(HttpServletRequest req, boolean createIfNotExist) {
        HttpSession session = req.getSession(createIfNotExist);
        CompletedFileBuilders completed = (CompletedFileBuilders) session.getAttribute(CompletedFileBuilders.SESSION_KEY);
        if (completed == null) {
            completed = new CompletedFileBuilders();
            session.setAttribute(CompletedFileBuilders.SESSION_KEY, completed);
        }
        return completed;
    }
    
}
