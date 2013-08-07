package session;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: AKutuzov
 * Date: 8/7/13
 * Time: 11:54 AM
 */
public class SessionInCookieFilter implements Filter {
    private static final String COOKIE_NAME = "session";
    private static ThreadLocal<HttpServletRequest> requestLocalStorage = new ThreadLocal<>();
    private static ThreadLocal<HttpServletResponse> responseLocalStorage = new ThreadLocal<>();
    private static SessionDecrypt sessionDecrypt = new SessionDecrypt();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            requestLocalStorage.set((HttpServletRequest) request);
            responseLocalStorage.set((HttpServletResponse) response);
            loadAttributes((HttpServletRequest) request);
            chain.doFilter(request, response);
        } finally {
            responseLocalStorage.remove();
        }
    }

    private void loadAttributes(HttpServletRequest req)  {
        Map<String, Object> attributesFromCookie = getSessionAttributesFromCookie(req);
        if (attributesFromCookie != null) {
            HttpSession session = req.getSession(true);
            for (Map.Entry<String, Object> entry : attributesFromCookie.entrySet()) {
                session.setAttribute(entry.getKey(), entry.getValue());
            }
        }
    }

    private Map<String, Object> getSessionAttributesFromCookie(HttpServletRequest req) {
        Cookie cookie = null;
        for (Cookie c : req.getCookies()) {
            if (c.getName().equals(COOKIE_NAME)) cookie = c;
        }

        Map<String, Object> attributes = null;
        if (cookie != null) {
            attributes = sessionDecrypt.decrypt(cookie.getValue());
        }
        return attributes;
    }

    public static void update() {
        if (requestLocalStorage.get() != null && responseLocalStorage.get() == null) {return;}
        HttpSession session = requestLocalStorage.get().getSession(false);
        if (session != null && session.getAttributeNames().hasMoreElements()) {
            Cookie cookie = new Cookie(COOKIE_NAME, sessionDecrypt.encrypt(session));
            cookie.setPath("/");
            responseLocalStorage.get().addCookie(cookie);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {

    }

}
