import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


/**
 * This IndexServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/index.
 */
@WebServlet(name = "ConfirmationServlet", urlPatterns = "/api/confirmation")
public class ConfirmationServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb2");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        try (Connection conn = dataSource.getConnection()) {
            String query = "select *, count(*) as quantity  from  sales s, movies m where s.saleDate=Date(now()) and s.movieID = m.id group by m.id";
            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);
            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray latestSale = new JsonArray();
            while(rs.next()){
                JsonObject soldItem = new JsonObject();
                String salesId = rs.getString("id");
                String name = rs.getString("title");
                String quantity = rs.getString("quantity");

                soldItem.addProperty("salesID", salesId);
                soldItem.addProperty("title", name);
                soldItem.addProperty("quantity", quantity);

                latestSale.add(soldItem);
            }

            rs.close();
            statement.close();
            out.write(latestSale.toString());
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