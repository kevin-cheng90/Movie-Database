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

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
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
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");

        System.out.println("gRecaptchaResponse= " + gRecaptchaResponse);
        if(!request.getHeader("User-Agent").toLowerCase().contains("android") && gRecaptchaResponse.equals("")){
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("status", "failed");
            responseJsonObject.addProperty("message", "Please fill out reCaptcha.");
            out.write(responseJsonObject.toString());
            // set response status to 500 (Internal Server Error)
            response.setStatus(200);
            out.close();
        }
        else{

            if (!request.getHeader("User-Agent").toLowerCase().contains("android"))
            {
                try {
                    RecaptchaVerifyUtils.verify(gRecaptchaResponse);
                    System.out.println("Verified");
                } catch (Exception e) {
                    JsonObject responseJsonObject = new JsonObject();
                    responseJsonObject.addProperty("status", "failed");
                    responseJsonObject.addProperty("message", "Verification failed. Please try again.");
                    out.write(responseJsonObject.toString());
                    response.setStatus(200);
                    out.close();
                }
            }
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            try (Connection conn = dataSource.getConnection()) {
                String query = "SELECT * FROM customers c WHERE c.email = '" + username + "' ";
                // Declare our statement
                PreparedStatement statement = conn.prepareStatement(query);
                // Perform the query
                ResultSet rs = statement.executeQuery();
                JsonObject responseJsonObject = new JsonObject();
                if(!rs.isBeforeFirst()){
                    responseJsonObject.addProperty("status", "failed");
                    responseJsonObject.addProperty("message", "user " + username + " doesn't exist");
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
                        responseJsonObject.addProperty("status", "failed");
                        responseJsonObject.addProperty("message", "incorrect password");
                    }
                }

                rs.close();
                statement.close();
                System.out.println("LOGIN DONE");
                out.write(responseJsonObject.toString());
                // set response status to 200 (OK)
                response.setStatus(200);

            } catch (Exception e) {
                System.out.print("err " + e.getMessage());
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
}