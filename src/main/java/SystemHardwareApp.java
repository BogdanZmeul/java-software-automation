import java.io.*;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "system", value = "/system")
public class SystemHardwareApp extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        int mb = 1024 * 1024;
        int gb = 1024 * mb;

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<html><body>");
        out.println("<h1>System characteristics</h1>");

        out.println("<p>CPU: " + osBean.getAvailableProcessors() + " cores</p>");

        out.println("<p>Total Memory: " + osBean.getTotalMemorySize() / mb + " MB</p>");
        out.println("<p>Free Memory: " + osBean.getFreeMemorySize() / mb + " MB</p>");
        out.println("<p>Committed Virtual Memory: " + osBean.getCommittedVirtualMemorySize() / mb + " MB</p>");

        out.println("<p>JVM heap Max memory: "+ Runtime.getRuntime().maxMemory() / mb + " MB</p>");
        out.println("<p>JVM heap Free memory: "+ Runtime.getRuntime().freeMemory() / mb + " MB</p>");
        out.println("<p>JVM heap Total memory: "+ Runtime.getRuntime().totalMemory() / mb + " MB</p>");

        out.println("<p>Disk: " + (new File("/").getTotalSpace() / gb) + " GB</p>");

        out.println("<p>Operating System: " + osBean.getName() + " " + osBean.getVersion() + "</p>");
        out.println("<p>Architecture: " + osBean.getArch() + "</p>");
        out.println("<p>Java Version: " + System.getProperty("java.version") + "</p>");

        out.println("<p>Current Time: " + new java.util.Date() + "</p>");
        out.println("<p>Current Timezone: " + java.util.TimeZone.getDefault().getID() + "</p>");

        out.println("</body></html>");
    }
}