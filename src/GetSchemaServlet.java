import com.google.gson.JsonArray;
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
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "GetSchemaServlet", urlPatterns = "/api/schema")
public class GetSchemaServlet extends HttpServlet {
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "SHOW tables ";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            ResultSet tableNameSet = statement.executeQuery();
            List<String> tables = new ArrayList<>();

            // Iterate through each row of rs
            while (tableNameSet.next()) {

                String tableName = tableNameSet.getString("Tables_in_moviedb");
//                String starDob = rs.getString("birthYear");
//                if(rs.wasNull()){
//                    starDob = "Unknown";
//                }
//                String movieId = rs.getString("movieId");
//                String movieTitle = rs.getString("title");
//                String movieYear = rs.getString("year");

                // Create a JsonObject based on the data we retrieve from rs
                tables.add(tableName);
            }
//name of each table and, for each table, each attribute and its type.
            JsonArray tableSchema = new JsonArray();
            for(String table : tables){
                JsonObject tableObject = new JsonObject();
                query = "describe " + table + " ";
                statement = conn.prepareStatement(query);
                ResultSet tableInfo = statement.executeQuery();
                JsonArray attributeList = new JsonArray();
                tableObject.addProperty("table", table);
                while(tableInfo.next()){
                    JsonObject attribute = new JsonObject();
                    attribute.addProperty("field", tableInfo.getString("Field"));
                    attribute.addProperty("type", tableInfo.getString("Type"));
                    attributeList.add(attribute);
                }
                tableObject.add("attributeList", attributeList);
                tableSchema.add(tableObject);
                tableInfo.close();
                statement.close();
            }

            // write JSON string to output
            out.write(tableSchema.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // always remember to close db connection after usage. Here it's done by try-with-resources


    }
}