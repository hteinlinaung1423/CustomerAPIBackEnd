/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sa44.g2.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Resource;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 *
 * @author Htein Lin Aung
 */
@WebServlet(urlPatterns = { "/customers", "/customer/*" })
public class CustomerServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    
    private static final String SQL0 = "SELECT customer_id,name,city,phone,email FROM customer  where customer_id = ?";
    
    @Resource(lookup = "jdbc/derby_sample")
	private DataSource derbyDS;
    
    
    protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
       final String requestUri = req.getRequestURI();

		resp.setHeader("Access-Control-Allow-Origin", "*");

		if (requestUri.endsWith("/customers")) {
			createFilmList(req, resp, derbyDS);
			return;
		}
                
                final String cid = req.getPathInfo();
		int custId = 0;

		try {
			custId = Integer.parseInt(cid.substring(1));
		} catch (NumberFormatException ex) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		//Single movie
		JsonObject result = null;
		try (Connection conn = derbyDS.getConnection()) {
			PreparedStatement ps = conn.prepareStatement(SQL0);
			ps.setInt(1, custId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				result = Json.createObjectBuilder()
						.add("customer_id", rs.getInt("customer_id"))
						.add("name", rs.getString("name"))
						.add("city", rs.getString("city"))
						.add("phone", rs.getString("phone"))
						.add("email", rs.getString("email"))
						.build();
			} else {
				resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
		} catch (SQLException ex) {
			resp.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			return;
		}


		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("application/json");

		try (PrintWriter pw = resp.getWriter()) {
			pw.print(result.toString());
			pw.flush();
		}

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
    
    private void createFilmList(HttpServletRequest req, 
			HttpServletResponse resp, DataSource derbyDS) 
			throws ServletException, IOException {

		JsonArray films = null;

		try (Connection conn = derbyDS.getConnection()) {

			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT customer_id,name FROM customer limit 20");

			JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
			while (rs.next()) {
				JsonObject rec = Json.createObjectBuilder()
						.add("customer_id", rs.getInt("customer_id"))
						.add("name", rs.getString("name"))
						.build();
				arrBuilder.add(rec);
			}

			films = arrBuilder.build();

		} catch (SQLException ex) {
			resp.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			return;
		}

		resp.setStatus(HttpServletResponse.SC_ACCEPTED);
		resp.setContentType("application/json");

		try (PrintWriter pw = resp.getWriter()) {
			pw.print(films.toString());
			pw.flush();
		}
	}

}
