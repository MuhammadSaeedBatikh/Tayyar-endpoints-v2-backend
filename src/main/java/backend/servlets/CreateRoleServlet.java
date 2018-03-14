package backend.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import backend.general.UserPrivileges;

/**
 * Created by Muhammad on 08/02/2018.
 */

public class CreateRoleServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserService userService = UserServiceFactory.getUserService();
        String thisUrl = req.getRequestURI();
        boolean admin = userService.isUserAdmin();
        resp.setContentType("text/html");
        if (req.getUserPrincipal() != null) {
            /*if (userService.)
            UserPrivileges.createFirstOwner();
            resp.getWriter().print();*/
        } else {
            resp.getWriter()
                    .println(
                            "<p>Please <a href=\"" + userService.createLoginURL(thisUrl) + "\">sign in</a>.</p>");
        }
    }
}
