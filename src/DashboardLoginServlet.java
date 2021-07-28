import com.google.gson.JsonObject;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name = "DashboardLoginServlet", urlPatterns = "/api/dashboard-login")
public class DashboardLoginServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb2");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        System.out.println("Got Params");
            // Output stream to STDOUT
        try (Connection conn = dataSource.getConnection()) {

            String query = "SELECT * FROM employees WHERE email = '" + username + "' ";
            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);
            // Perform the query
            ResultSet rs = statement.executeQuery();
            JsonObject responseJsonObject = new JsonObject();
            if(!rs.isBeforeFirst()){
                System.out.print("in isBeforeFirst()");
                responseJsonObject.addProperty("status", "failed");
                responseJsonObject.addProperty("message", "employee " + username + " doesn't exist");
            }

            while(rs.next()) {
                boolean success = false;
                // get the encrypted password from the database
                String encryptedPassword = rs.getString("password");
                // use the same encryptor to compare the user input password with encrypted password stored in DB
                success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
                if(success){
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                    request.getSession().setAttribute("user", new User(username));
                    User currentUser = (User) request.getSession().getAttribute("user");
                }
                else{
                    System.out.print("not match");
                    responseJsonObject.addProperty("status", "failed");
                    responseJsonObject.addProperty("message", "incorrect password");
                }
            }

            rs.close();
            statement.close();
            out.write(responseJsonObject.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            System.out.print("err" + e.getMessage());
            // write error message to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }

}
