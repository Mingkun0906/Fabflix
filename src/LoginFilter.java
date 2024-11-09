import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class LoginFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {}

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        if (requestURI.endsWith(".css") || requestURI.endsWith(".js") || requestURI.endsWith(".png")) {
            chain.doFilter(request, response);
            return;
        }

        if (requestURI.endsWith("/_dashboard")) {
            httpResponse.sendRedirect(contextPath + "/admin-login.html");
            return;
        }

        if (requestURI.equals(contextPath + "/") ||
                requestURI.endsWith("login.html") ||
                requestURI.endsWith("admin_login.html") || // Allow access to admin login page
                requestURI.endsWith("/login") ||
                requestURI.endsWith("/admin_login")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        boolean loggedIn = (session != null && session.getAttribute("user") != null);

        if (loggedIn)
        {
            chain.doFilter(request, response);
        }
        else
        {
            httpResponse.sendRedirect(contextPath + "/login.html?redirected=true");
        }
    }

    public void destroy() {}
}
