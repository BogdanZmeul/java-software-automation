import java.io.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "system", value = "/system")
public class SystemHardwareApp extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>System characteristics</h1>");

        out.println("<p>CPU: " + Runtime.getRuntime().availableProcessors() + " cores</p>");

        out.println("<p>Total Memory: " + Runtime.getRuntime().totalMemory() / (1024 * 1024) + " MB</p>");
        out.println("<p>Free Memory: " + Runtime.getRuntime().freeMemory() / (1024 * 1024) + " MB</p>");
        out.println("<p>Max Memory: " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB</p>");

        out.println("<p>Disk: " + (new File("/").getTotalSpace() / (1024 * 1024 * 1024)) + " GB</p>");

        out.println("<p>Operating System: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + "</p>");
        out.println("<p>Java Version: " + System.getProperty("java.version") + "</p>");

        out.println("<p>Current Time: " + new java.util.Date() + "</p>");
        out.println("<p>Current Timezone: " + java.util.TimeZone.getDefault().getID() + "</p>");

        out.println("</body></html>");
    }
}